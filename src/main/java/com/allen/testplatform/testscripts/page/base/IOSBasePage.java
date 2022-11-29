package com.allen.testplatform.testscripts.page.base;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.common.IOSCommon;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public abstract class IOSBasePage extends BasePage implements IOSCommon {

    private static final ReportLog reportLog = new ReportLog(IOSBasePage.class);

    public IOSDriver driver;

    public IOSBasePage() {
    }

    public IOSBasePage(IOSDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, IOSCommon.APP_COMMON_TIME_OUT), this);
    }

    public IOSBasePage(IOSDriver driver, String appPackage, String appActivity) {
        this.driver = driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IOSCommon.APP_COMMON_TIME_OUT));
        //TODO IOS driver开启页面方法
//        this.driver.startActivity(new Activity(appPackage,appActivity));
    }

    public IOSBasePage(IOSDriver driver, String title) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(IOSCommon.APP_COMMON_TIME_OUT));
        try {
            boolean flag = wait.until((ExpectedCondition<Boolean>) arg0 -> arg0.getTitle().equals(title));
        } catch (TimeoutException te) {
            throw new IllegalStateException("当前不是预期页面，当前页面title是：" + driver.getTitle());
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IOSCommon.APP_COMMON_TIME_OUT));
    }

}
