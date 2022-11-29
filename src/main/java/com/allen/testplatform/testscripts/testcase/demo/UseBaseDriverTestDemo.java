package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.page.jx.AdminHomePage;
import com.allen.testplatform.testscripts.page.jx.AdminLoginPage;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * demo 实例化基类的 {@link #baseWebDriver} 进行测试  <br>
 * Method 1 : {@link #testLoginDemo1}  关键字写法 - 调用基类方法,默认传入的 baseWebDriver 进行操作 无需调用者指定driver <br>
 * Method 2 : {@link #testLoginDemo2}  PageObject写法 - 调用页面Page已封装好的方法  <br>
 * 类似: {@link AdminLoginPage#loginToHome(String, String)}  及  {@link AdminHomePage#logout()}   <br>
 * 同时可直接调用 {@link com.allen.testplatform.testscripts.testcase.base.common.WebCommon} <br>
 * 及其父接口 {@link com.allen.testplatform.testscripts.testcase.base.common.TestCommon} 中方法 需调用者主动传入指定driver 本案例中传入的 baseWebDriver 与 testLoginDemo1效果一致
 *
 * @author Fan QingChuan
 * @since 2022/7/5 18:01
 */

public class UseBaseDriverTestDemo extends WebTestBase {

    private static final ReportLog reportLog = new ReportLog(UseBaseDriverTestDemo.class);

    @BeforeTest
    void beforeTest() {
        initBaseBrowser("chrome:102");
    }


    @Test(description = "匠星后台登录测试用例-TestBse 关键字写法")
    void testLoginDemo1(@Optional("ATE001")String username,
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

    @Test(description = "匠星后台登录测试用例-pageObject写法")
    void testLoginDemo2(@Optional("ATE001")String username,
                        @Optional("a123456")String password,
                        @Optional("NHATE-员工A")String expectRealname) throws MalformedURLException {
        //登录
        AdminLoginPage adminLoginPage = new AdminLoginPage(baseWebDriver,new URL("http://uat-jxadmin.host.cn/#/cooperation-manage/home"));
        AdminHomePage adminHomePage = adminLoginPage.loginToHome(username, password);

        //断言 3种调用
        adminHomePage.assertElementText(baseWebDriver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        assertElementText(baseWebDriver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        assertElementText(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        //退出登录
        adminHomePage.logout();
    }

    @AfterTest
    void tearDown() {
        threadSleep("2");
    }
}
