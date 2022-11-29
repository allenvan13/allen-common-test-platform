package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.common.RoomQuery;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.process.dto.DetailQuery;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetail;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetailIssue;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/2/21 14:00
 */

@Mapper
@DS("qc")
public interface ProcessV2Mapper extends BaseMapper<ProcessDetail> {

    /**
     * 获取标段信息
     */
    SectionInfo getSectionInfo(@Param("sectionName")String sectionName,
                               @Param("sectionId")Long sectionId,
                               @Param("sectionType")Integer sectionType);

    /**
     * 获取末级检查项List
     */
    List<JSONObject> getLastCheck(@Param("checkName")String checkName,
                                  @Param("parentCheckName")String parentCheckName);

    JSONObject getLastCheckById(@Param("checkId")Long checkId);

    List<JSONObject> getAllCheckList();

    /**
     * 获取检查项名称
     */
    JSONObject getCheckPathName(@Param("checkIdList")List<String> checkIdList);

    /**
     * 获取检查流程节点信息
     */
    List<JSONObject> getCheckFlow(@Param("checkId")Long checkId);

    /**
     * 获取检查点信息
     */
    List<JSONObject> getCheckPoint(@Param("checkId")Long checkId);

    /**
     * 获取房源信息 - 整栋方式
     */
    List<JSONObject> getPartByBan(@Param("sectionId")Long sectionId,
                                  @Param("roomQuery") RoomQuery roomQuery,
                                  @Param("checkId")Long checkId,
                                  @Param("hasSectionUnit")boolean hasSectionUnit);

    /**
     * 获取房源信息 - 不分单元&分层
     */
    List<JSONObject> getPartByFloor(@Param("sectionId")Long sectionId,
                                    @Param("roomQuery") RoomQuery roomQuery,
                                    @Param("checkId")Long checkId,
                                    @Param("hasSectionUnit")boolean hasSectionUnit);

    /**
     * 获取房源信息 - 分单元&分层
     */
    List<JSONObject> getPartByUnitFloor(@Param("sectionId")Long sectionId,
                                        @Param("roomQuery") RoomQuery roomQuery,
                                        @Param("checkId")Long checkId,
                                        @Param("hasSectionUnit")boolean hasSectionUnit);

    /**
     * 获取房源信息 - 分户
     */
    List<JSONObject> getPartByRoom(@Param("sectionId")Long sectionId,
                                   @Param("roomQuery") RoomQuery roomQuery,
                                   @Param("checkId")Long checkId,
                                   @Param("hasSectionUnit")boolean hasSectionUnit);

    /**
     * 获取自定义检验批信息
     */
    List<JSONObject> getPartByCustom(@Param("sectionId")Long sectionId,
                                     @Param("checkId")Long checkId);

    /**
     *
     * 获取分期下  各 角色 人员   0：检查人员 1：整改人员 3：抽查人员 4：管理人员
     */
    List<JSONObject> getBatchUser(@Param("stageCode")String stageCode,
                                  @Param("roleType")String roleType,
                                  @Param("category")String category);/**
     *
     * 获取分期下  各 角色 人员ID   0：检查人员 1：整改人员 3：抽查人员 4：管理人员
     */
    List<Long> getBatchUserId(@Param("stageCode")String stageCode,
                                  @Param("roleType")Integer roleType,
                                  @Param("category")String category);

    List<JSONObject> getDetailCheckFlow(@Param("detailId")Long detailId,
                                        @Param("flowId")Long flowId,
                                        @Param("node")Integer node);

    List<JSONObject> getDetailHandleFlow(@Param("detailId")Long detailId,
                                                          @Param("flowId")Long flowId,
                                                          @Param("node")Integer node);

    List<Long> getTargetDetailIdList(@Param("detailQuery") DetailQuery detailQuery);

    List<JSONObject> getProblemPartInfo(@Param("roomQuery") RoomQuery roomQuery,@Param("hasSectionUnit")boolean hasSectionUnit);

    List<ProcessDetailIssue> getIssuesInDetail(@Param("detailId")Long detailId,@Param("issueStatus")List<String> issueStatus);

    SectionInfo getSectionByIssue(@Param("detailId")Long detailId,@Param("issueId")Long issueId);

    ProcessDetail assertDetail(@Param("content")String content);

    int countDetail(@Param("stageCode")String stageCode);

}
