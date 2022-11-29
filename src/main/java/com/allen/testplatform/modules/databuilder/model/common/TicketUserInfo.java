package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/4/27 14:21
 */
@Data
public class TicketUserInfo {
    private List<Long> ticketIds;
    private Long processorId;
    private String status;


}

