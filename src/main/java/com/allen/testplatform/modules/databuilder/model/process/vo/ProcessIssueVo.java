package com.allen.testplatform.modules.databuilder.model.process.vo;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ProcessIssueVo {

    /**
     * 验收明细id
     */
    @NotNull(message = "验收明细id不能为空")
    private Long detailId;
    /**
     * 流程id
     */
    @NotNull(message = "流程id不能为空")
    private Long flowId;
    /**
     * 标段id
     */
    @NotNull(message = "标段id不能为空")
    private Long sectionId;
    /**
     * 检查项id
     */
    @NotNull(message = "检查项id不能为空")
    private Long checkId;
    /**
     * 检查项名称
     */
    private String checkName;
    /**
     * 检查项路径名称
     */
    private String checkPathName;
    /**
     * 检验区域名称
     */
    @NotBlank(message = "检验区域名称不能为空")
    private String partName;
    /**
     * 楼栋编码
     */
    @NotBlank(message = "楼栋编码不能为空")
    private String banCode;
    /**
     * 楼栋名称
     */
    private String banName;
    /**
     * 单元
     */
    private String unit;
    /**
     * 楼层
     */
    private String floor;
    /**
     * 房间编码
     */
    private String roomCode;
    /**
     * 房间名称
     */
    private String roomName;
    /**
     * 户型图id
     */
    private Long houseTypeId;
    /**
     * 问题位置X轴
     */
    private Double pointX;
    /**
     * 问题位置Y轴
     */
    private Double pointY;
    /**
     * 户型图
     */
    private String checkImageUrl;
    /**
     * 问题图片
     */
    private List<String> picture;
    /**
     * 严重程度(1:一般,2:重大,3:紧急)
     */
    @NotNull(message = "严重程度不能为空")
    private Integer severity;
    /**
     * 整改时长
     */
    @NotNull(message = "整改期限不能为空")
    private Integer deadlineDay;
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
    @NotNull(message = "整改人用户id不能为空")
    private Long rectifyUserId;
    /**
     * 整改人用户姓名
     */
    @NotBlank(message = "整改人用户姓名不能为空")
    private String rectifyRealName;
    /**
     * 整改人供应商公司guid
     */
    private String rectifyCompanyGuid;
    /**
     * 整改人供应商公司名称
     */
    private String rectifyCompanyName;
    /**
     * 复验人用户id
     */
    @NotNull(message = "复验人用户id不能为空")
    private Long reviewUserId;
    /**
     * 复验人用户姓名
     */
    @NotBlank(message = "复验人用户姓名不能为空")
    private String reviewRealName;
}
