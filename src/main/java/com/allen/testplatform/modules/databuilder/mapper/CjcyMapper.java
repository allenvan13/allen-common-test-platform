package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.cjcy.entity.OrderQuery;
import com.allen.testplatform.modules.databuilder.model.cjcy.entity.ProcessUserOrder;
import com.allen.testplatform.modules.databuilder.model.common.CheckBatch;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
@DS("qc")
public interface CjcyMapper {


    List<JSONObject> getBatchRoom(@Param("batchId") Long batchId,
                                  @Param("banName") String banName,
                                  @Param("roomNumber") String roomNumber,
                                  @Param("unit") String unit,
                                  @Param("floor") String floor);

    List<JSONObject> getRoomCheckItem(@Param("batchId") Long batchId,
                                  @Param("roomCode") String roomCode);

    JSONObject getParentCheckItem(@Param("checkItemId") Long checkItemId);

    JSONObject getDutyUser(@Param("stageCode") String stageCode,
                           @Param("banCode") String banCode,
                           @Param("checkItemId") Long checkItemId,
                           @Param("empType") int empType);

    CheckBatch getCheckBatch(@Param("batchId") Long batchId);

    List<Long> getTargetOrderIdList(@Param("orderQuery")OrderQuery orderQuery);

    List<ProcessUserOrder> getRecitifyProcessUser(@Param("orderIdList")List<Long> orderIdList);

    List<ProcessUserOrder> getCreateOrderUsers(@Param("orderIdList")List<Long> orderIdList);

    List<JSONObject> getOrderCheckType(@Param("orderIdList")List<Long> orderIdList);
}
