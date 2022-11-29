package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.modules.databuilder.enums.CheckTypeEnum;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.processreport.ProcessRiskVo;
import com.allen.testplatform.modules.databuilder.service.ProcessReportService;
import com.allen.testplatform.modules.databuilder.service.UcUserService;
import com.allen.testplatform.testscripts.api.ApiProcessReport;
import cn.nhdc.common.exception.BusinessException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.xiaoleilu.hutool.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Fan QingChuan
 * @since 2022/5/27 17:42
 */

@Slf4j
@Service
@DS("qc")
public class ProcessReportServiceImpl implements ProcessReportService {

    @Resource
    private ProcessV2Mapper processV2Mapper;

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @Resource
    @Qualifier(value = "callerRunsThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Resource
    private UcUserService ucUserService;

    @Override
    public void saveRisks(String sectionName,Boolean hasRisk,String riskContent,Integer pictureNum,String createUserName,Integer testCount) {

        UcUser user = ucUserService.getCreateUser(createUserName);
//        if (ObjectUtil.isNotEmpty(createUserName)) {
//            List<UcUser> users = ucMapper.getUserByOthers(null,createUserName,null,null, Constant.PS_SOURCE);
//            if (CollectionUtils.isNotEmpty(users)) {
//                user = users.get(RandomUtil.randomInt(users.size()));
//            }else {
//                throw new BusinessException("(姓名全匹配)未匹配到目标用户-->"+createUserName);
//            }
//        }else {
//            user = ucMapper.getUserByOthers("chengxm1", "程星铭", null, null, Constant.PS_SOURCE).get(0);
//        }

        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()),currentEnv.getENV()));

        SectionInfo sectionInfo = processV2Mapper.getSectionInfo(sectionName, null, null);
        if (ObjectUtil.isEmpty(sectionInfo)) {
            throw new BusinessException("未匹配到目标标段");
        }
        ProcessRiskVo processRiskVo = new ProcessRiskVo();
        BeanUtil.copyProperties(sectionInfo,processRiskVo);
        processRiskVo.setType(sectionInfo.getSectionType());

        if (ObjectUtil.isNotEmpty(testCount) && testCount >= 1) {
            for (int i = 0; i < testCount; i++) {
                taskExecutor.execute(() -> {
                    ProcessRiskVo tempVo = new ProcessRiskVo();
                    BeanUtil.copyProperties(processRiskVo,tempVo);
                    processRiskVo.setImages(ObjectUtil.isNotEmpty(pictureNum) && pictureNum > 0 && pictureNum <= 5 ? TestDataUtils.getPicture(pictureNum) : TestDataUtils.getPicture(RandomUtil.randomInt(5)));
                    if (ObjectUtil.isNotEmpty(hasRisk) && hasRisk) {
                        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
                        if (ObjectUtil.isNotEmpty(riskContent)) {
                            stringBuilder.append("-").append(CheckTypeEnum.PROCESS_REPORT.getMsg()).append("-").append("创建人：").append(user.getRealName()).append("-风险内容---->").append(riskContent);
                        }else {
                            stringBuilder.append("-").append(CheckTypeEnum.PROCESS_REPORT.getMsg()).append("-").append("创建人：").append(user.getRealName()).append("-风险内容---->").append(DateUtils.current()).append("-------").append(TestDataUtils.getRandomStrNum(DateUtils.now(),testCount));
                        }
                        processRiskVo.setRiskAndSale(stringBuilder.toString());
                    }else {
                        processRiskVo.setRiskAndSale(null);
                    }
                    String rs = HttpUtils.doPost(currentEnv.getHOST().concat(ApiProcessReport.PC_SAVE), header, JSONObject.toJSONString(processRiskVo));
                    log.info("{}", JSON.parseObject(rs));
                });
            }
        }
    }



}
