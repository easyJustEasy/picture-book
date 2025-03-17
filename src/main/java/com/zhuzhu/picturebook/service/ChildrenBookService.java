package com.zhuzhu.picturebook.service;

import com.zhuzhu.picturebook.dto.GenerateResultDTO;
import com.zhuzhu.picturebook.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChildrenBookService extends AbstractPictureBookService implements IPictureBook {
    public static final String ACTORS = "";
    public static final String system = """
            # 角色
            你是一位%s绘本创作者，专注于为儿童和家庭创造有趣、富有教育意义的绘本故事。你的创作基于%s的原有世界观和角色设定，确保内容符合%s的品牌形象。
                        
            ## 技能
            ### 技能1：构建趣味盎然的故事
            - 以%s及其朋友们为主角，编写充满幽默感和冒险精神的故事。
            - 故事情节紧凑，情节发展自然流畅，适合3-8岁儿童阅读。
            - 每个故事都包含一个简单的道德或教育寓意，帮助孩子在娱乐中学习。
                        
            ### 技能2：设计生动的视觉元素
            - 结合%s的经典形象和色彩，绘制出引人入胜的插图。
            - 插图风格保持与%s动画一致，色彩鲜艳，线条简洁明快。
            - 确保每个页面的图文搭配得当，文字量适中，便于家长与孩子共读。
                        
            ### 技能3：融入互动元素
            - 在故事中适当加入互动环节，如问题引导、小游戏等，增强孩子的参与感。
            - 设计一些小任务或挑战，鼓励孩子思考和动手操作，提高阅读的趣味性和互动性。
                        
            ## 限制：
            - 故事内容必须严格遵守%s的品牌形象和价值观，不得出现不适宜的内容。
            - 插图和文字应符合儿童认知水平，避免过于复杂或成人化的表达。
            - 每本绘本的故事长度控制在10-15页之间，确保阅读时间适中，适合睡前故事或课堂阅读。
            - 所有内容需经过%s版权方的审核，确保合法合规。
            """;
    @Autowired
    private AppConfig appConfig;

    @Override
    String generateVoice(String caption, String workDir) throws Exception {
        return getVoiceGenerate().generate(caption, "天童爱丽丝", 1.0F, workDir);
    }

    @Override
    String generateFrame(String system, String storyDesc) throws Exception {

        return getTextGenerate().generate(system, storyDesc);
    }

    @Override
    String generatePicture(String actors, String scene, String caption, String workDir) throws Exception {
        return getImageGenerate().generate(actors, scene, caption, workDir);
    }

    /**
     * 生成视频
     *
     * @param resultDTO 请求
     * @return 视频路径
     * @throws Exception 异常
     */

    @Override
    public String generate(GenerateResultDTO resultDTO, GenerateCallBack callBack) throws Exception {
        String role = resultDTO.getRole();
        String storyDesc = resultDTO.getStoryDesc();
        return generate(ACTORS, system, role, storyDesc, resultDTO, appConfig.tempDir(), callBack);
    }
}
