package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.page.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.List;

public class AdminHomePage extends BasePage implements HomePage {

    private static final ReportLog reportLog = new ReportLog(AdminHomePage.class);

    public AdminHomePage(WebDriver driver) {
        super(driver);
    }

    public AdminHomePage(WebDriver driver, URL url) {
        super(driver,url);
    }

    public AdminHomePage(WebDriver driver, String title) {
        super(driver, title);
    }

    @Override
    public void logout() {
        WebDriverWait wait = new WebDriverWait(super.driver,Duration.ofSeconds(30));
        WebElement username = wait.ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span")));

        Actions builder = new Actions(super.driver);
        builder.moveToElement(username, 0, 0).click().perform();

        List<WebElement> elements = wait.ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("el-dropdown-menu__item")));

        for (WebElement element : elements) {
            reportLog.info("标签-> [{}] 元素是否可见-> [{}] 元素是否可用-> [{}]",element.getAttribute("textContent"),element.isDisplayed(),element.isEnabled());
            if (element.getAttribute("textContent").contains("退出登录")) {
                JavascriptExecutor executor = (JavascriptExecutor)driver;
                executor.executeScript("arguments[0].click();", element);
//                builder.moveToElement(element, 0, 0).click().perform();
                break;
            }
        }
    }
}
