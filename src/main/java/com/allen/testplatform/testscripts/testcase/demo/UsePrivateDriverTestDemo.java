package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.page.jx.AdminHomePage;
import com.allen.testplatform.testscripts.page.jx.AdminLoginPage;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;


/**
 * demo 实例化私有的 {@link #driver} 进行测试  <br>
 * Method 1 :  {@link #testLoginDemo1}  关键字写法 - 调用基类方法, 指定传入 {@link #driver} 进行操作  <br>
 * Method 2 : {@link #testLoginDemo2}  PageObject写法 - 调用页面Page已封装好的方法  指定传入 {@link #driver} 进行操作  <br>
 * 类似: {@link AdminLoginPage#loginToHome(String, String)}  及  {@link AdminHomePage#logout()}   <br>
 * 同时可直接调用 {@link com.allen.testplatform.testscripts.testcase.base.common.WebCommon} <br>
 * 及其父接口 {@link com.allen.testplatform.testscripts.testcase.base.common.TestCommon} 中方法 需调用者主动传入指定driver 本案例中传入的  {@link #driver} 与 testLoginDemo1效果一致 <br>
 * Method 3 : {@link #testLoginDemo3}  selenium 原生写法 具体方法请查看文档
 * @author Fan QingChuan
 * @since 2022/7/5 18:01
 */

public class UsePrivateDriverTestDemo extends WebTestBase {

    private static final ReportLog reportLog = new ReportLog(UsePrivateDriverTestDemo.class);

    private WebDriver driver;

    @AfterMethod
    void reset() {
        driver.quit();
    }

    @BeforeMethod
    void init() {
        driver = buildWebBrowser("chrome:102");
//        driver = WebDriverFactory.initDriver("chrome","102");
//        ChromeOptions chromeOptions = DriverOptions.getNormalChromeOptions();
//        driver = new ChromeDriver(chromeOptions);
    }


    @Test(description = "匠星后台登录测试用例-TestBse 关键字写法")
    void testLoginDemo1(@Optional("ATE001")String username,
                        @Optional("a123456")String password,
                        @Optional("NHATE-员工A")String expectRealname) {

        //登录
        openUrl(driver,"http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        inputText(driver,"id","username",username);
        inputText(driver,"id","password",password);
        click(driver,"id","login-button");
        //断言
        assertElementText(driver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);

        //退出登录
        clickAndHold(driver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span");
        clickInElementsByText(driver,LocateType.CLASS_NAME,"el-dropdown-menu__item","退出登录");
    }

    @Test(description = "匠星后台登录测试用例-pageObject写法")
    void testLoginDemo2(@Optional("ATE001")String username,
                        @Optional("a123456")String password,
                        @Optional("NHATE-员工A")String expectRealname) throws MalformedURLException {
        //登录
        AdminLoginPage adminLoginPage = new AdminLoginPage(driver,new URL("http://uat-jxadmin.host.cn/#/cooperation-manage/home"));
        AdminHomePage adminHomePage = adminLoginPage.loginToHome(username, password);

        //断言 3种调用
        adminHomePage.assertElementText(driver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        assertElementText(driver,LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        assertElementText(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        //退出登录
        adminHomePage.logout();
    }

    @Test(description = "匠星后台登录测试用例-原生写法")
    void testLoginDemo3(@Optional("ATE001")String username,
                        @Optional("a123456")String password,
                        @Optional("NHATE-员工A")String expectRealname) {

        driver.get("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        //登录
        driver.findElement(By.id("username")).click();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).click();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-button")).click();
        //断言 3种调用
        String actualText = driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span")).getText();
        Assert.assertEquals(actualText,expectRealname,"看到这条消息! 代表测试失败");

        //退出登录
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(500));
        WebElement usernameElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span")));
        Actions actions = new Actions(driver);
        actions.clickAndHold(usernameElement).perform();

        List<WebElement> elements = wait.ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("el-dropdown-menu__item")));

        elements.forEach(e -> {
            reportLog.info("标签-> [{}] 元素是否可见-> [{}] 元素是否可用-> [{}]",e.getAttribute("textContent"),e.isDisplayed(),e.isEnabled());
            if (e.getAttribute("textContent").contains("退出登录")) {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click();", e);
            }
        });
    }

    @AfterTest
    void tearDown() {
        threadSleep("2");

        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
