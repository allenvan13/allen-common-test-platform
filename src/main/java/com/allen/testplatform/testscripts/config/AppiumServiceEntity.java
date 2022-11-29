package com.allen.testplatform.testscripts.config;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.Data;

/**
 * @author Fan QingChuan
 * @since 2022/7/7 21:15
 */

@Data
public class AppiumServiceEntity {
    private String url;
    private AppiumDriverLocalService service;
}
