package com.allen.testplatform.testscripts.testcase.demo;

import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import com.allen.testplatform.testscripts.testcase.base.AndroidTestBase;
import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.time.Duration;

/**
 *
 * @author Fan QingChuan
 * @since 2022/7/3 23:11
 */

@Slf4j
@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class JxAppLoginTestDemo extends AndroidTestBase {

    @BeforeTest
    void setUp() throws MalformedURLException {
        startCustomBaseService(4756,"127.0.0.1",null);
        DesiredCapabilities capabilities = AppiumCapabilities.getHarmonyJx();
        baseAndroidDriver = new AndroidDriver(getServiceUrl(),capabilities);
        baseAndroidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterTest
    void tearDown() {
    }

    @Test(dataProvider = "loginUsersProvider")
    void test001(String username,String password) {
        loginToWorkBanch(username,password);
        logout();
    }


    private void loginToWorkBanch(String username,String password){
        clickNegatively(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/tv_privacy_agree");
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etAccount",username);
        inputText(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/etPassword",password);
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/protocolCb");
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/btnLogin");
        threadSleep("3");
    }

    private void logout() {
        //点击我的
        click(baseAndroidDriver, LocateType.ID,"cn.host.qc:id/tab_profile");
        //点击退出登录
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/logoutTv");
        //点击确认
        click(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/confirm_tv");
    }

    @DataProvider(name = "loginUsersProvider")
    public Object[][] UserInfoProvider() {
        return new Object[][] {
                {"ATE001","a123456"},
                {"ATE007","a123456"},
                {"ATE006","a123456"},
                {"ATE005","a123456"},
        };
    }

}
