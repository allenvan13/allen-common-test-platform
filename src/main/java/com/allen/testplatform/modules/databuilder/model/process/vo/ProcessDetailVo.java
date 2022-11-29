package com.allen.testplatform.modules.databuilder.model.process.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/2/22 10:30
 */
@Data
public class ProcessDetailVo {

    /**
     * 验收详情id
     */
    private Long detailId;

    /**
     * 验收流程id
     */
    @NotNull(message = "验收流程id不能为空")
    private Long flowId;

    /**
     * 标段id
     */
    @NotNull(message = "标段id不能为空")
    private Long sectionId;

    /**
     * 城市公司
     */
    private String orgName;

    /**
     * 城市公司编号
     */
    @NotBlank(message = "城市公司编码不能为空")
    private String orgCode;

    /**
     * 项目code
     */
    @NotBlank(message = "项目编码不能为空")
    private String projectCode;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 分期编码
     */
    @NotBlank(message = "分期编码不能为空")
    private String stageCode;

    /**
     * 分期名称
     */
    private String stageName;

    /**
     * 楼栋编码
     */
    private String banCode;

    /**
     * 楼栋名称
     */
    private String banName;

    /**
     * 检查项id
     */
    @NotNull(message = "检查项id不能为空")
    private Long checkId;

    /**
     * 检查项名称
     */
    @NotBlank(message = "检查项名称不能为空")
    private String checkName;

    /**
     * 检查项路径名称
     */
    @NotBlank(message = "检查项路径名称不能为空")
    private String checkPathName;

    /**
     * 检验批编码
     */
    @NotBlank(message = "检验批编码不能为空")
    private String partCode;

    /**
     * 检验批名称
     */
    @NotBlank(message = "检验批名称不能为空")
    private String partName;

    /**
     * 提交人用户id
     */
    private Long userId;

    /**
     * 提交人用户名
     */
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
     * 提交时间
     */
    @NotNull(message = "提交时间不能为空")
    private String submitTime;

    /**
     * 补充说明
     */
    private String comment;

    /**
     * 验收点列表
     */
    @NotEmpty(message = "验收点不能为空")
    private List<PointDto> detailsPoint;

    /**
     * 验收人
     */
    private List<PersonnelDto> acceptor;

    /**
     * 共同验收人
     */
    private List<PersonnelDto> commonAcceptor;

    /**
     * 抄送人
     */
    private List<PersonnelDto> carbonCopy;

    /**
     * 验收点
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PointDto {

        /**
         * 验收点id
         */
        private Long pointId;

        /**
         * 验收点名称
         */
        private String title;

        /**
         * 图片地址
         */
        private List<String> picture;

        /**
         * 验收标准
         */
        private String remark;
    }

    /**
     * 验收人
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PersonnelDto {

        /**
         * 下一节点流程id
         */
        private Long nextFlowId;
        /**
         * 用户id
         */
        private Long userId;
        /**
         * 用户姓名
         */
        private String realName;
        /**
         * 供应商公司guid
         */
        private String companyGuid;
        /**
         * 供应商公司名称
         */
        private String companyName;

        // 是否指定下一节点人   //测试需要添加的字段 后端接口不接收处理
        private Boolean ifAppoint;
    }
}
