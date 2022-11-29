package com.allen.testplatform.modules.databuilder.model.process.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProcessDetailIssueRectifyVo {
    /**
     * 问题id
     */
    @NotNull(message = "问题id不能为空")
    private Long id;
    /**
     * 进度状态(0:创建问题,1:二次派单,2:重新整改,3:完成整改,4:非正常关闭,5:正常关闭)
     */
    @NotNull(message = "进度状态不能为空")
    private Integer status;
    /**
     * 问题图片
     */
    private List<String> picture;
    /**
     * 补充说明
     */
    private String remark;
    /**
     * 提交时间
     */
    @NotNull(message = "提交时间不能为空")
    private String submitTime;
    /**
     * 提交人用户id
     */
    @NotNull(message = "提交人用户id不能为空")
    private Long userId;
    /**
     * 提交人用户姓名
     */
    @NotBlank(message = "提交人用户姓名不能为空")
    private String realName;
    /**
     * 提交人供应商公司guid
     */
    private String companyGuid;
    /**
     * 提交人供应商公司名称
     */
    private String companyName;
    /**
     * 整改人用户id
     */
    private Long rectifyUserId;
    /**
     * 整改人用户姓名
     */
    private String rectifyRealName;
    /**
     * 整改人供应商公司guid
     */
    private String rectifyCompanyGuid;
    /**
     * 整改人供应商公司名称
     */
    private String rectifyCompanyName;
}
