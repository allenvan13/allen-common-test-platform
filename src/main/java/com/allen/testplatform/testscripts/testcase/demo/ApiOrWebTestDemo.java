package com.allen.testplatform.testscripts.testcase.demo;

import cn.hutool.core.text.StrSplitter;
import com.allen.testplatform.common.enums.ResponseEnum;
import com.allen.testplatform.common.listener.DataEasyExcelListener;
import com.allen.testplatform.common.utils.DownloadUtils;
import com.allen.testplatform.common.utils.HttpUtils;
import com.allen.testplatform.common.utils.TokenUtils;
import com.allen.testplatform.testscripts.config.ReportLog;
import cn.nhdc.common.exception.BusinessException;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.jayway.jsonpath.JsonPath;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Api方式 与 Web方式 做同样的操作demo
 *
 */

public class ApiOrWebTestDemo {
    
    private static final ReportLog reportLog = new ReportLog(ApiOrWebTestDemo.class);

    @Test(description = "测试导出选中-接口自动化请求方式 可对接解析excel后对比数据正确性")
    void testExport() throws InterruptedException {
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001", "a123456", "UAT"));
        String rs = HttpUtils.doGet("http://uat.api.host.cn/qc/fhcy/ticket/getByPage"
                , header, "createDateStart=&createDateEnd=&planRepairDateStart=&planRepairDateEnd=&actualRepairDateStart=&actualRepairDateEnd=&rectifyCompleteDateStart=&rectifyCompleteDateEnd=&category=XX.XXXXXXX.FHCY&banCode=&unit=&roomCode=&level1CheckItem=&level2CheckItem=&level3CheckItem=&current=1&size=10&_ifOverdue=false&_ifRejected=false&_ifReturn=false&orgCode=FCCDGS001&cityName=%E6%88%90%E9%83%BD%E5%85%AC%E5%8F%B8");

        String filePath;
        reportLog.info("{}", JSON.parseObject(rs));
        if (JsonPath.read(rs,"$.message").equals(ResponseEnum.SUCCESS.getMessage())) {
            List<Long> ids = JsonPath.read(rs, "$.body.records..id");
            String params = ("ids="+ids).replaceAll("\"", "").replaceAll("\\[", "").replaceAll("]", "");
            System.out.println(params);
            rs = HttpUtils.doGet("http://uat.api.host.cn/qc/fhcy/ticket/exportChoice", header, params);

            reportLog.info("{}", JSON.parseObject(rs));

            if (JsonPath.read(rs,"$.message").equals(ResponseEnum.SUCCESS.getMessage())) {

                String isComplete;
                do {
                    Thread.sleep(500);
                    rs = HttpUtils.doGet("http://uat.api.host.cn/qc/fhcy/ticket/findUrl", header, "name=&current=1&size=1");
                    reportLog.info("{}", JSON.parseObject(rs));

                    isComplete = JsonPath.read(rs, "$.body.records[0].isComplete");
                }while (!isComplete.equals("完成"));

                String excelUrl = JsonPath.read(rs, "$.body.records[0].excelUrl");
                List<String> strings = StrSplitter.split(excelUrl, "/", -1, true, true);
                filePath = "D:\\download\\download\\".concat(strings.get(strings.size()-1));

                DownloadUtils.downloadFileToDirectory(excelUrl,filePath);
            }else {
                throw new BusinessException("oo接口响应异常!"+JSON.parseObject(rs));
            }
        }else {
            throw new BusinessException("mm接口响应异常!"+JSON.parseObject(rs));
        }

        EasyExcel.read(filePath,new DataEasyExcelListener()).sheet().doRead();

    }

    @Test(description = "测试导出选中-页面自动化方式 也可对接解析excel后对比数据正确性")
    void testExportChoiceBySelenium() throws InterruptedException {

        System.setProperty("webdriver.chrome.driver","D:\\work\\code\\nhdc-cloud-test-platform\\src\\main\\resources\\driver\\chromedriver102.exe");
        ChromeOptions option = new ChromeOptions();
        option.setExperimentalOption("useAutomationExtension", false);
        option.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        option.addArguments("--start-maximized"); // 启动时自动最大化窗口
        option.addArguments("--disable-popup-blocking"); // 禁用阻止弹出窗口
        option.addArguments("no-sandbox"); // 启动无沙盒模式运行
        option.addArguments("disable-extensions"); // 禁用扩展
        option.addArguments("no-default-browser-check"); // 默认浏览器检查
        Map<String, Object> prefs = new HashMap();
        //修改浏览器文件下载路径
        prefs.put("download.default_directory","D:\\download\\download");
//        option.setLogLevel(ChromeDriverLogLevel.WARNING);
//        option.setHeadless(Boolean.TRUE);//设置chrome 无头模式
//        option.addArguments("--headless");//不用打开图形界面。
        WebDriver driver = new ChromeDriver(option);
//        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        try {

        driver.get("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        driver.findElement(By.id("username")).sendKeys("ATE001");
        driver.findElement(By.id("password")).sendKeys("a123456");
        driver.findElement(By.id("login-button")).click();

        driver.findElement(By.cssSelector(".el-menu:nth-child(1) > .el-submenu:nth-child(4) > .el-submenu__title")).click();
        driver.findElement(By.cssSelector(".is-opened .nest-menu:nth-child(1) span")).click();
        driver.findElement(By.id("tab-second")).click();

        WebElement cityChoice = driver.findElement(By.cssSelector(".conditions:nth-child(1) > .el-form-item:nth-child(1) .el-input__inner"));

        Actions builder = new Actions(driver);
        builder.moveToElement(cityChoice, 0, 0).click().perform();
        Thread.sleep(1000);
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/div[3]/div[1]/div[1]/ul/li"));
        reportLog.info("elements.size = [{}]",elements.size());
        for (WebElement element : elements) {
            reportLog.info("标签-> [{}] 元素是否可见-> [{}] 元素是否可用-> [{}]",element.getAttribute("textContent"),element.isDisplayed(),element.isEnabled());
            if (element.getAttribute("textContent").equals("成都公司")) {
                builder.moveToElement(element, 0, 0).click().perform();
                break;
            }
        }

        //查询结果
        driver.findElement(By.cssSelector("#pane-second > div > div.search > div > button.el-button.el-button--primary.el-button--mini > span")).click();
        Thread.sleep(10000);

        Actions action = new Actions(driver);

        action.sendKeys(Keys.valueOf("ARROW_DOWN")).perform();

//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

        //勾选需下载行数据
        driver.findElement(By.cssSelector("#pane-second > div > div.quesition_table > div > div.el-table__fixed > div.el-table__fixed-header-wrapper > table > thead > tr > th.el-table_2_column_10.is-center.el-table-column--selection.is-leaf.el-table__cell > div > label > span > span")).click();
        Thread.sleep(2000);

        //确认导出选中
        driver.findElement(By.cssSelector("#pane-second > div > div.question_list > div:nth-child(2) > button:nth-child(2) > span")).click();

        //tab跳转
        driver.findElement(By.id("tab-third")).click();


        //查询
        driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[1]/div[2]/button[1]/span")).click();
        Thread.sleep(1000);

        //选中
//        WebElement targetRow = driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]"));
//        System.out.println(targetRow.getText());

        WebElement status = driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]/td[5]/div/span"));
        while (!status.getText().equals("完成")) {
            driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[1]/div[2]/button[1]/span")).click();
            Thread.sleep(1000);
//            targetRow = driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]"));
            status = driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]/td[5]/div/span"));
            System.out.println(status.getText());
        }

        driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]/td[1]/div/label/span/span")).click();

//            driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[2]/div[3]/table/tbody/tr[1]/td[1]/div/label/span/span")).click();
        Thread.sleep(2000);
        //下载
        driver.findElement(By.xpath("//*[@id=\"pane-third\"]/div/div[1]/div[2]/button[2]/span")).click();

        Thread.sleep(5000);
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001", "a123456", "UAT"));
        String rs = HttpUtils.doGet("http://uat.api.host.cn/qc/fhcy/ticket/findUrl", header, "name=&current=1&size=1");
        reportLog.info("{}", JSON.parseObject(rs));
        String excelUrl = JsonPath.read(rs, "$.body.records[0].excelUrl");
        List<String> strings = StrSplitter.split(excelUrl, "/", -1, true, true);
        String filePath = "D:\\download\\download\\".concat(strings.get(strings.size()-1));
        System.out.println("文件路径 "+filePath);

        EasyExcel.read(filePath,new DataEasyExcelListener()).sheet().doRead();

        }finally {
            Thread.sleep(5000);
            driver.quit();
        }

    }
}
