大家好！我是老猿人，一名热爱编程和故事的开发者。今天，我要向大家介绍一个我亲手打造的“魔法工具”——AI 绘本生成程序！🎉

你是否曾经梦想过，把自己的奇思妙想变成一本精美的绘本？你是否希望为孩子、朋友或自己创作一个独一无二的故事？现在，这一切都可以轻松实现！

🌟什么是 AI 绘本生成程序？

这是一个基于人工智能的神奇工具，只需要输入你的想法，它就能帮你：

生成生动的故事情节

创作精美的插画

自动排版成一本完整的绘本

无论是童话、科幻、还是温馨的日常故事，它都能帮你轻松搞定！

🎨为什么你需要它？

零门槛创作不需要会画画，也不需要是写作高手！只要你有想法，AI 就能帮你实现。

快速生成传统绘本创作可能需要几个月，而现在，只需要几分钟！

独一无二每一本绘本都是根据你的想法定制的，绝无重复，充满个性！

适合所有人无论是家长、老师、作家，还是单纯的故事爱好者，都能用它创造出属于自己的童话世界。

🚀它是如何工作的？

输入你的灵感告诉我你的故事主题、角色设定，或者随便写几句话。

AI 开始创作程序会根据你的输入，生成一个完整的故事，并配上精美的插画。

一键生成绘本最后，程序会自动排版，生成一本可以直接打印或分享的电子绘本！

🌈你可以用它做什么？

为孩子创作专属故事让孩子的想象力在绘本中自由飞翔！

制作独特的礼物送朋友一本独一无二的绘本，绝对让人惊喜！

激发创意灵感写作卡壳？用它来激发你的灵感吧！

教育工具老师们可以用它制作有趣的教学材料，让孩子们爱上学习！

💡我的故事

作为一名开发者，我一直希望用技术让生活更有趣。这个 AI 绘本生成程序，正是我结合了对编程和故事的热爱，经过无数个日夜的打磨，终于诞生的“魔法工具”。

现在，我把它分享给大家，希望能为更多人带来创作的乐趣和惊喜！

🎁如何体验？

从github下载项目：
1 git clone https://github.com/easyJustEasy/picture-book.git
2 启动项目：BookApplication
3 打开浏览器并访问：
- 单独生成绘本: [http://localhost:8080](http://localhost:8080)
  <img src="screen1.png" width="1024"/>
- 批量生成绘本: [http://localhost:8080/batch.html](http://localhost:8080/batch.html)
  <img src="screen2.png" width="1024"/>
## 使用说明

1. **单独生成绘本**：在首页输入角色名称和故事描述，然后点击“生成”按钮。生成的绘本将显示在页面上，并保存到历史记录中。
2. **批量生成绘本**：在批量生成页面，按照格式要求输入多组角色和故事描述，然后点击“批量生成”按钮。完成后，所有生成的绘本都会被添加到历史记录中。


## 赞助开发者
您的支持将帮助我持续维护项目并开发新功能。无论是小额捐赠还是分享项目，都是对我的巨大支持。
# 立即支持
   <div >
    <img src="weixin.jpg" width="300"/>
    <img src="zhifubao.jpg" width="300"/>
   </div>

## 注意事项
- 项目中使用了三个大模型：
  
  1 `qwen-turbo`：用于生成故事描述 代码在这里TongYiTextGenerate。
  
  2 `wanx2.1-t2i-turbo`：用于生成绘本每一帧图片 代码在这里TongYiImageGenerate。
  
  3 `cosyvoice-v1`：用于生成绘本配音 代码在这里TongYiVoiceGenerate。
  
- 这些大模型都是使用的百炼平台中的模型，需要百炼的API KEY ,所以要确保环境变量 `DASHSCOPE_API_KEY` 已正确配置，否则API请求将会失败。
- 系统还需要ffmpeg转码音频为mp3格式，所以需要确保系统已经安装ffmpeg ，需要安装ffmpeg 7.1 https://www.ffmpeg.org/download.html。
- 视频应该放置nginx的根目录，具体配置是app.properties中的videoDir，即nginx的根目录
- 视频需要通过nginx映射，具体配置：
```
    server {
        listen       80;
        server_name  localhost;

        location / {      
        add_header 'Access-Control-Allow-Origin' '*';
        add_header 'Access-Control-Allow-Headers' '*';
        add_header 'Access-Control-Allow-Methods' '*'; 
        if ($request_method = 'OPTIONS') {
            return 204;
        }
        root   /opt/homebrew/var/www;
        index  index.html index.htm;
        }     
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
   
    }
```

## 联系方式

如果有任何问题或建议，请联系项目维护者 zhusiyuanhao@163.com。
