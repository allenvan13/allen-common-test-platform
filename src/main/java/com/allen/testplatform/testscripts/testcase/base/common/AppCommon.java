package com.allen.testplatform.testscripts.testcase.base.common;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.testscripts.config.AppiumServiceEntity;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.AppTestBase;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.appium.java_client.service.local.flags.ServerArgument;

import java.io.File;
import java.util.Map;

public interface AppCommon extends WebCommon{

    int APP_COMMON_TIME_OUT = AppTestBase.TIME_OUT;
    int APP_COMMON_SLEEP_TIME = AppTestBase.SLEEP_TIME;
    int APP_COMMON_DEFAULT_PORT = AppTestBase.DEFAULT_PORT;
    String APP_COMMON_DEFAULT_IP = AppTestBase.DEFAULT_IP;
    ReportLog reportLog = new ReportLog(AppCommon.class);

    default void checkPortAndKillTask(int port) {
        if (CommonUtils.isWindows()) {
            if (SocketUtils.isPortBeUsed(port)) {
                String rs = CommonUtils.stopWindowsAppiumService(port);
                reportLog.info(" ========== >> 端口[{}]被占用 taskkill {} ", port, rs);
            }
        }else {
            if (SocketUtils.isPortBeUsed(port)) {
                String rs = CommonUtils.stopLinuxAppiumService(port);
                reportLog.info(" ========== >> 端口[{}]被占用 taskkill {} ", port, rs);
            }
        }
    }

    default AppiumDriverLocalService buildCustomAppiumService(int port, String ipAddress, Map<ServerArgument,String> arguments) {
        AppiumServiceBuilder builder = getAppiumServiceBuilder(port, ipAddress, arguments);
        return AppiumDriverLocalService.buildService(builder).withBasePath("/wd/hub/");
    }

    default AppiumDriverLocalService buildDefaultAppiumService() {
        return AppiumDriverLocalService.buildDefaultService().withBasePath("/wd/hub/");
    }

    default AppiumServiceBuilder getAppiumServiceBuilder(int port, String ipAddress, Map<ServerArgument,String> arguments) {
        if (!MathUtils.rangeInDefined(port,4700,5000)) {
            throw new RuntimeException("端口限定范围 4700-5000");
        }
        if (!IpUtils.internalIp(ipAddress)) {
            throw new RuntimeException("IP地址格式不合法!");
        }

        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.withIPAddress(ipAddress);
        builder.usingPort(port);

        //默认设置 session覆盖 日志级别info 日志位置 调用者可覆盖
        builder.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
        builder.withArgument(GeneralServerFlag.LOG_LEVEL,"info");
        builder.withLogFile(new File(CommonUtils.getOutPutRootPath() + "nhdc-cloud-test-platform"
                + CommonUtils.SEPARATOR + "appium-log" + CommonUtils.SEPARATOR + DateUtils.getTimeSuffix() + "_info.log"));

        if (CollectionUtils.isNotEmpty(arguments)) {
            arguments.forEach((k,v) -> {
                if (ObjectUtil.isEmpty(v)) {
                    builder.withArgument(k);
                }else {
                    String argName = k.getArgument();
                    switch (argName) {
                        case "--port":
                        case "-p":
                            builder.usingPort(Integer.parseInt(v));
                            break;
                        case "--address":
                        case "-a":
                            builder.withIPAddress(v);
                            break;
                        case "--log":
                        case "-g":
                            builder.withLogFile(new File(v));
                            break;
                        default:
                            builder.withArgument(k,v);
                            break;
                    }
                }
            });
        }

        return builder;
    }

    default void closeAppiumService(AppiumDriverLocalService service) {
        try {
            if (service.isRunning()) {
                service.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            reportLog.info(" ========== >> Appium服务是否正常关闭 [{}]",!service.isRunning());
        }
    }

    default AppiumServiceEntity initOrConnectAppiumService(String initInfos) {
        if (ObjectUtil.isEmpty(initInfos)) {
            throw new IllegalArgumentException("initInfos 配置信息不能为空!");
        }

        if (CommonUtils.isJSONString(initInfos)) {
            throw new IllegalArgumentException("initInfos配置信息JSON 格式不合法! 请检查!");
        }

        JSONObject initJson = JSON.parseObject(initInfos);
        if (!initJson.containsKey("isStartDefaultService")) {
            throw new IllegalArgumentException("initInfos 中未配置 isStartDefaultService  true/false");
        }

        Boolean isStartDefaultService = initJson.getBoolean("isStartDefaultService");

        if (ObjectUtil.isEmpty(isStartDefaultService)) {
            throw new IllegalArgumentException("initInfos 中未正确配置 isStartDefaultService  true/false");
        }

        reportLog.info(" ========== >> isStartDefaultService [{}]",isStartDefaultService);

        AppiumServiceEntity serviceEntity = new AppiumServiceEntity();
        if (isStartDefaultService) {
            AppiumServiceBuilder builder = getAppiumServiceBuilder(APP_COMMON_DEFAULT_PORT, APP_COMMON_DEFAULT_IP, null);

            AppiumDriverLocalService service = AppiumDriverLocalService.buildService(builder).withBasePath("/wd/hub/");

            reportLog.info(" ========== >> 构建AppiumDriverLocalService 等待启动服务 [{}]",service.getUrl());
            serviceEntity.setService(service);
        }else {
            int port = initJson.getIntValue("port");
            String ipAddress = initJson.getString("ipAddress");
            String basePath = initJson.getString("basePath");
            String url = "http://" + ipAddress + ":" + port + basePath;
            serviceEntity.setUrl(url);

            reportLog.info(" ========== >> 用户指定Appium服务地址 [{}]",url);
        }

        return serviceEntity;
    }


}
