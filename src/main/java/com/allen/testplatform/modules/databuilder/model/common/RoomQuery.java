package com.allen.testplatform.modules.databuilder.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fan QingChuan
 * @since 2022/3/17 13:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomQuery {

    private String banName;
//    private String banCode;
    private String floorName;
//    private String floorCode;
    private String unitName;
//    private String unitCode;
    private String roomName;
//    private String roomCode;
}
