package com.allen.testplatform.testscripts.testcase.demo;

import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/7/7 9:42
 */

public class FhcyLocalTicketTest extends AndroidTestBase {

    private static final ReportLog reportLog = new ReportLog(FhcyLocalTicketTest.class);

    @BeforeTest
    void setUp() {
        startCustomBaseService(4756,"127.0.0.1",null);
        DesiredCapabilities capabilities = AppiumCapabilities.getHarmonyJx();
        baseAndroidDriver = new AndroidDriver(getServiceUrl(),capabilities);
        baseAndroidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterTest
    void tearDown() {

    }

    @Test
    void test001(@Optional("ATE002") String username,@Optional("a123456") String password,
                 @Optional("南京分户多楼层")String batchName) {
        loginToWorkBanch(username,password);
        testChooseStage();
        testChooseBatch(batchName);

        try {
            for (int i = 0; i < 50; i++) {
                if (i == 0) {
                    submitProblem(true);
                }else {
                    submitProblem(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loginToWorkBanch(String username,String password){
        clickNegatively(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/tv_privacy_agree");
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etAccount",username);
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etPassword",password);
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/protocolCb");
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/btnLogin");
        threadSleep("3");
    }

    private void testChooseStage() {
        //点击[选择分期]
        click(LocateType.ID,"cn.host.qc:id/projectTv");
        //选择[南京公司]
        clickInElementsByText(LocateType.ID,"cn.host.qc:id/cityNameTv","南京公司");
        //选择[1]分期
        click(LocateType.XPATH,"/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView[2]/android.widget.LinearLayout[4]/android.widget.TextView");
    }

    private void testChooseBatch(String batchName) {
        baseAndroidDriver.findElement(new AppiumBy.ByAndroidUIAutomator("new UiSelector().text(\"分户查验\")")).click();
        click(LocateType.XPATH,"//*[@text='"+batchName+"']");
        threadSleep("20");
    }

    private void submitProblem(boolean photoPermiss) {
        WebDriverWait wait = new WebDriverWait(baseAndroidDriver, Duration.ofSeconds(5), Duration.ofMillis(300));

        List<String> banList = Arrays.asList("B-11底商", "A-12精装高层", "A-3精装高层", "A-9精装高层", "A-4精装高层", "A-5精装高层", "A-14底商", "A-10精装高层");
        String banName = banList.get(RandomUtil.randomInt(banList.size()));
        click(LocateType.XPATH,"//*[@text='"+banName+"']");
        reportLog.info("banName ======== >> [{}]",banName);

        threadSleep("1");

        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/title_bar_title_tv")));
        reportLog.info("title ======== >> [{}]",title.getText());

        List<WebElement> roomList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvRoomShow")));
        int index = RandomUtil.randomInt(roomList.size());
        WebElement room = roomList.get(index);
        String roomName = room.getText();
        room.click();
        reportLog.info("roomName ======== >> [{}]",roomName);

        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        do {
            threadSleep("1");
            clickRandomPointInCustomArea(width*2/5,height*2/5,width*4/5,height*3/5);
        }while (isToastHasAppeared(1, "该问题暂未同步，请同步后查看"));

        WebElement tt = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(LocateType.APPIUM_ID,"cn.host.qc:id/title_bar_title_tv")));

        if (!tt.getText().equals("新增问题")) {
            pageBack();
            pageBack();
            pageBack();
        }else {
            //部位
            click(LocateType.ID,"cn.host.qc:id/tvPlaceAddQst");
            List<WebElement> projectSiteList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvShowPlace")));
            index = RandomUtil.randomInt(projectSiteList.size());
            projectSiteList.get(index).click();
//        baseAndroidDriver.findElement(By.xpath("//*[@text='厕所（次卧）']")).click();


            //检查项
            click(LocateType.ID,"cn.host.qc:id/tvCheckOneAddQst");
            baseAndroidDriver.findElement(By.xpath("//*[@text='房屋天花 - 楼板 - 楼板贯穿性裂缝']")).click();

            //方位类型
            click(LocateType.ID,"cn.host.qc:id/tvOriAddQst");
            baseAndroidDriver.findElement(By.xpath("//*[@text='三墙']")).click();

            String content = Constant.UI_AUTO_TEST +Constant.EXPORT_CASE + BusinessType.FHCY + TicketProcessEnum.Create.getProcessDesc() + DateUtils.getTimeSuffix();
            inputText(LocateType.ID,"cn.host.qc:id/edtExplainAddQst",content);
            //上传图片
            click(LocateType.ID,"cn.host.qc:id/photoIv");

            if (photoPermiss) {
                clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");
            }

            do {
                threadSleep("3");
                baseAndroidDriver.pressKey(new KeyEvent(AndroidKey.CAMERA));
            } while (!wait.until(ExpectedConditions.presenceOfElementLocated(By.id("com.huawei.camera:id/done_button"))).isDisplayed());

            click(LocateType.ID,"com.huawei.camera:id/done_button");
            threadSleep("1");

            drawMarkInScreen(RandomUtil.randomInt(1,4));
            click(LocateType.ID,"cn.host.qc:id/dispatch_tv");
            threadSleep("1");

            swipeToDown(baseAndroidDriver,1D,1);
            //提交
            click(LocateType.ID,"cn.host.qc:id/tvUpdateAddQst");
            threadSleep("1");
            reportLog.info("toast 提示是否出现 ======== >> [{}]",isToastHasAppeared(3,"问题已保存"));
            pageBack();
            pageBack();
        }


    }
}
