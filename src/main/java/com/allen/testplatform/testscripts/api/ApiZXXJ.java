package com.allen.testplatform.testscripts.api;

/**
 * @author FanQingChuan
 * @since 2021/11/20 1:36
 */
public class ApiZXXJ {
    //新增问题
    public static final String APP_ADD_PROBLEM = "/qc/specialInspection/addProblem";
    //整改问题
    public static final String APP_DO_PROBLEM = "/qc/specialInspection/doProblem";
    //复验问题-正常关闭
    public static final String APP_NORMAL_CLOSE = "/qc/specialInspection/normalShutdown";
    //复验问题-非正常关闭
    public static final String APP_UNNORMAL_CLOSE = "/qc/specialInspection/unNormalShutdown";
    //提交打分
    public static final String APP_SUBMIT_SCORE = "/qc/specialInspection/submit";

}
