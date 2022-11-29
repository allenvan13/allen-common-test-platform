package com.allen.testplatform.modules.databuilder.model.cjcy.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/9/22 13:44
 */

@Data
public class ReviewProblemVo {

    private Long orderId;
    private List<String> attachments;
    private String content;
    private String addTime;

}
