package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.common.BatchRoomInfo;
import com.allen.testplatform.modules.databuilder.model.common.CheckBatch;
import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.common.TicketUserInfo;
import com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
@DS("qc")
public interface FhcyV2Mapper {

    CheckBatch getCheckBatch(@Param("batchName")String batchName);

    List<String> getCheckCodeList(@Param("checkListId")Long checkListId,
                                  @Param("pathCode")String pathCode,
                                  @Param("firstCheckName")String firstCheckName,
                                  @Param("secondCheckName")String secondCheckName,
                                  @Param("lastCheckName")String lastCheckName);


    List<JSONObject> getItemParentPath(@Param("checkListId")Long checkListId,
                                       @Param("pathCode")String pathCode);

    List<BatchRoomInfo> getCheckBatchRooms(@Param("batchId")Long batchId,
                                           @Param("banName")String banName,
                                           @Param("unitName")String unitName,
                                           @Param("floorName")String floorName,
                                           @Param("roomName")String roomName);

    List<JSONObject> getAllSiteInProject(@Param("projectCode")String projectCode);

    List<CheckUser> getAllRoleUsers(@Param("batchId")Long batchId);

    List<CheckUser> getRectifyUsers(@Param("batchId")Long batchId);

    List<TicketUserInfo> getTicketInfos(@Param("tickerIds")List<Long> tickerIds,
                                        @Param("roleCode")Integer roleCode,
                                        @Param("ticketStatus")String ticketStatus);

    List<JSONObject> getBatchTicketInfos(@Param("tickerIds")List<Long> tickerIds,
                                         @Param("ticketStatus")String ticketStatus);

    List<Long> getTicketIds(@Param("stageCode")String stageCode,
                            @Param("batchId")Long batchId,
                            @Param("ticketStatus")String ticketStatus,
                            @Param("processorId")Long processorId);

    List<Long> getRectifyProcessIds(@Param("stageCode")String stageCode,
                             @Param("batchId")Long batchId,
                             @Param("ticketStatus")String ticketStatus);

    void removeBacklogData(@Param("removeContentList")List<String> removeContentList);

    void removeTicketData(@Param("category")String businessType,@Param("removeContentList")List<String> removeContentList);

    TkTicket assertTicket( @Param("content")String content);
}
