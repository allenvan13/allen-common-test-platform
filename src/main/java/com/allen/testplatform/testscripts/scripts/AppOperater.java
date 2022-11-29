package com.allen.testplatform.testscripts.scripts;

import com.allen.testplatform.TestPlatformApplication;
import com.allen.testplatform.modules.admin.service.impl.VersionManageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.annotation.Resource;

@SpringBootTest(classes = TestPlatformApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppOperater extends AbstractTestNGSpringContextTests {

    @Resource
    VersionManageServiceImpl versionManageService;

    @DisplayName("安卓端-安装目标版本APP -apk ========== >> 快速安装、版本覆盖升级等测试")
    @Test
    void testInstallApp(){
        String versionCode = "V2.0.0";
        String versionType = "PROD";//PROD  DEBUG
        String teamCode = "JX";
        String terminalCode = "JX_APP_ANDROID";
        String phoneSerialNumber = "HJS5T19718010374";  //PQY0221B17001204   HJS5T19718010374
        versionManageService.installAndroidApk(versionCode,versionType,teamCode,terminalCode,phoneSerialNumber);
    }

}
