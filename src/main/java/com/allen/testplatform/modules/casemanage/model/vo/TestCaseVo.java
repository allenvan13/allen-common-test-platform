package com.allen.testplatform.modules.casemanage.model.vo;

import com.allen.testplatform.modules.casemanage.service.Add;
import com.allen.testplatform.modules.casemanage.service.Update;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TestCaseVo {

    @NotNull(message = "caseId(用例ID)不能为空", groups = {Update.class})
    private Long caseId;

    /**
     * 测试集ID
     */
    private Long suiteId;

    /**
     * 团队code JX-匠星 XWH-希望云 DWWB-地网无边 JSPT-技术平台 DSJPT-大数据平台 ALL-所有团队
     */
    @NotBlank(message = "teamCode 团队code不能为空", groups = {Add.class, Update.class})
    private String teamCode;

    /**
     * 测试用例编码
     */
    @NotBlank(message = "caseCode 用例编号不能为空", groups = {Add.class, Update.class})
    private String caseCode;

    /**
     * 测试用例类型  1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试
     */
    @NotNull(message = "type 测试用例类型不能为空", groups = {Add.class, Update.class})
    private Integer type;

    /**
     * 用例顺序号
     */
    private Integer sort;

    /**
     * 用例描述
     */
    private String description;

    /**
     * 测试人员姓名
     */
    @NotBlank(message = "tester 测试人员姓名不能为空", groups = {Add.class, Update.class})
    private String tester;

    /**
     * 测试步骤List
     */
    @Valid
    private List<UiCaseStepVo> caseSteps;

    @Data
    public static class UiCaseStepVo{

        @NotNull(message = "caseId 测试用例ID不能为空",groups = {Update.class})
        private Long caseId;

        @NotBlank(message = "caseCode 测试用例编码不能为空",groups = {Update.class})
        private String caseCode;

        @NotNull(message = "stepId 步骤ID不能为空",groups = {Update.class})
        private Long stepId;

        /**
         * 步骤顺序号
         */
        @NotNull(message = "sort 步骤序号不能为空",groups = {Add.class, Update.class})
        private Integer sort;

        /**
         * 步骤描述
         */
        @ExcelProperty("测试步骤描述")
        private String description;

        /**
         * 关键字(操作)编码
         */
        @NotBlank(message = "actionKeyword 关键字(操作)编码不能为空",groups = {Add.class, Update.class})
        private String actionKeyword;

        /**
         * 元素定位方式(类型)
         */
        private String elementLocateType;

        /**
         * 元素定位信息
         */
        private String elementLocateValue;

        /**
         * 输入值
         */
        private String parameter;
    }
}
