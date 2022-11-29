package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.enums.MoveDirectionEnum;
import com.allen.testplatform.modules.databuilder.enums.RoleTypeEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketStatusType;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.mapper.FhcyV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.*;
import com.allen.testplatform.modules.databuilder.model.fhcy.vo.FhcyProblemVo;
import com.allen.testplatform.modules.databuilder.model.fhcy.vo.ReviewProblemVo;
import com.allen.testplatform.modules.databuilder.service.FhcyService;
import com.allen.testplatform.testscripts.api.ApiFHCY;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jayway.jsonpath.JsonPath;
import com.xiaoleilu.hutool.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.testng.Assert;

import javax.annotation.Resource;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FhcyServiceImpl implements FhcyService {

    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private FhcyV2Mapper fhcyV2Mapper;

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @Resource
    @Qualifier(value = "callerRunsThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    private final String[] bearingType = {"ONE","TWO","THREE","FOUR","EAST","WEST","NORTH","SOUTH"};

    @Override
    public void addBatchProblems(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                                 String banName, String unitName, String floorName, String roomName,
                                 String projectSiteName, String rectifyUserName, String checkUserName,
                                 String importance, Integer checkImageIndex, String positionName, String nearDirection,
                                 String nearPercent, Double x, Double y, Integer testCount
    )
    {
        addProblemsV2(batchName, firstCheckName, secondCheckName, lastCheckName, banName, unitName, floorName, roomName, projectSiteName, rectifyUserName, checkUserName, importance, checkImageIndex, positionName, nearDirection, nearPercent, x, y,true, testCount);
    }

    @Override
    public void rectifyBatchProblems(Long batchId,String stageCode)
    {
        List<Long> processIds = fhcyV2Mapper.getRectifyProcessIds(stageCode,batchId, TicketStatusType.FHCY_PROCESSING.getCode());

        processIds.forEach(rectify -> {
            UcUser user = ucMapper.getUserByIdSource(rectify, Constant.SUPPLIER_SOURCE);
            Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));
            List<Long> ticketIds = fhcyV2Mapper.getTicketIds(stageCode, batchId, TicketStatusType.FHCY_PROCESSING.getCode(), rectify);
            if (ticketIds.size() >= 30) {

                List<List<Long>> targetList = ListUtils.splitList(ticketIds,30);
                targetList.forEach(idList -> {
                   taskExecutor.execute(() -> {
                       try {
                           Map<String,Object> params = new HashMap<>();
                           params.put("ids",idList);
                           String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RECTIFY_PROBLEM), header, JSONObject.toJSONString(params));
                           log.info("{}",JSON.parseObject(rs));
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   });
                });
            }else {
                //工单数量少的情况下不用多线程
                Map<String,Object> params = new HashMap<>();
                params.put("ids",ticketIds);
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RECTIFY_PROBLEM), header, JSONObject.toJSONString(params));
                log.info("{}",JSON.parseObject(rs));
            }
        });
    }

    @Override
    public void reviewBatchPassOrNot(String stageCode,Long batchId,String reviewTypeName)
    {

        List<Long> ticketIds = fhcyV2Mapper.getTicketIds(stageCode,batchId,TicketStatusType.FHCY_COMPLATE.getCode(),null);
        CheckUser operatorUser = getOperatorUser(batchId, RoleTypeEnum.CheckAndReview.getRoleName(), null);
        UcUser ucUser = ucMapper.getUserById(operatorUser.getUserId());
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getUserName(), EncryptUtils.decrypt(ucUser.getPassword()), currentEnv.getENV()));

        if (CollectionUtils.isNotEmpty(ticketIds)) {
            ticketIds.forEach(ticketId -> {
                taskExecutor.execute(() -> {
                    try {
                        ReviewProblemVo reviewProblemVo = buildReviewPro(ticketId, reviewTypeName, operatorUser);
                        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_REVIEW_PROBLEM), header, JSONObject.toJSONString(reviewProblemVo));
                        log.info("{}",JSON.parseObject(rs));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }else {
            throw new BusinessException("未匹配到[已整改]状态的工单");
        }

    }

    @Override
    public void addAndRectifyAndReview(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                                       String banName, String unitName, String floorName, String roomName,
                                       String projectSiteName, String rectifyUserName, String checkUserName,
                                       String importance, Integer checkImageIndex, String positionName, String nearDirection,
                                       String nearPercent, Double x, Double y, Integer testCount, int nodeLimit
    ) throws ParseException
    {
        if(nodeLimit >= 1) {
            List<Long> ids = addProblems(batchName, firstCheckName, secondCheckName, lastCheckName, banName, unitName, floorName, roomName, projectSiteName, rectifyUserName, checkUserName, importance, checkImageIndex, positionName, nearDirection, nearPercent, x, y,false, testCount);
            if (nodeLimit >= 2) {
                rectifyBatchProblems(ids);
                if (nodeLimit >= 3) {
                    reviewPassOrNot(ids,TicketProcessEnum.ReviewPass.getProcessDesc());
                }
            }
        }else {
            throw new BusinessException("流程节点nodeLimit配置不正确  --------> 1-只创建问题 2-创建&整改问题 3-创建&整改&复验问题");
        }
    }

    /**
     * 退回问题 接口支持批量退回 注意GROUP_CONCAT(t.id) AS ticketIds 组装数据MYSQL有长度限制，该方法适用于id不多的情况下
     */
    @Override
    public void returnBatchProblems(Long batchId,String stageCode) {

        List<Long> processIds = fhcyV2Mapper.getRectifyProcessIds(stageCode,batchId, TicketStatusType.FHCY_PROCESSING.getCode());

        processIds.forEach(rectify -> {
            UcUser user = ucMapper.getUserByIdSource(rectify, Constant.SUPPLIER_SOURCE);
            Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));
            List<Long> ticketIds = fhcyV2Mapper.getTicketIds(stageCode, batchId, TicketStatusType.FHCY_PROCESSING.getCode(), rectify);
            if (ticketIds.size() >= 30) {

                List<List<Long>> targetList = ListUtils.splitList(ticketIds,30);
                targetList.forEach(idList -> {
                    taskExecutor.execute(() -> {
                        try {
                            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RETURN_PROBLEM), header, JSONObject.toJSONString(idList));
                            log.info("{}",JSON.parseObject(rs));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
            }else {
                //工单数量少的情况下不用多线程
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RETURN_PROBLEM), header, JSONObject.toJSONString(ticketIds));
                log.info("{}",JSON.parseObject(rs));
            }
        });
    }

    /**
     * 批量创建问题() 多线程模式
     */
    public void addProblemsV2(String batchName,String firstCheckName,String secondCheckName,String lastCheckName,
                                  String banName,String unitName,String floorName,String roomName,
                                  String projectSiteName,String rectifyUserName,String checkUserName,String importance,Integer checkImageIndex,
                                  String positionName,String nearDirection,String nearPercent,Double x,Double y,boolean batchThread,Integer testCount                    //执行测试次数
    )
    {
        testCount = ObjectUtil.isNotEmpty(testCount) && testCount > 0 ? testCount : 1;
        CheckBatch batch = fhcyV2Mapper.getCheckBatch(batchName);
        if (ObjectUtil.isEmpty(batch)) {
            throw new BusinessException("未查询到批次信息,请检查批次名称或数据库");
        }
        Map<String, String> checkHeader = getOperatorHeader(batch.getId(),"查验",checkUserName);

        FhcyProblemVo problemVo = new FhcyProblemVo();
        problemVo.setCheckBatchId(batch.getId());
        problemVo.setCategory(batch.getCategory());
        List<String> checkCodeList = fhcyV2Mapper.getCheckCodeList(batch.getCheckListId(), null, firstCheckName, secondCheckName, lastCheckName);
        List<BatchRoomInfo> checkBatchRooms = fhcyV2Mapper.getCheckBatchRooms(batch.getId(), banName, unitName, floorName, roomName);
        List<CheckUser> rectifyUsers = fhcyV2Mapper.getRectifyUsers(batch.getId());

        if (batchThread) {
            for (int i = 0; i < testCount; i++) {
                taskExecutor.execute(() -> {
                    FhcyProblemVo problemVoTemp = problemVo;
                    List<JSONObject> parentPathList = fhcyV2Mapper.getItemParentPath(batch.getCheckListId(), checkCodeList.get(RandomUtil.randomInt(checkCodeList.size())));
                    if (CollectionUtils.isNotEmpty(parentPathList)) {
                        BeanUtil.copyProperties(parentPathList.get(RandomUtil.randomInt(parentPathList.size())),problemVoTemp);
                    }else {
                        throw new BusinessException("未匹配到检查项,停止后续步骤");
                    }

                    if (CollectionUtils.isNotEmpty(checkBatchRooms)) {
                        BatchRoomInfo batchRoomInfo;
                        if (ObjectUtil.isNotEmpty(projectSiteName)) {
                            batchRoomInfo = checkBatchRooms.stream().filter(o -> ObjectUtil.isNotEmpty(o.getProjectSiteName())&& o.getProjectSiteName().equals(projectSiteName)).findAny().orElse(null);
                            if (ObjectUtil.isEmpty(batchRoomInfo)) {
                                batchRoomInfo = checkBatchRooms.get(RandomUtil.randomInt(checkBatchRooms.size()));
                                List<JSONObject> projectSites = fhcyV2Mapper.getAllSiteInProject(batch.getProjectCode());
                                JSONObject projectSite = projectSites.stream().filter(o -> o.getString("projectSiteName").equals(projectSiteName)).findAny().orElse(projectSites.get(RandomUtil.randomInt(projectSites.size())));
                                batchRoomInfo.setProjectSiteId(projectSite.getLong("projectSiteId"));
                                batchRoomInfo.setProjectSiteName(projectSite.getString("projectSiteName"));
                                log.info("未匹配到房源&部位!,将从项目下所有部位中匹配到 {}",batchRoomInfo);
                            }else {
                                log.info("匹配到房源&部位! {}",batchRoomInfo);
                            }
                        }else {
                            log.info("未指定部位");
                            batchRoomInfo = checkBatchRooms.get(RandomUtil.randomInt(checkBatchRooms.size()));
                            if (ObjectUtil.isEmpty(batchRoomInfo.getProjectSiteId())) {
                                List<JSONObject> projectSites = fhcyV2Mapper.getAllSiteInProject(batch.getProjectCode());
                                JSONObject projectSite = projectSites.stream().filter(o -> o.getString("projectSiteName").equals(projectSiteName)).findAny().orElse(projectSites.get(RandomUtil.randomInt(projectSites.size())));
                                batchRoomInfo.setProjectSiteId(projectSite.getLong("projectSiteId"));
                                batchRoomInfo.setProjectSiteName(projectSite.getString("projectSiteName"));
                            }
                        }
                        String[] checkImageUrls = batchRoomInfo.getCheckImageUrl().split(",");
                        if (checkImageUrls.length > 1) {
                            if (ObjectUtil.isNotEmpty(checkImageIndex) && checkImageIndex <= checkImageUrls.length && checkImageIndex > 0) {
                                batchRoomInfo.setCheckImageUrl(checkImageUrls[checkImageIndex-1]);
                            }else {
                                batchRoomInfo.setCheckImageUrl(checkImageUrls[RandomUtil.randomInt(checkImageUrls.length)]);
                            }
                        }
                        BeanUtil.copyProperties(batchRoomInfo,problemVoTemp);
                    }else {
                        throw new BusinessException("未匹配到房源,停止后续步骤");
                    }

                    setProblemRectifyInfo(rectifyUserName,importance,rectifyUsers,problemVoTemp);

                    MyDimension myDimension = null;
                    try {
                        myDimension = setDimension(positionName, nearDirection, ObjectUtil.isNotEmpty(nearPercent) ? nearPercent : "0%", x, y);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    problemVo.setPointX(myDimension.getX());
                    problemVo.setPointY(myDimension.getY());
                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_ADD_PROBLEM), checkHeader, JSONObject.toJSONString(problemVoTemp));
                    log.info("{}",JSON.parseObject(rs));
                });
            }
        }
    }

    public void setProblemRectifyInfo(String rectifyUserName,String importance,List<CheckUser> rectifyUsers,FhcyProblemVo problemVo){
        CheckUser rectifyUser;
        if (CollectionUtils.isNotEmpty(rectifyUsers)) {
            if (ObjectUtil.isNotEmpty(rectifyUserName)) {
                rectifyUser = rectifyUsers.stream().filter(o -> o.getRealName().contains(rectifyUserName)).findAny().orElse(rectifyUsers.get(RandomUtil.randomInt(rectifyUsers.size())));
            }else {
                rectifyUser = rectifyUsers.get(RandomUtil.randomInt(rectifyUsers.size()));
            }
        }else {
            throw new BusinessException("系统未配置承建商整改成员,请检查数据!");
        }
        problemVo.setProviderName(rectifyUser.getProviderName());
        problemVo.setProviderGuid(rectifyUser.getProviderGuid());
        problemVo.setDutyUserId(rectifyUser.getUserId());
        problemVo.setDutyUserCode(rectifyUser.getUserCode());
        problemVo.setDutyUserName(rectifyUser.getRealName());

        problemVo.setBearingType(bearingType[RandomUtil.randomInt(8)]);
        long current = DateUtils.current();
        StringBuilder stringBuilder = new StringBuilder();
        String checkSign = stringBuilder.append(IdWorker.getId()).append("-").append(current).append("-").append(IdWorker.get32UUID()).toString();
        problemVo.setCheckSign(checkSign);
        problemVo.setImgs(TestDataUtils.getPicture(RandomUtil.randomInt(6)));
        problemVo.setImportance(ObjectUtil.isNotEmpty(importance) ? importance : String.valueOf(RandomUtil.randomInt(0,3)));
//        MyDimension dimension = setDimension("随机", null, null, null, null);
        problemVo.setPointX(RandomUtil.randomDouble(0.0000000000000001,1));
        problemVo.setPointY(RandomUtil.randomDouble(0.0000000000000001,1));
        problemVo.setNotes("自动化测试-分户查验-".concat(TicketProcessEnum.Create.getProcessDesc()).concat("-整改人:")+rectifyUser.getRealName()+DateUtils.current());
        problemVo.setAddTime(current);
    }

    /**
     * 批量创建问题 多线程模式
     */
    public List<Long> addProblems(String batchName,String firstCheckName,String secondCheckName,String lastCheckName,
                                  String banName,String unitName,String floorName,String roomName,
                                  String projectSiteName,String rectifyUserName,String checkUserName,String importance,Integer checkImageIndex,
                                  String positionName,String nearDirection,String nearPercent,Double x,Double y,boolean batchThread,Integer testCount                    //执行测试次数
    ) throws ParseException
    {
        testCount = ObjectUtil.isNotEmpty(testCount) && testCount > 0 ? testCount : 1;
        List<FhcyProblemVo> problemVoList = new ArrayList<>();
        for (int i = 0; i < testCount; i++) {
            FhcyProblemVo problemVo = buildProblem(batchName, firstCheckName, secondCheckName, lastCheckName, banName, unitName, floorName, roomName, projectSiteName, rectifyUserName, importance,checkImageIndex);
            MyDimension myDimension = setDimension(positionName, nearDirection, ObjectUtil.isNotEmpty(nearPercent) ? nearPercent : "0%", x, y);
            problemVo.setPointX(myDimension.getX());
            problemVo.setPointY(myDimension.getY());
            problemVoList.add(problemVo);
        }

        Map<String, String> checkHeader = getOperatorHeader(problemVoList.get(0).getCheckBatchId(),"检查",checkUserName);

        List<Long> problemIds = new ArrayList<>();

        if (batchThread) {
            //开启多线程模式
            problemVoList.forEach(fhcyProblemVo -> {
                taskExecutor.execute(() -> {
                    try {
                        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_ADD_PROBLEM), checkHeader, JSONObject.toJSONString(fhcyProblemVo));
                        Object body = JsonPath.read(rs, "$.body");
                        problemIds.add(Long.valueOf(body.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }else {
            //不开启多线程模式
            problemVoList.forEach(fhcyProblemVo -> {
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_ADD_PROBLEM), checkHeader, JSONObject.toJSONString(fhcyProblemVo));
                Object body = JsonPath.read(rs, "$.body");
                problemIds.add(Long.valueOf(body.toString()));
            });
        }

        return problemIds;
    }

    public void reviewPassOrNot(List<Long> ids,String reviewTypeName){
        List<JSONObject> batchTicketInfos = fhcyV2Mapper.getBatchTicketInfos(ids,TicketStatusType.FHCY_COMPLATE.getCode());
        if (CollectionUtils.isNotEmpty(batchTicketInfos)) {
            batchTicketInfos.forEach(batch -> {
                CheckUser operatorUser = getOperatorUser(batch.getLong("batchId"), RoleTypeEnum.CheckAndReview.getRoleName(), null);
                UcUser ucUser = ucMapper.getUserById(operatorUser.getUserId());
                Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getUserName(), EncryptUtils.decrypt(ucUser.getPassword()), currentEnv.getENV()));
                List<String> temp = Arrays.asList(batch.getString("ticketIds").split(","));
                List<Long> targetTickets = temp.stream().map(o -> Long.valueOf(o)).collect(Collectors.toList());
                targetTickets.forEach(ticket -> {
                    ReviewProblemVo reviewProblemVo = buildReviewPro(ticket, reviewTypeName, operatorUser);
                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_REVIEW_PROBLEM), header, JSONObject.toJSONString(reviewProblemVo));
                    log.info("{}", JSON.parseObject(rs));
                });
            });
        }else {
            log.info("未匹配到已整改状态的工单!");
        }
    }

    /**
     * 整改问题 接口支持批量整改 注意GROUP_CONCAT(t.id) AS ticketIds 组装数据MYSQL有长度限制，该方法适用于id不多的情况下
     * @param ids
     * @return
     */
    public void rectifyBatchProblems(List<Long> ids) {

        List<TicketUserInfo> rectifyTickets = fhcyV2Mapper.getTicketInfos(ids, RoleTypeEnum.RectifyUser.getRoleCode(),TicketStatusType.FHCY_PROCESSING.getCode());

        if (CollectionUtils.isNotEmpty(rectifyTickets)) {
            rectifyTickets.forEach(o -> {
                UcUser user = ucMapper.getUserByIdSource(o.getProcessorId(), Constant.SUPPLIER_SOURCE);
                Map<String,String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(),EncryptUtils.decrypt(user.getPassword()),currentEnv.getENV()));
                Map<String,Object> params = new HashMap<>();
                params.put("ids",o.getTicketIds());
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RECTIFY_PROBLEM), header, JSONObject.toJSONString(params));
                log.info("{}", JSON.parseObject(rs));
            });
        }else {
            log.info("不存在待整改的工单!");
        }
    }



    /**
     * 核销复验问题 1-通过  2-不通过 3-作废 操作为同一个接口 不支持批量核销
     */
    public ReviewProblemVo buildReviewPro(Long ticketId,String reviewTypeName,CheckUser user) {
        int type;
        ReviewProblemVo reviewProblemVo = null;
        Assert.assertNotNull(reviewTypeName,"核销类型不能为空");

        if (TicketProcessEnum.ReviewPass.getProcessDesc().contains(reviewTypeName)) type = 1;
        else if (TicketProcessEnum.ReviewNoPass.getProcessDesc().contains(reviewTypeName)) type = 2;
        else if (TicketProcessEnum.Cancel.getProcessDesc().contains(reviewTypeName)) type = 3;
        else type = 0;

        switch (type) {
            case 1:
                reviewProblemVo = buildReviewPro(ticketId, 1, TicketProcessEnum.ReviewPass.getProcessCode(), user);
                break;
            case 2:
                reviewProblemVo = buildReviewPro(ticketId, 2, TicketProcessEnum.ReviewNoPass.getProcessCode(), user);
                break;
            case 3:
                reviewProblemVo = buildReviewPro(ticketId, 3, TicketProcessEnum.Cancel.getProcessCode(), user);
                break;
            default:
                break;
        }

        return reviewProblemVo;
    }

    public ReviewProblemVo buildReviewPro(Long ticketId, Integer reviewType, Integer operateCode, CheckUser user) {
        ReviewProblemVo reviewProblemVo = new ReviewProblemVo();
        reviewProblemVo.setId(ticketId);
        reviewProblemVo.setImageUrls(ArrayUtil.toArray(TestDataUtils.getPicture(RandomUtil.randomInt(4)),String.class));
        reviewProblemVo.setUpdateTime(String.valueOf(DateUtils.current()));
        reviewProblemVo.setUserId(user.getUserId());
        reviewProblemVo.setType(reviewType);
        reviewProblemVo.setContent("自动化测试-分户查验-".concat(TicketProcessEnum.getTargetDesc(operateCode)).concat("-操作人:")+user.getRealName()+DateUtils.current());
        return reviewProblemVo;
    }

    /**
     * 构建问题对象
     */
    public FhcyProblemVo buildProblem(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                                      String banName, String unitName, String floorName, String roomName, String projectSiteName,
                                      String rectifyUserName, String importance, Integer checkImageIndex) {

        FhcyProblemVo problemVo = new FhcyProblemVo();
        CheckBatch batch = fhcyV2Mapper.getCheckBatch(batchName);
        if (ObjectUtil.isEmpty(batch)) {
            throw new BusinessException("未查询到批次信息,请检查批次名称或数据库");
        }

        problemVo.setCheckBatchId(batch.getId());
        problemVo.setCategory(batch.getCategory());

        List<String> checkCodeList = fhcyV2Mapper.getCheckCodeList(batch.getCheckListId(), null, firstCheckName, secondCheckName, lastCheckName);
        List<JSONObject> parentPathList = fhcyV2Mapper.getItemParentPath(batch.getCheckListId(), checkCodeList.get(RandomUtil.randomInt(checkCodeList.size())));
        if (CollectionUtils.isNotEmpty(parentPathList)) {
            BeanUtil.copyProperties(parentPathList.get(RandomUtil.randomInt(parentPathList.size())),problemVo);
        }else {
            throw new BusinessException("未匹配到检查项,停止后续步骤");
        }

        List<BatchRoomInfo> checkBatchRooms = fhcyV2Mapper.getCheckBatchRooms(batch.getId(), banName, unitName, floorName, roomName);
        if (CollectionUtils.isNotEmpty(checkBatchRooms)) {
            BatchRoomInfo batchRoomInfo;
            if (ObjectUtil.isNotEmpty(projectSiteName)) {
                batchRoomInfo = checkBatchRooms.stream().filter(o -> ObjectUtil.isNotEmpty(o.getProjectSiteName())&& o.getProjectSiteName().equals(projectSiteName)).findAny().orElse(null);
                if (ObjectUtil.isEmpty(batchRoomInfo)) {
                    batchRoomInfo = checkBatchRooms.get(RandomUtil.randomInt(checkBatchRooms.size()));
                    List<JSONObject> projectSites = fhcyV2Mapper.getAllSiteInProject(batch.getProjectCode());
                    JSONObject projectSite = projectSites.stream().filter(o -> o.getString("projectSiteName").equals(projectSiteName)).findAny().orElse(projectSites.get(RandomUtil.randomInt(projectSites.size())));
                    batchRoomInfo.setProjectSiteId(projectSite.getLong("projectSiteId"));
                    batchRoomInfo.setProjectSiteName(projectSite.getString("projectSiteName"));
                    log.info("未匹配到房源&部位!,将从项目下所有部位中匹配到 {}",batchRoomInfo);
                }else {
                    log.info("匹配到房源&部位! {}",batchRoomInfo);
                }
            }else {
                log.info("未指定部位");
                batchRoomInfo = checkBatchRooms.get(RandomUtil.randomInt(checkBatchRooms.size()));
                if (ObjectUtil.isEmpty(batchRoomInfo.getProjectSiteId())) {
                    List<JSONObject> projectSites = fhcyV2Mapper.getAllSiteInProject(batch.getProjectCode());
                    JSONObject projectSite = projectSites.stream().filter(o -> o.getString("projectSiteName").equals(projectSiteName)).findAny().orElse(projectSites.get(RandomUtil.randomInt(projectSites.size())));
                    batchRoomInfo.setProjectSiteId(projectSite.getLong("projectSiteId"));
                    batchRoomInfo.setProjectSiteName(projectSite.getString("projectSiteName"));
                }
            }
            String[] checkImageUrls = batchRoomInfo.getCheckImageUrl().split(",");
            if (checkImageUrls.length > 1) {
                if (ObjectUtil.isNotEmpty(checkImageIndex) && checkImageIndex <= checkImageUrls.length && checkImageIndex > 0) {
                    batchRoomInfo.setCheckImageUrl(checkImageUrls[checkImageIndex-1]);
                }else {
                    batchRoomInfo.setCheckImageUrl(checkImageUrls[RandomUtil.randomInt(checkImageUrls.length)]);
                }
            }
            BeanUtil.copyProperties(batchRoomInfo,problemVo);
        }else {
            throw new BusinessException("未匹配到房源,停止后续步骤");
        }

        List<CheckUser> rectifyUsers = fhcyV2Mapper.getRectifyUsers(batch.getId());
        setProblemRectifyInfo(rectifyUserName,importance,rectifyUsers,problemVo);
        return problemVo;
    }

    /**
     * 按需求设置坐标值
     * @param positionName  点位存在方位 参数值:  左上 左下 右上 右下 中心 随机
     * @param nearDirection   偏移方向  参数值: 左 右 上 下
     * @param nearPercent  偏移百分比 字符串百分比格式 100% 50% 20%
     * @param x 指定坐标 X
     * @param y 指定坐标 Y
     * @return
     * @throws ParseException
     */
    public MyDimension setDimension(String positionName, String nearDirection, String nearPercent, Double x, Double y) throws ParseException {
        double xMin = 0;
        double yMin = 0;
        double xMax = 1;
        double yMax = 1;

        MyDimension myDimension = new MyDimension();
        if (ObjectUtil.isNotEmpty(positionName)) {
            MoveDirectionEnum typeEnum = MoveDirectionEnum.getPositionEnum(positionName);
            switch (typeEnum) {
                case CENTER:
                    xMin = 0.25;xMax = 0.75;yMin = 0.25;yMax = 0.75;
                    break;
                case LEFT_UP:
                    xMax = 0.5;yMax = 0.5;
                    break;
                case RIGHT_UP:
                    xMin = 0.5;yMax = 0.5;
                    break;
                case LEFT_BOTTOM:
                    xMax = 0.5;yMin = 0.5;
                    break;
                case RIGHT_BOTTOM:
                    xMin = 0.5;yMin = 0.5;
                    break;
                case ALL:
                default:
                    break;
            }
        }

        if (ObjectUtil.isNotEmpty(x) && ObjectUtil.isNotEmpty(y)){
            myDimension.setX(x * xMax);
            myDimension.setY(y * yMax);
        }else {
            myDimension.setX(RandomUtil.randomDouble(xMin+0.0000000000000001,xMax));
            myDimension.setY(RandomUtil.randomDouble(yMin+0.0000000000000001,yMax));
        }

        if (ObjectUtil.isNotEmpty(nearDirection)) {
            double percentNum = 0;
            if (ObjectUtil.isNotEmpty(nearPercent)) {
                NumberFormat nf = NumberFormat.getPercentInstance();
                percentNum = nf.parse(nearPercent).doubleValue();
                if (percentNum >= 1 || percentNum < 0) {
                    throw new BusinessException("偏移百分比不能大于等于1 或 小于0");
                }
            }

            switch (nearDirection) {
                case "上":
                    myDimension.setY(myDimension.getY() * (1-percentNum));
                    break;
                case "下":
                    myDimension.setY(myDimension.getY() * (1+percentNum) >= 1 ?  RandomUtil.randomDouble(0.90,0.999999999999999) : myDimension.getY() * (1+percentNum));
                    break;
                case "左":
                    myDimension.setX(myDimension.getX() * (1-percentNum));
                    break;
                case "右":
                    myDimension.setX(myDimension.getX() * (1+percentNum) >= 1 ?  RandomUtil.randomDouble(0.90,0.999999999999999) : myDimension.getY() * (1+percentNum));
                    break;
                default:
                    break;
            }
        }


        return myDimension;
    }

    /**
     * 获取指定操作人含token的header
     * @param batchId
     * @param roleName  角色名称 模糊匹配  未指定则随机取一
     * @param userRealName 人员名称  模糊匹配  未指定则随机取一
     * @return
     */
    public Map<String,String> getOperatorHeader(Long batchId,String roleName,String userRealName){
        List<CheckUser> allRoleUsers;
        CheckUser user;
        if (ObjectUtil.isNotEmpty(roleName)) {
            if (RoleTypeEnum.RectifyUser.getRoleName().contains(roleName)) {
                allRoleUsers = fhcyV2Mapper.getRectifyUsers(batchId);
            }else {
                allRoleUsers = fhcyV2Mapper.getAllRoleUsers(batchId);
                allRoleUsers = allRoleUsers.stream().filter(o -> o.getRoleName().contains(roleName)).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(allRoleUsers)) {
                throw new BusinessException("未匹配到目标角色的用户");
            }
        }else {
            throw new BusinessException("未指定角色类型，必填参数!");
        }

        if (ObjectUtil.isNotEmpty(userRealName)) {
            user = allRoleUsers.stream().filter(o -> o.getRealName().contains(userRealName)).findAny().orElse(allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size())));
        }else {
            user = allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size()));
        }

        UcUser ucUser = ucMapper.getUserById(user.getUserId());

        return TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(ucUser.getUserName(), EncryptUtils.decrypt(ucUser.getPassword()),currentEnv.getENV()));
    }

    public CheckUser getOperatorUser(Long batchId, String roleName, String userRealName){
        List<CheckUser> allRoleUsers;
        CheckUser user;
        if (ObjectUtil.isNotEmpty(roleName)) {
            if (RoleTypeEnum.RectifyUser.getRoleName().contains(roleName)) {
                allRoleUsers = fhcyV2Mapper.getRectifyUsers(batchId);
            }else {
                allRoleUsers = fhcyV2Mapper.getAllRoleUsers(batchId);
                allRoleUsers = allRoleUsers.stream().filter(o -> o.getRoleName().contains(roleName)).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(allRoleUsers)) {
                throw new BusinessException("未匹配到目标角色的用户");
            }
        }else {
            throw new BusinessException("未指定角色类型，必填参数!");
        }

        if (ObjectUtil.isNotEmpty(userRealName)) {
            user = allRoleUsers.stream().filter(o -> o.getRealName().contains(userRealName)).findAny().orElse(allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size())));
        }else {
            user = allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size()));
        }

        return user;
    }
}
