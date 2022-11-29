package com.allen.testplatform.testscripts.testcase.jx;

import com.allen.testplatform.testscripts.config.ReportLog;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * @author Fan QingChuan
 * @since 2022/8/4 12:56
 */

public class CommonFhcy extends CommonAndroid {

    private static final ReportLog reportLog = new ReportLog(CommonFhcy.class);

    public WebElement getTitleEle() {
        WebElement title;
        try {
            title = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.ById.id("cn.host.qc:id/tv_toolbar_title")));
        } catch (Exception e) {
            title = wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.ById.id("cn.host.qc:id/tv_toolbar_title")));
        }

        return title;
    }
}
