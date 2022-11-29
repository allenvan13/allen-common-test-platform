package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetailHandler;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fan QingChuan
 * @since 2022/5/13 16:39
 */

@DS("qc")
@Mapper
public interface ProcessCheckHandlerMapper extends BaseMapper<ProcessDetailHandler> {
}
