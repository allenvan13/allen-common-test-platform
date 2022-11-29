package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.page.base.AndroidBasePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AndroidLoginPage extends AndroidBasePage implements LoginPage {

    public AndroidLoginPage(AndroidDriver driver) {
        super(driver);
    }

    public AndroidLoginPage(AndroidDriver driver, String title) {
        super(driver,title);
    }

    @Override
    public AndroidHomePage loginToHome(String username, String password) {

        WebElement privacyAgree = new WebDriverWait(driver, Duration.ofSeconds(5))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(By.id("cn.host.qc:id/tv_privacy_agree")));

        if (privacyAgree != null) {
            privacyAgree.click();
        }

        inputText(driver, LocateType.ID,"cn.host.qc:id/etAccount",username);
        inputText(driver, LocateType.ID,"cn.host.qc:id/etPassword",password);
        click(driver,LocateType.ID,"cn.host.qc:id/protocolCb");
        click(driver,LocateType.ID,"cn.host.qc:id/btnLogin");
        threadSleep("3");
        return new AndroidHomePage(driver);
    }
}
