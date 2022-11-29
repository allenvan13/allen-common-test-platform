package com.allen.testplatform.modules.casemanage.mapper;

import com.allen.testplatform.modules.casemanage.model.entity.UiTestCaseStep;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 测试平台-UI测试用例步骤明细 Mapper 接口
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@DS("test")
public interface UiTestCaseStepMapper extends BaseMapper<UiTestCaseStep> {

    List<ExcelCaseStepVo> getCaseStepList(@Param("caseType") Integer caseType);

}
