package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.ReportLog;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Test(description = "测试用例示例-selenium原生写法")
public class SelenuimNativeMethodTestDemo {

    private static final ReportLog reportLog = new ReportLog(SelenuimNativeMethodTestDemo.class);

    private WebDriver driver;

    @BeforeMethod
    public void setUp() {
        System.setProperty("webdriver.chrome.driver","D:\\work\\code\\nhdc-cloud-test-platform\\src\\main\\resources\\driver\\chromedriver102.exe");
        ChromeOptions option = new ChromeOptions();
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        option.addArguments("--start-maximized"); // 启动时自动最大化窗口
        option.addArguments("--disable-popup-blocking"); // 禁用阻止弹出窗口
        option.addArguments("no-sandbox"); // 启动无沙盒模式运行
        option.addArguments("disable-extensions"); // 禁用扩展
        option.addArguments("no-default-browser-check"); // 默认浏览器检查
//        option.setLogLevel(ChromeDriverLogLevel.WARNING);
//        option.setHeadless(Boolean.TRUE);//设置chrome 无头模式
//        option.addArguments("--headless");//不用打开图形界面。
        //关闭浏览器密码保存提示
        Map<String, Object> prefs = new HashMap();
        prefs.put("credentials_enable_service",false);
        prefs.put("profile.password_manager_enabled",false);
        option.setExperimentalOption("prefs",prefs);

        driver = new ChromeDriver(option);
//        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @Test(priority = 0)
    void testOpen(){
        driver.get("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        driver.findElement(By.id("username")).sendKeys("ATE001");
        driver.findElement(By.id("password")).sendKeys("a123456");
        driver.findElement(By.id("login-button")).click();
        Assertion.verifyEquals("1","1");
    }

    @Test
    void testLogin() {
        driver.get("http://uat.cooperation.newhope.cn/");
        driver.findElement(By.cssSelector(".login-box")).click();
        driver.findElement(By.cssSelector(".tab-item:nth-child(2)")).click();
        driver.findElement(By.cssSelector(".el-form-item:nth-child(1) .el-input__inner")).click();
        driver.findElement(By.cssSelector(".el-form-item:nth-child(1) .el-input__inner")).sendKeys("13766777771");
        driver.findElement(By.cssSelector(".el-form-item:nth-child(2) .el-input__inner")).click();
        driver.findElement(By.cssSelector(".el-form-item:nth-child(2) .el-input__inner")).sendKeys("a123456");

        driver.findElement(By.cssSelector(".login-button")).click();

        WebElement username = driver.findElement(By.xpath("//*[@class='user-content']/div[1]/span[1]"));
        reportLog.info("用户名-> [{}]",username.getText());
//        WebElement username = driver.findElement(By.className("user-content"));
//        WebElement username = driver.findElement(By.xpath("//*[@id=\"app\"]/div/div[1]/div[2]/div[3]/div/span"));
        Assertion.verifyEquals(username.getText(),"B-员工A-AutoTest");

        Actions builder = new Actions(driver);
        builder.moveToElement(username, 0, 0).click().perform();

        List<WebElement> elements = driver.findElements(By.className("el-dropdown-menu__item"));
        reportLog.info("elements.size = [{}]",elements.size());
        for (WebElement element : elements) {
            reportLog.info("标签-> [{}] 元素是否可见-> [{}] 元素是否可用-> [{}]",element.getAttribute("textContent"),element.isDisplayed(),element.isEnabled());
            if (element.getAttribute("textContent").equals("退出登录")) {
                builder.moveToElement(element, 0, 0).click().perform();
                break;
            }
        }
    }

    @AfterMethod
    public void processStop() throws InterruptedException {
        Thread.sleep(2000);
        driver.close();
    }

    @AfterTest
    public void tearDown() throws InterruptedException {
        Thread.sleep(5000);
        if (driver != null) {
            driver.quit();
        }
    }

}
