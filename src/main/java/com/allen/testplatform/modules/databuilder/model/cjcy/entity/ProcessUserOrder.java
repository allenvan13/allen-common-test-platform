package com.allen.testplatform.modules.databuilder.model.cjcy.entity;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/9/22 15:44
 */

@Data
public class ProcessUserOrder {

    private List<String> processorOrderIds;
    private Long processorId;
    private String processorName;
    private String processorCode;
}
