package com.picture.voice;

import cn.hutool.core.io.FileUtil;
import com.picture.book.PlayWav;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.generate.voice.RemoteVoiceGenerate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = PictureBookApp.class)
public class Quarrel {
    @Autowired
    private RemoteVoiceGenerate remoteVoiceGenerate;

    @Test
    public void test() throws Exception {
        List<String> vlist = new ArrayList<>();
        String bVoice = "央视播音腔1";
        String aVoice = "李云龙";
        String temp = "temp";
        String a1 = "你总是这样，从来不考虑我的感受！每次计划旅行的时候，你都自作主张地决定一切，从不问我意见。";
        vlist.add(remoteVoiceGenerate.generate(a1, aVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String b1 = " 哎，等等，我怎么没有问你了？上次去海边还不是你说想去就临时改的目的地？";
        vlist.add(remoteVoiceGenerate.generate(b1, bVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String a2 = " 那是因为你最初的选择根本没考虑到我喜欢安静不喜欢人多的地方。而且那次我们差点就没订到住处了！";
        vlist.add(remoteVoiceGenerate.generate(a2, aVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String b2 = "我是想让你体验不同的风景和活动啊。再说，最后不是也找到一个很好的地方住了吗？";
        vlist.add(remoteVoiceGenerate.generate(b2, bVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String a3 = " 关键是你在做这些决定之前甚至都没有跟我商量过一次！";
        vlist.add(remoteVoiceGenerate.generate(a3, aVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String b3 = " 我以为这次你会喜欢尝试些新鲜事物。好吧，也许我在沟通上确实做得不够好，但你也应该告诉我你的想法啊，而不是事后才抱怨。";
        vlist.add(remoteVoiceGenerate.generate(b3, bVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String a4 = " 我希望我们可以一起讨论并作出决定，而不是由你单方面决定然后通知我。";
        vlist.add(remoteVoiceGenerate.generate(a4, aVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        String b4 = " 明白了，以后我会更注意这一点。不过，你也需要更主动地表达自己的想法哦。";
        vlist.add(remoteVoiceGenerate.generate(b4, bVoice, 1f, temp+ File.separator+ UUID.randomUUID()+".wav"));
        for (String s : vlist) {
            new PlayWav(s);
            FileUtil.del(s);
        }
    }
}
