package com.allen.testplatform.common.constant;

/**
 * Constant
 * 全局静态常量
 * @author FanQingChuan
 * @since 2021/11/15 9:33
 */
public class HostCommon {

    //GateWay
    public static final String FAT = "http://192.168.1.77:7100";
    public static final String UAT = "http://uat.api.host.cn";
    public static final String PRO = "http://api2.host.cn";

    public static final String UAT_SSL = "https://uat.api.host.cn";

    //OpenGateWay
    public static final String FAT_OPEN = "http://192.168.1.77:7001";
    public static final String UAT_OPEN = "http://uat.open.api.host.cn";
    public static final String PRO_OPEN = "https://app.host.cn";

    //认证模式获取token-前置表单接口
    public static final String FAT_AUTH_FORM = "https://uat-api.host.cn/auth/authentication/form";
    public static final String UAT_AUTH_FORM = "https://uat-api.host.cn/auth/authentication/form";
    public static final String PRO_AUTH_FORM = "https://api2.host.cn/auth/authentication/form";
    //认证模式获取token-匠星后台重定向地址
//    public static final String UAT_REDIRECT_URL = "https%3A%2F%2Fuat-api.host.cn%2Fauth%2Foauth%2Fauthorize%3Fclient_id%3Dupms%26response_type%3Dcode%26redirect_uri%3Dhttp%3A%2F%2Fuat-jxadmin.host.cn%2F%23%2Fcooperation-manage%2Fearly-warning-index";
    public static final String FAT_REDIRECT_URL = "https%3A%2F%2Fuat-api.host.cn%2Fauth%2Foauth%2Fauthorize%3Fclient_id%3Dupms%26response_type%3Dcode%26redirect_uri%3Dhttp%3A%2F%2Fuat-jxadmin.host.cn%2F%23%2Fcooperation-manage%2Fhome";
    public static final String UAT_REDIRECT_URL = "https%3A%2F%2Fuat-api.host.cn%2Fauth%2Foauth%2Fauthorize%3Fclient_id%3Dupms%26response_type%3Dcode%26redirect_uri%3Dhttp%3A%2F%2Fuat-jxadmin.host.cn%2F%23%2Fcooperation-manage%2Fhome";
    public static final String PRO_REDIRECT_URL = "https%3A%2F%2Fapi2.host.cn%2Fauth%2Foauth%2Fauthorize%3Fclient_id%3Dupms%26response_type%3Dcode%26redirect_uri%3Dhttp%253A%252F%252Fjxadmin.host.cn%252F%2523%252Fcooperation-manage%252Fhome%253Fcode%253DCi3Dwv";

    public static final String AUTH_TOKEN = "/auth/oauth/token";
    public static final String APP_AUTH_LOGOUT = "/app/auth/logout";
    public static final String PC_AUTH_LOGOUT = "/auth/oauth/logout";
}

