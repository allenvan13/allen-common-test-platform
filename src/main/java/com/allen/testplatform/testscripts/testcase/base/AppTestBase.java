package com.allen.testplatform.testscripts.testcase.base;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.common.AppCommon;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServerHasNotBeenStartedLocallyException;
import io.appium.java_client.service.local.flags.ServerArgument;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * App相关测试通用Base <br>
 * 继承该类需实现抽象方法-Excel执行测试方法 {@link AppTestBase#excuteExcelUiTest(List)} {@link AppTestBase#invokeExcelUiTest(List)}  <br>
 * 该类只提供AppiumDriverLocalService {@link AppTestBase#service} 不提供具体类型baseDriver  <br>
 * 即:
 * 测试case应早已明确该从事具体类型Driver的测试,应选择继承该类子类 {@link AndroidTestBase} 或 {@link IOSTestBase} 或其他
 *
 * @author Fan QingChuan
 * @since 2022/6/7 16:53
 */

public abstract class AppTestBase extends WebTestBase implements AppCommon {

    private static final ReportLog reportLog = new ReportLog(AppTestBase.class);

    public static final int TIME_OUT = 5;
    public static final int SLEEP_TIME = 300;
    public static final int DEFAULT_PORT = 4723;
    public static final String DEFAULT_IP = "127.0.0.1";

    protected AppiumDriverLocalService service;

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
            if (service != null && service.isRunning()) {
                service.stop();
                reportLog.info(" ========== >>  关闭AppiumService-是否成功关闭[{}]", !service.isRunning());
            }

            if (baseWebDriver != null) {
                baseWebDriver.quit();
                reportLog.info(" ========== >>  关闭baseWebDriver-是否成功关闭[{}]", ObjectUtil.isEmpty(baseWebDriver));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public abstract void excuteExcelUiTest(List<TestCaseVo> caseVoList);

    @Override
    public abstract void invokeExcelUiTest(List<TestCaseVo> caseVoList) throws InstantiationException, IllegalAccessException;


    public void startDefaultBaseService() {

        try {
            service = buildDefaultAppiumService();
            checkPortAndKillTask(DEFAULT_PORT);
            service.start();
            reportLog.info("开启Appium本地服务 ========== >>  URL: {}",service.getUrl());
        } catch (AppiumServerHasNotBeenStartedLocallyException e) {
            e.printStackTrace();
        }finally {
            reportLog.info("检查服务是否正常运行 ========== >>  [{}]",service.isRunning());
        }
    }

    public void startCustomBaseService(int port, String ipAddress, Map<ServerArgument,String> arguments) {

        try {
            service = buildCustomAppiumService(port,ipAddress,arguments);
            checkPortAndKillTask(port);
            service.start();
            reportLog.info("开启Appium本地服务 ========== >>  URL: [{}]",service.getUrl());
        } catch (AppiumServerHasNotBeenStartedLocallyException e) {
            e.printStackTrace();
        }finally {
            reportLog.info("服务是否正常开启 ========== >>  [{}]",service.isRunning());
        }
    }

    public URL getServiceUrl() {
        if (service.isRunning()) {
            return service.getUrl();
        }else {
            throw new RuntimeException("AppiumDriverLocalService service 未启动!");
        }
    }

    protected void closeBaseAppiumService() {
        closeAppiumService(service);
    }






}
