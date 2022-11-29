package com.allen.testplatform.modules.casemanage.model.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 测试平台-UI测试用例步骤明细
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("ui_test_case_step")
public class UiTestCaseStep extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 测试用例ID
     */

    private Long caseId;

    /**
     * 测试用例编码
     */
    private String caseCode;

    /**
     * 步骤顺序号
     */
    private Integer sort;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 关键字(操作)编码
     */
    private String actionKeyword;

    /**
     * 元素定位 定位方式(类型)
     */
    private String elementLocateType;

    /**
     * 元素定位信息
     */
    private String elementLocateValue;

    /**
     * 输入(操作)值
     */
    private String parameter;


}
