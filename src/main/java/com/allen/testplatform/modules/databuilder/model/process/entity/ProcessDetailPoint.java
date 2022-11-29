package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序验收流程节点验收点表(ProcessDetailPoint)实体类
 *
 * @author makejava
 * @since 2021-08-13 11:38:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail_point")
public class ProcessDetailPoint extends BaseEntity<ProcessDetailPoint> {

    private static final long serialVersionUID = 398915080580451844L;

    /**
     * 验收流程id
     */
    @TableField("flow_id")
    private Long flowId;

    /**
     * 验收明细id
     */
    @TableField("detail_id")
    private Long detailId;

    /**
     * 验收点Id
     */
    @TableField("point_id")
    private Long pointId;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 标准/说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 图片地址
     */
    @TableField("picture")
    private String picture;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private Long createUserId;

}
