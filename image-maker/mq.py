import pika
import json
import uuid
import base64
from pathlib import Path
import os
import logging
import time
from fx import generate
from translator import QwenTranslator
logging = logging.getLogger("MQ")
# 设置日志配置
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
# 加载翻译模型（限制在 CPU 上）
print("🧠 加载翻译模型...")
translator = QwenTranslator()
current_working_directory = str(Path(__file__).resolve().parent)
credentials = pika.PlainCredentials('pic', '12345678')  # 用户名和密码
parameters = pika.ConnectionParameters(
    host='localhost',
    port=5672,
    virtual_host='/',
    credentials=credentials,
    heartbeat=600,  # 设置心跳间隔为600秒
)
connection = pika.BlockingConnection(parameters)
channel = connection.channel()
channel.exchange_declare(exchange="PICTURE_GEN_IMG_EXCHANGE", exchange_type='direct', durable=True)
channel.queue_declare(queue='PICTURE_GEN_IMG_QUEUE', durable=True)
def image_to_base64(file_path):
    """将图片文件转换为Base64编码的字符串"""
    try:
        with open(file_path, "rb") as img_file:
            encoded_string = base64.b64encode(img_file.read()).decode('utf-8')
        logging.info(f"Successfully converted {file_path} to Base64.")
        return encoded_string
    except Exception as e:
        logging.error(f"Error converting file {file_path} to Base64: {e}")
        raise


def send_message(message_body,channel):
    """发送消息到 RabbitMQ 的指定 Exchange"""
    try:
        if isinstance(message_body, (dict, list)):
            body = json.dumps(message_body, ensure_ascii=False)
        else:
            body = str(message_body)
        channel.basic_publish(
            exchange="PICTURE_GEN_IMG_EXCHANGE",
            routing_key="picture.routing.done",
            body=body,
            properties=pika.BasicProperties(delivery_mode=2,)
        )
        logging.info("Message sent successfully.")
    except pika.exceptions.AMQPConnectionError as e:
            logging.warning(f"Failed to publish message: {e}. Retrying in 5 seconds...")


def get_image_remote(prompt: str, step: int = 4):
    """获取远程图片"""
    if step > 20:
        step = 25
    if step < 4:
        step = 4
    img = generate(prompt, step, 5.5, 1280, 720, -1)
    path = os.path.join(f"{current_working_directory}/temp", f"img_{uuid.uuid1()}.png")
    img.save(path)
    logging.info(f"Image saved at {path}")
    return path


def callback(ch, method, properties, body):
    """处理接收到的消息的回调函数"""
    try:
        message = body.decode('utf-8')
        data = json.loads(message)
        logging.info(f"Received message: {data}")
            
        logger.info(f" 翻译prompt=======>")

        translated_text = translator.translate(data['prompt'])
        logger.info(f"prompt is {prompt} ,translated_text is {translated_text}")
        img_path = get_image_remote(translated_text, data['step'])
        ch.basic_ack(delivery_tag=method.delivery_tag)
        
        jsonData = {
            "prompt": translated_text,
            "batchNo": data['batchNo'],
            "img": image_to_base64(img_path)
        }
        send_message(jsonData,ch)
        
        os.remove(img_path)  # 使用正确的路径变量
        logging.info(f"File {img_path} has been deleted successfully.")
    except FileNotFoundError:
        logging.error(f"File {img_path} does not exist.")
    except PermissionError:
        logging.error(f"No permission to delete file {img_path}.")
    except Exception as e:
        logging.error(f"An error occurred while deleting the file: {e}")


def consume_messages():
    """消费消息"""
    try:
        # 每次重连后都需要重新声明队列
        channel.basic_qos(prefetch_count=1)
        
        def inner_callback(ch, method, properties, body):
            callback(ch, method, properties, body)

        # 绑定消费者
        channel.basic_consume(queue='PICTURE_GEN_IMG_QUEUE', on_message_callback=inner_callback)
        logging.info(' [*] Waiting for messages. To exit press CTRL+C')
        channel.start_consuming()
    except pika.exceptions.StreamLostError as e:
        logging.warning(f"Connection lost (StreamLostError): {e}. Reconnecting in 5 seconds...")
    except KeyboardInterrupt:
        logging.info("User interrupted. Exiting.")
        logger.info("Shutting down...")
        connection.close()
    except Exception as e:
        logging.error(f"Unexpected error: {e}")


if __name__ == '__main__':
    consume_messages()