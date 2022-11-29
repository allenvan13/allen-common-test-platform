package com.allen.testplatform.modules.databuilder.model.cjcy.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Fan QingChuan
 * @since 2022/9/22 14:22
 */

@Data
public class OrderQuery {

    private String status;
    private String orgCode;
    private String orgName;
    private String projectName;
    private String projectCode;
    private String stageName;
    private String stageCode;

    private String banName;
    private String banCode;
    private String unit;
    private String roomName;
    private String roomCode;

    private String providerGuid;
    private String providername;

    private String createUsername;
    private String createRealname;

    private Long dutyUserId;
    private String dutyUserName;

    private String checkBatchName;
    private Long checkBatchId;

    private Long id;
    private String content;
    private String sn;
    private String title;

    private Integer importance;
    private String location;

    private Date submitTimeStart;
    private Date submitTimeEnd;
    private Date updateTimeStart;
    private Date updateTimeEnd;
}
