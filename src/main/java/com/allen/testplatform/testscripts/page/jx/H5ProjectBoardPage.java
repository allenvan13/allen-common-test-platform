package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.page.base.BasePage;
import org.openqa.selenium.WebDriver;

import java.net.URL;

public class H5ProjectBoardPage extends BasePage {

    public H5ProjectBoardPage(WebDriver driver) {
        super(driver);
    }

    public H5ProjectBoardPage(WebDriver driver, URL url) {
        super(driver,url);
    }

    public H5ProjectBoardPage(WebDriver driver, String title) {
        super(driver, title);
    }

}
