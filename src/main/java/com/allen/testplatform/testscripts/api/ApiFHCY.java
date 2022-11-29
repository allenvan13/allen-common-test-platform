package com.allen.testplatform.testscripts.api;

/**
 * AppFhcyApi
 * 分户查验App端相关接口
 * @author FanQingChuan
 * @since 2021/10/28 10:59
 */
public class ApiFHCY {

    //批次列表
    public static final String APP_BATCH_LIST = "/qc/cjcy/ticket/getBatchList";
    //新增问题
    public static final String APP_ADD_PROBLEM = "/qc/fhcy/ticket/addHouseProblemV2";
    //问题列表
    public static final String APP_PROBLEM_LIST = "/qc/fhcy/ticket/getRectifiedProblemList";
    //整改问题
    public static final String APP_RECTIFY_PROBLEM = "/qc/fhcy/ticket/rectifyComplete";
    //核销问题
    public static final String APP_REVIEW_PROBLEM = "/qc/fhcy/ticket/review";
    //退回问题
    public static final String APP_RETURN_PROBLEM = "/qc/fhcy/ticket/return";
    //重新编辑问题 提交
    public static final String APP_UPDATE_PROBLEM = "/qc/fhcy/ticket/updateProblem";
    //问题责任人指派团队成员
    public static final String APP_ASSIGN_TEAMUSER = "/qc/fhcy/ticket/assignTeamUser";


    /*
     PROCESSING("50", "待整改"),
     PASSED("51", "已通过"),
     CANCELLATION("52", "已作废"),
     //2021-03-23 处理中修改为待整改，新增已整改类型
     COMPLATE("53", "已整改"),
     APPOINT("5400", "待指派"),
     REJECT("5600","驳回"),
     */


}
