package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

/**
 * 查验批次(CheckBatch)实体类
 *
 * @author wanppenggui
 * @since 2020-07-24 09:57:30
 */
@Data
public class CheckBatch {

    private Long id;
    /**  table:check_batch*/
    /**
     * 分户查验：XX.XXXXXXX.FHCY 承接查验：XX.XXXXXXX.CJCY 工程检查：XX.XXXXXXX.GCJC
     */
    private String category;
    /**  table:check_batch*/
    /**
     * 组织编号
     */
    private String orgCode;
    /**  table:check_batch*/
    /**
     * 组织名字
     */
    private String orgName;
    /**  table:check_batch*/
    /**
     * 项目编号
     */
    private String projectCode;
    /**  table:check_batch*/
    /**
     * 项目名称
     */
    private String projectName;
    /**  table:check_batch*/
    /**
     * 分期编号
     */
    private String stageCode;
    /**  table:check_batch*/
    /**
     * 分期名称
     */
    private String stageName;
    /**  table:check_batch*/
    /**
     * 名称
     */
    private String name;
    /**  table:check_batch*/
    /**
     * 检查单ID
     */
    private Long checkListId;
    /**  table:check_batch*/
    /**
     * 启用/停用
     */
    private Boolean enable;
    /**  table:check_batch*/
    /**
     * 描述
     */
    private String remark;
    /**  table:check_batch*/
    /**
     * 创建操作者姓名
     */
    private String createUserName;
    /**  table:check_batch*/
    /**
     * 更新操作者姓名
     */
    private String updateUserName;

}