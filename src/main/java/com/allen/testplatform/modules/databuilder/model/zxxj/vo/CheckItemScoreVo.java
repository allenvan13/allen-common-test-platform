package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 巡检打分提交Vo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckItemScoreVo {

    @NotNull(message = "批次id不能为空")
    private Long batchId;
    @NotNull(message = "模板id不能为空")
    private Long templateId;
    @NotNull(message = "检查项id不能为空")
    private Long checkItemId;
    /**
     * 实测实量 算法名称
     */
    private String realMeasureName;
    /**
     * 检查合格-图片
     */
    private String imgUrls;
    /**
     * 检查合格-补充说明
     */
    private String content;
    @NotNull(message = "打分信息不能为空")
    @Valid
    private List<Area> areas;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Area {
        /**
         * 设计值
         */
        private Double designValue;
        @NotNull(message = "打分信息不能为空")
        private List<Score> scores;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Score {
        /**
         * 打分值
         */
        private String mark;
        /**
         * 打分值
         */
        private Double value;
        /**
         * 是否为爆点
         */
        private Boolean isCritical;
        /**
         * 是否合格
         */
        private Boolean isQualified;
    }
}