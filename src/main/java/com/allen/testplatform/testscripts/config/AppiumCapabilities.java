package com.allen.testplatform.testscripts.config;

import com.allen.testplatform.common.utils.CommonUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.Map;

/**
 * 更多配置相关信息  <a href="http://appium.io/docs/en/writing-running-appium/caps/">http://appium.io/docs/en/writing-running-appium/caps/</a>{@link }     <br>
 * and <a href="http://appium.io/docs/en/advanced-concepts/settings/">http://appium.io/docs/en/advanced-concepts/settings/</a>{@link }
 *
 *
 * @author Fan QingChuan
 * @since 2021/12/18 21:11
 */
public interface AppiumCapabilities {

    static DesiredCapabilities getNoxFeishu(){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName","127.0.0.1:62001");
        capabilities.setCapability("platformName","Android");
        capabilities.setCapability("platformVersion","7.1.2");
        capabilities.setCapability("appPackage","com.ss.android.lark");
        capabilities.setCapability("appActivity","main.app.MainActivity");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("noReset", "true");
        capabilities.setCapability("fullReset", "false");
        return capabilities;
    }

    static DesiredCapabilities getNoxJx(){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName","127.0.0.1:62001");
        capabilities.setCapability("platformName","Android");
        capabilities.setCapability("platformVersion","7.1.2");
        capabilities.setCapability("appPackage","cn.host.qc");
        capabilities.setCapability("appActivity",".ui.login.LaunchActivity");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("noReset", "true");
        capabilities.setCapability("fullReset", "false");
        return capabilities;
    }

    static DesiredCapabilities getHarmonyJx(){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName","PQY0221B17001204");   //   PQY0221B17001204  HJS5T19718010374
        capabilities.setCapability("platformName","Android");
        capabilities.setCapability("platformVersion","10");
        capabilities.setCapability("appPackage","cn.host.qc");
        capabilities.setCapability("appActivity",".ui.login.LaunchActivity");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("chromedriverExecutable", CommonUtils.getResourceRootPath()+"driver"+CommonUtils.SEPARATOR+"chromedriver88.exe");
        capabilities.setCapability("noReset", "false");
        capabilities.setCapability("fastReset", "false");
        capabilities.setCapability("newCommandTimeout", "1200");


        //Appium for Android支持多个自动化后端，其中它们各有利弊。默认值为UIAutomator2
//        capabilities.setCapability("automationName", "UIAutomator2");
//        capabilities.setCapability("automationName", "Espresso");
        //建议设置要测试的应用程序的完整路径
//        capabilities.setCapability("app", app.getAbsolutePath());
        //设备序列号(多设备连接时必需设置) adb devices-l
//        capabilities.setCapability("udid", "ABCD123456789");
        //设置 是否每次清除数据
//        capabilities.setCapability("noReset", false);
//        capabilities.setCapability("fastReset", true);
        return capabilities;
    }

    static DesiredCapabilities getConnectDevicesWithJxApp(String deviceName){
        List<String> adbDevices = CommonUtils.getAdbDevices();
        String device = adbDevices.stream().filter(d -> d.contains(deviceName)).findFirst().orElse(null);

        if (device == null) {
            throw new RuntimeException("目标设备未连接 或 未开启调试模式! 请检查");
        }

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName",device);
        capabilities.setCapability("platformName","Android");
        capabilities.setCapability("platformVersion","10");
        capabilities.setCapability("appPackage","cn.host.qc");
        capabilities.setCapability("appActivity",".ui.login.LaunchActivity");
        capabilities.setCapability("automationName", "Appium");
        capabilities.setCapability("chromedriverExecutable", CommonUtils.getResourceRootPath()+"driver"+CommonUtils.SEPARATOR+"chromedriver88.exe");
        capabilities.setCapability("noReset", "false");
        capabilities.setCapability("fastReset", "false");
        capabilities.setCapability("udid", device);

        return capabilities;
    }

    static DesiredCapabilities getConnectDevices(String deviceName, Map<String, ?> rawMap){
        List<String> adbDevices = CommonUtils.getAdbDevices();
        String device = adbDevices.stream().filter(d -> d.contains(deviceName)).findFirst().orElse(null);

        if (device == null) {
            throw new RuntimeException("目标设备未连接 或 未开启调试模式! 请检查");
        }

        DesiredCapabilities capabilities = new DesiredCapabilities(rawMap);
        capabilities.setCapability("deviceName",device);
        capabilities.setCapability("udid", device);
        return capabilities;
    }
}
