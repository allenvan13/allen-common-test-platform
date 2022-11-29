package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@DS("qc")
public interface CommonCheckMapper {

    List<CheckUser> getCheckUsers(@Param("stageCode")String stageCode,
                                  @Param("category")String category,
                                  @Param("roleType")Integer roleType);


    List<JSONObject> getCheckItems(@Param("category")String category,
                                   @Param("orgCode")String orgCode);

    JSONObject assertTicketCount(@Param("category")String category,
                                 @Param("stageCode")String stageCode);

    TkTicket getTicketByContent(@Param("content")String content,
                                @Param("category")String category);
}
