package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

/**
 * @author Fan QingChuan
 * @since 2022/4/24 16:09
 */
@Data
public class BatchRoomInfo {
    private String unit;
    private String floor;
    private String banCode;
    private String banName;
    private String roomCode;
    private String roomName;
    private String checkImageUrl;
    private Long houseTypeId;
    private Long projectSiteId;
    private String projectSiteName;
}
