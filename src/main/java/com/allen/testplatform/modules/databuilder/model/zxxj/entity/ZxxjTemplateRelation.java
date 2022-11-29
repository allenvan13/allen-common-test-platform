package com.allen.testplatform.modules.databuilder.model.zxxj.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * 组合模板-单项模板关联表
 * </p>
 *
 * @author Lu
 * @since 2021-05-07
 */
@Data
@TableName("zxxj_template_relation")
public class ZxxjTemplateRelation extends Model<ZxxjTemplateRelation> {

    private static final long serialVersionUID = 1L;

    /**
     * 组合模板id
     */
    private Long groupTemplateId;
    /**
     * 单项模板id
     */
    private Long singleTemplateId;

    private String templateName;
    private String templateType;
    /**
     * 权重
     */
    private Double weight;

    /**
     * 是否纳入加权平均
     */
    @TableField("is_weight_average")
    private Boolean weightAverage;
}
