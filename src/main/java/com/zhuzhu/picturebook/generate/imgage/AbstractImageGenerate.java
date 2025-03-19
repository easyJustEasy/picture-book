package com.zhuzhu.picturebook.generate.imgage;

import cn.hutool.core.util.StrUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

public abstract class AbstractImageGenerate {

   public   static String addCaption(String path, String caption,String workDir) throws Exception {

        //将 caption拆分47个字一组
        String[]captions = StrUtil.split(caption, 47);
        BufferedImage image = ImageIO.read(new File(path));
        Graphics2D g2d = image.createGraphics();


        // 设置字体和颜色
        Font font = new Font("Serif", Font.BOLD, 24);
        g2d.setFont(font);
        g2d.setColor(Color.WHITE); // 字体颜色

        // 获取字符串的边界框以计算背景框大小
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(captions[0]);
        int textHeight = metrics.getHeight();

        // 背景框的位置和尺寸
        int padding = 10; // 背景框与文字之间的填充
        int x = 50;
        int y = image.getHeight() - 50;
        int width = textWidth + 2 * padding;
        int height = textHeight*captions.length + 2 * padding;

        // 绘制黑色半透明背景框
        g2d.setColor(new Color(0, 0, 0, 180)); // 黑色，透明度为180/255
        g2d.fillRect(x, y - height, width, height);

        // 在背景框之上绘制文字
        g2d.setColor(Color.WHITE); // 文字颜色
        int step = -30;
        if(captions.length==1){
            step=-10;
        }
        for (String s : captions) {
            g2d.drawString(s, x + padding, y - padding+step);
            step = step+30;
        }

        g2d.dispose();
        String newPath = workDir+File.separator+ UUID.randomUUID()+".png";

        ImageIO.write(image, "png", new File(newPath));
        return new File(newPath).getAbsolutePath();
    }
}
