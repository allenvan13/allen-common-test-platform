package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.enums.ResponseEnum;
import com.allen.testplatform.modules.databuilder.enums.RoleTypeEnum;
import com.allen.testplatform.common.utils.TokenUtils;
import com.allen.testplatform.common.utils.HttpUtils;
import com.allen.testplatform.common.utils.MathUtils;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.enums.ZxxjTemplateTypeEnum;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.ZxxjV2Mapper;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjBatch;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.*;
import com.allen.testplatform.modules.databuilder.service.ZxxjAlgorithmService;
import com.allen.testplatform.testscripts.api.ApiZXXJ;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.AtomicDouble;
import com.jayway.jsonpath.JsonPath;
import com.xiaoleilu.hutool.util.ArrayUtil;
import com.xiaoleilu.hutool.util.NumberUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.testng.Assert;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 专项巡检-算法相关测试
 *
 * 测试功能需求:
 * 1、根据指定查验批次,指定模板,指定检查项名称 自动识别模板类型进行打分（测量值在配置范围内随机）
 * 2、可只对未打分的检查项（指定查验批次,指定模板,指定检查项名称，向下取值,比如指定了检查项名称/模板,必须指定批次）进行自动打分
 * 3、可对已打分的检查项（指定查验批次,指定模板,指定检查项名称，向下取值,比如指定了检查项名称/模板,必须指定批次）再次进行打分(覆盖)
 * 4、可自动生成测量数据进行提交   可传入指定测量数据进行提交（可用于测试需要，例如等价、边界测试  可用于快速提供测试数据 例如快速进行批次下打分）
 *
 * 5、统一断言: 打分后断言接口返回分数及数据库分数 是否符合功能需求算法 => 1、打分的末级检查项分数 2、父级路径上各分数 3、模板、组合模板分数、总分数
 *
 * 6、重置分数-将目标检查项、模板、批次下已打分重置为初始状态
 * 7、重置已完成查验状态的批次为可查验状态
 *
 * 8、可自动新增一个批次,并关联对应配置好的模板,自动完成打分-断言后,删除测试数据
 * @author Fan QingChuan
 * @since 2022/4/11 10:38
 */
@Slf4j
@Service
public class ZxxjAlgorithmServiceImpl implements ZxxjAlgorithmService {

    @Value("${zxxj.score.min.value}")
    public double MIN_VALUE;              //测量打分值最小值

    @Value("${zxxj.score.max.value}")
    public double MAX_VALUE;              //测量打分值最大值

    @Resource
    private ZxxjV2Mapper zxxjCheckMapper;
    @Resource
    private ProcessV2Mapper processV2Mapper;
    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    //重置清空指定批次-模板-检查项打分
    @Override
    public void resetScore(String batchName,String templateName,String checkItemName){
        Long batchId;
        Long templateId;
        Long checkItemId;

        if (ObjectUtil.isNotEmpty(batchName)) {
            ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(batchName,null);
            Assert.assertNotNull(batchInfo,"未获取到批次信息! 停止后续步骤");
            batchId = batchInfo.getBatchId();

            if (ObjectUtil.isNotEmpty(templateName)) {
                templateId = batchInfo.getTemplateId();
                if (ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
                    List<JSONObject> templates = zxxjCheckMapper.getBatchTemplate(batchId, templateName);
                    Assert.assertNotEquals(templates.size(),0,"templateName对应模板不存在! 停止后续步骤");
                    templateId = templates.get(0).getLong("templateId");
                }

                if (ObjectUtil.isNotEmpty(checkItemName)) {
                    JSONObject item = zxxjCheckMapper.getCheckItem(checkItemName, templateId);
                    Assert.assertNotNull(item,"checkItemName对应检查项不存在! 停止后续步骤");
                    checkItemId = item.getLong("checkItemId");
                    resetBatchScore(batchId,templateId, Arrays.asList(checkItemId));
                }else {
                    resetBatchScore(batchId,templateId, null);
                }
            }else {
                resetBatchScore(batchId,null, null);
            }
        }
    }

    //重置指定批次-模板 查验状态为未关闭-0
    @Override
    public void resetBatch(String batchName,String templateName){
        Long batchId;
        Long templateId;

        if (ObjectUtil.isNotEmpty(batchName)) {
            ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(batchName,null);
            Assert.assertNotNull(batchInfo,"未获取到批次信息! 停止后续步骤");
            batchId = batchInfo.getBatchId();

            if (ObjectUtil.isNotEmpty(templateName)) {
                if (ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
                    List<JSONObject> templates = zxxjCheckMapper.getBatchTemplate(batchId, templateName);
                    Assert.assertNotEquals(templates.size(),0,"templateName对应模板不存在! 停止后续步骤");
                    templateId = templates.get(0).getLong("templateId");

                    resetBatch(batchId,templateId);
                }
            }else {
                resetBatch(batchId,null);
            }
        }
    }

    //单次打分:指定批次-模板-检查项-条件名称进行打分 匹配规则:除条件名称 其余需全匹配
    @Override
    public void testScoreItem(String batchName,String templateName,String checkItemName,String itemName){
        List<Long> batchUserId = processV2Mapper.getBatchUserId("NJJYZYYXGS.NJJKQ36M.1Q", RoleTypeEnum.ManageUser.getRoleCode(),"XX.XXXXXXX.GCJC");
        log.info(JSONObject.toJSONString(batchUserId));

        ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(batchName,null);
        Assert.assertNotNull(batchInfo,"未获取到批次信息! 停止后续步骤");
        Long batchId = batchInfo.getBatchId();
        Long templateId = batchInfo.getTemplateId();
        if (ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
            List<JSONObject> templates = zxxjCheckMapper.getBatchTemplate(batchId, templateName);
            Assert.assertNotEquals(templates.size(),0,"templateName对应模板不存在! 停止后续步骤");
            templateId = templates.get(0).getLong("templateId");
        }
        JSONObject item = zxxjCheckMapper.getCheckItem(checkItemName, templateId);
        Assert.assertNotNull(item,"checkItemName对应检查项不存在! 停止后续步骤");
        Long checkItemId = item.getLong("checkItemId");
        log.info("检查项id[{}],模板id[{}],批次id[{}]",checkItemId,templateId,batchId);
        JSONObject checkItem = zxxjCheckMapper.getTargetLastCheckList(batchId, templateId, checkItemId, null).get(0);

        CheckItemScoreVo checkItemScoreVo = setDefaultScoreVo(checkItem,itemName);
        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_SUBMIT_SCORE), getBatchUserToken(batchId,1), JSONObject.toJSONString(checkItemScoreVo));
        Assertion.verifyEquals(JsonPath.read(rs,"$.message"),ResponseEnum.SUCCESS.getMessage(),"打分失败! 提交参数: " + JSONObject.toJSONString(checkItemScoreVo));
    }

    //单次打分&断言   匹配规则:除条件名称 其余需全匹配
    @Override
    public void testAssertScoreItem(String batchName,String templateName,String checkItemName,String itemName){

        ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(batchName,null);
        Assert.assertNotNull(batchInfo,"未获取到批次信息! 停止后续步骤");
        Long batchId = batchInfo.getBatchId();
        Long templateId = batchInfo.getTemplateId();
        if (ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
            List<JSONObject> templates = zxxjCheckMapper.getBatchTemplate(batchId, templateName);
            Assert.assertNotEquals(templates.size(),0,"templateName对应模板不存在! 停止后续步骤");
            templateId = templates.get(0).getLong("templateId");
        }
        JSONObject item = zxxjCheckMapper.getCheckItem(checkItemName, templateId);
        Assert.assertNotNull(item,"checkItemName对应检查项不存在! 停止后续步骤");
        Long checkItemId = item.getLong("checkItemId");
        log.info("检查项id[{}],模板id[{}],批次id[{}]",checkItemId,templateId,batchId);
        JSONObject checkItem = zxxjCheckMapper.getTargetLastCheckList(batchId, templateId, checkItemId, null).get(0);

        CheckItemScoreVo checkItemScoreVo = setDefaultScoreVo(checkItem,itemName);
        List<AssertItemScore> beforeItemScore = getAssertFamilyItemScore(batchId,templateId,checkItemId);
        List<AssertTemplateScore> beforeTemplateScore = zxxjCheckMapper.getTemplateScore(batchId,null);
        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_SUBMIT_SCORE), getBatchUserToken(batchId,1), JSONObject.toJSONString(checkItemScoreVo));
        Assertion.verifyEquals(JsonPath.read(rs,"$.message"), ResponseEnum.SUCCESS.getMessage(),"打分失败! 提交参数: " + JSONObject.toJSONString(checkItemScoreVo));
        List<AssertTemplateScore> actualTemplateScores = zxxjCheckMapper.getTemplateScore(batchId, null);
        assertCommonItemScore(checkItemScoreVo,beforeItemScore);
        List<AssertTemplateScore> expectScores = assertCommonTemplateScore(checkItemScoreVo, beforeTemplateScore);
        assertCommonBatchScore(checkItemScoreVo,expectScores,actualTemplateScores);
    }

    //批量打分&断言 指定批次-模板-检查项 中已打分/未打分的检查项进行打分  匹配规则:1、名称全匹配 2、批次可不带入,打分范围即为多个批次的同一种模板或检查项
    @Override
    public void testAssertScoreBatch(String batchName,String templateName,String checkItemName,Boolean hasBeenScored){
        Long batchId = null;
        Long templateId = null;
        Long checkItemId = null;
        if (ObjectUtil.isNotEmpty(batchName)) {
            batchId = findBatchIdByName(batchName);
        }
        if (ObjectUtil.isNotEmpty(templateName)) {
            templateId = zxxjCheckMapper.getTemplateId(templateName);
        }
        if (ObjectUtil.isNotEmpty(checkItemId)) {
            JSONObject item = zxxjCheckMapper.getCheckItem(checkItemName, templateId);
            checkItemId = item.getLong("checkItemId");
        }

        List<JSONObject> targetLastCheckList = filterCheckItem(zxxjCheckMapper.getTargetLastCheckList(batchId, templateId, checkItemId,hasBeenScored));

        log.info("匹配到[{}]个检查项",targetLastCheckList.size());
        if (CollectionUtils.isNotEmpty(targetLastCheckList)) {
            Map<String,String> header_check = getBatchUserToken(batchId,1);
            targetLastCheckList.forEach(i -> {
                log.info("检查项id[{}],模板id[{}],批次id[{}]",i.getLong("checkItemId"),i.getLong("templateId"),i.getLong("batchId"));
                CheckItemScoreVo checkItemScoreVo = setDefaultScoreVo(i,null);
                List<AssertItemScore> beforeScore = getAssertFamilyItemScore(i.getLong("batchId"),i.getLong("templateId"),i.getLong("checkItemId"));
                List<AssertTemplateScore> beforeTemplateScore = zxxjCheckMapper.getTemplateScore(i.getLong("batchId"),null);
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_SUBMIT_SCORE), header_check, JSONObject.toJSONString(checkItemScoreVo));
                Assertion.verifyEquals(JsonPath.read(rs,"$.message"),ResponseEnum.SUCCESS.getMessage(),"打分失败! 提交参数: " + JSONObject.toJSONString(checkItemScoreVo));
                List<AssertTemplateScore> actualTemplateScores = zxxjCheckMapper.getTemplateScore(i.getLong("batchId"), null);
                assertCommonItemScore(checkItemScoreVo,beforeScore);
                List<AssertTemplateScore> expectScores = assertCommonTemplateScore(checkItemScoreVo, beforeTemplateScore);
                assertCommonBatchScore(checkItemScoreVo,expectScores,actualTemplateScores);
            });
        }else {
            log.info("未匹配到符合条件的检查项! 停止后续步骤 批次Id:[{}]",batchId);
        }
    }

    //批量打分 指定批次-模板-检查项 中已打分/未打分的检查项进行打分 匹配规则:1、名称全匹配 2、批次可不带入,打分范围即为多个批次的同一种模板或检查项"
    @Override
    public void testScoreBatch(String batchName,String templateName,String checkItemName,Boolean hasBeenScored){
        Long batchId = null;
        Long templateId = null;
        Long checkItemId = null;
        if (ObjectUtil.isNotEmpty(batchName)) {
            batchId = findBatchIdByName(batchName);
        }
        if (ObjectUtil.isNotEmpty(templateName)) {
            templateId = zxxjCheckMapper.getTemplateId(templateName);
        }
        if (ObjectUtil.isNotEmpty(checkItemId)) {
            JSONObject item = zxxjCheckMapper.getCheckItem(checkItemName, templateId);
            checkItemId = item.getLong("checkItemId");
        }

        List<JSONObject> targetLastCheckList = filterCheckItem(zxxjCheckMapper.getTargetLastCheckList(batchId, templateId, checkItemId,hasBeenScored));

        log.info("匹配到[{}]个检查项",targetLastCheckList.size());
        if (CollectionUtils.isNotEmpty(targetLastCheckList)) {
            Map<String,String> header_check = getBatchUserToken(batchId,1);
            targetLastCheckList.forEach(i -> {
                log.info("检查项id[{}],模板id[{}],批次id[{}]",i.getLong("checkItemId"),i.getLong("templateId"),i.getLong("batchId"));
                CheckItemScoreVo checkItemScoreVo = setDefaultScoreVo(i,null);
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_SUBMIT_SCORE), header_check, JSONObject.toJSONString(checkItemScoreVo));
                Assertion.verifyEquals(JsonPath.read(rs,"$.message"),ResponseEnum.SUCCESS.getMessage(),"打分失败! 提交参数: " + JSONObject.toJSONString(checkItemScoreVo));
            });

        }else {
            log.info("未匹配到符合条件的检查项! 停止后续步骤 批次Id:[{}]",batchId);
        }
    }

    /**
     * 获取批次某业务人员token
     * @param batchId
     * @param identity 身份 1-检查人 2-抄送人
     * @return
     */
    public Map<String,String> getBatchUserToken(Long batchId,Integer identity){
        List<JSONObject> checkUserList = zxxjCheckMapper.getBatchUserInfo(batchId, identity);
        Long userId = checkUserList.get(RandomUtil.randomInt(checkUserList.size())).getLong("userId");
        JSONObject user = ucMapper.getTestUserById(userId);
        Assert.assertNotNull(user,"测试账号不存在!");
        return  user.getString("source").equals("SUPPLIER") ?
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getString("phone"),user.getString("password"),"UAT")) :
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getString("userName"),user.getString("password"),"UAT"));
    }

    /**
     * 统一断言--1、检查项及其父级分数
     * @param checkItemScoreVo 打分VO
     * @param beforeScores 打分前的父级路径所有分数
     */
    public void assertCommonItemScore(CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores) {

        Long batchId = checkItemScoreVo.getBatchId();
        Long templateId = checkItemScoreVo.getTemplateId();
        Long checkItemId = checkItemScoreVo.getCheckItemId();
        List<AssertItemScore> expectItemList;
        List<AssertItemScore> actualItemList;
        JSONObject targetCheckItem = zxxjCheckMapper.getTargetCheckItem(checkItemId);

        //末级检查项历史得分
        AssertItemScore itemScore = beforeScores.stream().filter(o -> o.getCheckItemId().equals(checkItemId)).findAny().orElse(null);
        int passPointBefore = ObjectUtil.isNotEmpty(itemScore.getPassPoint()) ? itemScore.getPassPoint() : 0;
        int totalPointBefore = ObjectUtil.isNotEmpty(itemScore.getTotalPoint()) ? itemScore.getTotalPoint() : 0;
        double scoreBefore = ObjectUtil.isNotEmpty(itemScore.getScore()) ? itemScore.getScore() : 0;
        double redLineScoreBefore = ObjectUtil.isNotEmpty(itemScore.getRedLineScore()) ? itemScore.getRedLineScore() : 0;

        //1-检查项极其父级分数
        String templateType = zxxjCheckMapper.getTemplateType(templateId);
        switch (ZxxjTemplateTypeEnum.getType(templateType)) {
            //实测实量
            case realMeasure:
                assertItemRealMeasure(targetCheckItem,checkItemScoreVo,beforeScores,passPointBefore,totalPointBefore,scoreBefore);
                break;
            //可打分，加/扣分
            case awardDeduct:
                assertItemAwardDeduct(checkItemScoreVo,beforeScores,scoreBefore);
                break;
            //定档打分
            case decideLevel:
                assertItemDecideLevel(targetCheckItem,checkItemScoreVo,beforeScores,scoreBefore,redLineScoreBefore);
                break;
            //可打分，按“权重+得分率”计算总分
            case weightScore:
                assertItemWeight(checkItemScoreVo,beforeScores,scoreBefore);
                break;
            //无打分，按合格计算点数/计算点总数，计算合格率
            case qualityPoint:
                assertItemQualityPoint(checkItemScoreVo,beforeScores,passPointBefore,totalPointBefore,scoreBefore);
                break;
            //实测实量-自定义加权
            case realMeasureCustom:
                assertItemRealMeasureCustom(targetCheckItem,checkItemScoreVo,beforeScores,passPointBefore,totalPointBefore,scoreBefore);
                break;
            default:
                break;
        }

        expectItemList = beforeScores;
        actualItemList = getAssertFamilyItemScore(batchId, templateId, checkItemId);
        log.info("检查项得分-期望:{}",expectItemList);
        log.info("检查项得分-实际:{}",actualItemList);
        Assert.assertEquals(actualItemList.size(),expectItemList.size(),"期望与实际长度不一致,停止后续步骤!");
        for (AssertItemScore actual : actualItemList) {
            for (AssertItemScore expect : expectItemList) {
                if (actual.getCheckItemId().equals(expect.getCheckItemId())) {
                    Assertion.verifyEquals(actual,expect,"检查项分数 测试不通过!");
                }
            }
        }
    }

    /**
     * 统一断言--2-模板分数
     * @param checkItemScoreVo  打分VO
     * @param beforeTemplateScore 打分前批次下所有模板分数
     * @return 期望批次下模板得分  => 批次关联单项模板时  = 批次期望得分
     */
    public List<AssertTemplateScore> assertCommonTemplateScore(CheckItemScoreVo checkItemScoreVo,List<AssertTemplateScore> beforeTemplateScore) {
        Long batchId = checkItemScoreVo.getBatchId();

        List<JSONObject> scoreLastList = zxxjCheckMapper.getScoreLastList(batchId, checkItemScoreVo.getTemplateId());
        List<JSONObject> templateArithmetic = zxxjCheckMapper.getTemplateArithmetic(checkItemScoreVo.getTemplateId());
        String weightRule = "3"; //权重规则：weightRule：1-一级项权重 2-末级项权重 3-无权重
        String computeMode = "null"; //计算方式：computeMode：1-加权 2-加权平均
        String fullMarkRule = "null"; //满分规则：fullMarkRule：1-二级项满分 2-末级项满分
        for (JSONObject arithmetic : templateArithmetic) {
            if ("weightRule".equals(arithmetic.getString("arithmetic_key"))) {
                weightRule = arithmetic.getString("arithmetic_value");
            }
            if ("computeMode".equals(arithmetic.getString("arithmetic_key"))) {
                computeMode = arithmetic.getString("arithmetic_value");
            }
            if ("fullMarkRule".equals(arithmetic.getString("arithmetic_key"))) {
                computeMode = arithmetic.getString("arithmetic_value");
            }
        }
        Double expectTemplateScore = 0D;

        //模板分数
        switch (ZxxjTemplateTypeEnum.getType(zxxjCheckMapper.getTemplateType(checkItemScoreVo.getTemplateId()))) {
            case realMeasure:
                expectTemplateScore = expectRealMeasureScore(scoreLastList);
                break;
            case realMeasureCustom:
                //取实际计算规则
                String customRuleValue = weightRule.concat("_").concat(computeMode);
                expectTemplateScore = expectRealMeasureCustomScore(scoreLastList,customRuleValue);
                break;
            case qualityPoint:
                expectTemplateScore = expectQualityPointScore(checkItemScoreVo,scoreLastList,weightRule,computeMode);
                break;
            case weightScore:
                //取实际计算规则
                String ruleValue = weightRule.concat("_").concat(fullMarkRule);
                expectTemplateScore = expectWeightScoreScore(scoreLastList,ruleValue);
                break;
            case decideLevel:
                expectTemplateScore = expectDecideLevelScore(scoreLastList);
                break;
            case awardDeduct:
                expectTemplateScore = expectAwardDeductScore(scoreLastList);
                break;
            default:
                break;
        }

        Double finalExpectTemplateScore = expectTemplateScore;
        beforeTemplateScore.forEach(i -> {
            if (i.getTemplateId().equals(checkItemScoreVo.getTemplateId())) {
                i.setScore(finalExpectTemplateScore);
            }
        });

//        List<AssertTemplateScore> expectTemplateScores = beforeTemplateScore;
        List<AssertTemplateScore> actualTemplateScores = zxxjCheckMapper.getTemplateScore(batchId, null);
        log.info("模板得分-期望:{}",beforeTemplateScore);
        log.info("模板得分-实际:{}",actualTemplateScores);
        Assert.assertEquals(actualTemplateScores.size(),beforeTemplateScore.size(),"期望与实际长度不一致,停止后续步骤!");
        for (AssertTemplateScore actual : actualTemplateScores) {
            for (AssertTemplateScore expect : beforeTemplateScore) {
                if (actual.getTemplateId().equals(expect.getTemplateId())) {
                    Assertion.verifyEquals(actual,expect,"模板得分 测试不通过! ");
                }
            }
        }

        return beforeTemplateScore;
    }

    /**
     * 统一断言--3-批次分数
     * @param checkItemScoreVo 打分VO
     * @param expectTemplateScores  期望批次下所有模板得分  => 单项模板时 批次得分=单项模板得分
     * @param actualTemplateScores  实际批次下所有模板得分  => 组合模板时 用于计算批次期望得分
     */
    public void assertCommonBatchScore(CheckItemScoreVo checkItemScoreVo,List<AssertTemplateScore> expectTemplateScores,List<AssertTemplateScore> actualTemplateScores) {
        Long batchId = checkItemScoreVo.getBatchId();
        Double expectBatchScore;
        ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(null, batchId);

        //批次得分
        Long templateId = batchInfo.getTemplateId();
        if (templateId.equals(checkItemScoreVo.getTemplateId())) {
            //如果批次关联的是单项模板  则批次得分=模板得分
            AssertTemplateScore templateScore = expectTemplateScores.stream().filter(o -> o.getTemplateId().equals(checkItemScoreVo.getTemplateId())).findFirst().get();
            if (ObjectUtil.isNotEmpty(templateScore)) {
                expectBatchScore = templateScore.getScore();
            }else {
                expectBatchScore = 0.0;
            }
        }else {
            //批次关联的组合模板
            List<JSONObject> templateArithmetic = zxxjCheckMapper.getTemplateArithmetic(templateId);
            String totalArithmetic = "null"; // 1-加权 2-加权平均
            for (JSONObject arithmetic : templateArithmetic) {
                if ("totalArithmetic".equals(arithmetic.getString("arithmetic_key"))) {
                    totalArithmetic = arithmetic.getString("arithmetic_value");
                }
            }

            //组合模板总分算法：totalArithmetic：1-加权 2-加权平均
            if ("1".equals(totalArithmetic)) {
//                log.info("模板计算规则-1-加权");
                AtomicDouble result = new AtomicDouble(0);
                actualTemplateScores.forEach(score -> result.getAndAdd(ObjectUtil.isNotEmpty(score.getScore()) ? score.getScore() * (score.getWeight() / 100) : 0));
//                actualTemplateScores.forEach(score -> result.getAndAdd(NumberUtil.mul(score.getScore().doubleValue(),NumberUtil.div(score.getWeight().doubleValue(),100))));
                expectBatchScore = NumberUtil.round(result.toString(),4, RoundingMode.HALF_UP).doubleValue();
            } else {
//                log.info("模板计算规则-2-加权平均");
                AtomicDouble weightAverage = new AtomicDouble(0);
                AtomicDouble weight = new AtomicDouble(0);
                actualTemplateScores.forEach(o -> {
                    if (o.getIsWeightAverage() != null && o.getIsWeightAverage()) {
//                        log.info("纳入加权平均[{}]",o.getTemplateId());
                        weightAverage.getAndAdd(ObjectUtil.isNotEmpty(o.getScore()) ? (o.getWeight() / 100 * o.getScore()) : 0);
//                        weightAverage.getAndAdd( o.getWeight() / 100 * o.getScore());
                    } else {
//                        log.info("不纳入加权平均[{}]",o.getTemplateId());
                        weight.getAndAdd(ObjectUtil.isNotEmpty(o.getScore()) ? (o.getWeight() / 100 * o.getScore()) : 0);
//                        weight.getAndAdd(o.getWeight() / 100 * o.getScore());
                    }
                });
                double weightSum = actualTemplateScores.stream().filter(AssertTemplateScore::getIsWeightAverage).mapToDouble(AssertTemplateScore::getWeight).sum();
                Double score = NumberUtil.div(weightAverage.get(), weightSum / 100, 4) + weight.get();
                expectBatchScore = NumberUtil.round(score.toString(),4,RoundingMode.HALF_UP).doubleValue();
            }
        }

        Double actualBatchScore = zxxjCheckMapper.getBatchInfo(null,batchId).getBatchScore();
        log.info("批次得分-期望:{}",expectBatchScore);
        log.info("批次得分-实际:{}",actualBatchScore);
        Assertion.verifyEquals(actualBatchScore,expectBatchScore,"批次分数 测试失败!");
//        Assert.assertEquals(actualBatchScore,expectBatchScore,"批次分数 测试失败! ");
    }

    public Double expectAwardDeductScore(List<JSONObject> scoreLastList) {
        return scoreLastList.stream().mapToDouble(o -> o.getDoubleValue("score")).sum();
    }

    public Double expectDecideLevelScore(List<JSONObject> scoreLastList) {
        AtomicDouble fullScore = new AtomicDouble(0);
        AtomicDouble currentScore = new AtomicDouble(0);
        AtomicDouble redLineScore = new AtomicDouble(0);
        scoreLastList.forEach(o -> {
            DecideLevelVo decideLevel = JSON.parseObject(o.getString("extension"), DecideLevelVo.class);
            Double fullScoreItem = decideLevel.getMarkStandard().getFullScore();
            fullScore.getAndAdd(fullScoreItem);
            currentScore.getAndAdd(fullScoreItem + o.getDouble("score") > 0 ? fullScoreItem + o.getDouble("score") : 0);
            redLineScore.getAndAdd(o.getDouble("redLineScore") != null ? o.getDouble("redLineScore") : 0);
        });
        return NumberUtil.round(NumberUtil.div(currentScore.get(), fullScore.get(), 4, RoundingMode.HALF_UP) - (redLineScore.get() * 0.01),4,RoundingMode.HALF_UP).doubleValue();
    }

    public Double expectWeightScoreScore(List<JSONObject> scoreList,String ruleValue) {
        switch (ruleValue) {
            //一级权重 二级满分
            case "1_1":
                break;
            //一级权重 末级满分
            case "1_2":
                break;
            //末级权重 末级满分
            case "2_2":
                break;
            //无权重 二级满分
            case "3_1":
                break;
            //无权重 末级满分
            case "3_2":
                break;
            default:
                break;
        }
        return null;
    }

    public Double expectQualityPointScore(CheckItemScoreVo checkItemScoreVo,List<JSONObject> scoreLastList,String weightRule,String computeMode) {
        switch (weightRule) {
            //一级权重
            case "1":
                List<JSONObject> scoreFirstList = zxxjCheckMapper.getScoreFirstList(checkItemScoreVo.getBatchId(), checkItemScoreVo.getTemplateId());
                return expectQualityPoint(scoreFirstList,computeMode);
            //末级权重
            case "2":
                return expectQualityPoint(scoreLastList,computeMode);
            //无权重 与实测实量计算方式一样 分数 = 合格点/总点数
            case "3":
                return expectRealMeasureScore(scoreLastList);
            default:
                throw new BusinessException("权重数据异常!");
        }
    }

    public Double expectQualityPoint(List<JSONObject> scoreList,String computeMode) {
        if ("1".equals(computeMode)) {
            //加权
            double scoreSum = scoreList.stream().mapToDouble(o -> {
                return ObjectUtil.isNotEmpty(o.getDouble("score")) ? o.getDouble("score") * o.getJSONObject("extension").getDouble("weight") / 100 : 0;
            }).sum();
            return NumberUtil.round(scoreSum,4,RoundingMode.HALF_UP).doubleValue();
        } else {
            //加权平均
            AtomicDouble weightAverageScore = new AtomicDouble(0d);
            AtomicDouble weightScore = new AtomicDouble(0d);
            AtomicDouble weightAverageSum = new AtomicDouble(0d);
            scoreList.forEach(o -> {
                QualityPointVo qualityPointVo = JSON.parseObject(o.getString("extension"), QualityPointVo.class);
                if (qualityPointVo.getWeightAverage() != null && qualityPointVo.getWeightAverage()) {
                    weightAverageScore.getAndAdd(ObjectUtil.isNotEmpty(o.getDouble("score")) ? o.getDouble("score") * (qualityPointVo.getWeight() / 100) : 0);
                    weightAverageSum.getAndAdd(qualityPointVo.getWeight() / 100);
                } else {
                    weightScore.getAndAdd(ObjectUtil.isNotEmpty(o.getDouble("score")) ? o.getDouble("score") * (qualityPointVo.getWeight() / 100)  : 0);
                }
            });
            return NumberUtil.round(NumberUtil.div(weightAverageScore.get(), weightAverageSum.get(), 4, RoundingMode.HALF_UP) + weightScore.get(),4,RoundingMode.HALF_UP).doubleValue();
        }
    }

    public Double expectRealMeasureCustomScore(List<JSONObject> scoreLastList,String customRuleValue){

        double score;
        //权重规则：weightRule：1-一级项权重 2-末级项权重 3-无权重
        //计算方式：computeMode：1-加权 2-加权平均
        if (StringUtils.equals(customRuleValue, "2_1")) {
            score = scoreLastList.stream().mapToDouble(s -> NumberUtil.mul(s.getDoubleValue("score"), NumberUtil.div(s.getJSONObject("extension").getDoubleValue("weight"), 100))).sum();
        } else if (StringUtils.equals(customRuleValue, "2_2")) {
            AtomicDouble weightAverage = new AtomicDouble(0d);
            AtomicDouble weight = new AtomicDouble(0d);
            scoreLastList.forEach(s -> {
                if (Boolean.TRUE.equals(s.getJSONObject("extension").getBooleanValue("weightAverage"))) {
                    weightAverage.getAndAdd(NumberUtil.mul(s.getDoubleValue("score"), NumberUtil.div(s.getJSONObject("extension").getDoubleValue("weight"), 100)));
                } else {
                    weight.getAndAdd(NumberUtil.mul(s.getDoubleValue("score"), NumberUtil.div(s.getJSONObject("extension").getDoubleValue("weight"), 100)));
                }
            });
            double weightSum = scoreLastList.stream().filter(s -> Boolean.TRUE.equals(s.getJSONObject("extension").getBooleanValue("weightAverage")))
                    .mapToDouble(o -> NumberUtil.div(o.getJSONObject("extension").getDoubleValue("weight"), 100)).sum();
            score = NumberUtil.add(NumberUtil.div(weightAverage.doubleValue(),weightSum,8), weight.get());
        } else {
            score = expectRealMeasureScore(scoreLastList);
        }

        return NumberUtil.round(score,4).doubleValue();
    }

    public Double expectRealMeasureScore(List<JSONObject> scoreLastList){
        double pass = scoreLastList.stream().mapToDouble(o -> o.getIntValue("passPoint")).sum();
        double total = scoreLastList.stream().mapToDouble(o -> o.getIntValue("totalPoint")).sum();
        return NumberUtil.div(pass,total,4,RoundingMode.HALF_UP);
    }

    /**
     * 获取打分目标-及其父级Id的分数
     * @param batchId
     * @param templateId
     * @param checkItemId
     * @return List<AssertItemScore>
     */
    public List<AssertItemScore> getAssertFamilyItemScore(Long batchId,Long templateId,Long checkItemId) {

        List<JSONObject> checkItemList = zxxjCheckMapper.getTemplateCheckList(templateId);
        List<Long> parentIds = queryParentIds(checkItemId, checkItemList);
        parentIds.add(checkItemId);
        return zxxjCheckMapper.getScoreFamily(batchId, templateId, parentIds);
    }

    /**
     * 重置清空批次所有分数
     * @param batchId
     * @param templateId
     * @param checkItemIds
     */
    public void resetBatchScore(Long batchId,Long templateId,List<Long> checkItemIds){
        zxxjCheckMapper.updateBatchScore(batchId);
        zxxjCheckMapper.updateTemplateScore(batchId,templateId);
        zxxjCheckMapper.updateCheckItemScore(batchId,templateId,checkItemIds);
    }

    /**
     * 重置批次及相关模板 查验完毕状态为未关闭状态
     * @param batchId
     * @param templateId
     */
    public void resetBatch(Long batchId,Long templateId){
        zxxjCheckMapper.updateBatch(batchId);
        zxxjCheckMapper.updateTemplate(batchId,templateId);
    }

    /**
     * 获取目标批次Id 全匹配
     * @param batchName
     * @return
     */
    public Long findBatchIdByName(String batchName) {
        ZxxjBatch batchInfo = zxxjCheckMapper.getBatchInfo(batchName,null);
        if (ObjectUtil.isNotEmpty(batchInfo)) {
            return batchInfo.getBatchId();
        }else {
            throw new BusinessException("未获取到目标批次!请检查批次名称");
        }
    }

    /**
     * 筛选出所有可打分的末级项
     * @param targetLastCheckList
     * @return
     */
    public List<JSONObject> filterCheckItem(List<JSONObject> targetLastCheckList) {
        return  targetLastCheckList.stream().filter(i -> {
            String extension = i.getString("extension");
            return extension.contains("checkGuide") || extension.contains("limit") || extension.contains("areaCount");
        }).collect(Collectors.toList());
    }

    /**
     * 自动识别模板类型并设置测量数据(数据在限制范围(MIN,MAX)内随机)
     * @param checkItem
     * @param itemName 实测实量类型模板 存在多条件 模糊匹配 null->随机取一
     * @return
     */
    public CheckItemScoreVo setDefaultScoreVo(@NonNull JSONObject checkItem, String itemName) {
        String templateType = zxxjCheckMapper.getTemplateType(checkItem.getLong("templateId"));
        ZxxjTemplateTypeEnum templateTypeEnum = ZxxjTemplateTypeEnum.getType(templateType);
        log.info("模板类型为:[{}][{}]",templateTypeEnum.getDesc(),templateTypeEnum.getCode());
        switch (templateTypeEnum) {
            case realMeasure:
                return realMeasureScore(checkItem,itemName,null,null);
            case realMeasureCustom:
                return realMeasureCustomScore(checkItem,itemName,null,null);
            case qualityPoint:
                return qualityPointScore(checkItem,null);
            case weightScore:
                return weightScore(checkItem,null,null,null);
            case awardDeduct:
                return awardDeductScore(checkItem,null);
            case decideLevel:
                return decideLevelScore(checkItem,null);
            default:
                throw new BusinessException("模板类型zxxj_template表type字段数据错误,请检查!");
        }
    }

    /**
     * 无打分，按合格计算点数/计算点总数，计算合格率-提交参数封装
     * @param checkItem
     * @param testNumbers 测量值 二维数组testNumbers[测区数][每测区点数]
     * @return
     */
    public CheckItemScoreVo qualityPointScore(JSONObject checkItem,boolean[][] testNumbers){
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        QualityPointVo qualityPointVo = JSON.parseObject(checkItem.getString("extension"), QualityPointVo.class);

        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);
        int pointCount = qualityPointVo.getAreaCount().getCount();
        int areaCount = qualityPointVo.getAreaCount().getNums();

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();

            if (ObjectUtil.isNotEmpty(testNumbers) && testNumbers.length != areaCount) {
                throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
            }

            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {

                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                boolean value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    value = hasSetValue ? testNumbers[i][j] : RandomUtil.randomBoolean();
                }else {
                    value = RandomUtil.randomBoolean();
                }
                score.setIsQualified(value);
                scores.add(score);
            }
            area.setScores(scores);
            areas.add(area);
        }
        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    /**
     * 可打分，按“权重+得分率”计算总分-提交参数封装
     * @param checkItem
     * @param isDefaultDeduct 是否默认扣分
     * @param deductScore 扣分值（格式:负数）
     * @return
     */
    public CheckItemScoreVo weightScore(JSONObject checkItem,Boolean isQualified,Boolean isDefaultDeduct, Double deductScore) {
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        List<CheckItemScoreVo.Score> scores = new ArrayList<>();
        CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
        WeightScoreVo weightScoreVo = JSON.parseObject(checkItem.getString("extension"), WeightScoreVo.class);
        double value;
        boolean isQualifiedTemp = RandomUtil.randomBoolean();

        //是否指定合格
        if (ObjectUtil.isNotEmpty(isQualified)) {
            if (isQualified) {
                value = 0;
                score.setIsQualified(true);
            }else {
                //是否按默认扣分值扣分
                if (ObjectUtil.isNotEmpty(isDefaultDeduct)) {
                    if (isDefaultDeduct) {
                        value = 0 - weightScoreVo.getProblemDeduct();
                    }else {
                        value = ObjectUtil.isNotEmpty(deductScore) ? deductScore : RandomUtil.randomDouble(0- weightScoreVo.getDeductLimit(),0,2,RoundingMode.HALF_UP);
                    }
                }else {
                    value = ObjectUtil.isNotEmpty(deductScore) ? deductScore : RandomUtil.randomDouble(0- weightScoreVo.getDeductLimit(),0,2,RoundingMode.HALF_UP);
                }
            }
        }else {
            //若不指定合格 则随机 合格 或 扣分
            if (isQualifiedTemp) {
                value = 0;
                score.setIsQualified(true);
            }else {
                //是否按默认扣分值扣分
                if (ObjectUtil.isNotEmpty(isDefaultDeduct)) {
                    if (isDefaultDeduct) {
                        value = 0 - weightScoreVo.getProblemDeduct();
                    }else {
                        value = ObjectUtil.isNotEmpty(deductScore) ? deductScore : RandomUtil.randomDouble(0- weightScoreVo.getDeductLimit(),0,2,RoundingMode.HALF_UP);
                    }
                }else {
                    value = ObjectUtil.isNotEmpty(deductScore) ? deductScore : RandomUtil.randomDouble(0- weightScoreVo.getDeductLimit(),0,2,RoundingMode.HALF_UP);
                }
            }
        }

        score.setValue(value);
        scores.add(score);
        areas.add(CheckItemScoreVo.Area.builder().scores(scores).build());
        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    /**
     * 可打分，加/扣分-提交参数封装
     * @param checkItem
     * @param score 加扣分值(格式 均为正数)
     * @return
     */
    public CheckItemScoreVo awardDeductScore(JSONObject checkItem,Double score){
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        List<CheckItemScoreVo.Score> scores = new ArrayList<>();
        AwardDeductVo awardDeductVo = JSON.parseObject(checkItem.getString("extension"), AwardDeductVo.class);

        //是否为扣分
        final boolean[] ifDeduct = {false};
        List<JSONObject> templateArithmetic = zxxjCheckMapper.getTemplateArithmetic(checkItem.getLong("templateId"));
        if (CollectionUtils.isNotEmpty(templateArithmetic)) {
            templateArithmetic.forEach(o -> {
                if (o.getString("arithmetic_key").equals("computeRule")) {
                    ifDeduct[0] = o.getIntValue("arithmetic_value") == 1;
                }
            });
        }else {
            throw new BusinessException("模板算法zxxj_template_arithmetic表数据异常!请检查");
        }

        double value = ObjectUtil.isNotEmpty(score) ? score : RandomUtil.randomDouble(0, awardDeductVo.getLimit(),2, RoundingMode.HALF_UP);

        if (ifDeduct[0]) {
            scores.add(CheckItemScoreVo.Score.builder().value(MathUtils.convertNeg(value)).build());
        }else {
            scores.add(CheckItemScoreVo.Score.builder().value(value).build());
        }

        areas.add(CheckItemScoreVo.Area.builder().scores(scores).build());
        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    /**
     * 定档打分-提交参数封装
     * @param checkItem
     * @param isQualified 是否指定合格
     * @return
     */
    public CheckItemScoreVo decideLevelScore(JSONObject checkItem,Boolean isQualified){
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();

        DecideLevelVo decideLevelVo = JSON.parseObject(checkItem.getString("extension"), DecideLevelVo.class);
        boolean isQualifiedTemp = RandomUtil.randomBoolean();

        if (ObjectUtil.isNotEmpty(isQualified)) {
            if (isQualified) {
                //合格
                List<CheckItemScoreVo.Score> scores = new ArrayList<>();
                scores.add(CheckItemScoreVo.Score.builder().isQualified(true).value(0D).build());
                areas.add(CheckItemScoreVo.Area.builder().scores(scores).build());
            }else {
                //不合格 即 扣分
                decideLevelAreas(decideLevelVo,areas);
            }
        }else {
            if (isQualifiedTemp) {
                //合格
                List<CheckItemScoreVo.Score> scores = new ArrayList<>();
                scores.add(CheckItemScoreVo.Score.builder().isQualified(true).value(0D).build());
                areas.add(CheckItemScoreVo.Area.builder().scores(scores).build());
            }else {
                //扣分
                decideLevelAreas(decideLevelVo,areas);
            }
        }

        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    public void decideLevelAreas(DecideLevelVo decideLevelVo, List<CheckItemScoreVo.Area> areas){
        int areaCount = decideLevelVo.getAreaCount();
        List<DecideLevelVo.DeductRule> deductRules = decideLevelVo.getMarkStandard().getDeductRules();
        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();
            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            DecideLevelVo.DeductRule deductRule = deductRules.get(RandomUtil.randomInt(deductRules.size()));
            scores.add(CheckItemScoreVo.Score
                    .builder()
                    .mark(deductRule.getMark())
                    .value(deductRule.getScore()).build());
            area.setScores(scores);
            areas.add(area);
        }
    }

    /**
     * 实测实量-提交参数封装
     * @param checkItem
     * @param itemName 条件名称 实测实量存在多条件
     * @param testNumbers 非观感测量值 二维数组testNumbers[测区数][每测区点数]
     * @param lookFeelNumbers 观感测量值 二维数组testNumbers[测区数][每测区点数]
     * @return
     */
    public CheckItemScoreVo realMeasureScore(JSONObject checkItem,String itemName,double[][] testNumbers,boolean[][] lookFeelNumbers) {
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();

        List<RealMeasureVo> realMeasureList = JSON.parseArray(checkItem.getString("extension"), RealMeasureVo.class);
        RealMeasureVo targetRealMeasureVo;
        if (ObjectUtil.isNotEmpty(itemName)) {
            List<RealMeasureVo> targetList = realMeasureList.stream().filter(o -> o.getName().contains(itemName)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetList)) {
                targetRealMeasureVo = targetList.get(RandomUtil.randomInt(targetList.size()));
                log.info("指定条件名[{}] 匹配到条件[{}]",itemName, targetRealMeasureVo.getName());
            }else {
                targetRealMeasureVo = realMeasureList.get(RandomUtil.randomInt(realMeasureList.size()));
                log.info("未匹配到条件[{}]! ,随机选取条件:[{}]",itemName, targetRealMeasureVo.getName());
            }
        }else {
            targetRealMeasureVo = realMeasureList.get(RandomUtil.randomInt(realMeasureList.size()));
            log.info("未指定itemName条件 ,随机选取条件:[{}]", targetRealMeasureVo.getName());
        }
        log.info("爆板值[{}]", ObjectUtil.isNotEmpty(targetRealMeasureVo.getBlastPlate()) ? targetRealMeasureVo.getBlastPlate() : "无");
        checkItemScoreVo.setRealMeasureName(targetRealMeasureVo.getName());
        switch (targetRealMeasureVo.getPassArithmetic()) {
            //合格率计算方法 1-标准 2-标准+设计值 3-极差 4-偏差 5-观感
            case 1:
                //标准
                areas = setStandardAreas(targetRealMeasureVo,testNumbers);
                break;
            case 2:
                //标准&设计
                areas = setDesignAndStandardAreas(targetRealMeasureVo,testNumbers);
                break;
            case 3:
                //极差
                areas = setRangeAreas(targetRealMeasureVo,testNumbers);
                break;
            case 4:
                //偏差
                areas = setDeviationAreas(targetRealMeasureVo,testNumbers);
                break;
            case 5:
                //观感
                areas = setLookFeelAreas(targetRealMeasureVo,lookFeelNumbers);
                break;
            default:
                break;
        }
        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    /**
     * 实测实量（自定义权重）-提交参数封装
     * @param checkItem
     * @param itemName 条件名称 实测实量存在多条件
     * @param testNumbers 非观感测量值 二维数组testNumbers[测区数][每测区点数]
     * @param lookFeelNumbers 观感测量值 二维数组testNumbers[测区数][每测区点数]
     * @return
     */
    public CheckItemScoreVo realMeasureCustomScore(JSONObject checkItem,String itemName,double[][] testNumbers,boolean[][] lookFeelNumbers) {
        CheckItemScoreVo checkItemScoreVo = new CheckItemScoreVo();
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();

        RealMeasureCustomVo realMeasureCustomVo = JSON.parseObject(checkItem.getString("extension"), RealMeasureCustomVo.class);
        List<RealMeasureVo> realMeasureList = realMeasureCustomVo.getConditions();
        RealMeasureVo targetRealMeasure;
        if (ObjectUtil.isNotEmpty(itemName)) {
            List<RealMeasureVo> targetList = realMeasureList.stream().filter(o -> o.getName().contains(itemName)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(targetList)) {
                targetRealMeasure = targetList.get(RandomUtil.randomInt(targetList.size()));
                log.info("指定条件名[{}] 匹配到条件[{}]",itemName,targetRealMeasure.getName());
            }else {
                targetRealMeasure = realMeasureList.get(RandomUtil.randomInt(realMeasureList.size()));
                log.info("未匹配到条件[{}]! ,随机选取条件:[{}]",itemName,targetRealMeasure.getName());
            }
        }else {
            targetRealMeasure = realMeasureList.get(RandomUtil.randomInt(realMeasureList.size()));
            log.info("未指定itemName条件 ,随机选取条件:[{}]",targetRealMeasure.getName());
        }
        log.info("爆板值[{}]", ObjectUtil.isNotEmpty(targetRealMeasure.getBlastPlate()) ? targetRealMeasure.getBlastPlate() : "无");
        checkItemScoreVo.setRealMeasureName(targetRealMeasure.getName());
        switch (targetRealMeasure.getPassArithmetic()) {
            //合格率计算方法 1-标准 2-标准+设计值 3-极差 4-偏差 5-观感 6-直接录合格点数与总点数
            case 1:
                //标准
                areas = setStandardAreas(targetRealMeasure,testNumbers);
                break;
            case 2:
                //标准&设计
                areas = setDesignAndStandardAreas(targetRealMeasure,testNumbers);
                break;
            case 3:
                //极差
                areas = setRangeAreas(targetRealMeasure,testNumbers);
                break;
            case 4:
                //偏差
                areas = setDeviationAreas(targetRealMeasure,testNumbers);
                break;
            case 5:
                //观感
                areas = setLookFeelAreas(targetRealMeasure,lookFeelNumbers);
                break;
            case 6:
                areas = setImpressionAreas(targetRealMeasure,testNumbers);
                //6-直接录合格点数与总点数
                break;
            default:
                break;
        }
        checkItemScoreVo.setAreas(areas);
        checkItemScoreVo.setCheckItemId(checkItem.getLong("checkItemId"));
        checkItemScoreVo.setTemplateId(checkItem.getLong("templateId"));
        checkItemScoreVo.setBatchId(checkItem.getLong("batchId"));
        return checkItemScoreVo;
    }

    /**
     * 实测实量-标准
     * @param targetRealMeasureVo
     * @param testNumbers  二维数组[i][j]为测量值
     * @return
     */
    public List<CheckItemScoreVo.Area> setStandardAreas(RealMeasureVo targetRealMeasureVo, double[][] testNumbers){
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);
        int pointCount = targetRealMeasureVo.getAreaCount().getCount();
        int areaCount = targetRealMeasureVo.getAreaCount().getNums();

        Double exitCritical = ObjectUtil.isEmpty(targetRealMeasureVo.getBlastPlate()) ? null : targetRealMeasureVo.getBlastPlate();

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();

            if (ObjectUtil.isNotEmpty(testNumbers) && testNumbers.length != areaCount) {
                throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
            }

            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {

                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                double value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    value = hasSetValue ? testNumbers[i][j] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }else {
                    value = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }

                score.setValue(value);
                if (ObjectUtil.isEmpty(exitCritical)) {
                    score.setIsCritical(false);
                }else {
                    score.setIsCritical(value >= exitCritical);
                }
                scores.add(score);
            }
            area.setScores(scores);
            areas.add(area);
        }

        return areas;
    }

    /**
     * 实测实量-标准&设计
     * @param targetRealMeasureVo
     * @param testNumbers 二维数组[i][0]为设计值 , [i][j]为测量值
     * @return
     */
    public List<CheckItemScoreVo.Area> setDesignAndStandardAreas(RealMeasureVo targetRealMeasureVo, double[][] testNumbers){
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);

        int pointCount = targetRealMeasureVo.getAreaCount().getCount();
        int areaCount = targetRealMeasureVo.getAreaCount().getNums();
        boolean exitCritical = !ObjectUtil.isEmpty(targetRealMeasureVo.getBlastPlate());

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();
            //每测区设计值
            double designValue;
            if (ObjectUtil.isNotEmpty(testNumbers)) {
                if (testNumbers.length != areaCount) {
                    throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
                }
                designValue = hasSetValue && testNumbers.length == areaCount ? testNumbers[i][0] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE,2,RoundingMode.HALF_UP);
            }else {
                designValue = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE,2,RoundingMode.HALF_UP);
            }

            area.setDesignValue(designValue);
            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {

                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                double value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount+1) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    value = hasSetValue && testNumbers[i].length == pointCount+1 ? testNumbers[i][j+1] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }else {
                    value = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }
                score.setValue(value);
                if (!exitCritical) {
                    score.setIsCritical(false);
                }else {
                    score.setIsCritical((value - designValue) >= targetRealMeasureVo.getBlastPlate());
                }
                scores.add(score);
            }
            area.setScores(scores);
            areas.add(area);
        }

        return areas;
    }

    /**
     * 实测实量-观感
     * @param targetRealMeasureVo
     * @param testNumbers
     * @return
     */
    public List<CheckItemScoreVo.Area> setLookFeelAreas(RealMeasureVo targetRealMeasureVo, boolean[][] testNumbers) {
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);
        int pointCount = targetRealMeasureVo.getAreaCount().getCount();
        int areaCount = targetRealMeasureVo.getAreaCount().getNums();

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();

            if (ObjectUtil.isNotEmpty(testNumbers) && testNumbers.length != areaCount) {
                throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
            }

            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {

                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                boolean value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    value = hasSetValue ? testNumbers[i][j] : RandomUtil.randomBoolean();
                }else {
                    value = RandomUtil.randomBoolean();
                }

                score.setIsQualified(value);
                scores.add(score);
            }
            area.setScores(scores);
            areas.add(area);
        }

        return areas;
    }

    /**
     * 实测实量-极差
     * @param targetRealMeasureVo
     * @param testNumbers
     * @return
     */
    public List<CheckItemScoreVo.Area> setRangeAreas(RealMeasureVo targetRealMeasureVo, double[][] testNumbers){
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);
        int pointCount = targetRealMeasureVo.getAreaCount().getCount();
        int areaCount = targetRealMeasureVo.getAreaCount().getNums();
        boolean exitCritical = !ObjectUtil.isEmpty(targetRealMeasureVo.getBlastPlate());

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();

            if (ObjectUtil.isNotEmpty(testNumbers) && testNumbers.length != areaCount) {
                throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
            }

            double[] pointNums = new double[pointCount];
            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {
                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                double value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    double minInArray = MathUtils.minInArray(testNumbers[i]);
                    value = hasSetValue ? testNumbers[i][j] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                    //是否爆板
                    score.setIsCritical(exitCritical && (value - minInArray > targetRealMeasureVo.getBlastPlate()));
                }else {
                    value = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                    pointNums[j] = value;  //先设置测量值,算出极差值 再计算是否爆板
                }
                score.setValue(value);
                scores.add(score);
            }

            if (!hasSetValue) {
                double minInArray = MathUtils.minInArray(pointNums);
                scores.forEach(x -> {
                    x.setIsCritical(exitCritical && (x.getValue() - minInArray > targetRealMeasureVo.getBlastPlate()));
                });
            }

            area.setScores(scores);
            areas.add(area);
        }

        return areas;
    }

    /**
     * 实测实量-偏差
     * @param targetRealMeasureVo
     * @param testNumbers 二维数组[i][0]为设计值 , [i][j]为测量值
     * @return
     */
    public List<CheckItemScoreVo.Area> setDeviationAreas(RealMeasureVo targetRealMeasureVo, double[][] testNumbers){
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        boolean hasSetValue = ArrayUtil.isNotEmpty(testNumbers);

        int pointCount = targetRealMeasureVo.getAreaCount().getCount();
        int areaCount = targetRealMeasureVo.getAreaCount().getNums();

        boolean exitCritical = !ObjectUtil.isEmpty(targetRealMeasureVo.getBlastPlate());

        for (int i = 0; i < areaCount; i++) {
            CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();

            //每测区设计值
            double designValue;
            if (ObjectUtil.isNotEmpty(testNumbers)) {
                if (testNumbers.length != areaCount) {
                    throw new BusinessException("测量值testNumbers数组长度不等于测区数,请检查");
                }
                designValue = hasSetValue && testNumbers.length == areaCount ? testNumbers[i][0] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE,2,RoundingMode.HALF_UP);
            }else {
                designValue = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE,2,RoundingMode.HALF_UP);
            }

            area.setDesignValue(designValue);
            List<CheckItemScoreVo.Score> scores = new ArrayList<>();
            for (int j = 0; j < pointCount; j++) {

                CheckItemScoreVo.Score score = new CheckItemScoreVo.Score();
                double value;
                if (ObjectUtil.isNotEmpty(testNumbers)) {
                    if (testNumbers[i].length != pointCount+1) {
                        throw new BusinessException("测量值testNumbers[i]数组长度不等于测点数,请检查");
                    }
                    value = hasSetValue && testNumbers[i].length == pointCount+1 ? testNumbers[i][j+1] : RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }else {
                    value = RandomUtil.randomDouble(MIN_VALUE, MAX_VALUE, 2, RoundingMode.HALF_UP);
                }
                score.setValue(value);
                if (!exitCritical) {
                    score.setIsCritical(false);
                }else {
                    score.setIsCritical(MathUtils.convertPos((value - designValue)) >= targetRealMeasureVo.getBlastPlate());
                }
                scores.add(score);
            }
            area.setScores(scores);
            areas.add(area);
        }

        return areas;
    }

    /**
     * 实测实量-直接录合格点数与总点数
     * @param targetRealMeasureVo
     * @param testNumbers {{合格数,总数}}  二维数组testNumbers长度必须=1 testNumbers[0]长度必须=2
     * @return
     */
    public List<CheckItemScoreVo.Area> setImpressionAreas(RealMeasureVo targetRealMeasureVo, double[][] testNumbers) {
        List<CheckItemScoreVo.Area> areas = new ArrayList<>();
        CheckItemScoreVo.Area area = new CheckItemScoreVo.Area();
        List<CheckItemScoreVo.Score> scores = new ArrayList<>();
        CheckItemScoreVo.Score score_success = new CheckItemScoreVo.Score();
        CheckItemScoreVo.Score score_total = new CheckItemScoreVo.Score();

        if (ObjectUtil.isNotEmpty(testNumbers)) {
            if (testNumbers.length !=1 || testNumbers[0].length != 2) {
                throw new BusinessException("测量值testNumbers数组长度不等于1或testNumbers[0]数组长度不等于2,请检查");
            }
            score_success.setValue(testNumbers[0][0]);
            score_total.setValue(testNumbers[0][1]);
        }else {
            int randomInt = RandomUtil.randomInt((int) MAX_VALUE);
            score_success.setValue((double) randomInt);
            score_total.setValue((double) (randomInt + RandomUtil.randomInt((int) MAX_VALUE)));
        }
        scores.add(score_success);
        scores.add(score_total);
        area.setScores(scores);
        areas.add(area);
        return areas;
    }

    /**
     * 获取检查项id的所有父级Id直到根节点id
     * @param treeId 目标检查项id
     * @param trees 所有检查项
     * @return
     */
    public List<Long> queryParentIds(Long treeId, List<JSONObject> trees) {
        //递归获取父级ids,不包含自己
        List<Long> parentIds = new ArrayList<>();
        treeOrgParent(trees, treeId, parentIds);
        return parentIds;
    }

    /**
     * 递归遍历获取检查项家族（id-parentId-parentId-。。。）id
     * @param trees 所有检查项
     * @param treeId 当前检查项id
     * @param parentIds 父级id List
     */
    public void treeOrgParent(List<JSONObject> trees, Long treeId, List<Long> parentIds) {
        for (JSONObject tree : trees) {
            if (null == tree.getLong("parentId")) {
                continue;
            }
            //判断是否有父节点
            if (treeId.equals(tree.getLong("checkItemId"))) {
                parentIds.add(tree.getLong("parentId"));
                treeOrgParent(trees, tree.getLong("parentId"), parentIds);
            }
        }
    }

    public void assertItemRealMeasureCustom(JSONObject targetCheckItem,CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,int passPointBefore,int totalPointBefore,double scoreBefore){
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger total = new AtomicInteger();
        RealMeasureCustomVo realMeasureCustomVo = JSON.parseObject(targetCheckItem.getString("extension"), RealMeasureCustomVo.class);
        List<RealMeasureVo> realMeasureList = realMeasureCustomVo.getConditions();
        //找到打分的条件
        RealMeasureVo realMeasureVo = realMeasureList.stream().filter(o -> o.getName().equals(checkItemScoreVo.getRealMeasureName())).findAny().orElse(null);
        if (ObjectUtil.isNotEmpty(realMeasureVo)) {
            switch (realMeasureVo.getPassArithmetic()) {
                //1-标准
                case 1:
                    setStandardExpect(realMeasureVo,areas,success,total);
                    break;
                //2-标准+设计值
                case 2:
                    setDesignAndStandardExpect(realMeasureVo,areas,success,total);
                    break;
                //3-极差
                case 3:
                    setRangeExpect(realMeasureVo,areas,success,total);
                    break;
                //4-偏差
                case 4:
                    setDeviationExpect(realMeasureVo,areas,success,total);
                    break;
                //5-观感
                case 5:
                    setLookFeelExpect(realMeasureVo,areas,success,total);
                    break;
                //6-直接录合格点数与总点数
                case 6:
                    setImpressionExpect(areas,success,total);
                    break;
                default:
                    break;
            }

        }else {
            throw new BusinessException("检查项配置extension数据异常,请检查!");
        }
        //计算期望值
        beforeScores.forEach(score -> {
            if (score.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                score.setScore(total.get() != 0 ? NumberUtil.div(success.doubleValue(),total.doubleValue(),4,RoundingMode.HALF_UP) : 0);
                score.setPassPoint(success.get());
                score.setTotalPoint(total.get());
            }else {
                score.setPassPoint(ObjectUtil.isNotEmpty(score.getPassPoint()) ? score.getPassPoint()-passPointBefore+success.get() : -passPointBefore+success.get());
                score.setTotalPoint(ObjectUtil.isNotEmpty(score.getTotalPoint()) ? score.getTotalPoint()-totalPointBefore+total.get() : -totalPointBefore+total.get());
                score.setScore(score.getTotalPoint() != 0 ? NumberUtil.div(score.getPassPoint(), score.getTotalPoint(), 4, RoundingMode.HALF_UP).doubleValue() : 0);
            }
        });
    }

    public void assertItemQualityPoint(CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,int passPointBefore,int totalPointBefore,double scoreBefore) {
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger total = new AtomicInteger(0);

        areas.forEach(area -> {
            List<CheckItemScoreVo.Score> scores = area.getScores();
            total.getAndAdd(scores.size());
            scores.forEach(s -> {
                if (s.getIsQualified() != null && s.getIsQualified()) {
                    success.getAndIncrement();
                }
            });
        });

        //计算期望值
        beforeScores.forEach(s -> {
            if (s.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                s.setScore(total.get() != 0 ? NumberUtil.div(success.doubleValue(),total.doubleValue(),4,RoundingMode.HALF_UP) : 0);
                s.setPassPoint(success.get());
                s.setTotalPoint(total.get());
            }else {
                s.setPassPoint(ObjectUtil.isNotEmpty(s.getPassPoint()) ? s.getPassPoint()-passPointBefore+success.get() : 0-passPointBefore+success.get());
                s.setTotalPoint(ObjectUtil.isNotEmpty(s.getTotalPoint()) ? s.getTotalPoint()-totalPointBefore+total.get() : 0-totalPointBefore+total.get());
                s.setScore(s.getTotalPoint() != 0 ? NumberUtil.div(s.getPassPoint(), s.getTotalPoint(), 4, RoundingMode.HALF_UP).doubleValue() : 0);
            }
        });
    }

    public void assertItemWeight(CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,double scoreBefore) {
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        List<CheckItemScoreVo.Score> scores = areas.get(0).getScores();
        AtomicDouble score = new AtomicDouble(0);

        if (ObjectUtil.isNotEmpty(scores.get(0).getIsQualified()) && scores.get(0).getIsQualified()) {
            score.set(0);
        }else {
            score.set(scores.get(0).getValue());
        }

        //计算期望值
        beforeScores.forEach(s -> {
            if (s.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                s.setScore(score.get());
            }else {
                s.setScore(ObjectUtil.isNotEmpty(s.getScore()) ? NumberUtil.add(NumberUtil.sub(s.getScore().doubleValue(),scoreBefore),score.doubleValue()) : score.doubleValue());
            }
        });

    }

    public void assertItemAwardDeduct(CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,double scoreBefore) {
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        AtomicDouble score = new AtomicDouble(0);
        score.set(areas.get(0).getScores().get(0).getValue());

        //计算期望值
        beforeScores.forEach(s -> {
            if (s.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                s.setScore(score.get());
            }else {
                s.setScore(ObjectUtil.isNotEmpty(s.getScore()) ? NumberUtil.add(NumberUtil.sub(s.getScore().doubleValue(),scoreBefore),score.doubleValue()) : score.doubleValue());
            }
        });
    }

    public void assertItemDecideLevel(JSONObject targetCheckItem,CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,double scoreBefore,double redLineScoreBefore){
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        List<CheckItemScoreVo.Score> scores = areas.get(0).getScores();

        DecideLevelVo decideLevelVo = JSON.parseObject(targetCheckItem.getString("extension"), DecideLevelVo.class);
        AtomicDouble score = new AtomicDouble(0);
        AtomicDouble redLineScore = new AtomicDouble(0);
        AtomicDouble fullScore = new AtomicDouble(0);
        fullScore.set(decideLevelVo.getMarkStandard().getFullScore());

        if (ObjectUtil.isEmpty(scores.get(0).getIsQualified())) {
            double score_temp = NumberUtil.div(areas.stream().mapToDouble(o -> o.getScores().get(0).getValue()).sum(),areas.size(),2,RoundingMode.HALF_UP);
            score.set(score_temp <= fullScore.get() ? MathUtils.convertNeg(score_temp) : MathUtils.convertNeg(fullScore.doubleValue()));

            if (1 == targetCheckItem.getIntValue("redLine")) {
                List<String> marks = areas.stream().map(o -> o.getScores().get(0).getMark()).distinct().collect(Collectors.toList());
                JSONObject templateArithmetic = zxxjCheckMapper.getTemplateArithmetic(checkItemScoreVo.getTemplateId()).stream().filter(o -> o.getString("arithmetic_key").equals("awardDeductRule")).findAny().orElse(null);
                AwardDeductRuleVo arithmeticValue = JSON.parseObject(templateArithmetic.getString("arithmetic_value"), AwardDeductRuleVo.class);
                Map<String, Double> deductRuleMap = arithmeticValue.getDeductRules().stream().collect(Collectors.toMap(AwardDeductRuleVo.DeductRule::getMark, AwardDeductRuleVo.DeductRule::getScore));
                for (String mark : marks) {
                    Double currentScore = deductRuleMap.get(mark);
                    if (currentScore > redLineScore.get()) {
                        redLineScore.set(currentScore);
                    }
                }
            }
        }

        //计算期望值
        beforeScores.forEach(s -> {
            if (s.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                s.setScore(score.get());
                s.setRedLineScore(redLineScore.get());
            }else {
                s.setScore(ObjectUtil.isNotEmpty(s.getScore()) ? NumberUtil.add(NumberUtil.sub(s.getScore().doubleValue(),scoreBefore),score.doubleValue()) : score.doubleValue());
            }
        });
    }

    public void assertItemRealMeasure(JSONObject targetCheckItem,CheckItemScoreVo checkItemScoreVo,List<AssertItemScore> beforeScores,int passPointBefore,int totalPointBefore,double scoreBefore){
        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger total = new AtomicInteger();

        //找到打分的条件
        List<RealMeasureVo> realMeasureList = JSON.parseArray(targetCheckItem.getString("extension"), RealMeasureVo.class);
        RealMeasureVo realMeasureVo = realMeasureList.stream().filter(o -> o.getName().equals(checkItemScoreVo.getRealMeasureName())).findAny().orElse(null);
        RealMeasureVo targetRealMeasureVo;

        if (ObjectUtil.isNotEmpty(realMeasureVo)) {
            switch (realMeasureVo.getPassArithmetic()) {
                //1-标准
                case 1:
                    setStandardExpect(realMeasureVo,areas,success,total);
                    break;
                //2-标准+设计值
                case 2:
                    setDesignAndStandardExpect(realMeasureVo,areas,success,total);
                    break;
                //3-极差
                case 3:
                    setRangeExpect(realMeasureVo,areas,success,total);
                    break;
                //4-偏差
                case 4:
                    setDeviationExpect(realMeasureVo,areas,success,total);
                    break;
                //5-观感
                case 5:
                    setLookFeelExpect(realMeasureVo,areas,success,total);
                    break;
                default:
                    break;
            }

        }else {
            throw new BusinessException("检查项配置extension数据异常,请检查!");
        }
        //计算期望值
        beforeScores.forEach(score -> {
            if (score.getCheckItemId().equals(checkItemScoreVo.getCheckItemId())) {
                score.setScore(total.get() != 0 ? NumberUtil.div(success.doubleValue(),total.doubleValue(),4,RoundingMode.HALF_UP) : 0);
                score.setPassPoint(success.get());
                score.setTotalPoint(total.get());
            }else {
                score.setPassPoint(ObjectUtil.isNotEmpty(score.getPassPoint()) ? score.getPassPoint()-passPointBefore+success.get() : -passPointBefore+success.get());
                score.setTotalPoint(ObjectUtil.isNotEmpty(score.getTotalPoint()) ? score.getTotalPoint()-totalPointBefore+total.get() : -totalPointBefore+total.get());
                score.setScore(score.getTotalPoint() != 0 ? NumberUtil.div(score.getPassPoint(), score.getTotalPoint(), 4, RoundingMode.HALF_UP).doubleValue() : 0);
            }
        });
    }

    public void setRangeExpect(RealMeasureVo realMeasureVo, List<CheckItemScoreVo.Area> areas, AtomicInteger success, AtomicInteger total) {
        if (1 == realMeasureVo.getAreaOrPoint()) {
            areas.forEach(area -> {
                List<CheckItemScoreVo.Score> scores = area.getScores();
                total.getAndAdd(scores.size());
                //判断是否有爆板值
                long criticalCount = scores.stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    List<Double> scoreValues = scores.stream().map(CheckItemScoreVo.Score::getValue).collect(Collectors.toList());
                    double min = Collections.min(scoreValues);
                    scores.forEach(score -> {
                        boolean result = computeDifferentValue(score.getValue() - min, realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                        if (result) {
                            success.getAndIncrement();
                        }
                    });
                }
            });
        } else {
            total.set(areas.size());
            areas.forEach(area -> {
                //判断是否有爆板值
                long criticalCount = area.getScores().stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    List<Double> scoreValues = area.getScores().stream().map(CheckItemScoreVo.Score::getValue).collect(Collectors.toList());
                    double min = Collections.min(scoreValues);
                    double max = Collections.max(scoreValues);
                    boolean result = computeDifferentValue(max - min, realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                    if (result) {
                        result = computeDifferentValue(min - min, realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                        if (result) {
                            success.getAndIncrement();
                        }
                    }
                }
            });
        }
    }

    public void setDeviationExpect(RealMeasureVo realMeasureVo, List<CheckItemScoreVo.Area> areas, AtomicInteger success, AtomicInteger total) {
        if (1 == realMeasureVo.getAreaOrPoint()) {
            areas.forEach(area -> {
                List<CheckItemScoreVo.Score> scores = area.getScores();
                total.getAndAdd(scores.size());
                //判断是否有爆板值
                long criticalCount = scores.stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    double designValue = area.getDesignValue();
                    scores.forEach(score -> {
                        boolean result = computeDifferentValue(Math.abs(score.getValue() - designValue), realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                        if (result) {
                            success.getAndIncrement();
                        }
                    });
                }
            });
        } else {
            total.set(areas.size());
            areas.forEach(area -> {
                //判断是否有爆板值
                long criticalCount = area.getScores().stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    double designValue = area.getDesignValue();
                    List<Double> scoreValues = area.getScores().stream().map(CheckItemScoreVo.Score::getValue).collect(Collectors.toList());
                    double min = Collections.min(scoreValues);
                    double max = Collections.max(scoreValues);
                    boolean result = computeDifferentValue(Math.abs(max - designValue), realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                    if (result) {
                        result = computeDifferentValue(Math.abs(min - designValue), realMeasureVo.getPassStandard().getPassMax(), realMeasureVo.getPassStandard().getPassMin());
                        if (result) {
                            success.getAndIncrement();
                        }
                    }
                }
            });
        }
    }

    public void setLookFeelExpect(RealMeasureVo realMeasureVo, List<CheckItemScoreVo.Area> areas, AtomicInteger success, AtomicInteger total){
        if (realMeasureVo.getAreaOrPoint() == 1) {
            areas.forEach(area -> {
                total.addAndGet(area.getScores().size());
                area.getScores().forEach(score -> {
                    if (score.getIsQualified() != null && score.getIsQualified()) {
                        success.getAndIncrement();
                    }
                });
            });
        }else {
            total.set(areas.size());
            areas.forEach(area -> {
                //统计合格数
                long qualifiedCount = (area.getScores().stream().filter(CheckItemScoreVo.Score::getIsQualified).count());
                if (qualifiedCount == area.getScores().size()) {
                    success.getAndIncrement();
                }
            });
        }
    }

    public void setDesignAndStandardExpect(RealMeasureVo realMeasureVo, List<CheckItemScoreVo.Area> areas, AtomicInteger success, AtomicInteger total) {

        if (realMeasureVo.getAreaOrPoint() == 1) {
            areas.forEach(area -> {
                List<CheckItemScoreVo.Score> scores = area.getScores();
                total.getAndAdd(scores.size());
                //判断是否有爆板值
                long criticalCount = scores.stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    double designValue = area.getDesignValue();
                    scores.forEach(score -> {
                        if (MathUtils.rangeInDefined(score.getValue() - designValue, realMeasureVo.getPassStandard().getPassMin(), realMeasureVo.getPassStandard().getPassMax())) {
                            success.getAndIncrement();
                        }
                    });
                }
            });
        } else {
            total.set(areas.size());
            areas.forEach(area -> {
                double designValue = area.getDesignValue();
                List<CheckItemScoreVo.Score> scores = area.getScores();
                AtomicBoolean compareResult = new AtomicBoolean(true);
                //判断是否有爆板值
                long criticalCount = scores.stream().filter(CheckItemScoreVo.Score::getIsCritical).count();
                if (criticalCount == 0) {
                    scores.forEach(score -> {
                        if (!MathUtils.rangeInDefined(score.getValue() - designValue, realMeasureVo.getPassStandard().getPassMin(), realMeasureVo.getPassStandard().getPassMax())) {
                            compareResult.set(false);
                        }
                    });
                    if (compareResult.get()) {
                        success.getAndIncrement();
                    }
                }
            });
        }
    }

    public void setStandardExpect(RealMeasureVo realMeasureVo, List<CheckItemScoreVo.Area> areas, AtomicInteger success, AtomicInteger total) {
        if (realMeasureVo.getAreaOrPoint() == 1) {
            areas.forEach(area -> {
                total.addAndGet(area.getScores().size());
                long count = area.getScores().stream().filter(o -> o.getIsCritical().equals(true)).count();
                if (count == 0) {
                    area.getScores().forEach(score -> {
                        if (MathUtils.rangeInDefined(score.getValue(), realMeasureVo.getPassStandard().getPassMin(), realMeasureVo.getPassStandard().getPassMax())) {
                            success.getAndIncrement();
                        }
                    });
                }
            });
        }else {
            total.set(areas.size());
            areas.forEach(area -> {
                long count = area.getScores().stream().filter(o -> o.getIsCritical().equals(true)).count();
                if (count == 0) {
                    long failCount = area.getScores().stream().filter(o -> !MathUtils.rangeInDefined(o.getValue(), realMeasureVo.getPassStandard().getPassMin(), realMeasureVo.getPassStandard().getPassMax())).count();
                    if (failCount == 0) {
                        success.getAndIncrement();
                    }
                }
            });
        }
    }

    public void setImpressionExpect(List<CheckItemScoreVo.Area> areas,AtomicInteger success,AtomicInteger total){
        //合格点数 总点数
        success.set(areas.get(0).getScores().get(0).getValue().intValue());
        total.set(areas.get(0).getScores().get(1).getValue().intValue());
    }

    private boolean computeDifferentValue(Double differentValue, Double psMax, Double psMin) {
        return psMin <= differentValue && differentValue <= psMax;
    }

    /**
     * 解析提交参数,获取目标测试数组
     * @param checkItemScoreVo
     * @return
     */
    public double[][] parseTestnumbers(String scoreType,String realMeasureType,CheckItemScoreVo checkItemScoreVo){

        List<CheckItemScoreVo.Area> areas = checkItemScoreVo.getAreas();
        int areaCount = areas.size();
        int pointCount = areas.get(0).getScores().size();
        double[][] testNumbers = new double[areaCount][pointCount+1];

        for (int i = 0; i < areas.size(); i++) {
            CheckItemScoreVo.Area area = areas.get(i);
            testNumbers[i][0] = area.getDesignValue();
            List<CheckItemScoreVo.Score> scores = area.getScores();
            for (int j = 0; j < scores.size(); j++) {
                testNumbers[i][j+1] = scores.get(j).getValue();
            }
        }

        return testNumbers;
    }

}
