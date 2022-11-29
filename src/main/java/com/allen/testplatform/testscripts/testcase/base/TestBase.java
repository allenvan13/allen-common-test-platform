package com.allen.testplatform.testscripts.testcase.base;

import com.allen.testplatform.testscripts.config.ReportLog;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * 普通测试基类<br>
 * 当你不需要启动Spring环境 且 不依赖 {@link WebTestBase} 类似的Web App等基类提供,可继承该类进行测试<br>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */

public abstract class TestBase {

    private static final ReportLog reportLog = new ReportLog(TestBase.class);

    public static int TIME_OUT = 10;

    @BeforeSuite
    public void beforeAllSuitesSetUp() {
        reportLog.info("准备执行测试 ========== >>  配置相关资源及环境");
    }

    @AfterSuite
    public void afterAllSuitesTearDown() {
        reportLog.info("测试整体执行结束 ========== >>  释放相关资源");
    }

}
