package com.allen.testplatform.testscripts.testcase.base;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.config.AppiumServiceEntity;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.enums.AndroidActionTypeEnum;
import com.allen.testplatform.testscripts.page.base.IOSBasePage;
import com.allen.testplatform.testscripts.testcase.base.common.IOSCommon;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Spring环境下IOS APP测试基类
 * 与{@link SpringAndroidTestBase}使用方式一致
 *
 * @author Fan QingChuan
 * @since 2022/6/7 16:53
 */

public abstract class SpringIOSTestBase extends SpringAppTestBase implements IOSCommon {

    private static final ReportLog reportLog = new ReportLog(SpringIOSTestBase.class);

    protected IOSDriver baseIOSDriver;

    @BeforeSuite
    @Override
    public void beforeAllSuitesSetUp(){
        reportLog.info("准备执行测试 ========== >>  配置相关资源及环境");
    }

    @AfterSuite
    @Override
    public void afterAllSuitesTearDown(){
        reportLog.info("测试整体执行结束 ========== >>  释放相关资源");
        threadSleep("2");

        try {

            if (baseIOSDriver != null) {
                baseIOSDriver.quit();
                reportLog.info(" ========== >>  关闭baseIOSDriver-是否成功关闭[{}]",ObjectUtil.isEmpty(baseIOSDriver));
            }

            if (service != null && service.isRunning()) {
                service.stop();
                reportLog.info(" ========== >>  关闭AppiumService-是否成功关闭[{}]", !service.isRunning());
            }

            if (baseWebDriver != null) {
                baseWebDriver.quit();
                reportLog.info(" ========== >>  关闭baseWebDriver-是否成功关闭[{}]",ObjectUtil.isEmpty(baseWebDriver));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void excuteExcelUiTest(List<TestCaseVo> caseVoList) {

    }

    @Override
    public void invokeExcelUiTest(List<TestCaseVo> caseVoList) throws InstantiationException, IllegalAccessException {
        Class<?> clazz = null;
        try {
            clazz = Class.forName("com.allen.testplatform.testscripts.page.base.IOSBasePage");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        assert clazz != null;

        IOSBasePage instance = (IOSBasePage) clazz.newInstance();
        Class<?> finalClazz = clazz;
        AtomicReference<AppiumServiceEntity> serviceEntity = new AtomicReference<>();
        caseVoList.forEach(caseVo -> {

            List<TestCaseVo.UiCaseStepVo> caseSteps = caseVo.getCaseSteps().stream().sorted(Comparator.comparingInt(TestCaseVo.UiCaseStepVo::getSort)).collect(Collectors.toList());

            caseSteps.forEach(step -> {
                try {
                    reportLog.info("测试用例编号[{}] 步骤序号:[{}],测试步骤描述 [{}],操作编码:[{}],元素定位方式[{}],定位值[{}],输入参数值[{}]",step.getCaseCode(),step.getSort(),step.getDescription(),step.getActionKeyword(),step.getElementLocateType(),step.getElementLocateValue(),step.getParameter());
                    if (step.getActionKeyword().equalsIgnoreCase(AndroidActionTypeEnum.InitOrConnectAppiumService.getActionKeyword())) {

                        Method method = finalClazz.getMethod(step.getActionKeyword(),String.class);
                        serviceEntity.set((AppiumServiceEntity) method.invoke(instance, step.getParameter()));

                    }else if (step.getActionKeyword().equalsIgnoreCase(AndroidActionTypeEnum.InitBaseDriverAndStartService.getActionKeyword())){

                        Method method = finalClazz.getMethod(step.getActionKeyword(), AppiumServiceEntity.class, String.class);
                        this.baseIOSDriver = (IOSDriver) method.invoke(instance,serviceEntity.get(),step.getParameter());

                    }else if (step.getActionKeyword().equalsIgnoreCase(AndroidActionTypeEnum.CloseAppiumService.getActionKeyword())){

                        Method method = finalClazz.getMethod(step.getActionKeyword(), AppiumDriverLocalService.class);
                        if (serviceEntity.get().getService().isRunning()) {
                            reportLog.info("准备关闭Appium服务 ========== >>  [{}]",serviceEntity.get().getService().getUrl());
                            this.baseIOSDriver.quit();
                            reportLog.info(" ========== >>  [已关闭baseIOSDriver]");
                            method.invoke(instance,serviceEntity.get().getService());
                        }else {
                            throw new RuntimeException("CloseAppiumService失败! 不存在已启动的AppiumService");
                        }

                    }else {
                        invokeMethod(finalClazz, instance, this.baseIOSDriver, step.getActionKeyword(), step.getElementLocateType(), step.getElementLocateValue(), step.getParameter());
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void invokeMethod(Class<?> clazz, Object instance, IOSDriver driver, String actionKeyword, String elementLocateType, String elementLocateValue, String parameter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        AndroidActionTypeEnum androidActionTypeEnum = AndroidActionTypeEnum.getTargetType(actionKeyword);
        if (ObjectUtil.isNull(androidActionTypeEnum)) {
            throw new IllegalStateException("操作编码 actionKeyword 不合法");
        }
        Method method;
        switch (androidActionTypeEnum.getType()) {
            case 0:
                method = clazz.getMethod(actionKeyword, WebDriver.class);
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

    public IOSDriver getDriver() {
        if (baseIOSDriver != null) {
            return baseIOSDriver;
        }else {
            throw new RuntimeException(" baseIOSDriver 未初始化");
        }
    }

    public String takeScreenshot(ITestResult tr) throws IOException {
        return takeScreenshot(tr,this.baseIOSDriver);
    }



    //以下 为IOSDriver操作

    /**
     * 在屏幕上画图标记(滑动操作)
     * @param type 1-勾 2-叉 3-圆形
     */
    protected void drawMarkInScreen(int type) {
        drawMarkInScreen(baseIOSDriver,type);
    }

    /**
     * 向上滑动 (手指向上) 单次滑动比例 20%
     *
     * @param second 持续时间
     * @param num 滚动次数
     */
    protected void swipeUp(double second, int num) {
        swipeToDown(baseIOSDriver,second,num);
    }

    /**
     * 向下滑动 (手指向下) 单次滑动比例 20%
     *
     * @param second 持续时间
     * @param num 滚动次数
     */
    protected void swipeDown(double second, int num) {
        swipeToUp(baseIOSDriver,second,num);
    }

    /**
     * 向左滑动 (手指向左) 单次滑动比例 20%
     *
     * @param second 持续时间
     * @param num 滑动次数
     */
    protected void swipeLeft( double second, int num) {
        swipeToRight(baseIOSDriver,second,num);
    }

    /**
     * 向右滑动 (手指向右) 单次滑动比例 20%
     *
     * @param second 持续时间
     * @param num 滑动次数
     */
    protected void swipeRight(double second, int num) {
        swipeToLeft(baseIOSDriver,second,num);
    }

    /**
     * 按X、Y坐标点值 屏幕滑动某点至某点
     *
     * @param startX 原点X坐标
     * @param startY 原点Y坐标
     * @param moveToX 目标点X坐标
     * @param moveToY 目标点Y坐标
     */
    protected void swipeToPoint(double startX, double startY, double moveToX, double moveToY) {
        swipeToPoint(baseIOSDriver,startX,startY,moveToX,moveToY);
    }

    /**
     * 按方向、屏幕比例 屏幕滑动
     *
     * @param ratioX 横向滑动比例
     * @param ratioY 纵向滑动比例
     * @param direction 滑动方向 UP 自下向上 DOWN 自上向下 LEFT 从右往左  RIGHT 从左往右
     */
    protected void swipeByDirect(double ratioX, double ratioY, String direction) {
        swipeByDirect(baseIOSDriver,ratioX,ratioY,direction);
    }

    /**
     * 给定区域(左上角点坐标 & 右下角坐标 即 x坐标最大最小,y坐标最大最小值),在该区域内随机点击某点一次
     *
     * @param minX x坐标最小值
     * @param minY y坐标最小值
     * @param maxX x坐标最大值
     * @param maxY y坐标最大值
     */
    protected PointOption clickRandomPointInCustomArea(double minX, double minY, double maxX, double maxY) {
        return clickRandomPointInCustomArea(baseIOSDriver,minX,minY,maxX,maxY);
    }



    /**
     * 根据某点 进行扩放整体屏幕(双指扩放)
     * @param pointX 坐标X
     * @param pointY 坐标Y
     */
    protected void enlargePoint(double pointX,double pointY) {
        enlargePoint(baseIOSDriver,pointX,pointY);
    }


    //type-5 int String
    protected void assertToastHasAppeared(int outTimeSeconds, String message) {
        assertToastHasAppeared(baseIOSDriver,outTimeSeconds,message);
    }

    protected boolean isToastHasAppeared(int outTimeSeconds, String message) {
        return isToastHasAppeared(baseIOSDriver,outTimeSeconds,message);
    }


    //type-0
    @Override
    protected void closePage() {
        closePage(baseIOSDriver);
    }

    @Override
    protected void closeBrowser() {
        closeBrowser(baseIOSDriver);
    }

    @Override
    protected void pageBack() {
        pageBack(baseIOSDriver);
    }

    @Override
    protected void pageForward() {
        pageForward(baseIOSDriver);
    }

    @Override
    protected void pageRefresh() {
        pageRefresh(baseIOSDriver);
    }

    /**
     * 缩放整体屏幕(双指缩放)
     */
    protected void scaleScreen() {
        scaleScreen(baseIOSDriver);
    }

    /**
     * 扩放整体屏幕(双指扩放)
     */
    protected void enlargeScreen() {
        enlargeScreen(baseIOSDriver);
    }


    //type-1
    @Override
    protected void openUrl(String url) {
        openUrl(baseIOSDriver,url);
    }

    @Override
    protected void navigateToUrl(String url) {
        navigateToUrl(baseIOSDriver,url);
    }

    @Override
    protected void navigateToWindows(String windowTitle) {
        navigateToWindows(baseIOSDriver,windowTitle);
    }

    @Override
    protected void pause(String seconds) {
        pause(baseIOSDriver,seconds);
    }

    @Override
    protected void openUrlBlank(String url) {
        openUrlBlank(baseIOSDriver,url);
    }

    @Override
    protected void keyBoard(String keyValue) {
        keyBoard(baseIOSDriver,keyValue);
    }

    @Override
    protected void javascript(String javascriptString) {
        javascript(baseIOSDriver,javascriptString);
    }

    protected void clickText(String text) {
        clickText(baseIOSDriver,text);
    }

    //type-2
    @Override
    protected void click(String elementLocateType, String elementLocateValue) {
        click(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void clickNegatively(String elementLocateType, String elementLocateValue) {
        clickNegatively(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void rightClick(String elementLocateType, String elementLocateValue) {
        rightClick(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void moveToElement(String elementLocateType, String elementLocateValue) {
        moveToElement(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void clickAndHold(String elementLocateType, String elementLocateValue) {
        clickAndHold(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void doubleClick(String elementLocateType, String elementLocateValue) {
        doubleClick(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void release(String elementLocateType, String elementLocateValue) {
        release(baseIOSDriver,elementLocateType,elementLocateValue);
    }
    @Override
    protected void navigateToFrame(String elementLocateType, String elementLocateValue) {
        navigateToFrame(baseIOSDriver,elementLocateType,elementLocateValue);
    }

    //type-3
    @Override
    protected void clickCustomWait(long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        clickCustomWait(baseIOSDriver,outTimeSeconds,sleep,elementLocateType,elementLocateValue);
    }
    @Override
    protected void clickNegativelyCustomWait(long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        clickNegativelyCustomWait(baseIOSDriver,outTimeSeconds,sleep,elementLocateType,elementLocateValue);
    }
    @Override
    protected void inputText(String elementLocateType, String elementLocateValue, String text) {
        inputText(baseIOSDriver,elementLocateType,elementLocateValue,text);
    }
    @Override
    protected void assertElementText(String elementLocateType, String elementLocateValue, String expectText) {
        assertElementText(baseIOSDriver,elementLocateType,elementLocateValue,expectText);
    }
    @Override
    protected void dragAndDropToElement(String sourceLocateType, String sourceLocateValue, String targetLocateType, String targettLocateValue) {
        dragAndDropToElement(baseIOSDriver,sourceLocateType,sourceLocateValue,targetLocateType,targettLocateValue);
    }
    @Override
    protected void dragAndDropToPoint(String elementLocateType, String elementLocateValue, int pointX, int pointY) {
        dragAndDropToPoint(baseIOSDriver,elementLocateType,elementLocateValue,pointX,pointY);
    }
    @Override
    protected void clickInElementsByText(String elementLocateType, String elementLocateValue, String targetText) {
        clickInElementsByText(baseIOSDriver,elementLocateType,elementLocateValue,targetText);
    }


    //type-4

    /**
     * 初始化 baseIOSDriver
     * @param capabilities 配置信息
     */
    protected void initBaseIOSDriver(DesiredCapabilities capabilities) {
        if (service == null) {
            throw new RuntimeException("AppiumDriverLocalService service  未实例化!");
        }
        if (!service.isRunning()) {
            throw new RuntimeException("AppiumDriverLocalService service  未运行!");
        }
        baseIOSDriver = buildNewIOSDriver(capabilities);
    }

    private IOSDriver buildNewIOSDriver(DesiredCapabilities capabilities) {
        return new IOSDriver(getServiceUrl(),capabilities);
    }

    protected void setBaseTimeOut(int second) {
        setTimeOut(baseIOSDriver,second);
    }

}
