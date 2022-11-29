package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 工序检查验收明细流程表(ProcessDetailCheckFlow)实体类
 *
 * @author makejava
 * @since 2021-08-16 10:56:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail_check_flow")
public class ProcessDetailCheckFlow extends BaseEntity<ProcessDetailCheckFlow> {

    private static final long serialVersionUID = -15201834535455822L;

    /**
     * 验收明细id
     */
    @TableField("detail_id")
    private Long detailId;

    /**
     * 验收流程id
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * 验收节点id
     */
    @TableField("node_id")
    private Long nodeId;

    /**
     * 上一个验收节点id
     */
    @TableField("parent_node_id")
    private Long parentNodeId;

    /**
     * 验收节点(1:报验,2:验收,3:抽检)
     */
    @TableField("node")
    private Integer node;

    /**
     * 角色类型(1:施工,2:监理,3:项目甲方,4:城市平台)
     */
    @TableField("role_type")
    private Integer roleType;

    /**
     * 是否指定后续验收人
     */
    @TableField("if_appoint")
    private Boolean ifAppoint;

    /**
     * 状态(1:驳回,2:通过)
     */
    @TableField(value = "status", strategy = FieldStrategy.IGNORED)
    private Integer status;

    /**
     * 用户id
     */
    @TableField(value = "user_id", strategy = FieldStrategy.IGNORED)
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField(value = "real_name", strategy = FieldStrategy.IGNORED)
    private String realName;

    /**
     * 岗位名称
     */
    @TableField("position_name")
    private String positionName;

    /**
     * 供应商公司guid
     */
    @TableField(value = "company_guid", strategy = FieldStrategy.IGNORED)
    private String companyGuid;

    /**
     * 供应商公司名字
     */
    @TableField(value = "company_name", strategy = FieldStrategy.IGNORED)
    private String companyName;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private Date submitTime;

    /**
     * 补充说明
     */
    @TableField("comment")
    private String comment;


}
