package com.allen.testplatform.testscripts.testcase.base.common;

import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.WebDriverFactory;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import cn.nhdc.common.util.CollectionUtils;
import io.appium.java_client.AppiumBy;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public interface WebCommon extends TestCommon {

    int WEB_COMMON_TIME_OUT = WebTestBase.TIME_OUT;
    int WEB_COMMON_SLEEP_TIME = WebTestBase.SLEEP_TIME;

    default void assertElementText(WebDriver driver, String elementLocateType, String elementLocateValue, String expectText) {
        Boolean isText;
        try {
            isText = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                    .until(ExpectedConditions.textToBePresentInElementLocated(getBy(elementLocateType, elementLocateValue),expectText));
            Assertion.verifyTrue(isText,"测试不通过! 元素文本不符合需求");
            reportLog.info("断言 ======== >> 元素上文本是否符合预期[{}]",expectText);
        } catch (Exception e) {
            reportLog.info("结果 ======== >> 测试不通过!");
        }
    }

    default void assertElementText(WebElement element, String expectText) {
        String format = String.format("实际值: %s 期望值: %s ", element.getText(), expectText);
        reportLog.info("断言 ======== >> 元素上文本是否符合预期 {}",format);
        Assertion.verifyEquals(element.getText(),expectText,"测试不通过!" + format);
    }

    default boolean isElementTextToBe(WebDriver driver, String elementLocateType, String elementLocateValue, String expectText) {
        boolean isText = false;
        WebElement element_text = null;
        try {
            reportLog.info("元素上文本是否符合预期 ======== >>  定位类型:[{}],定位值[{}]",elementLocateType,elementLocateValue);
            element_text = driver.findElement(getBy(elementLocateType,elementLocateValue));
            isText = element_text.getText().equalsIgnoreCase(expectText);
        } catch (Exception e) {
            reportLog.info("");
        } finally {
            if (element_text == null) {
                reportLog.info("结果 ======== >> 未获取到元素");
            }else {
                reportLog.info("结果 ======== >> 元素上文本是否符合预期[{}] 实际值:[{}] 期望值:[{}]",isText,element_text.getText(),expectText);
            }
        }

        return isText;
    }

    default boolean isElementTextToBe(WebDriver driver,long outTimeSeconds,  String elementLocateType, String elementLocateValue, String expectText) {
        boolean isText = false;
        WebElement element_text = null;
        try {
            reportLog.info("元素上文本是否符合预期 ======== >>  定位类型:[{}],定位值[{}],等待时间上限[{}]",elementLocateType,elementLocateValue,outTimeSeconds);
            element_text = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
            isText = element_text.getText().equalsIgnoreCase(expectText);
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            if (element_text == null) {
                reportLog.info("结果 ======== >> 未获取到元素");
            }else {
                reportLog.info("结果 ======== >> 元素上文本是否符合预期[{}] 实际值:[{}] 期望值:[{}]",isText,element_text.getText(),expectText);
            }
        }
        return isText;
    }

    default void assertElementExsit(WebDriver driver,long outTimeSeconds, String elementLocateType, String elementLocateValue) {
        Assertion.verifyTrue(isElementExsit(driver,outTimeSeconds,elementLocateType,elementLocateValue),"测试不通过! 元素不存在");
    }

    default boolean isElementExsit(WebDriver driver, String elementLocateType, String elementLocateValue) {
        boolean isExsit = false;

        try {
            reportLog.info("元素是否存在 ======== >> 定位类型:[{}],定位值[{}]",elementLocateType,elementLocateValue);
            driver.findElement(getBy(elementLocateType,elementLocateValue));
            isExsit = true;
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> 元素是否存在[{}]",isExsit);
        }
        return isExsit;
    }

    default boolean isElementExsit(WebDriver driver,long outTimeSeconds, String elementLocateType, String elementLocateValue) {
        boolean isExsit = false;

        try {
            reportLog.info("元素是否存在 ======== >> 定位类型:[{}],定位值[{}],等待时间上限[{}]",elementLocateType,elementLocateValue,outTimeSeconds);
            new WebDriverWait(driver,Duration.ofSeconds(outTimeSeconds))
                    .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType,elementLocateValue)));
            isExsit = true;
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> 元素是否存在[{}]",isExsit);
        }
        return isExsit;
    }

    default boolean isElementsExsit(WebDriver driver, String elementLocateType, String elementLocateValue) {
        boolean isExsit = false;

        try {
            reportLog.info("组元素是否存在 ======== >> 定位类型:[{}],定位值[{}]",elementLocateType,elementLocateValue);
            driver.findElements(getBy(elementLocateType,elementLocateValue));
            isExsit = true;
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> 组元素是否存在[{}]",isExsit);
        }
        return isExsit;
    }

    default boolean isElementsExsit(WebDriver driver,long outTimeSeconds, String elementLocateType, String elementLocateValue) {
        boolean isExsit = false;

        try {
            reportLog.info("组元素是否存在 ======== >> 定位类型:[{}],定位值[{}],等待时间上限[{}]",elementLocateType,elementLocateValue,outTimeSeconds);
            new WebDriverWait(driver,Duration.ofSeconds(outTimeSeconds))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy(elementLocateType,elementLocateValue)));
            isExsit = true;
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> 组元素是否存在[{}]",isExsit);
        }
        return isExsit;
    }

    default void assertElementsCount(WebDriver driver,int expectCount,long outTimeSeconds, String elementLocateType, String elementLocateValue) {

        List<WebElement> elements = null;
        try {
            reportLog.info("断言 ======== >> 组元素是否展示 且个数是否符合预期 [{}]个",expectCount);
            elements = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy(elementLocateType, elementLocateValue)));

        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        }finally {
            if (CollectionUtils.isNotEmpty(elements)) {
                Assertion.verifyEquals(elements.size(),expectCount,"测试不通过! 实际个数与预期不符");
                reportLog.info("结果 ======== >> 组元素是否展示[{}] 且个数是否符合预期",elements.size() > 0);
            }else {
                reportLog.info("结果 ======== >> 组元素不存在");
            }
        }
    }

    default boolean isElementsHasKid(List<WebElement> elements,String kidName) {

        if (CollectionUtils.isEmpty(elements)) {
            return false;
        }

        boolean isExsit = false;

        try {
            reportLog.info("是否含目标子元素 ======== >> 子元素:[{}]",kidName);
            isExsit = elements.stream().anyMatch(e -> e.getText().equalsIgnoreCase(kidName));
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        } finally {
            reportLog.info("结果 ======== >> 是否存在目标子元素[{}]",isExsit);
        }
        return isExsit;
    }

    default boolean isElementsHasKid(WebDriver driver,long outTimeSeconds, String elementLocateType, String elementLocateValue,String kidName) {
        boolean isExsit = false;

        List<WebElement> elements = null;

        try {
            reportLog.info("是否存在目标子元素 ======== >> 子元素:[{}] 定位类型:[{}],定位值[{}],等待时间上限[{}]",kidName,elementLocateType,elementLocateValue,outTimeSeconds);
            elements = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy(elementLocateType, elementLocateValue)));
        } catch (Exception e) {
            reportLog.info("结果 ======== >> 未定位到组元素");
        } finally {
            if (CollectionUtils.isNotEmpty(elements)) {
                isExsit = elements.stream().anyMatch(e -> e.getText().equalsIgnoreCase(kidName));
                reportLog.info("结果 ======== >> 是否存在目标子元素[{}]",isExsit);
            }
        }

        return isExsit;
    }

    default By getBy(String elementLocateType, String elementLocateValue) {
        switch (elementLocateType.toLowerCase()) {
            case "id":
                return By.id(elementLocateValue);
            case "name":
                return By.name(elementLocateValue);
            case "classname":
                return By.className(elementLocateValue);
            case "cssselector":
                return By.cssSelector(elementLocateValue);
            case "tagname":
                return By.tagName(elementLocateValue);
            case "linktext":
                return By.linkText(elementLocateValue);
            case "partiallinktext":
                return By.partialLinkText(elementLocateValue);
            case "xpath":
                return By.xpath(elementLocateValue);
            case "accessibilityid":
                return AppiumBy.ByAccessibilityId.accessibilityId(elementLocateValue);
            case "androiduiautomator":
                return AppiumBy.ByAndroidUIAutomator.androidUIAutomator(elementLocateValue);
            case "androiddatamatcher":
                return AppiumBy.ByAndroidDataMatcher.androidDataMatcher(elementLocateValue);
            case "androidviewmatcher":
                return AppiumBy.ByAndroidViewMatcher.androidViewMatcher(elementLocateValue);
            case "androidviewtag":
                return AppiumBy.ByAndroidViewTag.androidViewTag(elementLocateValue);
            case "appiumid":
                return AppiumBy.ById.id(elementLocateValue);
            case "appiumname":
                return AppiumBy.ByName.name(elementLocateValue);
            case "appiumtagname":
                return AppiumBy.ByTagName.tagName(elementLocateValue);
            case "appiumclassname":
                return AppiumBy.ByClassName.className(elementLocateValue);
            case "appiumcssselector":
                return AppiumBy.ByCssSelector.cssSelector(elementLocateValue);
            case "appiumlinktext":
                return AppiumBy.ByLinkText.linkText(elementLocateValue);
            case "appiumpartiallinktext":
                return AppiumBy.ByPartialLinkText.partialLinkText(elementLocateValue);
            case "appiumxpath":
                return AppiumBy.ByXPath.xpath(elementLocateValue);
            case "appiumimage":
                return AppiumBy.ByImage.image(elementLocateValue);
            case "iOSClassChain":
                return AppiumBy.ByIosClassChain.iOSClassChain(elementLocateValue);
            case "iosnspredicatestring":
                return AppiumBy.ByIosNsPredicate.iOSNsPredicateString(elementLocateValue);
            case "appiumcustom":
                return AppiumBy.ByCustom.custom(elementLocateValue);
            default:
                throw new IllegalArgumentException("定位类型不合法!");
        }
    }

    /**
     * 传入自定义(testcase中自行声明并实例化的driver)WebDriver进行截图
     * @param tr
     * @param driver
     * @return 截图文件绝对路径
     * @throws IOException
     */
    default String takeScreenshot(ITestResult tr, WebDriver driver) throws IOException {
        String formatDate = DateUtils.getTimeSuffix();
        String filePath = tr.getTestClass().getRealClass().getSimpleName().concat("_").concat(tr.getMethod().getMethodName()).concat("_").concat(formatDate).concat(".png");
        return takeScreenshot(driver,filePath);
    }

    /**
     * 截图
     * @param driver Webdriver
     * @param path 文件路径 .png或其他类型图片结尾
     * @return 截图文件绝对路径
     * @throws IOException
     */
    default String takeScreenshot(WebDriver driver,String path) throws IOException {
        if (driver == null) {
            throw new RuntimeException("WebDriver未初始化");
        }
        TakesScreenshot driverName = (TakesScreenshot) driver;
        String homePath = System.getProperty("user.dir");
        File file = driverName.getScreenshotAs(OutputType.FILE);
        String separator = System.getProperty("file.separator");
        String filePath = homePath + separator + "test-output" + separator +"screenshot" + separator + path;
        FileUtils.copyFile(file,new File(filePath));
        return filePath;
    }

    //type-0

    default void closePage(WebDriver driver) {
        if (driver != null) {
            driver.close();
        }else {
            throw new RuntimeException("不存在运行的浏览器  closePage失败! ");
        }
    }

    default void closeBrowser(WebDriver driver) {
        if (driver != null) {
            driver.quit();
        }else {
            throw new RuntimeException("不存在运行的浏览器  closeBrowser失败! ");
        }
    }

    default void pageBack(WebDriver driver) {
        driver.navigate().back();
    }

    default void pageForward(WebDriver driver) {
        driver.navigate().forward();
    }

    default void pageRefresh(WebDriver driver) {
        driver.navigate().refresh();
    }

    //type-1

    default void openUrl(WebDriver driver,String url) {
        driver.get(url);
    }

    default void navigateToUrl(WebDriver driver,String url) {
        driver.navigate().to(url);
    }

    default void navigateToWindows(WebDriver driver,String windowTitle) {
        WebDriver window = driver.switchTo().window(windowTitle);
        while (!window.getTitle().equalsIgnoreCase(windowTitle)){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            window = driver.switchTo().window(windowTitle);
            if (window.getTitle().equalsIgnoreCase(windowTitle)) {
                break;
            }
        }
    }

    default void pause(WebDriver driver,String seconds) {
        Actions actions = new Actions(driver);
        long second = Long.parseLong(seconds);
        actions.pause(Duration.ofMillis(second * 1000)).perform();
    }

    default void openUrlBlank(WebDriver driver,String url) {
        JavascriptExecutor js_open = (JavascriptExecutor) driver;
        js_open.executeScript("window.open('"+url+"')");
        ArrayList<String> handles = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(handles.get(handles.size()-1));
    }

    default void keyBoard(WebDriver driver,String keyValue) {
        Actions actions_key = new Actions(driver);
        actions_key.sendKeys(Keys.valueOf(keyValue)).perform();
    }

    default void javascript(WebDriver driver,String javascriptString) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(javascriptString);
    }

    //type-2

    default void click(WebDriver driver,String elementLocateType, String elementLocateValue) {
        WebElement element_click = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
        element_click.click();
    }

    /**
     * 点击某元素-消极等待 元素不一定出现
     * @param driver
     * @param elementLocateType
     * @param elementLocateValue
     */
    default void clickNegatively(WebDriver driver, String elementLocateType, String elementLocateValue) {
        try {
            WebElement element_click = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                    .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
            if (element_click != null) {
                element_click.click();
            }
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        }
    }

    default void rightClick(WebDriver driver,String elementLocateType, String elementLocateValue) {
        Actions actions_right = new Actions(driver);
        WebElement element_right = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
        actions_right.contextClick(element_right);
    }

    default void moveToElement(WebDriver driver,String elementLocateType, String elementLocateValue) {
        Actions action_2 = new Actions(driver);
        WebElement element_moveTo = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
        action_2.moveToElement(element_moveTo).perform();
    }

    default void clickAndHold(WebDriver driver,String elementLocateType, String elementLocateValue) {
        Actions actions_3 = new Actions(driver);
        WebElement element_hold = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
        actions_3.clickAndHold(element_hold).perform();
    }

    default void doubleClick(WebDriver driver,String elementLocateType, String elementLocateValue) {
        Actions actions_1 = new Actions(driver);
        WebElement element_double = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
        actions_1.doubleClick(element_double).perform();
    }

    default void release(WebDriver driver,String elementLocateType, String elementLocateValue) {
        Actions actions_4 = new Actions(driver);
        WebElement element_release = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
        actions_4.release(element_release).perform();
    }

    default void navigateToFrame(WebDriver driver,String elementLocateType, String elementLocateValue) {
        WebElement element_frame = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
        driver.switchTo().frame(element_frame);
    }

    //type-3
    /**
     * 点击元素-自定义等待
     *
     * @param driver
     * @param outTimeSeconds  最大等待时间 单位秒
     * @param sleep 检查频率 单位 毫秒
     * @param elementLocateType 定位方式
     * @param elementLocateValue 定位信息
     */
    default void clickCustomWait(WebDriver driver,long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        WebElement element_click = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds), Duration.ofMillis(sleep))
                .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
        element_click.click();
    }

    /**
     * 点击元素-自定义消极等待 元素不一定出现
     *
     * @param driver
     * @param outTimeSeconds  最大等待时间 单位秒
     * @param sleep 检查频率 单位 毫秒
     * @param elementLocateType 定位方式
     * @param elementLocateValue 定位信息
     */
    default void clickNegativelyCustomWait(WebDriver driver, long outTimeSeconds, long sleep, String elementLocateType, String elementLocateValue) {
        try {
            WebElement element_click = new WebDriverWait(driver, Duration.ofSeconds(outTimeSeconds), Duration.ofMillis(sleep))
                    .until(ExpectedConditions.elementToBeClickable(getBy(elementLocateType, elementLocateValue)));
            if (element_click != null) {
                element_click.click();
            }
        } catch (Exception e) {
            reportLog.error("错误 ======== >> {}",e.getMessage());
        }
    }

    default void inputText(WebDriver driver,String elementLocateType, String elementLocateValue, String text) {
        WebElement element_input = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
        element_input.clear();
        element_input.sendKeys(text);
    }

    default void dragAndDropToElement(WebDriver driver,String sourceLocateType, String sourceLocateValue, String targetLocateType, String targettLocateValue) {
        Actions actions_drag2 = new Actions(driver);
        WebElement element_dragSource = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(sourceLocateType, sourceLocateValue)));
        WebElement element_dragTarget = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(targetLocateType, targettLocateValue)));
        actions_drag2.dragAndDrop(element_dragSource,element_dragTarget);
    }

    default void dragAndDropToPoint(WebDriver driver,String elementLocateType, String elementLocateValue, int pointX, int pointY) {
        Actions actions_drag1 = new Actions(driver);
        WebElement element_drag1 = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(getBy(elementLocateType, elementLocateValue)));
        actions_drag1.dragAndDropBy(element_drag1,pointX,pointY);
    }

    default void clickInElementsByText(WebDriver driver,String elementLocateType, String elementLocateValue, String targetText) {
        List<WebElement> elements = new WebDriverWait(driver, Duration.ofSeconds(WEB_COMMON_TIME_OUT), Duration.ofMillis(WEB_COMMON_SLEEP_TIME))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(getBy(elementLocateType, elementLocateValue)));
        for (WebElement element : elements) {
            if (element.getText().equalsIgnoreCase(targetText)) {
                element.click();
                break;
            }
        }
    }

    //type-4

    /**
     * 初始化浏览器并返回给调用者
     * @param browserInfo
     * @return
     */
    default WebDriver buildWebBrowser(String browserInfo) {
        if (browserInfo == null) {
            throw new IllegalArgumentException("buildBrowser 输入值不能为空 格式 浏览器名称:版本号 例如 chrome:102");
        }
        String[] browser = browserInfo.split(":");
        if (browser.length > 2) {
            throw new IllegalArgumentException("buildBrowser 浏览器名称及版本号格式不合法 示例: 类型:版本号 chrome:102");
        }
        //实例化新的driver
        return WebDriverFactory.initDriver(browser[0],browser[1]);
    }

}
