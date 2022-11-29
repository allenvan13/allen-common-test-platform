package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * @author Fan QingChuan
 * @since 2022/7/6 12:24
 */

public class WebAndAppTestDemo extends AndroidTestBase{

    private static final ReportLog reportLog = new ReportLog(WebAndAppTestDemo.class);

    @BeforeTest
    void beforeTest() {
        //初始化 baseWebDriver
        initBaseBrowser("chrome:102");

        //初始化 baseAndroidDriver
        startCustomBaseService(4756,"127.0.0.1",null);
        DesiredCapabilities capabilities = AppiumCapabilities.getHarmonyJx();
        baseAndroidDriver = new AndroidDriver(getServiceUrl(),capabilities);
        baseAndroidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test(description = "Web测试")
    void testWebDemo(@Optional("ATE001")String username,
                        @Optional("a123456")String password,
                        @Optional("NHATE-员工A")String expectRealname) {

        //登录
        openUrl("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        inputText("id","username",username);
        inputText("id","password",password);
        click("id","login-button");
        //断言
        assertElementText(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);

        //退出登录
        clickAndHold(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span");
        clickInElementsByText(LocateType.CLASS_NAME,"el-dropdown-menu__item","退出登录");
    }


    @Test(description = "App测试")
    void testAppDemo(@Optional("ATE001")String username,
                     @Optional("a123456")String password) {
        clickNegatively(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/tv_privacy_agree");
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etAccount",username);
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etPassword",password);
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/protocolCb");
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/btnLogin");
    }
}
