package com.allen.testplatform.common.utils;

import java.io.*;
import java.util.Properties;

/**
 * @author Fan QingChuan
 * @since 2022/6/29 20:58
 */

public class PropertiesUtils {

    private static String path;
    private Properties prop;

    private PropertiesUtils() {}

    /**
     * 构造方法，初始化就传入文件路径，并加载文件
     * */
    public PropertiesUtils(String sourceName) {
        this.path = CommonUtils.getResourceRootPath()+"properties"+CommonUtils.SEPARATOR +sourceName +".properties";
        this.prop = loadProperties();
    }

    private Properties loadProperties() {

        Properties properties = new Properties();

        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(path));
            properties.load(new InputStreamReader(inputStream,"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;

    }

    /**
     * 获取内容，getProperty
     * */
    public String getProperty(String key) {

        if (prop.containsKey(key)) {
            String value = prop.getProperty(key);
            return value;
        }else {
            return null;
        }
    }

    public boolean containsKey(String key) {
        return prop.containsKey(key);
    }

    public void setProperty(String key,String value){
        Properties pro = new Properties();
        try {
            FileOutputStream fos = new FileOutputStream(path,true);
            pro.setProperty(key, value);
            pro.store(fos, key);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
