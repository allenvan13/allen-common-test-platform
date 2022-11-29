package com.allen.testplatform.testscripts.testcase;

import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import io.appium.java_client.android.AndroidDriver;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * @author Fan QingChuan
 * @since 2022/7/1 10:08
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class DebugDemo2Test {

    private static final ReportLog reportLog = new ReportLog(DebugDemo2Test.class);

    private AndroidDriver driver;

    @BeforeTest
    public void setUp() throws MalformedURLException {
        DesiredCapabilities capabilities = AppiumCapabilities.getHarmonyJx();
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub/"),capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterTest
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @SneakyThrows(Exception.class)
    @Test
    void testScreenShot() {
        Thread.sleep(10000);
        Assert.assertEquals(1,3,"测试失败!!!!");
    }

    /**
     * 截图
     * @param driver Webdriver
     * @param path 文件路径 .png或其他类型图片结尾
     * @return 截图文件绝对路径
     * @throws IOException
     */
    public String takeScreenshot(WebDriver driver, String path) throws IOException {
        if (driver == null) {
            throw new RuntimeException("WebDriver未初始化");
        }
        TakesScreenshot driverName = (TakesScreenshot) driver;
        File file = driverName.getScreenshotAs(OutputType.FILE);
        String filePath = CommonUtils.getOutPutRootPath() +"screenshot" + CommonUtils.SEPARATOR + path;
        FileUtils.copyFile(file,new File(filePath));
        return filePath;
    }

    public WebDriver getDriver() {
        if (driver != null) {
            return driver;
        }else {
            throw new RuntimeException(" driver 未初始化");
        }
    }
}
