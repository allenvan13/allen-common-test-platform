package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 工序验收明细表(ProcessDetail)实体类
 *
 * @author makejava
 * @since 2021-08-10 16:46:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail")
public class ProcessDetail extends BaseEntity<ProcessDetail> {

    private static final long serialVersionUID = 159602025844848701L;

    /**
     * 验收编号
     */
    @TableField("code")
    private String code;

    /**
     * 检验区域编码
     */
    @TableField("part_code")
    private String partCode;

    /**
     * 检验区域名称
     */
    @TableField("part_name")
    private String partName;

    /**
     * 城市公司
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 城市公司编号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 项目名称
     */
    @TableField("project_name")
    private String projectName;

    /**
     * 项目编号
     */
    @TableField("project_code")
    private String projectCode;

    /**
     * 项目分期名称
     */
    @TableField("stage_name")
    private String stageName;

    /**
     * 项目分期编号
     */
    @TableField("stage_code")
    private String stageCode;

    /**
     * 标段id
     */
    @TableField("section_id")
    private Long sectionId;

    /**
     * 标段名称
     */
    @TableField("section_name")
    private String sectionName;

    /**
     * 楼栋编码
     */
    @TableField("ban_code")
    private String banCode;

    /**
     * 楼栋名称
     */
    @TableField("ban_name")
    private String banName;

    /**
     * 检查项id
     */
    @TableField("check_id")
    private Long checkId;

    /**
     * 检查项名称
     */
    @TableField("check_name")
    private String checkName;

    /**
     * 检查项路径名称
     */
    @TableField("check_path_name")
    private String checkPathName;

    /**
     * 工序类型(1:工程,2:装饰，3：景观)
     */
    @TableField("check_type")
    private Integer checkType;

    /**
     * 部位划分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)
     */
    @TableField("check_part")
    private Integer checkPart;

    /**
     * 检查指引
     */
    @TableField("check_guide")
    private String checkGuide;

    /**
     * 验收节点
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 验收状态(0:重新报验,1:待验收,2:待抽检,3:已完成)
     */
    @TableField("status")
    private Integer status;

    /**
     * 报验时间
     */
    @TableField(value = "report_time", strategy = FieldStrategy.IGNORED)
    private Date reportTime;

    /**
     * 验收时间
     */
    @TableField(value = "inspection_time", strategy = FieldStrategy.IGNORED)
    private Date inspectionTime;

    /**
     * 抽查时间
     */
    @TableField("spot_check_time")
    private Date spotCheckTime;

    @TableField("create_user_id")
    private Long createUserId;

    @TableField("create_user_name")
    private String createUserName;
}
