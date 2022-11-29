package com.allen.testplatform.testscripts.page.base;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.common.AndroidCommon;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class AndroidBasePage extends BasePage implements AndroidCommon {

    private static final ReportLog reportLog = new ReportLog(AndroidBasePage.class);

    public AndroidDriver driver;

    public AndroidBasePage() {
    }

    public AndroidBasePage(AndroidDriver driver) {
        this.driver = driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(AndroidCommon.APP_COMMON_TIME_OUT));
    }

    public AndroidBasePage(AndroidDriver driver, String appPackage, String appActivity) {
        this.driver = driver;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(AndroidCommon.APP_COMMON_TIME_OUT));
        this.driver.startActivity(new Activity(appPackage,appActivity));
    }

    public AndroidBasePage(AndroidDriver driver, String title) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(AndroidCommon.APP_COMMON_TIME_OUT));
        try {
            boolean flag = wait.until((ExpectedCondition<Boolean>) arg0 -> arg0.getTitle().equals(title));
        } catch (TimeoutException te) {
            throw new IllegalStateException("当前不是预期页面，当前页面title是：" + driver.getTitle());
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(AndroidCommon.APP_COMMON_TIME_OUT));
    }

}
