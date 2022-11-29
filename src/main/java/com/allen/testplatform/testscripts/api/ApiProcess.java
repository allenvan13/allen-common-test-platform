package com.allen.testplatform.testscripts.api;

/**
 * 工序验收接口
 */
public class ApiProcess {
    //报验、验收、抽检-提交
    public static final String APP_PROCESS_SUBMIT = "/qc/process/mobile/acceptance/submit";
    //报验详情
    public static final String APP_PROCESS_DETAIL = "/qc/process/mobile/acceptance/detail";
    //匠星后台-验收列表-删除验收
    public static final String PC_DETAIL_DELETE = "/qc/process/detail/delete";
    //新增问题
    public static final String APP_ISSUE_SAVE = "/qc/process/mobile/issue/save";
    //完成整改问题 重新整改 二次派单 正常关闭 非正常关闭
    public static final String APP_ISSUE_RECTIFY = "/qc/process/mobile/issue/rectify";
    //转派问题
    public static final String APP_ISSUE_TRANSFER = "/qc/process/mobile/acceptance/transfer";
    //退回问题
    public static final String APP_ISSUE_BACK = "/qc/process/mobile/acceptance/back";

}
