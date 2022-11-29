package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.page.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;

import java.net.URL;

public class SupplierLoginPage extends BasePage implements LoginPage {

    @FindBy(css = ".el-form-item:nth-child(1) .el-input__inner")
    @CacheLookup
    WebElement inputUsername;

    @FindBy(css = ".el-form-item:nth-child(2) .el-input__inner")
    @CacheLookup
    WebElement inputPassword;

    @FindBy(css = ".login-button")
    @CacheLookup
    WebElement loginButton;

    public SupplierLoginPage(WebDriver driver) {
        super(driver);
    }

    public SupplierLoginPage(WebDriver driver, URL url) {
        super(driver,url);
    }

    public SupplierLoginPage(WebDriver driver, String title) {
        super(driver, title);
    }

    @Override
    public SupplierHomePage loginToHome(String phoneNumber, String password) {
        //点击选择 密码登录 方式
        driver.findElement(By.cssSelector(".tab-item:nth-child(2)")).click();
        inputUsername.sendKeys(phoneNumber);
        inputPassword.sendKeys(password);
        loginButton.click();
        return new SupplierHomePage(driver,"供应商门户");
    }
}
