package com.allen.testplatform.testscripts.api;

/**
 * AppCommon
 * App端通用接口
 * @author FanQingChuan
 * @since 2021/11/9 18:26
 */
public class ApiCommon {

    //APP登录接口-密码登录
    public static final String API_APP_TOKEN = "/pdp/manage/auth/token/app";

    //APP登录接口-验证码登录
    public static final String API_APP_SMS_TOKEN = "/pdp/manage/auth/sms/app";

    //找回密码-发送验证码接口

    //发送验证码接口
    public static final String API_APP_SMS_SEND = "/pdp/manage/auth/send";

    //找回密码接口

}
