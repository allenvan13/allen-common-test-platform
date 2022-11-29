package com.allen.testplatform.modules.databuilder.model.fhcy.vo;

import lombok.Data;

@Data
public class ReviewProblemVo {

    private Long id;
    private String[] imageUrls;
    private String content;
    /**
     * 审核类型 1-消项通过  2-消项不通过 3- 已作废 9-抽查不通过
     */
    private Integer type;
    /**
     * 创建时间 时间戳
     */
    private String updateTime;

    private Long userId;
}
