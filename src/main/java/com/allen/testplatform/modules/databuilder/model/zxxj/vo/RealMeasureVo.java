package com.allen.testplatform.modules.databuilder.model.zxxj.vo;


import lombok.Data;

/**
 * RealMeasureDto
 * 实测实量算法
 * @author FanQingChuan
 *
 */
@Data
public class RealMeasureVo {

    private String name;
    /**
     * 合格率计算方法 1-标准 2-标准+设计值 3-极差 4-偏差 5-观感 6-直接录合格点数与总点数
     */
    private Integer passArithmetic;
    /**
     * 测区/测点法选择  1-测点法 2-测区法
     */
    private Integer areaOrPoint;
    /**
     * 合格标准
     */
    private PassStandard passStandard;
    /**
     * 测区点数
     */
    private AreaCount areaCount;
    /**
     * 爆板值
     */
    private Double blastPlate;
    /**
     * 检查指引
     */
    private String checkGuide;

    @Data
    public static class PassStandard {
        private Double passMin;
        private Double passMax;
    }

    @Data
    public static class AreaCount {
        private Integer nums;
        private Integer count;
    }
}
