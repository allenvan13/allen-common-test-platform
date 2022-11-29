package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.Data;

/**
 * @author Fan QingChuan
 * @since 2022/4/5 21:24
 */
@Data
public class AssertItemScore {
    private Long checkItemId;
    private Double score;
    private Integer level;
    private Integer passPoint;
    private Integer totalPoint;
    private Double redLineScore;
    private Long parentId;
    private Long templateId;
    private Long batchId;
//    private JSONObject scoreInfo;
}
