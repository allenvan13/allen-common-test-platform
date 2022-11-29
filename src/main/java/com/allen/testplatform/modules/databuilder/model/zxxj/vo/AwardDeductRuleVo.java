package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.Data;

import java.util.List;

/**
 * 定档打分-带星扣分规则（zxxj_template_arithmetic arithmetic_value）
 */
@Data
public class AwardDeductRuleVo {

    //测区
    private Integer deductNums;
    //档位
    private List<DeductRule> deductRules;

    @Data
    public static class DeductRule {
        //档位名称
        private String mark;
        //分数
        private Double score;
    }
}