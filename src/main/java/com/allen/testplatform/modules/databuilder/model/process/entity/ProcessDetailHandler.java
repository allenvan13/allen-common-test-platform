package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序验收处理人表(ProcessDetailHandler)实体类
 *
 * @author makejava
 * @since 2021-08-13 11:38:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail_handler")
public class ProcessDetailHandler extends BaseEntity<ProcessDetailHandler> {

    private static final long serialVersionUID = 765377832417485418L;

    /**
     * 验收流程id
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * 下一节点流程id
     */
    @TableField("next_flow_id")
    private Long nextFlowId;

    /**
     * 验收明细id
     */
    @TableField("detail_id")
    private Long detailId;

    /**
     * 用户类型(1:共同验收人,2:抄送人,3:验收人,4:发起人)
     */
    @TableField("type")
    private Integer type;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 岗位名称
     */
    @TableField("position_name")
    private String positionName;

    /**
     * 供应商公司guid
     */
    @TableField("company_guid")
    private String companyGuid;

    /**
     * 供应商公司名字
     */
    @TableField("company_name")
    private String companyName;


}
