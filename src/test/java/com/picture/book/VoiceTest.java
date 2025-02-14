package com.picture.book;

import com.picture.book.generate.IVoiceGenerate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class VoiceTest {
    @Autowired
    IVoiceGenerate voice;
    @Test
    public void test() throws Exception {
        voice.generate("""
                海绵宝宝是方块形的黄色海绵，住在比基尼海滩（裤头村、比奇堡）的一个菠萝里，他的宠物是一只会“猫~猫~”叫的海蜗牛小蜗，海绵宝宝喜欢捕捉水母，职业是蟹堡王里的头号厨师。派大星和珊迪都是他的朋友。海绵宝宝总是能给平静的世界制造麻烦，虽然闹出一些笑话，不过他总能摆脱困境，然后又制造出新的麻烦。
                派大星:粉红色的海星。智商极低，头脑与身体仅使用插头连结。做什么事都会搞砸，但开船却异常厉害，居住在圆顶石头底下。懒惰并相当孩子气，时常在无意间呆滞的流口水，且讨厌洗澡、也不爱洗手、偏爱睡觉。兴趣是看电视。跟海绵宝宝的交情最好；并时常鼓励海绵宝宝做出一些危险行动，往往让彼此陷入困境。
                痞老板是蟹老板的死对头，《海绵宝宝》的主要反派角色。由1%的邪恶和99%的热毒气组成。只有一只眼睛，时常刺痛（因为他忘记眨眼）。快餐店“海之霸”的老板。管一台取名为凯伦的电脑叫妻子。为了让自己的餐厅生意兴隆，一直偷取蟹堡王著名料理“蟹黄堡”的秘密配方，却从未成功，最大的希望是统治全世界。
   """);

    }
}
