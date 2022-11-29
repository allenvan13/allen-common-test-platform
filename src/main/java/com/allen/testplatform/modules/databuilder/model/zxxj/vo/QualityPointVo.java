package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.Data;

/**
 * QualityPointDto
 * 无打分，按合格计算点数/计算点总数，计算合格率
 * @author FanQingChuan
 *
 */
@Data
public class QualityPointVo {

    /**
     * 权重
     */
    private Double weight;
    /**
     * 是否纳入加权平均
     */
    private Boolean weightAverage;
    /**
     * 测区点数
     */
    private AreaCount areaCount;
    /**
     * 检查指引
     */
    private String checkGuide;

    @Data
    public static class AreaCount {
        private Integer nums;
        private Integer count;
    }
}
