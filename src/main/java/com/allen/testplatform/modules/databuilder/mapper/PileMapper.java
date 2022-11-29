package com.allen.testplatform.modules.databuilder.mapper;


import com.allen.testplatform.modules.databuilder.model.pile.AssertDetail;
import com.allen.testplatform.modules.databuilder.model.pile.AssertDetailHandlerEntity;
import com.allen.testplatform.modules.databuilder.model.pile.AssertDetailPoint;
import com.allen.testplatform.modules.databuilder.model.pile.DetailQueryVO;
import com.allen.testplatform.testscripts.testcase.jx.ZjysMainProcessTest;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2021/11/22 11:30
 */
@Mapper
@DS("qc")
public interface PileMapper {

    //获取桩基区域、及图 标段id、注意桩基区域未定义时 为楼栋code
    List<JSONObject> getPileSection(@Param("sectionName")String sectionName);

    //获取检查项信息
    List<JSONObject> getPileCheckType();

    //根据检查项 获取 检查项下的检查点
    List<JSONObject> getPoint(@Param("typeId") Long typeId);

    JSONObject getSectionCompany(@Param("sectionName")String sectionName);

    AssertDetail assertDetail(@Param("pileDetailId")Long pileDetailId);

    List<AssertDetailPoint> assertDetailPoint(@Param("pileDetailId")Long pileDetailId);

    List<AssertDetailHandlerEntity> assertDetailHandler(@Param("pileDetailId")Long pileDetailId,
                                                        @Param("identity")int identity);

    List<Long> getDetailIdList(@Param("qvo") DetailQueryVO qvo, @Param("stages") String[] stages);

    ZjysMainProcessTest.AssertPileDetail assertPileDetail(@Param("pileSn")String pileSn, @Param("stageCode")String stageCode);

    List<ZjysMainProcessTest.AssertPileDetailPoint> assertPileDetailPoint(@Param("pileDetailId")Long pileDetailId);

    JSONObject countPileDetail(@Param("checkPartCode")String checkPartCode,@Param("sectionId")Long sectionId);

}
