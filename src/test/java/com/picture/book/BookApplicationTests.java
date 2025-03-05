package com.picture.book;

import com.alibaba.fastjson.JSONObject;
import com.picture.book.dto.Story;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookApplicationTests {

	@Test
	void contextLoads() {
		String s = """
				<think>\\n好，用户希望生成一个关于海绵宝宝去游泳的故事，并且要求有六个场景，每个场景都有旁白和场景描述。同时，结构要符合他们提供的格式。\\n\\n首先，我需要确定故事的标题，确保吸引人又有教育意义。然后，考虑每个场景的内容，确保情节紧凑、有趣，并且包含一定的教育意义或幽默元素。\\n\\n接下来是旁白部分，应该简短生动，能够引导孩子们进入情境并理解故事。场景描述要详细，但不过于复杂，适合3-8岁儿童阅读。\\n\\n最后，检查整个故事是否符合海绵宝宝的品牌形象和价值观，确保内容安全合规，并且语言简洁明了。\\n</think>\\n\\n### 故事标题：海绵宝宝的游泳冒险\\n\\n@@@@@\\n\\n**场景1**\\n\\n旁白：海绵宝宝决定 today is a special day! 今天是他的生日！  \\n场景描述：海绵宝宝穿着生日蛋糕泳装，坐在派对椅上，等待着大家的到来。周围 filled with colorful balloons and music. \\n\\n@@@@@\\n\\n**场景2**\\n\\n旁白：突然, 一个神秘的气泡升到了空中, 它上面写着 \\"Swim with me!\\"  \\n场景描述：海绵宝宝和派大星、章鱼哥、派大女、路飞、派大神一起跳进游泳池。池水看起来有点不一样，但他们都很好奇地跳了进去。\\n\\n@@@@@\\n\\n**场景3**\\n\\n旁白：他们遇到了很多海洋生物, like 海豹、海豚、鲨鱼和小丑鱼.  \\n场景描述：海绵宝宝问：\\"你们为什么在这里生活？\\" 小丑鱼笑着说：\\"我们是海洋的艺术家！\\"\\n\\n@@@@@\\n\\n**场景4**\\n\\n旁白：突然, 一个大浪打来, 所有的海洋生物都跑开了.  \\n场景描述：海绵宝宝和朋友们吓了一跳，但派大神却说：\\"我们必须学会游泳！\\"\\n\\n@@@@@\\n\\n**场景5**\\n\\n旁白：回到家中, 海绵宝宝打开水族箱, 发现水里有更多的小鱼在游来游去.  \\n场景描述：他兴奋地说：\\"今天我也要学游泳！\\" 朋友们都笑着说：\\"那我们一起教教你吧！\\"\\n\\n@@@@@\\n\\n**场景6**\\n\\n旁白：第二天 morning, 海绵宝宝站在池边, 心里充满了自信.  \\n场景描述：他的朋友们在他耳边唱着：\\"You can do it!\\" 海绵宝宝深吸一口气，跳进了池塘，顺利游到了对岸！\\n\\n故事结束
				""";
		Story story = Story.parseStory(s);
		System.out.println(JSONObject.toJSONString(story));
	}

}
