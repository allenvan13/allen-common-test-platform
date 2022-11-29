package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.test.entity.HeartCheckTarget;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fan QingChuan
 * @since 2022/4/19 12:28
 */
@DS("test")
@Mapper
public interface HeartCheckMapper extends BaseMapper<HeartCheckTarget> {

}
