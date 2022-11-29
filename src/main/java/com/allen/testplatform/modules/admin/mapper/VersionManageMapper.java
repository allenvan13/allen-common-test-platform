package com.allen.testplatform.modules.admin.mapper;

import com.allen.testplatform.modules.admin.model.entity.VersionManage;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Fan QingChuan
 * @since 2022/4/19 12:28
 */
@DS("test")
@Mapper
public interface VersionManageMapper extends BaseMapper<VersionManage> {



}
