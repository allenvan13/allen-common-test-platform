package com.allen.testplatform.testscripts.api;

/**
 * 项目进度汇报
 * @author Fan QingChuan
 * @since 2022/5/27 17:44
 */
public class ApiProcessReport {
    //新增
    public static final String PC_SAVE = "/pb/project/process/save";
    //编辑
    public static final String PC_UPDATE = "/pb/project/process/update";
    //列表查询
    public static final String PC_LIST_PAGE = "/pb/project/process/page";
    //删除
    public static final String PC_DELETE_BY_ID = "/pb/project/process/delete/{id}";
    //详情
    public static final String PC_DETAIL_BY_ID = "/pb/project/process/detail/{id}";
}
