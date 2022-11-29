package com.allen.testplatform.modules.casemanage.mapper;

import com.allen.testplatform.modules.casemanage.model.entity.TestCase;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 测试平台-测试用例 Mapper 接口
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@DS("test")
public interface TestCaseMapper extends BaseMapper<TestCase> {

}
