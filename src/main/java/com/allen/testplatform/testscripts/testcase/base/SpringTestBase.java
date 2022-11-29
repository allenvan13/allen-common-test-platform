package com.allen.testplatform.testscripts.testcase.base;

import com.allen.testplatform.TestPlatformApplication;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.common.TestCommon;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * 基于Spring环境的测试基类<br>
 * 如果需要启动Spring环境,可继承该基类进行测试<br>
 * <br>
 * ps： testng注解启动spring环境须知:  <br>
 * AbstractTestNGSpringContextTests：测试类只有继承了该类才能拥有注入实例能力，否则注入报错<br>
 * AbstractTransactionalTestNGSpringContextTests：测试类继承该类后拥有注入实例能力，同时拥有事务控制能力<br>
 * "@Rollback"注解：默认为true,即case执行前开启事务，case结束后rollback回滚事务。  注解可加在测试方法上<br>
 *
 * @author Fan QingChuan
 * @since 2022/6/1 18:09
 */

@SpringBootTest(classes = TestPlatformApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringTestBase extends AbstractTestNGSpringContextTests implements TestCommon {

    private static final ReportLog reportLog = new ReportLog(SpringTestBase.class);

    @BeforeSuite
    public void beforeAllSuitesSetUp() {
        reportLog.info("准备执行测试 ========== >>  配置相关资源及环境");
    }

    @AfterSuite
    public void afterAllSuitesTearDown() {
        reportLog.info("测试整体执行结束 ========== >>  释放相关资源");
    }
}
