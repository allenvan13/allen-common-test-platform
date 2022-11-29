package com.allen.testplatform.testscripts.testcase.base;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.enums.WebActionTypeEnum;
import com.allen.testplatform.testscripts.page.base.BasePage;
import com.allen.testplatform.testscripts.testcase.base.common.WebCommon;
import org.openqa.selenium.WebDriver;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 Selenium<? extends {@link WebDriver}> 交互的测试基类 <br>
 * 使用技巧:   <br>
 * 浏览器Web交互相关测试用例只需要继承该类,就可专注于 业务测试代码的开发  <br>
 *  = 你可以直接初始化该基类的baseWebDriver进行测试 示例 <br> {@link com.allen.testplatform.testscripts.testcase.demo.UseBaseDriverTestDemo} <br> {@link com.allen.testplatform.testscripts.testcase.demo.ExcelWebTestDemo} <br>
 *  = 也可以自行声明并实例化私有的 WebDriver进行测试, 示例 <br> {@link com.allen.testplatform.testscripts.testcase.demo.UsePrivateDriverTestDemo}  <br>
 *  <br>
 * 其他:   <br>
 * = 该基类中封装了Excel自动化方式相关方法,你可以通过Excel编辑好用例,调用执行测试 示例 <br> {@link com.allen.testplatform.testscripts.testcase.demo.ExcelWebTestDemo}  <br>
 * 同时配合平台导入导出接口 {@link com.allen.testplatform.modules.casemanage.controller.TestCaseController#exportTestCase(HttpServletResponse, Integer)}  <br>
 *  {@link com.allen.testplatform.modules.casemanage.controller.TestCaseController#importTestCase(MultipartFile, String, Integer)}  <br>
 * = 如果不继承相关基类,那么你需要用原生方式调用或者自行封装 示例 {@link com.allen.testplatform.testscripts.testcase.demo.SelenuimNativeMethodTestDemo}  <br>
 * = 如果测试需求存在 Web测试 与 数据库及Spring交互 同时存在,则可参照{@link SpringWebTestBase} 说明
 * @author Fan QingChuan
 * @since 2022/6/7 16:53
 */

public abstract class WebTestBase implements WebCommon {

    public static final int TIME_OUT = 10;
    public static final int SLEEP_TIME = 300;
    private static final ReportLog reportLog = new ReportLog(WebTestBase.class);

    protected WebDriver baseWebDriver;

    @BeforeSuite
    public void beforeAllSuitesSetUp(){
        reportLog.info("准备执行测试 ========== >>  配置相关资源及环境");
    }

    @AfterSuite
    public void afterAllSuitesTearDown(){
        reportLog.info("测试整体执行结束 ========== >>  释放相关资源");
        threadSleep("2");

        try {
            if (baseWebDriver != null) {
                baseWebDriver.quit();
                reportLog.info(" ========== >>  关闭baseWebDriver-是否成功关闭[{}]",ObjectUtil.isEmpty(baseWebDriver));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前类(SeleniumTestBase)的driver <br>
     * 用于自定义testng监听 {@link com.allen.testplatform.testscripts.listener.AssertListener} 中测试失败onTestFailure执行截图操作时,需获取当前操作driver
     *
     * @return  {@link WebDriver} of this class
     */
    public WebDriver getDriver() {
        if (baseWebDriver != null) {
            return baseWebDriver;
        }else {
            throw new RuntimeException("baseWebDriver未初始化");
        }
    }

    /**
     * 执行测试（Excel UI自动化测试-正常逻辑）<br>
     * 正常逻辑: 按测试用例循环遍历测试,在一个测试用例中,先将用例下所有测试步骤按步骤序号进行排序(升序), <br>
     * 再按升序进行测试, 每个步骤按操作编码进行识别调用对应操作方法进行测试,
     * 调用方式: switch(actionType) case ...actionMethod 将所有操作类型分为4种调用方式 详情请看{@link WebActionTypeEnum}
     *
     * @param caseVoList 解析后的测试用例List  {@link TestCaseVo}
     */
    public void excuteExcelUiTest(List<TestCaseVo> caseVoList) {
        caseVoList.forEach(caseVo -> {
            List<TestCaseVo.UiCaseStepVo> caseSteps = caseVo.getCaseSteps().stream().sorted(Comparator.comparingInt(TestCaseVo.UiCaseStepVo::getSort)).collect(Collectors.toList());

            caseSteps.forEach(step -> {

                reportLog.info("测试用例编号[{}] 步骤序号:[{}],测试步骤描述 [{}],操作编码:[{}],元素定位方式[{}],定位值[{}],输入参数值[{}]",step.getCaseCode(),step.getSort(),step.getDescription(),step.getActionKeyword(),step.getElementLocateType(),step.getElementLocateValue(),step.getParameter());

                WebActionTypeEnum webActionTypeEnum = WebActionTypeEnum.getTargetType(step.getActionKeyword());

                Assert.assertNotNull(webActionTypeEnum,"操作编码不正确或不存在!");

                switch (webActionTypeEnum) {
                    //type-0
                    case ClosePage:
                        closePage();
                        break;
                    case CloseBrowser:
                        closeBrowser();
                        break;
                    case PageBack:
                        pageBack();
                        break;
                    case PageForward:
                        pageForward();
                        break;
                    case PageRefresh:
                        pageRefresh();
                        break;

                    //type-1
                    case OpenUrl:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("openUrl 输入值(网址)不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        openUrl(step.getParameter());
                        break;
                    case NavigateToUrl:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("navigateToUrl 输入值(跳转URL)不能为空! 步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        navigateToUrl(step.getParameter());
                        break;
                    case NavigateToWindows:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("navigateToWindows 未设置目标页面标题 即输入值不能为空 步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        navigateToWindows(step.getParameter());
                        break;
                    case Pause:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("pause 未设置暂停秒数 即输入值不能为空 步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        pause(step.getParameter());
                        break;
                    case OpenUrlBlank:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("openUrlBlank 未设置网址 即输入值不能为空 步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        openUrlBlank(step.getParameter());
                        break;
                    case KeyBoard:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("keyBoard 输入值(按键KEY)不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        keyBoard(step.getParameter());
                        break;
                    case Javascript:
                        if (step.getParameter() == null) {
                            throw new IllegalArgumentException("javascript 执行js脚本 输入值不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        javascript(step.getParameter());
                        break;

                        //type-2
                    case Click:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("click 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        click(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case ClickNegatively:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("clickNegatively 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        clickNegatively(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case RightClick:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("rightClick 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        rightClick(step.getElementLocateType(),step.getElementLocateValue());
                        break;
                    case MoveToElement:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("moveToElement 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        moveToElement(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case ClickAndHold:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("clickAndHold 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        clickAndHold(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case DoubleClick:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("doubleClick 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        doubleClick(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case Release:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("release 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        release(step.getElementLocateType(), step.getElementLocateValue());
                        break;
                    case NavigateToFrame:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null) {
                            throw new IllegalArgumentException("navigateToFrame 元素定位不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        navigateToFrame(step.getElementLocateType(), step.getElementLocateValue());
                        break;

                        //type-3
                    case ClickCustomWait:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("clickCustomWait 元素定位与输入值均不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        String[] parameters = step.getParameter().split(":");
                        if (parameters.length > 2) {
                            throw new IllegalArgumentException("clickCustomWait 等待配置输入值不合法 格式：超时时间(单位秒):检查频率(单位毫秒) 例如：3:500 表示等待3秒,每500毫秒检查一次  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        clickCustomWait(Long.parseLong(parameters[0]),Long.parseLong(parameters[1]),step.getElementLocateType(),step.getElementLocateValue());
                        break;
                    case ClickNegativelyCustomWait:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("clickNegativelyCustomWait 元素定位与输入值均不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        String[] times = step.getParameter().split(":");
                        if (times.length > 2) {
                            throw new IllegalArgumentException("clickNegativelyCustomWait 等待配置输入值不合法 格式：超时时间(单位秒):检查频率(单位毫秒) 例如：3:500 表示等待3秒,每500毫秒检查一次  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        clickNegativelyCustomWait(Long.parseLong(times[0]),Long.parseLong(times[1]),step.getElementLocateType(),step.getElementLocateValue());
                        break;
                    case InputText:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("inputText 元素定位与输入值均不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        inputText(step.getElementLocateType(), step.getElementLocateValue(),step.getParameter());
                        break;
                    case AssertElementText:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("assertText 元素定位与输入值均不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        assertElementText(step.getElementLocateType(),step.getElementLocateValue(),step.getParameter());
                        break;
                    case DragAndDropToPoint:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("dragAndDropToPoint 元素定位与输入值(坐标点)均不能为空 输入值格式 x坐标:y坐标 例如：500:800 步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        String[] pointXY = step.getParameter().split(":");
                        if (pointXY.length > 2) {
                            throw new IllegalArgumentException("dragAndDropToPoint 目标位置输入值不合法 格式：x坐标:y坐标  例如：  500:800  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        dragAndDropToPoint(step.getElementLocateType(),step.getElementLocateValue(),Integer.parseInt(pointXY[0]),Integer.parseInt(pointXY[1]));
                        break;
                    case DragAndDropToElement:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("dragAndDropToElement 元素定位与输入值(元素定位信息)均不能为空 格式： 类型|定位值  例如：id|password  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        String[] targetElement = step.getParameter().split("\\|");
                        if (targetElement.length > 2) {
                            throw new IllegalArgumentException("dragAndDropToElement 目标位置元素输入值不合法 格式： 类型|定位值  例如：id|password  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        dragAndDropToElement(step.getElementLocateType(),step.getElementLocateValue(),targetElement[0],targetElement[1]);
                        break;
                    case ClickInElementsByText:
                        if (step.getElementLocateType() == null || step.getElementLocateValue() == null || step.getParameter() == null) {
                            throw new IllegalArgumentException("clickInElementsByText 元素定位与输入值均不能为空  步骤序号:" +step.getSort() +"用例编号:"+caseVo.getCaseCode());
                        }
                        clickInElementsByText(step.getElementLocateType(),step.getElementLocateValue(),step.getParameter());
                        break;

                        //type-4
                    case InitBaseWebBrowser:
                        initBaseBrowser(step.getParameter());
                        break;
                    case ThreadSleep:
                        threadSleep(step.getParameter());
                        break;
                    default:
                        throw new IllegalArgumentException("操作编码不合法! 或 不存在该操作 如有必要请联系我增加 > <");
                }
            });
        });
    }

    /**
     * 执行测试（Excel UI自动化测试-反射逻辑）<br>
     * 反射逻辑: 按测试用例循环遍历测试,在一个测试用例中,先将用例下所有测试步骤按步骤序号进行排序(升序), <br>
     * 再按升序进行测试, 每个步骤按操作编码进行识别反射调用对应操作方法进行测试, <br>
     * 调用方式: switch(actionType) case ...invoke(actionMethod)  将所有操作类型分为4种调用方式 详情请看{@link WebActionTypeEnum} <br>
     * 与正常逻辑的区别在于 反射逻辑需实例化对应基类- {@link BasePage} 并在每个测试步骤传入上下文的 WebDriver,
     * 另需注意buildWebBrowser(初始化打开浏览器)操作属特殊情况,不能传入WebDriver,因为在正常情况下,buildWebBrowser前WebDriver为null没初始化,
     * 当然就不存在上文WebDriver
     *
     * @param caseVoList 解析后的测试用例List  {@link TestCaseVo}
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void invokeExcelUiTest(List<TestCaseVo> caseVoList) throws InstantiationException, IllegalAccessException {

        Class<?> clazz = null;
        try {
            clazz = Class.forName("com.allen.testplatform.testscripts.page.base.BasePage");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert clazz != null;

        BasePage instance = (BasePage) clazz.newInstance();
        Class<?> finalClazz = clazz;

        caseVoList.forEach(caseVo -> {

            List<TestCaseVo.UiCaseStepVo> caseSteps = caseVo.getCaseSteps().stream().sorted(Comparator.comparingInt(TestCaseVo.UiCaseStepVo::getSort)).collect(Collectors.toList());

            caseSteps.forEach(step -> {
                try {
                    reportLog.info("测试用例编号[{}] 步骤序号:[{}],测试步骤描述 [{}],操作编码:[{}],元素定位方式[{}],定位值[{}],输入参数值[{}]",step.getCaseCode(),step.getSort(),step.getDescription(),step.getActionKeyword(),step.getElementLocateType(),step.getElementLocateValue(),step.getParameter());
                    if (step.getActionKeyword().equals(WebActionTypeEnum.InitBaseWebBrowser.getActionKeyword())) {
                        Method method = finalClazz.getMethod(step.getActionKeyword(),String.class);
                        this.baseWebDriver = (WebDriver) method.invoke(instance,step.getParameter());
                    }else {
                        invokeMethod(finalClazz, instance, this.baseWebDriver, step.getActionKeyword(), step.getElementLocateType(), step.getElementLocateValue(), step.getParameter());
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * 使用SeleniumTestBase基类中WebDriver截图 用于testng监听 (测试失败或其他时)自动截图
     * @param tr 测试结果对象
     * @return 截图文件绝对路径
     * @throws IOException
     */
    public String takeScreenshot(ITestResult tr) throws IOException {
        return takeScreenshot(tr,this.baseWebDriver);
    }

    //type-0

    protected void closePage() {
        closePage(baseWebDriver);
    }

    protected void closeBrowser() {
        closeBrowser(baseWebDriver);
    }

    protected void pageBack() {
        pageBack(baseWebDriver);
    }

    protected void pageForward() {
        pageForward(baseWebDriver);
    }

    protected void pageRefresh() {
        pageRefresh(baseWebDriver);
    }

    //type-1

    protected void openUrl(String url) {
        openUrl(baseWebDriver,url);
    }

    protected void navigateToUrl(String url) {
        navigateToUrl(baseWebDriver,url);
    }

    protected void navigateToWindows(String windowTitle) {
        navigateToWindows(baseWebDriver,windowTitle);
    }

    protected void pause(String seconds) {
        pause(baseWebDriver,seconds);
    }

    protected void openUrlBlank(String url) {
        openUrlBlank(baseWebDriver,url);
    }

    protected void keyBoard(String keyValue) {
        keyBoard(baseWebDriver,keyValue);
    }

    protected void javascript(String javascriptString) {
        javascript(baseWebDriver,javascriptString);
    }

    //type-2

    protected void click(String elementLocateType, String elementLocateValue) {
        click(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void clickNegatively(String elementLocateType, String elementLocateValue) {
        clickNegatively(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void rightClick(String elementLocateType, String elementLocateValue) {
        rightClick(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void moveToElement(String elementLocateType, String elementLocateValue) {
        moveToElement(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void clickAndHold(String elementLocateType, String elementLocateValue) {
        clickAndHold(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void doubleClick(String elementLocateType, String elementLocateValue) {
        doubleClick(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void release(String elementLocateType, String elementLocateValue) {
        release(baseWebDriver,elementLocateType,elementLocateValue);
    }

    protected void navigateToFrame(String elementLocateType, String elementLocateValue) {
        navigateToFrame(baseWebDriver,elementLocateType,elementLocateValue);
    }

    //type-3

    protected void clickCustomWait(long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        clickCustomWait(baseWebDriver,outTimeSeconds,sleep,elementLocateType,elementLocateValue);
    }

    protected void clickNegativelyCustomWait(long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        clickNegativelyCustomWait(baseWebDriver,outTimeSeconds,sleep,elementLocateType,elementLocateValue);
    }

    protected void inputText(String elementLocateType, String elementLocateValue, String text) {
        inputText(baseWebDriver,elementLocateType,elementLocateValue,text);
    }

    protected void assertElementText(String elementLocateType, String elementLocateValue, String expectText) {
          assertElementText(baseWebDriver,elementLocateType,elementLocateValue,expectText);
    }

    protected void dragAndDropToElement(String sourceLocateType, String sourceLocateValue, String targetLocateType, String targettLocateValue) {
        dragAndDropToElement(baseWebDriver,sourceLocateType,sourceLocateValue,targetLocateType,targettLocateValue);
    }

    protected void dragAndDropToPoint(String elementLocateType, String elementLocateValue, int pointX, int pointY) {
        dragAndDropToPoint(baseWebDriver,elementLocateType,elementLocateValue,pointX,pointY);
    }

    protected void clickInElementsByText(String elementLocateType, String elementLocateValue, String targetText) {
       clickInElementsByText(baseWebDriver,elementLocateType,elementLocateValue,targetText);
    }

    //type-4
    /**
     * 初始化基类baseWebDriver
     * @param browserInfo
     */
    protected void initBaseBrowser(String browserInfo) {
        //实例化新的driver
        baseWebDriver = buildWebBrowser(browserInfo);
    }

    private void invokeMethod(Class<?> clazz, Object instance, WebDriver driver, String actionKeyword, String elementLocateType, String elementLocateValue, String parameter)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        WebActionTypeEnum webActionTypeEnum = WebActionTypeEnum.getTargetType(actionKeyword);
        if (ObjectUtil.isNull(webActionTypeEnum)) {
            throw new IllegalStateException("操作编码 actionKeyword 不合法");
        }
        Method method;
        switch (webActionTypeEnum.getType()) {
            case 0:
                method = clazz.getMethod(actionKeyword,WebDriver.class);
                method.invoke(instance,driver);
                break;
            case 1:
                method = clazz.getMethod(actionKeyword,WebDriver.class,String.class);
                method.invoke(instance,driver,parameter);
                break;
            case 2:
                method = clazz.getMethod(actionKeyword,WebDriver.class,String.class,String.class);
                method.invoke(instance,driver,elementLocateType,elementLocateValue);
                break;
            case 3:
                method = clazz.getMethod(actionKeyword,WebDriver.class,String.class,String.class,String.class);
                method.invoke(instance,driver,elementLocateType,elementLocateValue,parameter);
                break;
            case 4:
                method = clazz.getMethod(actionKeyword,String.class);
                method.invoke(instance,parameter);
                break;
            default:
                break;
        }
    }

}
