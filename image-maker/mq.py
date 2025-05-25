import pika
import json
import uuid
import base64
from pathlib import Path
import os 
from flux_generage import generate
current_working_directory = str(Path(__file__).resolve().parent)
# 建立到RabbitMQ服务器的连接
credentials = pika.PlainCredentials('pic', '12345678')  # 用户名和密码
parameters = pika.ConnectionParameters(
    host='localhost',
    port=5672,
    virtual_host='/',
    credentials=credentials
)


def image_to_base64(file_path):
    """
    将图片文件转换为Base64编码的字符串。

    :param file_path: 图片文件路径
    :return: Base64编码的字符串
    """
    # 以二进制模式打开图片文件
    with open(file_path, "rb") as img_file:
        # 使用base64进行编码，并解码为字符串形式返回
        encoded_string = base64.b64encode(img_file.read()).decode('utf-8')
    
    return encoded_string
def send_message(message_body):
    """
    发送消息到 RabbitMQ 的指定 Exchange
    :param message_body: 要发送的消息内容（字典或字符串）
    """
    # 建立连接和通道
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    try:
        # 可选：声明一个 Exchange（如果确保已存在可省略）
        channel.exchange_declare(
            exchange="PICTURE_GEN_IMG_EXCHANGE",
            exchange_type='direct',  # 根据你使用的 Exchange 类型调整
            durable=True
        )

        # 将消息体转换为字符串（支持 dict 自动转 JSON）
        if isinstance(message_body, (dict, list)):
            body = json.dumps(message_body, ensure_ascii=False)
        else:
            body = str(message_body)

        # 发送消息到 Exchange
        channel.basic_publish(
            exchange="PICTURE_GEN_IMG_EXCHANGE",
            routing_key="picture.routing.done",
            body=body,
            properties=pika.BasicProperties(
                delivery_mode=2,  # 持久化消息
            )
        )
    finally:
        connection.close()
def get_image_remote(prompt:str,step:int=4):
    if step>20:
        step = 25
    if step<4:
        step = 4
    img = generate(prompt, step, 3.5, 1280, 720, -1)
    path = os.path.join(
        f"{current_working_directory}/temp", f"img_{uuid.uuid1()}.png"
    )
    img.save(path)
    return path
def callback(ch, method, properties, body):
    """
    定义处理接收到的消息的回调函数。
    
    :param ch: 通道对象
    :param method: 方法对象，包含投递信息
    :param properties: 消息属性
    :param body: 接收到的消息体
    """
    # 将字节数据转换为字符串
    message = body.decode('utf-8')  # 假定消息是UTF-8编码的文本
    print(f" [x] Received {message}")
    data = json.loads(message)
    print(f"prompt is {data['prompt']} step is {data['step']}")
    img = get_image_remote(data['prompt'],data['step'])
    # 确认消息已经被处理
    ch.basic_ack(delivery_tag=method.delivery_tag)
    jsonData = {
        "prompt":data['prompt'],
        "batchNo":data['batchNo'],
        "img":image_to_base64(img)
    }
    send_message(jsonData)
    # 删除文件
    try:
        os.remove(img)
        print(f"文件 {img} 已成功删除")
    except FileNotFoundError:
        print(f"文件 {img} 不存在")
    except PermissionError:
        print(f"没有权限删除文件 {img}")
    except Exception as e:
        print(f"删除文件时发生错误: {e}")
connection = pika.BlockingConnection(parameters)

# 创建一个通道
channel = connection.channel()

# 声明队列，确保队列存在
channel.queue_declare(queue='PICTURE_GEN_IMG_QUEUE', durable=True)  # 根据实际情况设置参数

# 设置QoS（服务质量），限制prefetch count可以改善吞吐量并防止消息积压
channel.basic_qos(prefetch_count=1)

# 订阅队列并指定回调函数
channel.basic_consume(queue='PICTURE_GEN_IMG_QUEUE', on_message_callback=callback)

print(' [*] Waiting for messages. To exit press CTRL+C')

# 开始消费消息，进入阻塞状态，等待数据
try:
    channel.start_consuming()
except KeyboardInterrupt:
    print("Consumer canceled by user")
finally:
    connection.close()