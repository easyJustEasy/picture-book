package com.zhuzhu.picturebook.util;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtil {
    private static final Map<String, Properties> PROPERTIES_MAP = Maps.newConcurrentMap();

    public static String getProperty(String file, String key) throws IOException {
        Properties properties = loadProperties(file);
        if (properties == null) {
            throw new RuntimeException("配置文件不存在");
        }

        //获取key对应的value值
        return properties.getProperty(key);
    }

    public static synchronized Properties loadProperties(String file) throws IOException {
        if (StrUtil.isBlankIfStr(file)) {
            throw new RuntimeException("配置文件名称不能为空");
        }
        Properties properties = null;
        if (PROPERTIES_MAP.containsKey(file)) {
            properties = PROPERTIES_MAP.get(file);
        } else {
            properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = PropertiesUtil.class.getClassLoader().getResourceAsStream(file + ".properties");
            // 使用properties对象加载输入流
            assert in != null;
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));

            PROPERTIES_MAP.put(file, properties);
        }
        return properties;

    }
}
