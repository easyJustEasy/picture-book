package com.picture.book;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.zhuzhu.PictureBookApp;
import com.zhuzhu.picturebook.dto.BatchGenerateRequestDTO;
import com.zhuzhu.picturebook.dto.GenerateRequestDTO;
import com.zhuzhu.picturebook.dto.Story;
import com.zhuzhu.picturebook.generate.text.OllamaDeepSeekTextGenerate;
import com.zhuzhu.picturebook.service.BookGenerateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = PictureBookApp.class)
class BookApplicationTests {

	@Test
	void contextLoads() {
		String s = """
				@@@@@
				     故事标题：海绵宝宝的快乐野餐
				     
				     @@@@@
				     
				     场景1 \s
				     旁白：海绵宝宝一大早就开始准备野餐的食物啦！他做了一大篮子章鱼酥、菠菜派和蟹黄三明治。 \s
				     场景描述：画面中海绵宝宝穿着围裙，在厨房里忙碌着，桌上摆满了各种美食。阳光透过窗户洒进来，显得温馨又明亮。
				     
				     @@@@@
				     
				     场景2 \s
				     旁白：海绵宝宝邀请派大星一起去野餐，他们决定去比奇堡的海滩公园。 \s
				     场景描述：海绵宝宝和派大星手牵手走在路上，路两旁是五彩缤纷的花朵，远处能看到蓝色的大海和沙滩。他们的篮子里装满了美味的野餐食物。
				     
				     @@@@@
				     
				     场景3 \s
				     旁白：他们来到公园后，发现珍珍已经铺好了漂亮的格子布，还摆放了一些水果。 \s
				     场景描述：珍珍站在毯子旁边，笑眯眯地迎接他们。毯子上放着切好的西瓜、苹果和葡萄，看起来清爽可口。
				     
				     @@@@@
				     
				     场景4 \s
				     旁白：大家正吃得开心时，珊迪带着她的机器人朋友突然来访。 \s
				     场景描述：珊迪站在一旁，她的机器人朋友好奇地看着周围的一切。海绵宝宝热情地邀请它一起享用美食，机器人也开心地点头。
				     
				     @@@@@
				     
				     场景5 \s
				     旁白：大家玩起了沙滩排球，笑声充满了整个公园。 \s
				     场景描述：大家分成两组玩排球，派大星和机器人一组，珍珍和珊迪一组。海绵宝宝在场边欢呼，气氛热烈而欢乐。
				     
				     @@@@@
				     
				     场景6 \s
				     旁白：夕阳西下，大家一起收拾东西准备回家。 \s
				     场景描述：天边的晚霞映红了天空，大家依依不舍地告别。他们拎着空篮子，脸上洋溢着满足的笑容，慢慢走回家。
				     
				     @@@@@
				     
				     故事结束				""";
		Story story = Story.parseStory(s);
		System.out.println(JSONObject.toJSONString(story));
	}
	@Autowired
	BookGenerateService bookGenerateService;
	@Autowired
	OllamaDeepSeekTextGenerate ollamaDeepSeekTextGenerate;
	@Test
	public void test1() throws Exception {
		String systemMessage = """
                """;
		String userMessage = "生成关于海绵宝宝的21件事，要求事件符合逻辑，只需要事件的标题，不需要详细描述事件的经过,每个标题用@@@@@@@隔开只需要文字不需要序号";
		String desc = ollamaDeepSeekTextGenerate.generate(systemMessage, userMessage);
		String role = "海绵宝宝";

		List<GenerateRequestDTO> list = new ArrayList<>();
		String[]s = desc.split("@@@@@@@");
		for (String s1 : s) {
			if(StrUtil.isBlankIfStr(s1)){
				continue;
			}
			GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
			generateRequestDTO.setId(null);
			generateRequestDTO.setRole(role);
			generateRequestDTO.setStoryDesc(s1);
			list.add(generateRequestDTO);

		}
		BatchGenerateRequestDTO requestDTO  = new BatchGenerateRequestDTO();
		requestDTO.setList(list);
		bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
	}
	@Test
	public void test2() throws Exception {

		String desc = """
				相思
				红豆生南国，春来发几枝。
				愿君多采撷，此物最相思。
				@@@@@@@
				赋得古原草送别
				离离原上草，一岁一枯荣。
				野火烧不尽，春风吹又生。
				远芳侵古道，晴翠接荒城。
				又送王孙去，萋萋满别情。
				@@@@@@@
				渡荆门送别
				渡远荆门外，来从楚国游。
				山随平野尽，江入大荒流。
				月下飞天镜，云生结海楼。
				仍怜故乡水，万里送行舟
				@@@@@@@
				春晓
				春眠不觉晓，处处闻啼鸟。
				夜来风雨声，花落知多少。
				""";
		String role = "海绵宝宝";

		List<GenerateRequestDTO> list = new ArrayList<>();
		String[]s = desc.split("@@@@@@@");
		for (String s1 : s) {
			if(StrUtil.isBlankIfStr(s1)){
				continue;
			}
			GenerateRequestDTO generateRequestDTO = new GenerateRequestDTO();
			generateRequestDTO.setId(null);
			generateRequestDTO.setRole(role);
			generateRequestDTO.setStoryDesc(s1);
			generateRequestDTO.setBookType(2);
			list.add(generateRequestDTO);

		}
		BatchGenerateRequestDTO requestDTO  = new BatchGenerateRequestDTO();
		requestDTO.setList(list);
		bookGenerateService.generateBatch(requestDTO, UUID.randomUUID().toString());
	}
}
