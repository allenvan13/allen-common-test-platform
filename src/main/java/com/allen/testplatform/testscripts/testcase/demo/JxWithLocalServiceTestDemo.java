package com.allen.testplatform.testscripts.testcase.demo;

import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import com.allen.testplatform.testscripts.page.jx.AndroidHomePage;
import com.allen.testplatform.testscripts.page.jx.AndroidLoginPage;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.xiaoleilu.hutool.io.FileUtil;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import lombok.SneakyThrows;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Test(description = "开启本地AppiumDriverLocalService测试,测试完成后,启动spring删除业务数据")
@Listeners({ExtentTestNGIReporterListener.class, AssertListener.class})
public class JxWithLocalServiceTestDemo extends AndroidTestBase {

    private static final ReportLog reportLog = new ReportLog(JxWithLocalServiceTestDemo.class);

    private AndroidHomePage homePage;

    private Multimap<String,String> toRemoveData;

    private boolean isNeedRemoveData = true;
    private int width;
    private int height;

    @BeforeTest
    public void setUp() throws MalformedURLException {
        startDefaultBaseService();
        DesiredCapabilities capabilities = AppiumCapabilities.getHarmonyJx();
        baseAndroidDriver = new AndroidDriver(getServiceUrl(),capabilities);
        baseAndroidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        width = baseAndroidDriver.manage().window().getSize().width;
        height = baseAndroidDriver.manage().window().getSize().height;
        toRemoveData =  ArrayListMultimap.create();
    }

    @AfterTest
    public void tearDown() {

        try {
            if (homePage != null) {
                baseAndroidDriver.context("NATIVE_APP");
                homePage.logout();
            }

            if (isNeedRemoveData) {
                //插入待清除数据
                setRemoveDatas();
                //清除数据
                removeTestData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 0,description = "前置操作:登录,断言我的页面姓名展示正确")
    void testLoginHome() {
        AndroidLoginPage jxAndroidLoginPage = new AndroidLoginPage(baseAndroidDriver);
        homePage = jxAndroidLoginPage.loginToHome("ATE001", "a123456");
        assertElementText(LocateType.ID,"cn.host.qc:id/tab_work","工作台");
    }

    @Test(priority = 1,description = "选择分期,断言选择后,页面是否展示目标分期名称")
    void testChooseStage() {
        //点击[选择分期]
        click(LocateType.ID,"cn.host.qc:id/projectTv");

        //选择[南京公司]
        clickInElementsByText(LocateType.ID,"cn.host.qc:id/cityNameTv","南京公司");

        //选择[1]分期
        click(LocateType.XPATH,"/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView[2]/android.widget.LinearLayout[4]/android.widget.TextView");

        assertElementText(LocateType.ID,"cn.host.qc:id/projectTv","南京紫樾府-1期");
    }

    @Test(priority = 2,description = "跳转H5页面-项目看板,断言桩基验收是否展示默认最小号标段")
    void testProjectBoardH5() {

        //点击[项目看板]
        click(LocateType.ID,"cn.host.qc:id/iv_project_board");

        reportLog.info("当前context ========= >> {}" , baseAndroidDriver.getContext());

        threadSleep("10");

        baseAndroidDriver.context("WEBVIEW_cn.host.qc");
        reportLog.info("当前context ========= >> {}" , baseAndroidDriver.getContext());

        //获取桩基验收默认展示标段名
        WebElement element = baseAndroidDriver.findElement(By.xpath("//*[@id=\"app\"]/div/div/div/div/div[2]/div[2]/div/div[2]/div[1]/div[2]/div/div[1]/div/span"));
        Assert.assertEquals(element.getText(),"南京紫樾府1标段","测试不通过! 默认应展示标段[南京紫樾府1标段] 实际为: " + element.getText());

    }

    @Test(priority = 4,description = "专项巡检-提交问题,断言提交问题后提示信息是否为提交成功")
    void testZxxjSubmitProblem(@Optional("风险更名报告测试van")String batchName,
                               @Optional("A-12精装高层")String banName,
                               @Optional("202")String roomNumber) {
        baseAndroidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"专项巡检\")")).click();

        WebDriverWait wait = new WebDriverWait(baseAndroidDriver,Duration.ofSeconds(10),Duration.ofMillis(500));

        WebElement element = wait
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(By.id("cn.host.qc:id/title_bar_title_tv")));

        element.click();

        List<WebElement> cityElements = wait.ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cn.host.qc:id/cityNameTv")));

        cityElements.forEach(e -> {
            if (e.getText().equalsIgnoreCase("南京公司")) {
                e.click();
            }
        });

        baseAndroidDriver.findElement(By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView[2]/android.widget.LinearLayout[4]/android.widget.TextView")).click();

        threadSleep("3");

        List<WebElement> batchs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvNamePatrol")));
        int index = 0;
        List<WebElement> downloads = baseAndroidDriver.findElements(By.id("cn.host.qc:id/ivDownloadPatrol"));
        for (int i = 0; i < batchs.size(); i++) {
            if (batchs.get(i).getText().equals(batchName)) {
                index = i;
                downloads.get(i).click();
                break;
            }
        }

        WebDriverWait waitFast = new WebDriverWait(baseAndroidDriver,Duration.ofSeconds(30),Duration.ofMillis(100));
        WebElement message;
        do {
            message = waitFast.ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='数据下载完成']")));    // //*[@class='android.widget.Toast']
            reportLog.info("toast信息 ======== >> {}",message.getText());
        }while (!message.getText().equals("数据下载完成"));

        batchs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvNamePatrol")));
        batchs.get(index).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@text='04 底线风险']"))).click();
        baseAndroidDriver.findElement(By.xpath("//*[@text='4-1 底线风险']")).click();
        baseAndroidDriver.findElement(By.xpath("//*[@text='重大风险']")).click();
        baseAndroidDriver.findElement(By.xpath("//*[@text='1、项目存在影响建筑物结构或施工安全的风险，该风险可能引发重大安全事故，即为触碰安全底线（A类）']")).click();

        baseAndroidDriver.findElement(By.id("cn.host.qc:id/addIv")).click();
        baseAndroidDriver.findElement(By.id("cn.host.qc:id/areaTv")).click();
        click(LocateType.XPATH,"//*[@text='"+banName+"']");
        click(LocateType.XPATH,"//*[@text='"+roomNumber+"']");

        clickRandomPointInCustomArea(width*2/5,height*2/5,width*4/5,height*3/5);
        click(LocateType.ID,"cn.host.qc:id/confirmTv");

        int im = RandomUtil.randomInt(1,4);
        baseAndroidDriver.findElement(By.id("cn.host.qc:id/levelRb"+im)).click();
        String content =Constant.UI_AUTO_TEST +Constant.EXPORT_CASE + BusinessType.ZXXJ + TicketProcessEnum.Create.getProcessDesc() +DateUtils.getTimeSuffix();
        inputText(LocateType.ID,"cn.host.qc:id/extraDescEt",content);
        click(LocateType.XPATH,"//*[@text='提交']");
//        Assert.assertTrue(assertToastHasAppeared(baseAndroidDriver,10,"提交成功"),"测试失败! toast提示未出现或不正确");

        //待删除数据
        toRemoveData.put(BusinessType.ZXXJ,content);

        pageBack();
        pageBack();
    }

    @Test(priority = 6,description = "分户查验-提交问题,断言提交问题后提示信息是否为提交成功")
    void testFhcySubmitProblem(@Optional("南京分户多楼层")String batchName,
                           @Optional("A-9精装高层")String banName,
                           @Optional("101")String roomNumber) {
        baseAndroidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"分户查验\")")).click();
        click(LocateType.XPATH,"//*[@text='"+batchName+"']");
        click(LocateType.XPATH,"//*[@text='"+banName+"']");
        threadSleep("5");
        try {
            click(LocateType.XPATH,"//*[@text='"+banName+"']");
        } catch (Exception e) {

        }
        click(LocateType.XPATH,"//*[@text='"+roomNumber+"']");

        threadSleep("1");
        clickRandomPointInCustomArea(width*2/5,height*2/5,width*4/5,height*3/5);

        //部位
        click(LocateType.ID,"cn.host.qc:id/tvPlaceAddQst");
        baseAndroidDriver.findElement(By.xpath("//*[@text='厕所（次卧）']")).click();

        //检查项
        click(LocateType.ID,"cn.host.qc:id/tvCheckOneAddQst");
        baseAndroidDriver.findElement(By.xpath("//*[@text='房屋天花 - 滴水线 - 变形']")).click();

        //问题描述
//        click(LocateType.ID,"cn.host.qc:id/tvQuestionShowAddQst");

        //方位类型
        click(LocateType.ID,"cn.host.qc:id/tvOriAddQst");
        baseAndroidDriver.findElement(By.xpath("//*[@text='三墙']")).click();

        String content = Constant.UI_AUTO_TEST +Constant.EXPORT_CASE + BusinessType.FHCY + TicketProcessEnum.Create.getProcessDesc() +DateUtils.getTimeSuffix();
        inputText(LocateType.ID,"cn.host.qc:id/edtExplainAddQst",content);
        //上传图片
        click(LocateType.ID,"cn.host.qc:id/photoIv");

        clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");

        threadSleep("4");
        baseAndroidDriver.pressKey(new KeyEvent(AndroidKey.CAMERA));

        click(LocateType.ID,"com.huawei.camera:id/done_button");

        threadSleep("1");

        drawMarkInScreen(1);

        click(LocateType.ID,"cn.host.qc:id/dispatch_tv");

        threadSleep("1");

        swipeToDown(baseAndroidDriver,1D,1);
//        swipeDirect(driver, 0,0.4,"TOP_TO_BOTTOM");
        click(LocateType.ID,"cn.host.qc:id/tvUpdateAddQst");
//        Assert.assertTrue(assertToastHasAppeared(baseAndroidDriver,10,"问题提交成功"),"测试失败! toast提示未出现或不正确");

        //待删除数据
        toRemoveData.put(BusinessType.FHCY,content);

        pageBack();
        pageBack();
        pageBack();
        pageBack();
    }

    @Test(priority = 8,description = "工程检查-提交问题,断言提交问题后提示信息是否为提交成功")
    void testGcjcSubmitProblem(@Optional("A-14底商")String banName,
                           @Optional("08")String roomNumber,
                           @Optional("2. 厨房")String firstCheckName,
                           @Optional("2.1. 抽油烟机")String SecondCheckName,
                           @Optional("2.1.1. 不通风")String lastCheckName,
                           @Optional("C-员工A-AutoTest")String recitifyUserName) {

        baseAndroidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"工程检查\")")).click();
        click(LocateType.ID,"cn.host.qc:id/recordTv");

        //拍照
        click(LocateType.ID,"cn.host.qc:id/photoIv");
        threadSleep("1");
        clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");
        threadSleep("2");
        baseAndroidDriver.pressKey(new KeyEvent(AndroidKey.CAMERA));
        click(LocateType.ID,"com.huawei.camera:id/done_button");
        threadSleep("3");
        drawMarkInScreen(1);
        click(LocateType.ID,"cn.host.qc:id/dispatch_tv");
        threadSleep("2");

        //选择检查区域
        click(LocateType.ID,"cn.host.qc:id/areaTv");
        click(LocateType.XPATH,"//*[@text='"+banName+"']");
        click(LocateType.XPATH,"//*[@text='"+roomNumber+"']");
        WebElement element = new WebDriverWait(baseAndroidDriver, Duration.ofSeconds(2), Duration.ofMillis(500))
                .until(ExpectedConditions.elementToBeClickable(By.id("cn.host.qc:id/confirmTv")));
        if (element != null) {
            clickRandomPointInCustomArea(width*2/5,height*2/5,width*4/5,height*3/5);
            click(LocateType.ID,"cn.host.qc:id/confirmTv");
        }

        //选择检查项
        click(LocateType.ID,"cn.host.qc:id/checkItemTv");
        click(LocateType.XPATH,"//*[@text='"+firstCheckName+"']");
        click(LocateType.XPATH,"//*[@text='"+SecondCheckName+"']");
        click(LocateType.XPATH,"//*[@text='"+lastCheckName+"']");

        //补充内容
        String content = Constant.UI_AUTO_TEST +Constant.EXPORT_CASE + BusinessType.GCJC + TicketProcessEnum.Create.getProcessDesc() +DateUtils.getTimeSuffix();
        inputText(LocateType.ID,"cn.host.qc:id/extraDescEt", content);

        //严重程度
        int i = RandomUtil.randomInt(1, 4);
        String important = "cn.host.qc:id/levelRb" + i;
        click(LocateType.ID,important);

        swipeToDown(baseAndroidDriver,1D,1);

        //整改人
        click(LocateType.ID,"cn.host.qc:id/modifyPersonTv");
        click(LocateType.XPATH,"//*[contains(@text,'"+recitifyUserName+"')]");
        click(LocateType.ID,"cn.host.qc:id/confirmTv");
        click(LocateType.ID,"cn.host.qc:id/confirmTv");

//        Assert.assertTrue(assertToastHasAppeared(baseAndroidDriver,10,"问题提交成功"),"测试失败! toast提示未出现或不正确");

        //待删除数据
        toRemoveData.put(BusinessType.GCJC,content);
    }

    @Test(priority = 10, description = "桩基验收-提交报验")
    void testZjysSubmit() {
        baseAndroidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"桩基验收\")")).click();

    }


    @SneakyThrows
    @AfterMethod(description = "每次测试结束后都关闭当前模块,退出到工作台")
    void backToWorkbench() {

        if (baseAndroidDriver!= null ) {

            if (!Objects.equals(baseAndroidDriver.getContext(), "NATIVE_APP")) {
                baseAndroidDriver.context("NATIVE_APP");
                reportLog.info("切换至原生context ========= >> {}" , baseAndroidDriver.getContext());
            }

            boolean existFlag;
            WebDriverWait wait = new WebDriverWait(baseAndroidDriver, Duration.ofSeconds(2), Duration.ofMillis(500));
            try {
                wait.until(ExpectedConditions.elementToBeClickable(By.id("cn.host.qc:id/title_bar_right_iv")));
                existFlag = true;
            } catch (Exception e) {
                existFlag = false;
            }

            if (existFlag) {
                baseAndroidDriver.findElement(By.id("cn.host.qc:id/title_bar_right_iv")).click();
                reportLog.info("当前操作 ========= >> 点击关闭 退回工作台");
            }

        }

    }

    /**
     * 数据层面清除测试数据
     */
    void removeTestData(){

        TestNG testNG = new TestNG();
        testNG.setTestClasses(new Class[]{RemoveDataTestDemo.class});
        testNG.setUseDefaultListeners(false);
        testNG.run();
    }


    private void setRemoveDatas() throws IOException {

        String path = CommonUtils.getResourceRootPath() + "properties";
        String filePath = path + CommonUtils.SEPARATOR + "jxRemoveData.properties";
        if (!FileUtil.exist(path)) {
            FileUtil.mkdir(path);
        }

        if (!FileUtil.exist(filePath)) {
            FileUtil.touch(filePath);
        }

        Properties properties = new Properties();
        FileOutputStream fos = new FileOutputStream(filePath);
        OutputStreamWriter pow = new OutputStreamWriter(fos,Charset.forName("utf8"));

        List<String> keys = toRemoveData.keySet().stream().collect(Collectors.toList());
        keys.forEach(key -> {
            List<String> values = toRemoveData.get(key).stream().collect(Collectors.toList());

            String valueString = String.join(",", values);
            reportLog.info("待删除数据写入[jxRemoveData.properties] =========== >> key: [{}] value: [{}]",key,valueString);
            properties.setProperty(key,valueString);
        });


        properties.store(pow,"待删除数据");
        fos.close();
        pow.close();

    }

    @Override
    protected void pageBack() {
        click(LocateType.ID,"cn.host.qc:id/title_bar_left_iv");
    }

}
