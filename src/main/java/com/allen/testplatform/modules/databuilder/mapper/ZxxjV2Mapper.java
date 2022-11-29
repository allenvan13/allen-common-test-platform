package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.common.RoomQuery;
import com.allen.testplatform.modules.databuilder.model.zxxj.ZxxjOrderQuery;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.*;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.AssertItemScore;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.AssertTemplateScore;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/4/1 14:49
 */
@Mapper
@DS("qc")
public interface ZxxjV2Mapper extends BaseMapper<ZxxjBatchEntity> {

    /**
     * 获取某检查项属性
     * @param checkItemId
     * @return
     */
    JSONObject getTargetCheckItem(@Param("checkItemId")Long checkItemId);

    JSONObject getCheckItem(@Param("checkItemName")String checkItemName,
                            @Param("templateId")Long templateId);

    /**
     * 获取模板下检查项List
     * @param templateId
     * @return
     */
    List<JSONObject> getTemplateCheckList(@Param("templateId")Long templateId);

    Long getTemplateId(@Param("templateName") String templateName);

    /**
     * 获取符合目标的 可打分的末级检查项
     * @param batchId 批次Id 选填
     * @param templateId 模板Id 选填
     * @param checkItemId 检查项Id 选填
     * @param hasBeenScored 是否已打分 选填  false、true、null-全量
     * @return
     */
    List<JSONObject> getTargetLastCheckList(@Param("batchId") Long batchId,
                                            @Param("templateId")Long templateId,
                                            @Param("checkItemId")Long checkItemId,
                                            @Param("hasBeenScored")Boolean hasBeenScored);

    List<JSONObject> getScoreLastList(@Param("batchId") Long batchId,
                                      @Param("templateId")Long templateId);

    List<JSONObject> getScoreFirstList(@Param("batchId") Long batchId,
                                       @Param("templateId")Long templateId);

    /**
     * 获取所有检查项的分数集
     * @param batchId
     * @param templateId
     * @param checkItemIds 目标检查项id
     * @return
     */
    List<AssertItemScore> getScoreFamily(@Param("batchId") Long batchId,
                                         @Param("templateId") Long templateId,
                                         @Param("checkItemIds") List<Long> checkItemIds);

    List<JSONObject> getBatchTemplate(@Param("batchId") Long batchId,
                                      @Param("templateName") String templateName);

    List<ZxxjTemplateRelation> getBatchSingleTemplate(@Param("groupTemplateId")Long groupTemplateId);

    JSONObject getSingleTemplate(@Param("templateId")Long templateId);

    /**
     * 获取模板算法模式
     * @param templateId
     * @return
     */
    List<JSONObject> getTemplateArithmetic(@Param("templateId")Long templateId);

    List<AssertTemplateScore> getTemplateScore(@Param("batchId") Long batchId,
                                               @Param("templateId") Long templateId);

    String getTemplateType(@Param("templateId")Long templateId);

    /**
     * 获取批次人员信息
     * @param batchId 批次Id 必填
     * @param identity 身份 1-检查人 2-抄送人
     * @return
     */
    List<JSONObject> getBatchUserInfo(@Param("batchId")Long batchId,
                                      @Param("identity")Integer identity);

    /**
     * 获取批次信息
     * @param batchName 批次名称 需全匹配
     * @return
     */
    ZxxjBatch getBatchInfo(@Param("batchName")String batchName,
                           @Param("batchId") Long batchId);

    List<ZxxjTemplateCheckItem> getLastCheckList(@Param("templateId") Long templateId);

    List<ZxxjTemplateCheckItem> getCheckItemList(@Param("templateId") Long templateId);

    int updateBatchScore(@Param("batchId") Long batchId);

    int updateBatch(@Param("batchId") Long batchId);

    int updateTemplateScore(@Param("batchId") Long batchId,
                             @Param("templateId") Long templateId);

    int updateTemplate(@Param("batchId") Long batchId,
                       @Param("templateId") Long templateId);

    int updateCheckItemScore(@Param("batchId") Long batchId,
                              @Param("templateId") Long templateId,
                              @Param("checkItemIds") List<Long> checkItemIds);

    List<JSONObject> getProblemPartInfo(@Param("roomQuery") RoomQuery roomQuery,
                                        @Param("sectionId") Long sectionId,
                                        @Param("hasSectionUnit")boolean hasSectionUnit);

    List<Long> getTargetOrderIdList(@Param("orderQuery") ZxxjOrderQuery orderQuery);

    List<ZxxjOrderProcessor> getOrderProcessor(@Param("orderId")Long orderId,@Param("roleType") Integer roleType);

    List<CheckUser> getBatchUsers(@Param("batchId")Long batchId, @Param("identity")Integer identity);

}
