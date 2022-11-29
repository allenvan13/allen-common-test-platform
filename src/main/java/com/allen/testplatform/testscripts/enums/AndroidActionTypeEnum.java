package com.allen.testplatform.testscripts.enums;

import io.appium.java_client.android.nativekey.AndroidKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.Keys;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum AndroidActionTypeEnum {

    //类型-0  无需定位 无需输入参数
    ClosePage("关闭页面","closePage",0,"关闭当前所在页面窗口"),
    CloseBrowser("关闭浏览器浏览器","closeBrowser",0,"关闭整个浏览器(所有页面)"),
    PageBack("页面返回","pageBack",0,"页面返回"),
    PageForward("页面前进","pageForward",0,"页面前进"),
    PageRefresh("刷新页面","pageRefresh",0,"页面刷新"),
    CloseAppiumService("关闭AppiumService","closeAppiumService",0,"关闭AppiumService 无输入值"),
    ScaleScreen("双指触控缩放屏幕","scaleScreen",0,"双指触控缩放屏幕"),
    EnlargeScreen("双指触控扩放屏幕","enlargeScreen",0,"双指触控扩放屏幕"),
    SwipeDown("页面向下滑动","swipeDown",0,"页面向下滑动"),
    SwipeUp("页面向上滑动","swipeUp",0,"页面向上滑动"),
    SwipeLeft("页面向左滑动","swipeLeft",0,"页面向左滑动"),
    SwipeRight("页面向右滑动","swipeRight",0,"页面向右滑动"),



    //类型-1  无需定位 需输入参数
    OpenUrl("打开网络地址","openUrl",1,"打开网址（输入值-网址）"),
    NavigateToUrl("跳转网络地址","navigateToUrl",1,"(同一个页面窗口下)跳转至网址  (输入值-网址)"),
    NavigateToWindows("跳转窗口至目标窗口","navigateToWindows",1,"跳转窗口至目标窗口(输入值-页面标题)"),
    Pause("暂停N秒","pause",1,"暂停N秒(输入值-秒值 支持小数)"),
    OpenUrlBlank("Blank方式(新开页面)打开网址","openUrlBlank",1,"Blank方式(新开页面)打开网址 （输入值-网址）"),
    KeyBoard("输入键盘","keyBoard",1,"输入键盘（输入值 org.openqa.selenium.Keys枚举中的键盘值）  例如"+ Arrays.stream(Keys.values()).map(o -> o.name()).collect(Collectors.toList())),
    Javascript("执行js脚本","javascript",1,"执行js脚本（输入值-js脚本）"),
    PressKey("输入手机按键","pressKey",1,"输入手机按键（输入值-io.appium.java_client.android.nativekey.AndroidKey枚举中的按键值) 例如"+ Arrays.stream(AndroidKey.values()).map(o -> o.name()).collect(Collectors.toList())),
    ClickText("点击目标文本元素","clickText",1,"点击目标文本元素（输入值-目标文本）"),
    AssertToastHasAppeared("断言目标toast信息是否出现 (检查频率0.1秒) 供Excel自动化使用","assertToastHasAppeared",1,"断言目标toast信息是否出现 (检查频率0.1秒) 输入值格式:  等待时间:等待消息内容 例如  3:提交成功"),
    DrawMarkInScreen("在屏幕上画图标记(滑动操作)","drawMarkInScreen",1,"在屏幕上画图标记(滑动操作) 输入值- 1-勾 2-叉 3-圆形"),
    ClickRandomPointInCustomArea("给定区域内随机点击1次","clickRandomPointInCustomArea",1,"在给定区域内随机点击某点,输入值-左上角坐标:右下角坐标 坐标格式-X坐标,Y坐标  例如 200,200:300,300"),
    ClickPoint("点击目标坐标点","clickPoint",1,"点击目标坐标点,输入值-X坐标:Y坐标 例如 200,200"),
    SetTimeOut("设置全局隐式等待时间","setTimeOut",1,"设置全局隐式等待时间,输入值-秒值"),


    //类型-2  需定位 无需输入参数
    Click("点击","click",2,"点击(元素)"),
    ClickNegatively("消极等待点击","clickNegatively",2,"消极等待点击(元素)"),
    RightClick("右键单击元素","rightClick",2,"右键单击元素（元素）"),
    MoveToElement("移动至元素","moveToElement",2,"焦点移动至(元素)"),
    ClickAndHold("点击并保持按住","clickAndHold",2,"点击并保持按下状态(元素)"),
    DoubleClick("双击","doubleClick",2,"双击(元素)"),
    Release("释放元素","release",2,"释放（元素）"),
    NavigateToFrame("跳转至Frame","navigateToFrame",2,"跳转Frame(元素)"),

    //类型-3  需定位 需输入参数
    ClickCustomWait("自定义等待点击","clickCustomWait",3,"自定义等待点击(元素)+ 输入值(时间配置 格式: 等待时间(单位秒):检查频率(单毫秒) 例如: 5:300 等待5秒每300毫秒检查一次)"),
    ClickNegativelyCustomWait("自定义消极等待点击","clickNegativelyCustomWait",3,"自定义消极等待点击(元素)+ 输入值(时间配置 格式: 等待时间(单位秒):检查频率(单毫秒) 例如: 10:500 等待10秒每500毫秒检查一次)"),
    InputText("输入文本","inputText",3,"输入文本（元素 + 输入值）"),
    AssertElementText("断言类型-元素文本","assertElementText",3,"断言元素上文本是否符合预期 （元素 + 输入值-预期文本）"),
    DragAndDropToPoint("拖拽某元素至目标坐标位置并释放","dragAndDropToPoint",3,"拖拽某元素至目标坐标位置并释放 （元素 +输入值-格式: x坐标:y坐标 例如：300:400 ）"),
    DragAndDropToElement("拖拽某元素至目标元素位置并释放","dragAndDropToElement",3,"拖拽某元素至目标元素位置并释放 (元素 + 输入值 定位方式|定位值   例如: xpath|//*[@id=\"pane-third\"]"),
    ClickInElementsByText("点击组元素中文本符合预期的元素","clickInElementsByText",3,"点击组元素中文本符合预期的元素-（元素）"),

    //类型-4  特殊类型 不需要WebDriver(其他类型需要WebDriver) 不需定位 传入需输入参数
    ThreadSleep("线程休眠","threadSleep",4,"线程休眠(输入值 秒值-支持小数)"),
    InitOrConnectAppiumService("根据用户配置 创建AppiumService或连接自定义的服务地址","initOrConnectAppiumService",4,"根据用户配置 是否创建AppiumService(输入值-JSON配置 配置说明: {\"isStartDefaultService\": true, #是否由服务器开启默认服务\"port\": 4721,  # 如果不由服务器开启,则必须指定port\"ipAddress\": \"127.0.0.1\",# 如果不由服务器开启,则必须指定IP地址\"basePath\": \"/wd/hub/\"  # 如果不由服务器开启,则必须指定basePath} "),
    InitBaseDriverAndStartService("开启AppiumService并实例化Driver(打开APP)","initBaseDriverAndStartService",4,"开启AppiumService并实例化Driver(打开APP) (输入值-APP JOSN格式的DesiredCapabilities配置) 配置示例: {\"deviceName\":\"XXXXXXXXX\",\"platformName\":\"Android\",\"platformVersion\":\"10\",\"appPackage\":\"cn.host.qc\",\"appActivity\":\".ui.login.LaunchActivity\",\"automationName\":\"Appium\",\"chromedriverExecutable\":\"D:\\\\work\\\\code\\\\nhdc-cloud-test-platform\\\\src\\\\main\\\\resources\\\\driver\\\\chromedriver88.exe\",\"noReset\":\"false\",\"fastReset\":\"false\"}"),
    ;

    private String name;
    private String actionKeyword;
    private Integer type;
    private String description;

    public static AndroidActionTypeEnum getTargetType(String actionKeyword) {
        for (AndroidActionTypeEnum value : AndroidActionTypeEnum.values()) {
            if (value.getActionKeyword().equalsIgnoreCase(actionKeyword)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getAllKeyword() {
        return Arrays.stream(AndroidActionTypeEnum.values()).map(AndroidActionTypeEnum::getActionKeyword).collect(Collectors.toList());
    }

    public static List<String> getAllDescription() {
        return Arrays.stream(AndroidActionTypeEnum.values()).map(AndroidActionTypeEnum::getDescription).collect(Collectors.toList());
    }
}
