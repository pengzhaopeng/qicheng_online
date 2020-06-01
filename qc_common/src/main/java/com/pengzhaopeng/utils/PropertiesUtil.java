package com.pengzhaopeng.utils;

import java.io.*;
import java.util.Properties;

/**
 * description
 * author 鹏鹏鹏先森
 * date 2020/5/27 0:14
 * Version 1.0
 */
public class PropertiesUtil {

    public static void main(String[] args) {
        // sysConfig.properties(配置文件)
        PropertiesUtil p = new PropertiesUtil("config.properties");
        System.out.println(p.getProperties().get("kafka.broker.list"));
        System.out.println(p.readProperty("kafka.broker.list"));
//        PropertiesUtil q = new PropertiesUtil("config.properties");
//        q.writeProperty("myUtils", "wang");
//        System.exit(0);
    }

    private String properiesName = "";

    public PropertiesUtil() {

    }

    public PropertiesUtil(String fileName) {
        this.properiesName = fileName;
    }

    /**
     * 按key获取值
     * @param key
     * @return
     */
    public String readProperty(String key) {
        String value = "";
        InputStream is = null;
        try {
            is = PropertiesUtil.class.getClassLoader().getResourceAsStream(properiesName);
            Properties p = new Properties();
            p.load(is);
            value = p.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 获取整个配置信息
     * @return
     */
    public Properties getProperties() {
        Properties p = new Properties();
        InputStream is = null;
        try {
            is = PropertiesUtil.class.getClassLoader().getResourceAsStream(properiesName);
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return p;
    }

    /**
     * key-value写入配置文件
     * @param key
     * @param value
     */
    public void writeProperty(String key, String value) {
        InputStream is = null;
        OutputStream os = null;
        Properties p = new Properties();
        try {
            is = new FileInputStream(properiesName);
//            is = PropertiesUtil.class.getClassLoader().getResourceAsStream(properiesName);
            p.load(is);
//            os = new FileOutputStream(PropertiesUtil.class.getClassLoader().getResource(properiesName).getFile());
            os = new FileOutputStream(properiesName);

            p.setProperty(key, value);
            p.store(os, key);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is)
                    is.close();
                if (null != os)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
