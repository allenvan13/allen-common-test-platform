package com.allen.testplatform.testscripts.page.base;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.common.TestCommon;
import com.allen.testplatform.testscripts.testcase.base.common.WebCommon;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;

public class BasePage implements TestCommon, WebCommon {

    private static final ReportLog reportLog = new ReportLog(BasePage.class);

    public WebDriver driver;

    public BasePage() {
    }

    public BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, WebCommon.WEB_COMMON_TIME_OUT), this);
    }

    public BasePage(WebDriver driver, URL url) {
        this.driver = driver;
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, WebCommon.WEB_COMMON_TIME_OUT), this);
        this.driver.get(url.toString());
    }

    public BasePage(WebDriver driver, final String title) {
        this.driver = driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WebCommon.WEB_COMMON_TIME_OUT));
        try {
            boolean flag = wait.until((ExpectedCondition<Boolean>) arg0 -> arg0.getTitle().equals(title));
        } catch (TimeoutException te) {
            throw new IllegalStateException("当前不是预期页面，当前页面title是：" + driver.getTitle());
        }
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, WebCommon.WEB_COMMON_TIME_OUT), this);
    }

}
