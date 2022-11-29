package com.allen.testplatform.testscripts.config;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.testscripts.enums.DriverVersionEnum;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;

public class WebDriverFactory {


    public static WebDriver initDriver(String browserName,String version) {
        String driverPath = CommonUtils.getResourceRootPath() + "driver" + CommonUtils.SEPARATOR;

        String targetFileName = DriverVersionEnum.getTargetFileName(browserName, version);
        driverPath = driverPath.concat(targetFileName);

        if (ObjectUtil.isEmpty(targetFileName)) {
            throw new IllegalStateException("target driver is not exist");
        }

        AbstractDriverOptions options;

        switch (browserName.toLowerCase()) {
            case "ie":
                System.setProperty("webdriver.ie.driver", driverPath);
                options = DriverOptions.getNormalIEOptions();
                return new InternetExplorerDriver((InternetExplorerOptions) options);
            case "chrome":
                options = DriverOptions.getNormalChromeOptions();
                System.setProperty("webdriver.chrome.driver", driverPath);
                return new ChromeDriver((ChromeOptions) options);
            case "firefox":
                System.setProperty("webdriver.gecko.driver", driverPath);
                options = DriverOptions.getNormalFirefoxOptions();
                return new FirefoxDriver((FirefoxOptions) options);
            case "edge":
                System.setProperty("webdriver.edge.driver", driverPath);
                EdgeDriverService edgeService = DriverOptions.getNormalEdgeService();
                return new EdgeDriver(edgeService);
            case "chromeh5":
                options = DriverOptions.getH5ChromeOptions();
                System.setProperty("webdriver.chrome.driver", driverPath);
                return new ChromeDriver((ChromeOptions) options);
            default:
                throw new IllegalStateException("browserName is not correct");
        }
    }
}
