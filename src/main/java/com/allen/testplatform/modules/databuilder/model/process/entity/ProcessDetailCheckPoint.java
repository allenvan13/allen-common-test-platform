package com.allen.testplatform.modules.databuilder.model.process.entity;

import cn.nhdc.common.database.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序验收明细验收点表(ProcessDetailCheckPoint)实体类
 *
 * @author makejava
 * @since 2021-08-10 17:49:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("process_detail_check_point")
public class ProcessDetailCheckPoint extends BaseEntity<ProcessDetailCheckPoint> {

    private static final long serialVersionUID = 589053794153820539L;

    /**
     * 验收点Id
     */
    @TableField("point_id")
    private Long pointId;

    /**
     * 验收明细id
     */
    @TableField("detail_id")
    private Long detailId;

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


}
