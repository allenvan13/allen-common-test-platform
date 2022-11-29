package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.Data;

/**
 * @author Fan QingChuan
 * @since 2022/4/9 13:14
 */
@Data
public class AssertTemplateScore {

    private Long batchId;
    private Long templateId;
    private Double weight;
    private Boolean isWeightAverage;
    private Double score;
    private Boolean isFinish;
}
