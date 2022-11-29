package com.allen.testplatform.modules.databuilder.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
@DS("qc")
public interface QcDeleteDataMapper {

    int removeBacklogBySn(@Param("snList")List<String> snList);

    int removeProcessDetailAndProblemByIds(@Param("detailIdList")List<Long> detailIdList);

    int removeProcessBacklogByContent(@Param("removeContentList")List<String> removeContentList);

    List<String> getProcessDetailSnList(@Param("removeContentList")List<String> removeContentList);

    int removeProcessDetailAndProblemByContent(@Param("removeContentList")List<String> removeContentList);


    /**
     * 只支持ticket表相关的待办
     * @param businessType
     * @param removeContentList
     * @return
     */
    int removeTicketBacklogByTypeContent(@Param("category")String businessType, @Param("removeContentList")List<String> removeContentList);

    int removeTicketByTypeContent(@Param("category")String businessType, @Param("removeContentList")List<String> removeContentList);

    int removeZxxjOrderByContent(@Param("removeContentList")List<String> removeContentList);

    int removeZxxjBacklogByContent(@Param("removeContentList")List<String> removeContentList);
}
