package com.allen.testplatform.modules.databuilder.model.zxxj.entity;

import lombok.Data;

/**
 * <p>
 * 单项模板-检查项关联表
 * </p>
 *
 * @author Lu
 * @since 2021-05-07
 */
@Data
public class ZxxjTemplateCheckItem {

    private Long id;
    /**
     * 检查项名称
     */
    private String name;
    /**
     * 父级检查项
     */
    private Long parentId;
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 层级
     */
    private Integer level;
    /**
     * 红线 1-是 0-否
     */
    private Boolean redLine;
    /**
     * 拓展信息 json格式
     */
    private String extension;
}
