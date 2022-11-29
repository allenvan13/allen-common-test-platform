package com.allen.testplatform.testscripts.api;

/**
 * @author Fan QingChuan
 * @since 2022/9/20 15:58
 */

public class ApiCjcy {

    //录入问题
    public static final String APP_ADD_PROBLEM = "/qc/mobileOrder/addCJProblem";
    //作废问题
    public static final String APP_ADD_CANCEL = "/qc/mobileOrder/cancellation";
    //撤回问题
    public static final String APP_ADD_WITHDRAW = "/qc/mobileOrder/withdraw";
    //完成整改
    public static final String APP_ADD_COMPLETERECT = "/qc/mobileOrder/completeRect";
    //复验不通过问题
    public static final String APP_ADD_REVIEWNOPASS = "/qc/mobileOrder/handBack";
    //复验通过问题
    public static final String APP_ADD_REVIEWPASS = "/qc/mobileOrder/recheckPass";
    //整改人退回
    public static final String APP_ADD_RETURN = "/qc/mobileOrder/rectHandBack";
    //指派团队成员
    public static final String APP_ADD_ASSIGN = "/qc/mobileOrder/assignTeamUser";
    //查验完成
    public static final String APP_ADD_CHECKFINISH = "/qc/mobileOrder/checkFinish";
    //抽检不通过
    public static final String APP_ADD_CHECKFAIL = "/qc/mobileOrder/orderCheckFail";

    //问题详情
    public static final String APP_ORDER_DETAILS = "/qc/cjcyOrder/details";

}
