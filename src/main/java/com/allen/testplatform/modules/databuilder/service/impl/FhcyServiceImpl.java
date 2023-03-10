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
                //??????????????????????????????????????????
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
            throw new BusinessException("????????????[?????????]???????????????");
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
            throw new BusinessException("????????????nodeLimit???????????????  --------> 1-??????????????? 2-??????&???????????? 3-??????&??????&????????????");
        }
    }

    /**
     * ???????????? ???????????????????????? ??????GROUP_CONCAT(t.id) AS ticketIds ????????????MYSQL????????????????????????????????????id??????????????????
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
                //??????????????????????????????????????????
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiFHCY.APP_RETURN_PROBLEM), header, JSONObject.toJSONString(ticketIds));
                log.info("{}",JSON.parseObject(rs));
            }
        });
    }

    /**
     * ??????????????????() ???????????????
     */
    public void addProblemsV2(String batchName,String firstCheckName,String secondCheckName,String lastCheckName,
                                  String banName,String unitName,String floorName,String roomName,
                                  String projectSiteName,String rectifyUserName,String checkUserName,String importance,Integer checkImageIndex,
                                  String positionName,String nearDirection,String nearPercent,Double x,Double y,boolean batchThread,Integer testCount                    //??????????????????
    )
    {
        testCount = ObjectUtil.isNotEmpty(testCount) && testCount > 0 ? testCount : 1;
        CheckBatch batch = fhcyV2Mapper.getCheckBatch(batchName);
        if (ObjectUtil.isEmpty(batch)) {
            throw new BusinessException("????????????????????????,?????????????????????????????????");
        }
        Map<String, String> checkHeader = getOperatorHeader(batch.getId(),"??????",checkUserName);

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
                        throw new BusinessException("?????????????????????,??????????????????");
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
                                log.info("??????????????????&??????!,??????????????????????????????????????? {}",batchRoomInfo);
                            }else {
                                log.info("???????????????&??????! {}",batchRoomInfo);
                            }
                        }else {
                            log.info("???????????????");
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
                        throw new BusinessException("??????????????????,??????????????????");
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
            throw new BusinessException("????????????????????????????????????,???????????????!");
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
//        MyDimension dimension = setDimension("??????", null, null, null, null);
        problemVo.setPointX(RandomUtil.randomDouble(0.0000000000000001,1));
        problemVo.setPointY(RandomUtil.randomDouble(0.0000000000000001,1));
        problemVo.setNotes("???????????????-????????????-".concat(TicketProcessEnum.Create.getProcessDesc()).concat("-?????????:")+rectifyUser.getRealName()+DateUtils.current());
        problemVo.setAddTime(current);
    }

    /**
     * ?????????????????? ???????????????
     */
    public List<Long> addProblems(String batchName,String firstCheckName,String secondCheckName,String lastCheckName,
                                  String banName,String unitName,String floorName,String roomName,
                                  String projectSiteName,String rectifyUserName,String checkUserName,String importance,Integer checkImageIndex,
                                  String positionName,String nearDirection,String nearPercent,Double x,Double y,boolean batchThread,Integer testCount                    //??????????????????
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

        Map<String, String> checkHeader = getOperatorHeader(problemVoList.get(0).getCheckBatchId(),"??????",checkUserName);

        List<Long> problemIds = new ArrayList<>();

        if (batchThread) {
            //?????????????????????
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
            //????????????????????????
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
            log.info("????????????????????????????????????!");
        }
    }

    /**
     * ???????????? ???????????????????????? ??????GROUP_CONCAT(t.id) AS ticketIds ????????????MYSQL????????????????????????????????????id??????????????????
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
            log.info("???????????????????????????!");
        }
    }



    /**
     * ?????????????????? 1-??????  2-????????? 3-?????? ???????????????????????? ?????????????????????
     */
    public ReviewProblemVo buildReviewPro(Long ticketId,String reviewTypeName,CheckUser user) {
        int type;
        ReviewProblemVo reviewProblemVo = null;
        Assert.assertNotNull(reviewTypeName,"????????????????????????");

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
        reviewProblemVo.setContent("???????????????-????????????-".concat(TicketProcessEnum.getTargetDesc(operateCode)).concat("-?????????:")+user.getRealName()+DateUtils.current());
        return reviewProblemVo;
    }

    /**
     * ??????????????????
     */
    public FhcyProblemVo buildProblem(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                                      String banName, String unitName, String floorName, String roomName, String projectSiteName,
                                      String rectifyUserName, String importance, Integer checkImageIndex) {

        FhcyProblemVo problemVo = new FhcyProblemVo();
        CheckBatch batch = fhcyV2Mapper.getCheckBatch(batchName);
        if (ObjectUtil.isEmpty(batch)) {
            throw new BusinessException("????????????????????????,?????????????????????????????????");
        }

        problemVo.setCheckBatchId(batch.getId());
        problemVo.setCategory(batch.getCategory());

        List<String> checkCodeList = fhcyV2Mapper.getCheckCodeList(batch.getCheckListId(), null, firstCheckName, secondCheckName, lastCheckName);
        List<JSONObject> parentPathList = fhcyV2Mapper.getItemParentPath(batch.getCheckListId(), checkCodeList.get(RandomUtil.randomInt(checkCodeList.size())));
        if (CollectionUtils.isNotEmpty(parentPathList)) {
            BeanUtil.copyProperties(parentPathList.get(RandomUtil.randomInt(parentPathList.size())),problemVo);
        }else {
            throw new BusinessException("?????????????????????,??????????????????");
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
                    log.info("??????????????????&??????!,??????????????????????????????????????? {}",batchRoomInfo);
                }else {
                    log.info("???????????????&??????! {}",batchRoomInfo);
                }
            }else {
                log.info("???????????????");
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
            throw new BusinessException("??????????????????,??????????????????");
        }

        List<CheckUser> rectifyUsers = fhcyV2Mapper.getRectifyUsers(batch.getId());
        setProblemRectifyInfo(rectifyUserName,importance,rectifyUsers,problemVo);
        return problemVo;
    }

    /**
     * ????????????????????????
     * @param positionName  ?????????????????? ?????????:  ?????? ?????? ?????? ?????? ?????? ??????
     * @param nearDirection   ????????????  ?????????: ??? ??? ??? ???
     * @param nearPercent  ??????????????? ???????????????????????? 100% 50% 20%
     * @param x ???????????? X
     * @param y ???????????? Y
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
                    throw new BusinessException("?????????????????????????????????1 ??? ??????0");
                }
            }

            switch (nearDirection) {
                case "???":
                    myDimension.setY(myDimension.getY() * (1-percentNum));
                    break;
                case "???":
                    myDimension.setY(myDimension.getY() * (1+percentNum) >= 1 ?  RandomUtil.randomDouble(0.90,0.999999999999999) : myDimension.getY() * (1+percentNum));
                    break;
                case "???":
                    myDimension.setX(myDimension.getX() * (1-percentNum));
                    break;
                case "???":
                    myDimension.setX(myDimension.getX() * (1+percentNum) >= 1 ?  RandomUtil.randomDouble(0.90,0.999999999999999) : myDimension.getY() * (1+percentNum));
                    break;
                default:
                    break;
            }
        }


        return myDimension;
    }

    /**
     * ????????????????????????token???header
     * @param batchId
     * @param roleName  ???????????? ????????????  ????????????????????????
     * @param userRealName ????????????  ????????????  ????????????????????????
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
                throw new BusinessException("?????????????????????????????????");
            }
        }else {
            throw new BusinessException("????????????????????????????????????!");
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
                throw new BusinessException("?????????????????????????????????");
            }
        }else {
            throw new BusinessException("????????????????????????????????????!");
        }

        if (ObjectUtil.isNotEmpty(userRealName)) {
            user = allRoleUsers.stream().filter(o -> o.getRealName().contains(userRealName)).findAny().orElse(allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size())));
        }else {
            user = allRoleUsers.get(RandomUtil.randomInt(allRoleUsers.size()));
        }

        return user;
    }
}
