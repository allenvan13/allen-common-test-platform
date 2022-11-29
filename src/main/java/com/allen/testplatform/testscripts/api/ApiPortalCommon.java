package com.allen.testplatform.testscripts.api;

/**
 * PortalCommon
 * 供应商门户API
 * @author FanQingChuan
 * @since 2021/11/15 9:31
 */
public class ApiPortalCommon {

    //门户登录接口-密码登录
    public static final String AUTH_TOKEN = "/pdp/manage/auth/token";

    //门户登录接口-验证码登录
    public static final String PC_AUTH_SMS = "/pdp/manage/auth/sms";

    //门户发送验证码接口
    public static final String PC_AUTH_SEND = "/pdp/manage/auth/send";

    //门户 待办/已办列表接口
    public static final String PC_TODO_PAGE = "/qc/ticket/getBacklogData";

    //门户 待办/已办 条数总计接口
    public static final String PC_TODO_TOTAL = "/qc/ticket/getTotalPC";
}
