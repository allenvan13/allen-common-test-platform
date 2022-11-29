package com.allen.testplatform.testscripts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.Keys;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum WebActionTypeEnum {

    //类型-0  无需定位 无需输入参数
    ClosePage("关闭页面","closePage",0,"关闭当前所在页面窗口"),
    CloseBrowser("关闭浏览器浏览器","closeBrowser",0,"关闭整个浏览器(所有页面)"),
    PageBack("页面返回","pageBack",0,"页面返回"),
    PageForward("页面前进","pageForward",0,"页面前进"),
    PageRefresh("刷新页面","pageRefresh",0,"页面刷新"),

    //类型-1  无需定位 需输入参数
    OpenUrl("打开网络地址","openUrl",1,"打开网址（输入值-网址）"),
    NavigateToUrl("跳转网络地址","navigateToUrl",1,"(同一个页面窗口下)跳转至网址  (输入值-网址)"),
    NavigateToWindows("跳转窗口至目标窗口","navigateToWindows",1,"跳转窗口至目标窗口(输入值-页面标题)"),
    Pause("暂停N秒","pause",1,"暂停N秒(输入值-秒值 支持小数)"),
    OpenUrlBlank("Blank方式(新开页面)打开网址","openUrlBlank",1,"Blank方式(新开页面)打开网址 （输入值-网址）"),
    KeyBoard("输入键盘","keyBoard",1,"输入键盘（输入值 org.openqa.selenium.Keys枚举中的键盘值）  例如"+ Arrays.stream(Keys.values()).map(o -> o.name()).collect(Collectors.toList())),
    Javascript("执行js脚本","javascript",1,"执行js脚本（输入值-js脚本）"),

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
    InitBaseWebBrowser("实例化(打开)浏览器","buildWebBrowser",4,"实例化(打开)浏览器(输入值-浏览器类型:版本号)"),
    ;

    private String name;
    private String actionKeyword;
    private Integer type;
    private String description;

    public static WebActionTypeEnum getTargetType(String actionKeyword) {
        for (WebActionTypeEnum value : WebActionTypeEnum.values()) {
            if (value.getActionKeyword().equalsIgnoreCase(actionKeyword)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getAllKeyword() {
        return Arrays.stream(WebActionTypeEnum.values()).map(WebActionTypeEnum::getActionKeyword).collect(Collectors.toList());
    }

    public static List<String> getAllDescription() {
        return Arrays.stream(WebActionTypeEnum.values()).map(WebActionTypeEnum::getDescription).collect(Collectors.toList());
    }
}
