package com.allen.testplatform.testscripts.config;

import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.testscripts.enums.DriverVersionEnum;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 更多配置相关信息  <a href="https://www.selenium.dev/documentation/webdriver/capabilities/">https://www.selenium.dev/documentation/webdriver/capabilities/</a>{@link }     <br>
 *
 */

public interface DriverOptions {

    static ChromeOptions getNormalChromeOptions(){

        ChromeOptions option = new ChromeOptions();
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        option.addArguments("--start-maximized"); // 启动时自动最大化窗口
        option.addArguments("--disable-popup-blocking"); // 禁用阻止弹出窗口
        option.addArguments("no-sandbox"); // 启动无沙盒模式运行
//        option.addArguments("disable-extensions"); // 禁用扩展
//        option.addArguments("no-default-browser-check"); // 默认浏览器检查
//        option.setLogLevel(ChromeDriverLogLevel.WARNING);
//        option.setHeadless(Boolean.TRUE);//设置chrome 无头模式
//        option.addArguments("--headless");//不用打开图形界面。
        Map<String, Object> prefs = new HashMap();
        //修改浏览器文件下载路径
        prefs.put("download.default_directory","D:\\download\\download");
        //关闭浏览器密码保存提示
        prefs.put("credentials_enable_service",false);
        prefs.put("profile.password_manager_enabled",false);
        option.setExperimentalOption("prefs",prefs);

        return option;
    }

    static ChromeOptions getSandboxChromeOptions(){

        ChromeOptions option = new ChromeOptions();
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        option.addArguments("--start-maximized"); // 启动时自动最大化窗口
        option.addArguments("--disable-popup-blocking"); // 禁用阻止弹出窗口
//        option.addArguments("no-sandbox"); // 启动无沙盒模式运行
//        option.addArguments("disable-extensions"); // 禁用扩展
//        option.addArguments("no-default-browser-check"); // 默认浏览器检查
//        option.setLogLevel(ChromeDriverLogLevel.WARNING);
//        option.setHeadless(Boolean.TRUE);//设置chrome 无头模式
//        option.addArguments("--headless");//不用打开图形界面。
        Map<String, Object> prefs = new HashMap();
        //修改浏览器文件下载路径
        prefs.put("download.default_directory","D:\\download\\download");
        //关闭浏览器密码保存提示
        prefs.put("credentials_enable_service",false);
        prefs.put("profile.password_manager_enabled",false);
        option.setExperimentalOption("prefs",prefs);

        return option;
    }

    static ChromeOptions getH5ChromeOptions(){

        ChromeOptions options = new ChromeOptions();
        Map<String, String> mobileEmulation = new HashMap<>();

//		    mobileEmulation.put("deviceName", "iPhone 4");
//		    mobileEmulation.put("deviceName", "iPhone 6");
        mobileEmulation.put("deviceName", "iPhone X");
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("mobileEmulation", mobileEmulation);
        options.addArguments("no-sandbox"); // 启动无沙盒模式运行
        return options;
    }

    static InternetExplorerOptions getNormalIEOptions(){
        InternetExplorerOptions options = new InternetExplorerOptions();
        //在某些环境中, 当打开文件上传对话框时, Internet Explorer可能会超时. IEDriver的默认超时为1000毫秒, 但您可以使用fileUploadDialogTimeout功能来增加超时时间
        options.waitForUploadDialogUpTo(Duration.ofSeconds(10));
        //设置为 true时, 此功能将清除InternetExplorer所有正在运行实例的 缓存, 浏览器历史记录和Cookies (包括手动启动或由驱动程序启动的实例) . 默认情况下，此设置为 false.
        //使用此功能将导致启动浏览器时性能下降, 因为驱动程序将等待直到缓存清除后再启动IE浏览器.
//        options.destructivelyEnsureCleanSession();
        //InternetExplorer驱动程序期望浏览器的缩放级别为100%, 否则驱动程序将可能抛出异常. 通过将 ignoreZoomSetting 设置为 true, 可以禁用此默认行为.
        options.ignoreZoomSettings();
        return options;
    }

    static FirefoxOptions getNormalFirefoxOptions(){
        //可以为Firefox创建自定义配置文件,
//        FirefoxProfile profile = new FirefoxProfile();
        //设置代理
//        profile.setPreference("network.proxy.type", 1);
//        profile.setPreference("network.proxy.http", "hzproxy.xxxx.com");
//        profile.setPreference("network.proxy.http_port", 8080);
        FirefoxOptions options = new FirefoxOptions()
                .addPreference("browser.startup.page", 1);
//        options.setProfile(profile);
//        options.addArguments("-headless");
//        options.addArguments("--disable-gpu");
        options.addPreference("dom.webdriver.enabled", false);
        return options;
    }

    static EdgeDriverService getNormalEdgeService(){
        System.setProperty("webdriver.edge.verboseLogging", "false");
        return EdgeDriverService.createDefaultService();
    }

    static EdgeOptions getNormalEdgeOptions(String version){
        String targetFileName = DriverVersionEnum.getTargetFileName("edge", version);
        String driverPath = System.getProperty("user.dir") + CommonUtils.SEPARATOR + "src" + CommonUtils.SEPARATOR +"main"
        + CommonUtils.SEPARATOR +"resources" + CommonUtils.SEPARATOR + "driver" + CommonUtils.SEPARATOR + targetFileName;
        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.setBinary(driverPath);
        return edgeOptions;
    }
}
