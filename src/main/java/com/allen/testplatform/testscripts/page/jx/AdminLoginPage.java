package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.page.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;

import java.net.URL;

public class AdminLoginPage extends BasePage implements LoginPage {

    @FindBy(id = "username")
    @CacheLookup
    WebElement inputUsername;

    @FindBy(id = "password")
    @CacheLookup
    WebElement inputPassword;

    @FindBy(id = "login-button")
    @CacheLookup
    WebElement loginButton;

    public AdminLoginPage(WebDriver driver) {
        super(driver);
    }

    public AdminLoginPage(WebDriver driver, URL url) {
        super(driver, url);
    }

    public AdminLoginPage(WebDriver driver, String title) {
        super(driver, title);
    }

    public AdminHomePage loginToHome(String username, String password){
        inputUsername.sendKeys(username);
        inputPassword.sendKeys(password);
        loginButton.click();
        return new AdminHomePage(driver);
    }

    public AdminHomePage loginToHome2(String username, String password) {
        inputText(driver,"id","username",username);
        inputText(driver,"id","password",password);
        click(driver,"id","login-button");
        return new AdminHomePage(driver);
    }
}
