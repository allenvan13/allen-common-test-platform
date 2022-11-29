package com.allen.testplatform.modules.databuilder.model.zxxj.vo;
import lombok.Data;

/**
 * WeightScoreDto
 * 可打分，按“权重+得分率”计算总分
 * @author FanQingChuan
 *
 */
@Data
public class WeightScoreVo {

    /**
     * 权重
     */
    private Double weight;
    /**
     * 是否纳入加权平均
     */
    private Boolean weightAverage;
    /**
     * 满分
     */
    private Double fullScore;
    /**
     * 扣分上限
     */
    private Double deductLimit;
    /**
     * 登记问题默认扣分
     */
    private Double problemDeduct;
    /**
     * 检查指引
     */
    private String checkGuide;
}
