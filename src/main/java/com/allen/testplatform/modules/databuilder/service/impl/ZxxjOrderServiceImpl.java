package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.enums.*;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.mapper.ZxxjV2Mapper;
import com.allen.testplatform.modules.databuilder.model.common.RoomQuery;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.zxxj.ZxxjOrderQuery;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjBatch;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjOrderProcessor;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjTemplateCheckItem;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjTemplateRelation;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.ZxxjOrderCloseVo;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.ZxxjProblemVo;
import com.allen.testplatform.modules.databuilder.service.ZxxjOrderService;
import com.allen.testplatform.testscripts.api.ApiZXXJ;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 专项巡检-问题相关测试
 *
 */
@Slf4j
@Service
public class ZxxjOrderServiceImpl implements ZxxjOrderService {

    @Resource
    private ZxxjV2Mapper zxxjMapper;

    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private ProcessV2Mapper processMapper;

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @Resource
    @Qualifier(value = "callerRunsThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    private SectionInfo sectionInfo;
    private List<UcUser> supervisorList;
    private List<UcUser> constructorList;
    private List<UcUser> contractorList;
    private List<UcUser> managerUserList;
    private List<UcUser> recitifyUserList;
    private List<UcUser> reviewUserList;

    @Override
    public void addProblems(String batchName, Long batchId, String templateName, String lastCheckName,
                                                                                    String banName, String floorName, String unitName, String roomName,
                                                                                    Boolean hasPoint, Double pointX, Double pointY,
                                                                                    Boolean hasNotice, String rectifyName,String reviewName,String checkName,
                                                                                    Integer importance, Integer writeOffDays, Integer pictureNum, Integer testCount) {
        ZxxjBatch batchInfo = zxxjMapper.getBatchInfo(batchName, batchId);
        ZxxjProblemVo problemVo = new ZxxjProblemVo();
        problemVo.setBatchId(batchInfo.getBatchId());
        //模板
        if (!ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
            problemVo.setTemplateId(batchInfo.getTemplateId());

            if (ObjectUtil.isNotEmpty(lastCheckName)) {
                //检查项
                List<ZxxjTemplateCheckItem> lastCheckList = zxxjMapper.getLastCheckList(problemVo.getTemplateId());
                if (CollectionUtils.isEmpty(lastCheckList)) {
                    throw new BusinessException("模板未配置可打分的末级检查项!");
                }
                ZxxjTemplateCheckItem lastCheck = lastCheckList.stream().filter(check -> check.getName().contains(lastCheckName)).findAny().orElse(null);
                if (ObjectUtil.isEmpty(lastCheck)) {
                    throw new BusinessException("未匹配到目标名称的末级检查项");
                }
                problemVo.setCheckItemId(lastCheck.getId());
            }
        }

        RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
        List<JSONObject> problemPartList = zxxjMapper.getProblemPartInfo(roomQuery, batchInfo.getSectionId(),false);
        if (CollectionUtils.isEmpty(problemPartList)) {
            throw new BusinessException("未匹配到房源信息! 停止后续步骤");
        }

        int count = ObjectUtil.isNotEmpty(testCount) && testCount > 0 ? testCount : 1;

        updateSectionInfo(batchInfo,null);

        List<JSONObject> checkUserList = zxxjMapper.getBatchUserInfo(batchId, 1);
        JSONObject checkUser = checkUserList.stream().filter(user -> user.getString("realName").contains(checkName)).findAny().orElse(checkUserList.get(RandomUtil.randomInt(checkUserList.size())));
        Long userId = checkUser.getLong("userId");
        UcUser ucUser = ucMapper.getUserById(userId);
        Map<String,String> header = ucUser.getSource().equals(Constant.SUPPLIER_SOURCE) ?
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getPhone(), EncryptUtils.decrypt(ucUser.getPassword()),currentEnv.getENV())) :
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getUserName(), EncryptUtils.decrypt(ucUser.getPassword()),currentEnv.getENV()));

        AtomicInteger success = new AtomicInteger();
        List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, count)
                .mapToObj(n ->
                        CompletableFuture.runAsync(() -> {
                            ZxxjProblemVo zxxjProblemVo = problemVo;

                            if (ObjectUtil.isEmpty(zxxjProblemVo.getTemplateId())) {
                                List<ZxxjTemplateRelation> batchSingleTemplates = zxxjMapper.getBatchSingleTemplate(batchInfo.getTemplateId());
                                List<ZxxjTemplateRelation> collect = batchSingleTemplates.stream().filter(template -> template.getTemplateName().contains(templateName)).collect(Collectors.toList());
                                if (CollectionUtils.isEmpty(collect)) {
                                    throw new BusinessException("未匹配到目标名称的模板!");
                                }
                                ZxxjTemplateRelation singleTemplate = collect.get(RandomUtil.randomInt(collect.size()));
                                zxxjProblemVo.setTemplateId(singleTemplate.getSingleTemplateId());
                            }

                            if (ObjectUtil.isEmpty(lastCheckName)) {
                                //检查项
                                List<ZxxjTemplateCheckItem> lastCheckList = zxxjMapper.getLastCheckList(zxxjProblemVo.getTemplateId());
                                if (CollectionUtils.isEmpty(lastCheckList)) {
                                    throw new BusinessException("模板未配置可打分的末级检查项!");
                                }
                                ZxxjTemplateCheckItem lastCheck = lastCheckList.get(RandomUtil.randomInt(lastCheckList.size()));
                                zxxjProblemVo.setCheckItemId(lastCheck.getId());
                            }

                            JSONObject partInfo = problemPartList.get(RandomUtil.randomInt(problemPartList.size()));
                            setOrderCheckPart(partInfo,zxxjProblemVo,roomQuery,hasPoint,pointX,pointY);
                            zxxjProblemVo.setImportance(ObjectUtil.isNotEmpty(importance) && importance > 0 && importance < 4 ? importance : RandomUtil.randomInt(1,4));
                            zxxjProblemVo.setWriteOffDays(ObjectUtil.isNotEmpty(writeOffDays) && writeOffDays > 0 && writeOffDays < 101 ? writeOffDays : RandomUtil.randomInt(1,101));

                            if (ObjectUtil.isNotEmpty(pictureNum) && pictureNum >= 0) {
                                List<String> pictures = TestDataUtils.getPicture(pictureNum);
                                String[] imageUrls = new String[pictures.size()];
                                pictures.toArray(imageUrls);
                                zxxjProblemVo.setImageUrls(imageUrls);
                            }else {
                                List<String> pictures = TestDataUtils.getPicture(RandomUtil.randomInt(10));
                                String[] imageUrls = new String[pictures.size()];
                                pictures.toArray(imageUrls);
                                zxxjProblemVo.setImageUrls(imageUrls);
                            }

                            if (hasNotice) {
                                zxxjProblemVo.setIfPush(true);
                                UcUser recitifyUser = ObjectUtil.isNotEmpty(rectifyName) ? recitifyUserList.stream().filter(user -> user.getRealName().contains(rectifyName)).findAny().orElse(recitifyUserList.get(RandomUtil.randomInt(recitifyUserList.size()))) : recitifyUserList.get(RandomUtil.randomInt(recitifyUserList.size()));
                                if (ObjectUtil.isNotEmpty(recitifyUser)) {
                                    zxxjProblemVo.setProcessor(ZxxjProblemVo.Processor.builder().id(recitifyUser.getUserId()).code(recitifyUser.getUserName()).name(recitifyUser.getRealName()).build());
                                    zxxjProblemVo.setProviderGuid(recitifyUser.getProviderGuid());
                                    zxxjProblemVo.setProviderName(recitifyUser.getProviderName());
                                    UcUser reviewUser =  ObjectUtil.isNotEmpty(reviewName) ? reviewUserList.stream().filter(user -> user.getRealName().contains(reviewName)).findAny().orElse(reviewUserList.get(RandomUtil.randomInt(reviewUserList.size()))) : reviewUserList.get(RandomUtil.randomInt(reviewUserList.size()));
                                    zxxjProblemVo.setReProcessor(ZxxjProblemVo.Processor.builder().id(reviewUser.getUserId()).code(reviewUser.getUserName()).name(reviewUser.getRealName()).build());
                                }else {
                                    zxxjProblemVo.setIfPush(false);
                                }
                            }else {
                                zxxjProblemVo.setIfPush(false);
                            }

                            String content;
                            if (zxxjProblemVo.getIfPush()) {
                                content = TestDataUtils.getTestContent(1, BusinessType.ZXXJ, TicketProcessEnum.Create.getProcessDesc(), "创建人:", ucUser.getRealName(), " 整改人:", zxxjProblemVo.getProcessor().getName(), " 复验人:", zxxjProblemVo.getReProcessor().getName());
                            }else {
                                content = TestDataUtils.getTestContent(1, BusinessType.ZXXJ, TicketProcessEnum.Create.getProcessDesc(), "创建人:", ucUser.getRealName());
                            }
                            zxxjProblemVo.setContent(content);

                            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_ADD_PROBLEM), header, JSONObject.toJSONString(zxxjProblemVo));
                            log.info("{}", JSON.parseObject(rs));
                            success.incrementAndGet();
                            throw new RuntimeException("新增问题失败异常: " + n);
                        }, taskExecutor)).collect(Collectors.toList());
        CompletableFuture<Void> headerFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
        try {
            headerFuture.join();
        } catch (Exception ex) {
            System.out.println("done count: " + success.get());
        }
    }

    @Override
    public void addPro(String batchName, Long batchId, String templateName, String lastCheckName,
                       String banName, String floorName, String unitName, String roomName,
                       Boolean hasPoint, Double pointX, Double pointY,
                       Boolean hasNotice, String rectifyName,String reviewName,String checkName,
                       Integer importance, Integer writeOffDays, Integer pictureNum) {
        ZxxjBatch batchInfo = zxxjMapper.getBatchInfo(batchName, batchId);
        ZxxjProblemVo problemVo = new ZxxjProblemVo();
        problemVo.setBatchId(batchInfo.getBatchId());
        //模板
        if (!ZxxjTemplateTypeEnum.group.getCode().equals(batchInfo.getTemplateType())) {
            problemVo.setTemplateId(batchInfo.getTemplateId());

            if (ObjectUtil.isNotEmpty(lastCheckName)) {
                //检查项
                List<ZxxjTemplateCheckItem> lastCheckList = zxxjMapper.getLastCheckList(problemVo.getTemplateId());
                if (CollectionUtils.isEmpty(lastCheckList)) {
                    throw new BusinessException("模板未配置可打分的末级检查项!");
                }
                ZxxjTemplateCheckItem lastCheck = lastCheckList.stream().filter(check -> check.getName().contains(lastCheckName)).findAny().orElse(null);
                if (ObjectUtil.isEmpty(lastCheck)) {
                    throw new BusinessException("未匹配到目标名称的末级检查项");
                }
                problemVo.setCheckItemId(lastCheck.getId());
            }
        }

        RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
        List<JSONObject> problemPartList = zxxjMapper.getProblemPartInfo(roomQuery, batchInfo.getSectionId(),false);
        if (CollectionUtils.isEmpty(problemPartList)) {
            throw new BusinessException("未匹配到房源信息! 停止后续步骤");
        }

        updateSectionInfo(batchInfo,null);

        List<JSONObject> checkUserList = zxxjMapper.getBatchUserInfo(batchId, 1);
        JSONObject checkUser = checkUserList.stream().filter(user -> user.getString("realName").contains(checkName)).findAny().orElse(checkUserList.get(RandomUtil.randomInt(checkUserList.size())));
        Long userId = checkUser.getLong("userId");
        UcUser ucUser = ucMapper.getUserById(userId);
        Map<String,String> header = ucUser.getSource().equals(Constant.SUPPLIER_SOURCE) ?
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getPhone(), EncryptUtils.decrypt(ucUser.getPassword()),currentEnv.getENV())) :
                TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getUserName(), EncryptUtils.decrypt(ucUser.getPassword()),currentEnv.getENV()));

        ZxxjProblemVo zxxjProblemVo = problemVo;

        if (ObjectUtil.isEmpty(zxxjProblemVo.getTemplateId())) {
            List<ZxxjTemplateRelation> batchSingleTemplates = zxxjMapper.getBatchSingleTemplate(batchInfo.getTemplateId());
            List<ZxxjTemplateRelation> collect = batchSingleTemplates.stream().filter(template -> template.getTemplateName().contains(templateName)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(collect)) {
                throw new BusinessException("未匹配到目标名称的模板!");
            }
            ZxxjTemplateRelation singleTemplate = collect.get(RandomUtil.randomInt(collect.size()));
            zxxjProblemVo.setTemplateId(singleTemplate.getSingleTemplateId());
        }

        if (ObjectUtil.isEmpty(lastCheckName)) {
            //检查项
            List<ZxxjTemplateCheckItem> lastCheckList = zxxjMapper.getLastCheckList(zxxjProblemVo.getTemplateId());
            if (CollectionUtils.isEmpty(lastCheckList)) {
                throw new BusinessException("模板未配置可打分的末级检查项!");
            }
            ZxxjTemplateCheckItem lastCheck = lastCheckList.get(RandomUtil.randomInt(lastCheckList.size()));
            zxxjProblemVo.setCheckItemId(lastCheck.getId());
        }

        JSONObject partInfo = problemPartList.get(RandomUtil.randomInt(problemPartList.size()));
        setOrderCheckPart(partInfo,zxxjProblemVo,roomQuery,hasPoint,pointX,pointY);
        zxxjProblemVo.setImportance(ObjectUtil.isNotEmpty(importance) && importance > 0 && importance < 4 ? importance : RandomUtil.randomInt(1,4));
        zxxjProblemVo.setWriteOffDays(ObjectUtil.isNotEmpty(writeOffDays) && writeOffDays > 0 && writeOffDays < 101 ? writeOffDays : RandomUtil.randomInt(1,101));

        if (ObjectUtil.isNotEmpty(pictureNum) && pictureNum >= 0) {
            List<String> pictures = TestDataUtils.getPicture(pictureNum);
            String[] imageUrls = new String[pictures.size()];
            pictures.toArray(imageUrls);
            zxxjProblemVo.setImageUrls(imageUrls);
        }else {
            List<String> pictures = TestDataUtils.getPicture(RandomUtil.randomInt(10));
            String[] imageUrls = new String[pictures.size()];
            pictures.toArray(imageUrls);
            zxxjProblemVo.setImageUrls(imageUrls);
        }

        if (hasNotice) {
            zxxjProblemVo.setIfPush(true);
            UcUser recitifyUser = ObjectUtil.isNotEmpty(rectifyName) ? recitifyUserList.stream().filter(user -> user.getRealName().contains(rectifyName)).findAny().orElse(recitifyUserList.get(RandomUtil.randomInt(recitifyUserList.size()))) : recitifyUserList.get(RandomUtil.randomInt(recitifyUserList.size()));
            if (ObjectUtil.isNotEmpty(recitifyUser)) {
                zxxjProblemVo.setProcessor(ZxxjProblemVo.Processor.builder().id(recitifyUser.getUserId()).code(recitifyUser.getUserName()).name(recitifyUser.getRealName()).build());
                zxxjProblemVo.setProviderGuid(recitifyUser.getProviderGuid());
                zxxjProblemVo.setProviderName(recitifyUser.getProviderName());
                UcUser reviewUser =  ObjectUtil.isNotEmpty(reviewName) ? reviewUserList.stream().filter(user -> user.getRealName().contains(reviewName)).findAny().orElse(reviewUserList.get(RandomUtil.randomInt(reviewUserList.size()))) : reviewUserList.get(RandomUtil.randomInt(reviewUserList.size()));
                zxxjProblemVo.setReProcessor(ZxxjProblemVo.Processor.builder().id(reviewUser.getUserId()).code(reviewUser.getUserName()).name(reviewUser.getRealName()).build());
            }else {
                zxxjProblemVo.setIfPush(false);
            }
        }else {
            zxxjProblemVo.setIfPush(false);
        }

        String content;
        if (zxxjProblemVo.getIfPush()) {
            content = TestDataUtils.getTestContent(1, BusinessType.ZXXJ, TicketProcessEnum.Create.getProcessDesc(), "创建人:", ucUser.getRealName(), " 整改人:", zxxjProblemVo.getProcessor().getName(), " 复验人:", zxxjProblemVo.getReProcessor().getName());
        }else {
            content = TestDataUtils.getTestContent(1, BusinessType.ZXXJ, TicketProcessEnum.Create.getProcessDesc(), "创建人:", ucUser.getRealName());
        }
        zxxjProblemVo.setContent(content);

        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_ADD_PROBLEM), header, JSONObject.toJSONString(zxxjProblemVo));
        log.info("{}", JSON.parseObject(rs));
    }

    @Override
    public void recitifyOrReviewProblems(String batchName, Long batchId, String orgName, String projectName, String stageCode,
                                         String banName, String floorName, String unitName, String roomName,
                                         String providerName, String lastCheckName, String creatorName,Integer pictureNum,
                                         Integer operateType) {
        ZxxjOrderQuery zxxjOrderQuery = new ZxxjOrderQuery();
        zxxjOrderQuery.setBatchId(batchId);
        zxxjOrderQuery.setBanName(banName);
        zxxjOrderQuery.setUnit(unitName);
        zxxjOrderQuery.setFloor(floorName);
        zxxjOrderQuery.setRoomName(roomName);
        zxxjOrderQuery.setOrgName(orgName);
        zxxjOrderQuery.setProjectName(projectName);
        zxxjOrderQuery.setStageCode(stageCode);
        zxxjOrderQuery.setProviderName(providerName);
        zxxjOrderQuery.setLastCheckName(lastCheckName);
        zxxjOrderQuery.setCreatorName(creatorName);

        switch (operateType) {
            case 1:
                //整改
                zxxjOrderQuery.setStatus(TicketStatusType.ZXXJ_WAIT_COMPLATE.getCode().concat(",").concat(TicketStatusType.ZXXJ_RE_COMPLATE.getCode()));
                break;
            case 2:
            case 3:
                //非正常关闭
                zxxjOrderQuery.setStatus(TicketStatusType.ZXXJ_WAIT_VERIFY.getCode());
                break;
            default:
                throw new BusinessException("操作类型operateType错误");
        }

        List<Long> idList = zxxjMapper.getTargetOrderIdList(zxxjOrderQuery);

        if (CollectionUtils.isEmpty(idList)) {
            throw new BusinessException("未匹配到状态正确的问题");
        }

        idList.forEach(id -> {
            taskExecutor.execute(() -> {

                ZxxjOrderProcessor zxxjOrderProcessor;
                switch (operateType) {
                    case 1:
                        zxxjOrderProcessor = zxxjMapper.getOrderProcessor(id, 1).get(0);
                        break;
                    case 2:
                    case 3:
                        zxxjOrderProcessor = zxxjMapper.getOrderProcessor(id, 2).get(0);
                        break;
                    default:
                        throw new BusinessException("操作类型operateType错误");
                }
                UcUser operateUser = ucMapper.getUserById(zxxjOrderProcessor.getProcessorId());
                Map<String,String> header = operateUser.getSource().equals(Constant.SUPPLIER_SOURCE) ?
                        TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(operateUser.getPhone(), EncryptUtils.decrypt(operateUser.getPassword()),currentEnv.getENV())) :
                        TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(operateUser.getUserName(), EncryptUtils.decrypt(operateUser.getPassword()),currentEnv.getENV()));

                ZxxjOrderCloseVo orderCloseVo = new ZxxjOrderCloseVo();
                orderCloseVo.setId(id);
                orderCloseVo.setImageUrls(ObjectUtil.isNotEmpty(pictureNum) && pictureNum >0 && pictureNum <=10 ? TestDataUtils.getPicture(pictureNum) :TestDataUtils.getPicture(RandomUtil.randomInt(6)));
                StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);

                String rs;
                switch (operateType) {
                    case 1:
                        //整改
                        stringBuilder.append(CheckTypeEnum.SPECIAL.getMsg()).append("-").append(TicketProcessEnum.Rectify.getProcessDesc())
                                .append(" 整改人：").append(zxxjOrderProcessor.getProcessorName()).append(DateUtils.now());
                        orderCloseVo.setContent(stringBuilder.toString());
                        rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_DO_PROBLEM), header, JSONObject.toJSONString(orderCloseVo));
                        break;
                    case 2:
                        stringBuilder.append(CheckTypeEnum.SPECIAL.getMsg()).append("-").append(TicketProcessEnum.NormalClose.getProcessDesc())
                                .append(" 复验人：").append(zxxjOrderProcessor.getProcessorName()).append(DateUtils.now());
                        orderCloseVo.setContent(stringBuilder.toString());
                        rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_NORMAL_CLOSE), header, JSONObject.toJSONString(orderCloseVo));
                        break;
                    case 3:
                        //非正常关闭
                        stringBuilder.append(CheckTypeEnum.SPECIAL.getMsg()).append("-").append(TicketProcessEnum.UnNormalClose.getProcessDesc())
                                .append(" 复验人：").append(zxxjOrderProcessor.getProcessorName()).append(DateUtils.now());
                        orderCloseVo.setContent(stringBuilder.toString());
                        rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiZXXJ.APP_UNNORMAL_CLOSE), header, JSONObject.toJSONString(orderCloseVo));
                        break;
                    default:
                        throw new BusinessException("操作类型operateType错误");
                }
                log.info("{}", JSON.parseObject(rs));
            });
        });
    }

    public void setOrderCheckPart(JSONObject partInfo, ZxxjProblemVo zxxjProblemVo, RoomQuery roomQuery, Boolean hasPoint, Double pointX, Double pointY) {
        if (ObjectUtil.isNotEmpty(roomQuery.getRoomName())) {
            setRoomPart(partInfo,hasPoint,pointX,pointY,zxxjProblemVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setFloorPart(partInfo,zxxjProblemVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getUnitName()) && ObjectUtil.isEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setUnitPart(partInfo,zxxjProblemVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getBanName()) && ObjectUtil.isEmpty(roomQuery.getUnitName()) && ObjectUtil.isEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setBanPart(partInfo,zxxjProblemVo);
        }else {
            setRoomPart(partInfo,hasPoint,pointX,pointY,zxxjProblemVo);
        }
    }

    public void setRoomPart(JSONObject partInfo, Boolean hasPoint, Double pointX, Double pointY, ZxxjProblemVo zxxjProblemVo){
        zxxjProblemVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        zxxjProblemVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        zxxjProblemVo.setFloor(ObjectUtil.isNotEmpty(partInfo.getString("floor")) ? partInfo.getString("floor"):null);
        zxxjProblemVo.setRoomCode(ObjectUtil.isNotEmpty(partInfo.getString("roomCode")) ? partInfo.getString("roomCode"):null);

        if (hasPoint) {
            ZxxjProblemVo.ProblemInHouse problemInHouse = new ZxxjProblemVo.ProblemInHouse();
            problemInHouse.setCheckImageUrl(ObjectUtil.isNotEmpty(partInfo.getString("checkImageUrl")) ? partInfo.getString("checkImageUrl"):null);
            problemInHouse.setHouseTypeId(ObjectUtil.isNotEmpty(partInfo.getLong("houseTypeId")) ? partInfo.getLong("houseTypeId"):null);
            problemInHouse.setPointX(ObjectUtil.isNotEmpty(pointX) && pointX >= 0 && pointX <= 1 ? pointX : RandomUtil.randomDouble(0.0000000000000001,1));
            problemInHouse.setPointY(ObjectUtil.isNotEmpty(pointY) && pointY >= 0 && pointY <= 1 ? pointY : RandomUtil.randomDouble(0.0000000000000001,1));
            zxxjProblemVo.setProblemInHouse(problemInHouse);
        }
    }

    public void setFloorPart(JSONObject partInfo, ZxxjProblemVo zxxjProblemVo){
        zxxjProblemVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        zxxjProblemVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        zxxjProblemVo.setFloor(ObjectUtil.isNotEmpty(partInfo.getString("floor")) ? partInfo.getString("floor"):null);
    }

    public void setUnitPart(JSONObject partInfo, ZxxjProblemVo zxxjProblemVo){
        zxxjProblemVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        zxxjProblemVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
    }

    public void setBanPart(JSONObject partInfo, ZxxjProblemVo zxxjProblemVo){
        zxxjProblemVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
    }

    /**
     * 更新初始化标段及人员信息
     * @param batch
     * @param sectionType
     */
    public void updateSectionInfo(ZxxjBatch batch,Integer sectionType){

        sectionInfo = processMapper.getSectionInfo(null,batch.getSectionId(),sectionType);
        sectionType = sectionInfo.getSectionType();
        contractorList = ucMapper.getSupplierUsers(sectionInfo.getContractorGuid());
        constructorList = ucMapper.getSupplierUsersByList(sectionInfo.getConstructionGuid());
        supervisorList = ucMapper.getSupplierUsers(sectionInfo.getSupervisorGuid());
        String category = setCategory(sectionType);
        managerUserList = ucMapper.getUsersByIdList(processMapper.getBatchUserId(sectionInfo.getStageCode(), RoleTypeEnum.ManageUser.getRoleCode(), category),Constant.PS_SOURCE);

        recitifyUserList = new ArrayList<>();
        recitifyUserList.addAll(constructorList);
        recitifyUserList.addAll(contractorList);
        if (CollectionUtils.isEmpty(recitifyUserList)) {
            throw new BusinessException("整改人为空,请检查配置!");
        }
        reviewUserList = new ArrayList<>();
        reviewUserList.addAll(supervisorList);
        reviewUserList.addAll(managerUserList);
        if (CollectionUtils.isEmpty(managerUserList)) {
            throw new BusinessException("整改人为空,请检查配置!");
        }
    }

    public String setCategory(int sectionType) {
        String category;
        switch (sectionType) {
            case 1:
                category = Constant.GCJC;
                break;
            case 2:
                category = Constant.ZSJC;
                break;
            case 3:
                category = Constant.JGJC;
                break;
            default:
                throw new IllegalStateException("标段类型错误! sectionType:" + sectionType);
        }
        return category;
    }

}
