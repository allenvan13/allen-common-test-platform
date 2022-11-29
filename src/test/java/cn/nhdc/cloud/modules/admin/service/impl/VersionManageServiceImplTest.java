package com.allen.testplatform.modules.admin.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class VersionManageServiceImplTest {

    @Resource
    VersionManageServiceImpl versionManageService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @DisplayName("安卓安装目标版本apk脚本")
    @Test
    void testInstallApp(){
        String versionCode = "V1.26.0";
        String versionType = "DEBUG";
        String teamCode = "JX";
        String terminalCode = "JX_APP_ANDROID";
        String phoneSerialNumber = "HJS5T19718010374";
        versionManageService.installAndroidApk(versionCode,versionType,teamCode,terminalCode,phoneSerialNumber);
    }
}