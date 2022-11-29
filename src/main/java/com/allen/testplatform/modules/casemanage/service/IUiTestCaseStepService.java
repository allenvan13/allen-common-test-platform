package com.allen.testplatform.modules.casemanage.service;

import com.allen.testplatform.modules.casemanage.model.entity.UiTestCaseStep;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 测试平台-UI测试用例步骤明细 服务类
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
public interface IUiTestCaseStepService extends IService<UiTestCaseStep> {

    void saveNewBatch(List<TestCaseVo.UiCaseStepVo> caseSteps,Long testCaseId,String testCaseCode,String createUser);

    void updateBatchById(List<TestCaseVo.UiCaseStepVo> caseSteps,String updateUser);

    List<ExcelCaseStepVo> getCaseStepList(Integer caseType);
}
