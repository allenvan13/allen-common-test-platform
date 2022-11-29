package com.allen.testplatform.testscripts.page.jx;

import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.page.base.AndroidBasePage;
import io.appium.java_client.android.AndroidDriver;

public class AndroidHomePage extends AndroidBasePage implements HomePage {

    public AndroidHomePage(AndroidDriver driver) {
        super(driver);
    }

    public AndroidHomePage(AndroidDriver driver, String title) {
        super(driver, title);
    }

    @Override
    public void logout() {
        //点击我的
        click(driver, LocateType.ID,"cn.host.qc:id/tab_profile");
        //点击退出登录
        click(driver,LocateType.ID,"cn.host.qc:id/logoutTv");
        //点击确认
        click(driver,LocateType.ID,"cn.host.qc:id/confirm_tv");
    }
}
