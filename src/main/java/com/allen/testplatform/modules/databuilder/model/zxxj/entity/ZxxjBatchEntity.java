package com.allen.testplatform.modules.databuilder.model.zxxj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 专项巡检-批次
 * </p>
 *
 * @author Lu
 * @since 2021-05-07
 */
@Data
@TableName(value = "zxxj_batch")
public class ZxxjBatchEntity {

    private Long id;
    /**
     * 城市公司名称
     */
    private String orgName;
    /**
     * 城市公司code
     */
    private String orgCode;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目code
     */
    private String projectCode;
    /**
     * 分期名称
     */
    private String stageName;
    /**
     * 分期code
     */
    private String stageCode;
    /**
     * 巡检类型
     * 毛坯过程评估:mpProcessAssess,
     * 土建材料评估:tjMaterialAssess,
     * 毛坯交付预评估:mpDeliveryPreAssess,
     * 毛坯交付评估:mpDeliveryAssess,
     * 精装过程评估（精装房）:jzRoomAssess,
     * 精装过程评估（公区）:jzPublicAssess,
     * 精装界面评估:jzInterfaceAssess,
     * 精装材料评估:jzMaterialAssess,
     * 精装交付评估:jzDeliveryAssess
     */
    private String type;
    /**
     * 标段信息
     */
    private Long sectionId;
    /**
     * 巡检方式 1-三方飞检 2-城市自检
     */
    private Integer method;
    /**
     * 评估年份
     */
    private Integer assessYear;
    /**
     * 评估月份
     */
    private Integer assessMonth;
    /**
     * 巡检模板
     */
    private Long templateId;

    /**
     * 批次名称
     */
    private String name;
    /**
     * 启用状态
     */
    private Boolean enable;

    /**
     * 下载状态 true-已被下载,false-未被下载
     */
    private Boolean downloadStatus;
    /**
     * 创建人名称
     */
    private String createUserName;
    /**
     * 创建人id
     */
    private Long createUserId;
    /**
     * 是否完成
     */
    private Boolean isFinish;
    /**
     * 完成时间
     */
    private Date finishDate;

}
