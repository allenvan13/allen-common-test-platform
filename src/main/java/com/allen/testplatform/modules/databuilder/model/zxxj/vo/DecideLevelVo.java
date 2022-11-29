package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AwardDeductRuleDto
 * 定档打分
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecideLevelVo {

    //测区
    private Integer areaCount;
    private MarkStandard markStandard;
    private String checkGuide;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MarkStandard {
        //满分
        private Double fullScore;
        private List<DeductRule> deductRules;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeductRule {
        private String mark;
        private Double score;
    }
}