package com.allen.testplatform.modules.casemanage.service.impl;

import com.allen.testplatform.modules.casemanage.mapper.UiTestCaseStepMapper;
import com.allen.testplatform.modules.casemanage.model.entity.UiTestCaseStep;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.modules.casemanage.service.IUiTestCaseStepService;
import cn.nhdc.common.util.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoleilu.hutool.bean.BeanUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 测试平台-UI测试用例步骤明细 服务实现类
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@Service
public class UiTestCaseStepServiceImpl extends ServiceImpl<UiTestCaseStepMapper, UiTestCaseStep> implements IUiTestCaseStepService {

    @Override
    public void saveNewBatch(List<TestCaseVo.UiCaseStepVo> caseSteps, Long testCaseId, String testCaseCode, String createUser) {
        if (CollectionUtils.isNotEmpty(caseSteps)) {
            List<UiTestCaseStep> uiTestCaseSteps = new ArrayList<>();
            caseSteps.forEach(stepVo -> {
                UiTestCaseStep testCaseStep = new UiTestCaseStep();
                BeanUtil.copyProperties(stepVo,testCaseStep);
                testCaseStep.setId(IdWorker.getId());
                testCaseStep.setCaseId(testCaseId);
                testCaseStep.setCaseCode(testCaseCode);
                testCaseStep.setCreateUser(createUser);
                uiTestCaseSteps.add(testCaseStep);
            });
            this.saveBatch(uiTestCaseSteps);
        }
    }

    @Override
    public void updateBatchById(List<TestCaseVo.UiCaseStepVo> caseSteps,String updateUser) {
        List<UiTestCaseStep> uiTestCaseSteps = new ArrayList<>();
        caseSteps.forEach(stepVo -> {
            UiTestCaseStep testCaseStep = new UiTestCaseStep();
            BeanUtil.copyProperties(stepVo,testCaseStep);
            testCaseStep.setId(stepVo.getStepId());
            testCaseStep.setUpdateUser(updateUser);
            uiTestCaseSteps.add(testCaseStep);
        });
        this.updateBatchById(uiTestCaseSteps);
    }


    @Override
    public List<ExcelCaseStepVo> getCaseStepList(Integer caseType){
        return baseMapper.getCaseStepList(caseType);
    }

}
