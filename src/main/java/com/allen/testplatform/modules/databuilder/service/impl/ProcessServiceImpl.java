package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.constant.HostCommon;
import com.allen.testplatform.modules.databuilder.enums.CheckTypeEnum;
import com.allen.testplatform.modules.databuilder.enums.RoleTypeEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketStatusType;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.feign.QualityCheckServiceFeign;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.mapper.ProcessCheckFlowMapper;
import com.allen.testplatform.modules.databuilder.mapper.ProcessCheckHandlerMapper;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.RoomQuery;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.process.dto.DetailQuery;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetail;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetailCheckFlow;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetailHandler;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetailIssue;
import com.allen.testplatform.modules.databuilder.model.process.vo.ProcessDetailIssueRectifyVo;
import com.allen.testplatform.modules.databuilder.model.process.vo.ProcessDetailVo;
import com.allen.testplatform.modules.databuilder.model.process.vo.ProcessIssueVo;
import com.allen.testplatform.modules.databuilder.service.ProcessService;
import com.allen.testplatform.testscripts.api.ApiProcess;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jayway.jsonpath.JsonPath;
import com.xiaoleilu.hutool.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.testng.Assert;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 1???????????????  ??????&??????&?????? ??????
 * ?????????????????????????????????????????????????????? ??????????????????????????????????????????????????????????????????,  ???????????? ????????????????????????????????????????????????????????????????????????-?????????-??????????????????
 *  ??????????????????????????????????????? ????????????-   ?????????????????????  ??????????????????????????????????????????????????????????????????  ???????????? ???????????? ,????????????????????????????????????????????????
 *  ???????????? ???????????????????????????,??????????????????  ??????: ?????????????????????-??????????????????-????????????????????????????????????
 * 2????????????????????? ??????-??????-????????????
 *  ????????????????????????????????????????????? ??????????????????
 *  ????????????????????? ??? ????????????????????????????????????????????????
 */
@Slf4j
@Service
public class ProcessServiceImpl implements ProcessService {

    @Resource
    private ProcessV2Mapper processMapper;

    @Resource
    private ProcessCheckFlowMapper flowMapper;

    @Resource
    private ProcessCheckHandlerMapper handlerMapper;

    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private QualityCheckServiceFeign processFeign;

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
    private List<UcUser> inspectorCompanyList;
    private List<UcUser> acceptorCompanyList;

    @Override
    public void addBatchProblem(Long detailId, String acceptorName, String rectifyName, String reviewName,
                         String banName, String floorName, String unitName, String roomName,
                         Integer severity, Integer deadlineDay, Integer pictureNum, Integer testCount, Integer nodeLimit){

        List<ProcessDetailDto> detailVos = processFeign.getDetail(detailId).getBody();
        Assert.assertEquals(detailVos.size(),1,"?????????????????????!");
        ProcessDetailDto processDetailDto = detailVos.get(0);
        updateSectionInfo(processDetailDto.getSectionName(),null);
        ProcessIssueVo issueVo = new ProcessIssueVo();
        issueVo.setDetailId(detailId);
        BeanUtil.copyProperties(processDetailDto,issueVo);

        List<ProcessDetailHandler> acceptors = new ArrayList<>();
        processDetailDto.getCheckFlows().forEach(flow -> {
            if (flow.getNode() == 1) {
                acceptors.addAll(flow.getAcceptor());
            }
        });

        //???????????????
        ProcessDetailHandler acceptor;
        if (ObjectUtil.isNotEmpty(acceptorName)) {
            acceptor = acceptors.stream().filter(o -> o.getRealName().contains(acceptorName)).findFirst().orElse(acceptors.get(RandomUtil.randomInt(acceptors.size())));
        }else {
            acceptor = acceptors.get(RandomUtil.randomInt(acceptors.size()));
        }
        issueVo.setDetailId(acceptor.getDetailId());
        issueVo.setRealName(acceptor.getRealName());
        issueVo.setUserId(acceptor.getUserId());
        issueVo.setCompanyGuid(ObjectUtil.isNotEmpty(acceptor.getCompanyGuid()) ? acceptor.getCompanyGuid():null);
        issueVo.setCompanyName(ObjectUtil.isNotEmpty(acceptor.getCompanyName()) ? acceptor.getCompanyName():null);
        issueVo.setFlowId(acceptor.getNextFlowId());

        UcUser user = ucMapper.getUserById(acceptor.getUserId());
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));

        setIssueUser(rectifyName,reviewName,issueVo);

        RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
        List<JSONObject> partList = processMapper.getProblemPartInfo(roomQuery, false);
        if (CollectionUtils.isNotEmpty(partList)) {

            int count = ObjectUtil.isNotEmpty(testCount) && testCount > 0 ? testCount : 1;

            AtomicInteger success = new AtomicInteger();
            List<CompletableFuture<Void>> futures = IntStream.rangeClosed(1, count)
                    .mapToObj(n ->
                            CompletableFuture.runAsync(() -> {
                                ProcessIssueVo issueTemp = issueVo;
                                JSONObject partInfo = partList.get(RandomUtil.randomInt(partList.size()));
                                setIssueCheckPart(partInfo,issueTemp,roomQuery);
                                setIssueOthers(issueTemp,pictureNum,severity,deadlineDay);
                                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_ISSUE_SAVE), header, JSONObject.toJSONString(issueTemp));
                                log.info("{}", JSON.parseObject(rs));
                                success.incrementAndGet();
                                throw new RuntimeException("????????????????????????: " + n);
                            }, taskExecutor)).collect(Collectors.toList());
            CompletableFuture<Void> headerFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));
            try {
                headerFuture.join();
            } catch (Exception ex) {
                System.out.println("done count: " + success.get());
            }
//            for (int i = 0; i < count; i++) {
//                taskExecutor.execute(() -> {
//                    ProcessDetailIssueVo issueTemp = issueVo;
//                    JSONObject partInfo = partList.get(RandomUtil.randomInt(partList.size()));
//                    setIssueCheckPart(partInfo,issueTemp,roomQuery);
//                    setIssueOthers(issueTemp,pictureNum,severity,deadlineDay);
//                    String s = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_ISSUE_SAVE), header, JSONObject.toJSONString(issueTemp));
//                    log.info("{}", JSON.parseObject(s));
//                });


//                CompletableFuture future = CompletableFuture.runAsync(() -> {
//                    ProcessDetailIssueVo issueTemp = issueVo;
//                    JSONObject partInfo = partList.get(RandomUtil.randomInt(partList.size()));
//                    setIssueCheckPart(partInfo,issueTemp,roomQuery);
//                    setIssueOthers(issueTemp,pictureNum,severity,deadlineDay);
//                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_ISSUE_SAVE), header, JSONObject.toJSONString(issueTemp));
//                    log.info("{}", JSON.parseObject(rs));
//                },taskExecutor);
//            }

            if (nodeLimit >= 2 ) {
                while (headerFuture.isDone()) {

                    List<String> status = new ArrayList<>();
                    status.add(TicketStatusType.GXYS_PROBLEM_WAIT_COMPLATE.getCode());
                    status.add(TicketStatusType.GXYS_PROBLEM_RE_COMPLATE.getCode());
                    List<ProcessDetailIssue> issues = processMapper.getIssuesInDetail(detailId,status);
                    if (CollectionUtils.isNotEmpty(issues)) {

                        List<CompletableFuture<Void>> futures2 = IntStream.rangeClosed(0, issues.size()-1)
                                .mapToObj(n ->
                                        CompletableFuture.runAsync(() -> {
                                            String rs = recitfyOrReviewOne(issues.get(n), 3, TicketProcessEnum.CompleteRectify.getProcessDesc(), null, RandomUtil.randomInt(6));
                                            log.info("{}", JSON.parseObject(rs));
                                            throw new RuntimeException("????????????????????????: " + n);
                                        }, taskExecutor)).collect(Collectors.toList());
                        CompletableFuture<Void> headerFuture2 = CompletableFuture.allOf(futures2.toArray(new CompletableFuture[]{}));
                        try {
                            headerFuture2.join();
                            if (nodeLimit >= 3) {
                                while (headerFuture2.isDone()) {
                                    recitfyOrReview(detailId,5,RandomUtil.randomInt(6),null);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
//                        issues.forEach(issue -> {
//
//                            taskExecutor.execute(() -> {
//                                String rs = recitfyOrReviewOne(issue, operateType, operateTypeName, secondRecitifyName, pictureNum);
//                                log.info("{}", JSON.parseObject(rs));
//                            });
//                        });


                    }else {
                        throw new BusinessException("???????????????????????????[?????????][????????????]???????????????");
                    }
                }
            }

        }else {
            throw  new BusinessException("????????????????????????! ??????????????????");
        }
    }

    @Override
    public void submitAndAcceptOrSpotCheckOne(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                              String banName,String floorName,String unitName,String roomName,
                                              Integer nodeLimit){
        if (ObjectUtil.isNotEmpty(sectionName)) {
            updateSectionInfo(sectionName,sectionType);
        }else {
            throw new BusinessException("???????????????,??????????????????");
        }

        ProcessDetailVo detail = buildOneDetail(inspectorName,lastCheckName,parentCheckName,banName,floorName,unitName,roomName);
        UcUser inspector = ucMapper.getUserByOthers(null, detail.getRealName(), null, detail.getCompanyGuid(), Constant.SUPPLIER_SOURCE).get(0);
        Map<String,String> header_inspector = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(inspector.getUserName(),EncryptUtils.decrypt(inspector.getPassword()),currentEnv.getENV()));
        if (nodeLimit >= 1) {
            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_inspector, JSONObject.toJSONString(detail));
            if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.body"))) {
                Long detailId = Long.valueOf(JsonPath.read(rs,"$.body"));
                detail.setDetailId(detailId);
                log.info("???????????????: [{}] ??????????????????! ??????ID:[{}],???????????? [{}] ?????????path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
            }else {
                log.info("????????????,???????????????ID  ???????????? [{}] ?????????path:[{}],????????????:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
            }

            if (nodeLimit >= 2 ) {
                callAcceptOrSpotCheck(detail);
            }

            if (nodeLimit >= 3) {
                callAcceptOrSpotCheck(detail);
            }
        }else {
            throw new BusinessException("?????????nodeLimit!");
        }
    }

    @Override
    public void submitAndAcceptOrSpotCheckBatch(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                                String banName,String floorName,String unitName,String roomName,
                                                Integer nodeLimit) {
        if (ObjectUtil.isNotEmpty(sectionName)) {
            updateSectionInfo(sectionName,sectionType);
        }else {
            throw new BusinessException("???????????????,??????????????????");
        }

        List<ProcessDetailVo> detailList = createBatchDetail(inspectorName, lastCheckName, parentCheckName,banName, floorName, unitName, roomName);
        UcUser inspector = ucMapper.getUserByOthers(null, detailList.get(0).getRealName(), null, detailList.get(0).getCompanyGuid(), Constant.SUPPLIER_SOURCE).get(0);
        Map<String,String> header_inspector = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(inspector.getUserName(),EncryptUtils.decrypt(inspector.getPassword()),currentEnv.getENV()));
        if (nodeLimit >= 1) {

//            List<ProcessDetailVo> acceptDetailList = new ArrayList<>();
            detailList.forEach(detail -> {

                CompletableFuture<ProcessDetailVo> future = CompletableFuture.supplyAsync(() -> {
                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_inspector, JSONObject.toJSONString(detail));
                    log.info("{}", JSON.parseObject(rs));
                    if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.body"))) {
                        Long detailId = Long.valueOf(JsonPath.read(rs,"$.body"));
                        detail.setDetailId(detailId);
//                        acceptDetailList.add(detail);
                        log.info("???????????????: [{}] ??????????????????! ??????ID:[{}],???????????? [{}] ?????????path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
                    }else {
                        log.info("????????????,???????????????ID  ???????????? [{}] ?????????path:[{}],????????????:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
                    }
                    ProcessDetailVo detailAfter = detail;
                    return detailAfter;
                },taskExecutor).thenApply(detailAfter -> {
                    if (nodeLimit >= 2 ) {
                        callAcceptOrSpotCheck(detailAfter);
                    }
                    return detailAfter;
                }).thenApply(detailAfter -> {
                    if (nodeLimit >= 3) {
                        callAcceptOrSpotCheck(detailAfter);
                    }
                    return detailAfter;
                });
//                taskExecutor.execute(() -> {
//                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_inspector, JSONObject.toJSONString(detail));
//                    log.info("{}", JSON.parseObject(rs));
//                    if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.body"))) {
//                        Long detailId = Long.valueOf(JsonPath.read(rs,"$.body"));
//                        detail.setDetailId(detailId);
//                        acceptDetailList.add(detail);
//                        log.info("???????????????: [{}] ??????????????????! ??????ID:[{}],???????????? [{}] ?????????path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
//                    }else {
//                        log.info("????????????,???????????????ID  ???????????? [{}] ?????????path:[{}],????????????:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
//                    }
//                });
            });

//            System.out.println("===========2==========");
//            if (nodeLimit >= 2 ) {
//
//                acceptDetailList.forEach(detail -> {
//                    taskExecutor.execute(() -> {
//                        callAcceptOrSpotCheck(detail);
//                    });
//                });
//            }
//
//            System.out.println("===========3==========");
//
//            if (nodeLimit >= 3) {
//                acceptDetailList.forEach(detail -> {
//                    taskExecutor.execute(() -> {
//                        callAcceptOrSpotCheck(detail);
//                    });
//                });
//            }
        }else {
            log.info("?????????nodeLimit!");
        }
    }

    //????????????detailId??????????????????????????????????????????
    @Override
    public void recitfyOrReview(Long detaiLId,Integer operateType,Integer pictureNum,String secondRecitifyName) {
        List<String> status = new ArrayList<>();
        String operateTypeName;
        switch (operateType) {
            case 1:
                SectionInfo section = processMapper.getSectionByIssue(detaiLId, null);
                updateSectionInfo(section.getSectionName(),section.getSectionType());
                status.add(TicketStatusType.GXYS_PROBLEM_WAIT_COMPLATE.getCode());
                operateTypeName = TicketProcessEnum.ReAssign.getProcessDesc();
                break;
            case 2:
                status.add(TicketStatusType.GXYS_PROBLEM_WAIT_VERIFY.getCode());
                operateTypeName = TicketProcessEnum.ReRectify.getProcessDesc();
                break;
            case 3:
                status.add(TicketStatusType.GXYS_PROBLEM_WAIT_COMPLATE.getCode());
                status.add(TicketStatusType.GXYS_PROBLEM_RE_COMPLATE.getCode());
                operateTypeName = TicketProcessEnum.CompleteRectify.getProcessDesc();
                break;
            case 4:
                status.add(TicketStatusType.GXYS_PROBLEM_WAIT_VERIFY.getCode());
                operateTypeName = TicketProcessEnum.UnNormalClose.getProcessDesc();
                break;
            case 5:
                status.add(TicketStatusType.GXYS_PROBLEM_WAIT_VERIFY.getCode());
                operateTypeName = TicketProcessEnum.NormalClose.getProcessDesc();
                break;
            default:
                throw new BusinessException("?????????????????????!");
        }
        List<ProcessDetailIssue> issues = processMapper.getIssuesInDetail(detaiLId,status);
        if (CollectionUtils.isNotEmpty(issues)) {
            issues.forEach(issue -> {
                taskExecutor.execute(() -> {
                    String rs = recitfyOrReviewOne(issue, operateType, operateTypeName, secondRecitifyName, pictureNum);
                    log.info("{}", JSON.parseObject(rs));
                });
            });
        }else {
            throw new BusinessException("????????????????????????????????????????????????");
        }
        log.info("????????????[{}]?????????[{}],[{}]?????????",detaiLId,operateTypeName,issues.size());
    }

    public String recitfyOrReviewOne(ProcessDetailIssue issue,Integer operateType,String operateTypeName,String secondRecitifyName,Integer pictureNum ){
        ProcessDetailIssueRectifyVo rectifyVo = new ProcessDetailIssueRectifyVo();
        UcUser user;
        switch (operateType) {
            //1:????????????
            case 1:
                user = ucMapper.getUserById(issue.getRectifyUserId());
                UcUser reRecitify = CommonUtils.findTargetUser(secondRecitifyName, inspectorCompanyList);
                if (ObjectUtil.isNotEmpty(reRecitify)) {
                    rectifyVo.setRectifyUserId(reRecitify.getUserId());
                    rectifyVo.setRectifyRealName(reRecitify.getRealName());
                    rectifyVo.setRectifyCompanyGuid(reRecitify.getProviderGuid());
                    rectifyVo.setRectifyCompanyName(reRecitify.getProviderName());
                }else {
                    throw new BusinessException("???????????????????????????!");
                }
                break;
            //3:????????????
            case 3:
                user = ucMapper.getUserById(issue.getRectifyUserId());
                break;
            //2:???????????? 4:???????????????,5:???????????? ?????????????????????
            case 2:
            case 4:
            case 5:
                user = ucMapper.getUserById(issue.getReviewUserId());

                break;
            default:
                throw new BusinessException("?????????????????????!");
        }

        if (ObjectUtil.isNotEmpty(user)) {
            if (Constant.SUPPLIER_SOURCE.equals(user.getSource())) {
                user = ucMapper.getUserByIdSource(user.getUserId(), Constant.SUPPLIER_SOURCE);
            }
        }else {
            throw new BusinessException("?????????????????????!");
        }

        rectifyVo.setCompanyName(user.getProviderName());
        rectifyVo.setCompanyGuid(user.getProviderGuid());

        rectifyVo.setId(issue.getId());
        rectifyVo.setRealName(user.getRealName());
        rectifyVo.setUserId(user.getUserId());

        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS_PROBLEM.getMsg()).append("-").append(operateTypeName)
                .append(" ????????????").append(user.getRealName()).append(DateUtils.now());
        rectifyVo.setRemark(stringBuilder.toString());
        rectifyVo.setPicture(TestDataUtils.getPicture(ObjectUtil.isNotEmpty(pictureNum) && pictureNum > 0 ? pictureNum : RandomUtil.randomInt(6)));
        rectifyVo.setSubmitTime(DateUtils.now());
        rectifyVo.setStatus(operateType);

        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getSource().equals(Constant.SUPPLIER_SOURCE) ? user.getPhone():user.getUserName(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));
        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_ISSUE_RECTIFY), header, JSONObject.toJSONString(rectifyVo));
        return rs;
    }

    //??????????????????????????? ???????????????  ????????????????????????
    @Override
    public void callAcceptOrSpotCheckById(Long detaiId, Integer operateType) {

        if (ObjectUtil.isEmpty(operateType)) {
            throw new BusinessException("????????????????????????");
        }
        if (operateType != 1 && operateType != 2) {
            throw new BusinessException("?????????????????????! 1-?????? 2-??????");
        }
        int detailStatus = operateType; //????????????    1 ?????????   2 ?????????
        int lastNode = operateType;     //??????????????????
        int currentNode = operateType + 1;//?????????????????? 1-?????? 2-?????? 3-??????

        //????????????(0:????????????,1:?????????,2:?????????,3:?????????)
        ProcessDetail processDetail = processMapper.selectOne(new QueryWrapper<ProcessDetail>().lambda().eq(ProcessDetail::getId, detaiId));
        if (ObjectUtil.isEmpty(processDetail)) {
            throw new BusinessException("????????????????????????,???????????????ID");
        }
        if (processDetail.getStatus() != detailStatus && processDetail.getStatus() != detailStatus-1) {
            throw new BusinessException("?????????????????????");
        }

        List<ProcessDetailCheckFlow> detailCheckFlows = flowMapper.selectList(
                new QueryWrapper<ProcessDetailCheckFlow>().lambda().eq(ProcessDetailCheckFlow::getDetailId,detaiId).
                        eq(ProcessDetailCheckFlow::getNode, currentNode).eq(ProcessDetailCheckFlow::getDelFlag, false));
//        List<JSONObject> detailCheckFlows = processMapper.getDetailHandleFlow(detaiId, null, currentNode);

        if (CollectionUtils.isEmpty(detailCheckFlows)) {
            throw new BusinessException("ProcessDetailCheckFlow????????????!");
        }

        ProcessDetailVo commonDetailVo = new ProcessDetailVo();
        commonDetailVo.setDetailId(detaiId);
        BeanUtil.copyProperties(processDetail,commonDetailVo);

        updateSectionInfo(processDetail.getSectionName(),null);

        List<ProcessDetailHandler> handlers = handlerMapper.selectList(new QueryWrapper<ProcessDetailHandler>().lambda().eq(ProcessDetailHandler::getDetailId, detaiId).eq(ProcessDetailHandler::getDelFlag, false));


        detailCheckFlows.forEach(flow -> {
            ProcessDetailVo processDetailVo = commonDetailVo;
            if (Objects.equals(flow.getNode(), TicketProcessEnum.AcceptProcess.getProcessCode()) && flow.getIfAppoint()) {
                List<ProcessDetailCheckFlow> nextFlows = flowMapper.selectList(new QueryWrapper<ProcessDetailCheckFlow>().lambda().eq(ProcessDetailCheckFlow::getDetailId,detaiId).eq(ProcessDetailCheckFlow::getNode, currentNode+1).eq(ProcessDetailCheckFlow::getDelFlag, false));

                //???????????????????????????
                List<String> selectedUsers = new ArrayList<>();
                //?????????
                selectedUsers.add(processDetail.getCreateUserName());
                handlers.forEach(handler -> {
                    //??????????????????
                    selectedUsers.add(handler.getRealName());
                });

                List<ProcessDetailVo.PersonnelDto> nowAcceptorList = new ArrayList<>(nextFlows.size());
                nextFlows.forEach(next -> {
                    int roleType = next.getRoleType();
                    UcUser target = null;

                    //????????????(2:??????(?????????????????????????????????),3:????????????,4:????????????)
                    if (roleType == 2){
                        do {
                            target = CommonUtils.findTargetUser(null,supervisorList);
                        }while (selectedUsers.contains(target.getRealName()));
                    }

                    if (roleType == 3 || roleType == 4) {
                        do {
                            target = CommonUtils.findTargetUser(null,managerUserList);
                        }while (selectedUsers.contains(target.getRealName()));
                    }
                    selectedUsers.add(target.getRealName());
                    setPersonalDto(target,next.getIfAppoint(),next.getFlowId(),nowAcceptorList);
                });
                processDetailVo.setAcceptor(nowAcceptorList);
            }else {
                processDetailVo.setAcceptor(null);
            }

//            //??????????????????????????????????????????   ?????????????????????
//            if (flow.getIntValue("node") == TicketProcessEnum.AcceptProcess.getProcessCode() && flow.getBoolean("ifAppoint")) {
//
//                List<JSONObject> nextFlows = processMapper.getDetailCheckFlow(detaiId, null, currentNode);
//
//                List<ProcessDetailVo.PersonnelDto> nowAcceptorList = new ArrayList<>();
//
//                nextFlows.forEach(next -> {
//                    int roleType = flow.getIntValue("roleType");
//                    UcUser target = null;
//
//                    //????????????(2:??????(?????????????????????????????????),3:????????????,4:????????????)
//                    if (roleType == 2){
//                        do {
//                            target = CommonUtils.findTargetUser(null,supervisorList);
//                        }while (selectedUsers.contains(target.getRealName()));
//                    }
//
//                    if (roleType == 3 || roleType == 4) {
//                        do {
//                            target = CommonUtils.findTargetUser(null,managerUserList);
//                        }while (selectedUsers.contains(target.getRealName()));
//                    }
//                    selectedUsers.add(target.getRealName());
//                    setPersonalDto(target,flow.getBoolean("ifAppoint"),flow.getLong("flowId"),nowAcceptorList);
//                });
//                processDetailVo.setAcceptor(nowAcceptorList);
//                selectedUsers.clear();
//            }else {
//                processDetailVo.setAcceptor(null);
//            }

            ProcessDetailHandler nowHandler = handlers.stream().filter(handle -> handle.getNextFlowId().equals(flow.getFlowId())).findFirst().orElse(null);

            //???????????????????????????
            setCheckPoint(processDetailVo);
            processDetailVo.setUserId(nowHandler.getUserId());
            processDetailVo.setRealName(nowHandler.getRealName());
            processDetailVo.setCompanyGuid(ObjectUtil.isNotEmpty(nowHandler.getCompanyGuid()) ? nowHandler.getCompanyGuid() : null);
            processDetailVo.setCompanyName(ObjectUtil.isNotEmpty(nowHandler.getCompanyName()) ? nowHandler.getCompanyName() : null);
            processDetailVo.setFlowId(flow.getFlowId());

            StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
            if (flow.getNode() == TicketProcessEnum.AcceptProcess.getProcessCode()) {
                stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.AcceptProcess.getProcessDesc())
                        .append(" ????????????").append(processDetail.getCreateUserName()).append(" ??????????????????").append(nowHandler.getRealName()).append(" ").append(DateUtils.now());
            }else if (flow.getNode() == TicketProcessEnum.SpotCheckProcess.getProcessCode()) {
                stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.SpotCheckProcess.getProcessDesc())
                        .append(" ????????????").append(processDetail.getCreateUserName()).append(" ??????????????????").append(nowHandler.getRealName()).append(" ").append(DateUtils.now());
            }
            processDetailVo.setComment(stringBuilder.toString());
            processDetailVo.setSubmitTime(DateUtils.now());

            UcUser user = ucMapper.getUserByIdSource(processDetailVo.getUserId(),ObjectUtil.isNotEmpty(processDetailVo.getCompanyGuid()) ? Constant.SUPPLIER_SOURCE:Constant.PS_SOURCE);
            Assert.assertNotNull(user.getUserName(),"?????????????????????!");
            log.info("???????????????: [{}] ??????????????????node=[{}] ??????ID:[{}],???????????? [{}] ?????????path:[{}]",processDetailVo.getRealName(),currentNode,detaiId,processDetailVo.getPartName(),processDetailVo.getCheckPathName());
            Map<String,String> header_acceptor = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()),currentEnv.getENV()));
            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_acceptor, JSONObject.toJSONString(processDetailVo));
            log.info("{}",JSON.parseObject(rs));
        });
    }

    @Override
    public void handleDetailByTarget(String orgName,String projectName,String stageName,String sectionName,
                                     Integer checkType,String checkPathName,String partName,
                                     String submitCompanyName,String acceptCompanyName,Integer operateType)
    {
        DetailQuery detailQuery = new DetailQuery();
        detailQuery.setOrgName(orgName);
        detailQuery.setProjectName(projectName);
        detailQuery.setStageName(stageName);
        detailQuery.setSectionName(sectionName);
        detailQuery.setCheckType(checkType);
        detailQuery.setCheckPathName(checkPathName);
        detailQuery.setPartName(partName);
        detailQuery.setSubmitCompanyName(submitCompanyName);
        detailQuery.setAcceptCompanyName(acceptCompanyName);
        detailQuery.setStatus(operateType == 1 ? "1":"1,2");

        List<Long> detailIdList = processMapper.getTargetDetailIdList(detailQuery);

        if (CollectionUtils.isEmpty(detailIdList)) {
            throw new BusinessException("??????????????????????????????");
        }

        detailIdList.forEach(detail -> {
            taskExecutor.execute(() -> {
                callAcceptOrSpotCheckById(detail,operateType);
            });
        });
    }

    @Override
    public void deleteDetailByTarget(String orgName,String projectName,String stageName,String sectionName,
                              Integer checkType,String checkPathName,String partName,
                              String submitCompanyName,String acceptCompanyName,String status)
    {
        DetailQuery detailQuery = new DetailQuery();
        detailQuery.setOrgName(orgName);
        detailQuery.setProjectName(projectName);
        detailQuery.setStageName(stageName);
        detailQuery.setSectionName(sectionName);
        detailQuery.setCheckType(checkType);
        detailQuery.setCheckPathName(checkPathName);
        detailQuery.setPartName(partName);
        detailQuery.setSubmitCompanyName(submitCompanyName);
        detailQuery.setAcceptCompanyName(acceptCompanyName);
        detailQuery.setStatus(status);

        List<Long> detailIdList = processMapper.getTargetDetailIdList(detailQuery);

        deleteList(detailIdList);
    }

    //??????????????????????????? ???????????????  ????????????????????????
    public void callAcceptOrSpotCheck(ProcessDetailVo submitDetailVo){
        List<String> selectedUsers = new ArrayList<>();
        selectedUsers.add(submitDetailVo.getRealName());

        List<ProcessDetailVo.PersonnelDto> acceptorList = submitDetailVo.getAcceptor();
        if (CollectionUtils.isNotEmpty(acceptorList)) {

            acceptorList.forEach(i -> {
                selectedUsers.add(i.getRealName());
            });

            acceptorList.forEach(i -> {

                JSONObject thisFlow = processMapper.getDetailCheckFlow(submitDetailVo.getDetailId(),i.getNextFlowId(),null).get(0);
                //?????????????????????????????????????????? ???????????? ????????????????????????????????????????????????
                if (i.getIfAppoint()) {
                    //????????????= 2-?????? ??? ??????????????? 3-??????
                    if (thisFlow.getIntValue("node") == 2) {
                        List<JSONObject> nextFlowList = processMapper.getDetailCheckFlow(submitDetailVo.getDetailId(), null, 3);
                        if (CollectionUtils.isNotEmpty(nextFlowList)) {
                            List<ProcessDetailVo.PersonnelDto> nowAcceptorList = new ArrayList<>();
                            nextFlowList.forEach(x -> {
                                int roleType = x.getIntValue("roleType");
                                UcUser target = null;

                                //????????????(2:??????(?????????????????????????????????),3:????????????,4:????????????)
                                if (roleType == 2){
                                    do {
                                        target = CommonUtils.findTargetUser(null,supervisorList);
                                    }while (selectedUsers.contains(target.getRealName()));
                                }

                                if (roleType == 3 || roleType == 4) {
                                    do {
                                        target = CommonUtils.findTargetUser(null,managerUserList);
                                    }while (selectedUsers.contains(target.getRealName()));
                                }
                                setPersonalDto(target,x.getBoolean("ifAppoint"),x.getLong("flowId"),nowAcceptorList);
                                selectedUsers.add(target.getRealName());
                                submitDetailVo.setAcceptor(nowAcceptorList);
                            });
                        }else {
                            log.info("???????????????????????????,???????????????-??????(node=2)???????????????!");
                        }
                    }else {
                        log.info("??????????????????node=[{}] ,?????????????????????", thisFlow.getIntValue("node"));
                    }
                }else {
                    //???????????????????????????????????????
                    submitDetailVo.setAcceptor(null);
                }
                //???????????????????????????
                setCheckPoint(submitDetailVo);
                //???????????? flowId ????????????????????? ???????????? ????????????
                if (ObjectUtil.isNotEmpty(i.getCompanyGuid())) {
                    submitDetailVo.setCompanyGuid(i.getCompanyGuid());
                    submitDetailVo.setCompanyName(i.getCompanyName());
                }else {
                    submitDetailVo.setCompanyName(null);
                    submitDetailVo.setCompanyGuid(null);
                }
                submitDetailVo.setFlowId(i.getNextFlowId());
                if (thisFlow.getIntValue("node") == 2) {
                    submitDetailVo.setComment("???????????????-????????????-??????".concat(submitDetailVo.getCheckPathName()).concat("-").concat(i.getRealName()).concat("-").concat(DateUtils.now()));
                }else {
                    submitDetailVo.setComment("???????????????-????????????-??????:".concat(submitDetailVo.getCheckPathName()).concat("-").concat(i.getRealName()).concat("-").concat(DateUtils.now()));
                }
                submitDetailVo.setSubmitTime(DateUtils.now());

                //???????????????????????????
//            submitDetailDto.setCommonAcceptor();
                //?????????????????????
//            submitDetailDto.setCarbonCopy();

                UcUser user = ucMapper.getUserByIdSource(i.getUserId(),ObjectUtil.isNotEmpty(i.getCompanyGuid()) ? Constant.SUPPLIER_SOURCE:Constant.PS_SOURCE);
                Assert.assertNotNull(user.getUserName(),"?????????????????????!");
                log.info("???????????????: [{}] ??????????????????node=[{}] ??????ID:[{}],???????????? [{}] ?????????path:[{}]",i.getRealName(),thisFlow.getIntValue("node"),submitDetailVo.getDetailId(),submitDetailVo.getPartName(),submitDetailVo.getCheckPathName());
                Map<String,String> header_acceptor = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()),currentEnv.getENV()));
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_acceptor, JSONObject.toJSONString(submitDetailVo));
                log.info("{}",JSON.parseObject(rs));
            });
        }else {
            log.info("?????????????????????! ");
        }
    }

    /**
     * ????????????????????????
     * @param detailIdList ??????ID List
     */
    void deleteList(List<Long> detailIdList){

        Map<String,String> header_admin = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001","a123456","UAT"));

        if (CollectionUtils.isNotEmpty(detailIdList)) {
            Map<String, String> finalHeader_admin = header_admin;

            detailIdList.forEach(i -> {
                taskExecutor.execute(() -> {
                    String params = "id=".concat(i.toString());
                    String rs = HttpUtils.doGet(HostCommon.UAT.concat(ApiProcess.PC_DETAIL_DELETE), finalHeader_admin, params);
                    Assert.assertNotNull(JsonPath.read(rs,"$.message"),"??????????????????");
                });
            });
            log.info("????????????[{}]?????????",detailIdList.size());
        }else {
            throw new BusinessException("????????????????????????");
        }
    }

    public void setIssueUser(String rectifyName, String reviewName, ProcessIssueVo issueVo) {
        //?????????
        UcUser rectify = CommonUtils.findTargetUser(rectifyName, inspectorCompanyList);
        issueVo.setRectifyUserId(rectify.getUserId());
        issueVo.setRectifyRealName(rectify.getRealName());
        issueVo.setRectifyCompanyName(ObjectUtil.isNotEmpty(rectify.getProviderName()) ? rectify.getProviderName() : null);
        issueVo.setRectifyCompanyGuid(ObjectUtil.isNotEmpty(rectify.getProviderGuid()) ? rectify.getProviderGuid() : null);

        //?????????
        UcUser review = CommonUtils.findTargetUser(reviewName, acceptorCompanyList);
        issueVo.setReviewUserId(review.getUserId());
        issueVo.setReviewRealName(review.getRealName());
    }

    public void setIssueOthers(ProcessIssueVo issueVo, Integer pictureNum, Integer severity, Integer deadlineDay){
        issueVo.setPicture(ObjectUtil.isNotEmpty(pictureNum) && pictureNum > 0 ? TestDataUtils.getPicture(pictureNum) : TestDataUtils.getPicture(RandomUtil.randomInt(10)));
        issueVo.setSeverity(ObjectUtil.isNotEmpty(severity) && MathUtils.rangeInDefined(severity,1,3) ? severity : RandomUtil.randomInt(1,4));
        issueVo.setSubmitTime(DateUtils.now());
        issueVo.setDeadlineDay(ObjectUtil.isNotEmpty(deadlineDay) && MathUtils.rangeInDefined(deadlineDay,1,100) ? deadlineDay : RandomUtil.randomInt(3,101));
        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS_PROBLEM.getMsg()).append(TicketProcessEnum.Create.getProcessDesc()).append("?????????-").append(issueVo.getRectifyRealName()).append(" ?????????-").append(issueVo.getReviewRealName()).append(DateUtils.current());
        issueVo.setRemark(stringBuilder.toString());
    }

    public void setIssueCheckPart(JSONObject partInfo, ProcessIssueVo issueVo, RoomQuery roomQuery) {
        if (ObjectUtil.isNotEmpty(roomQuery.getRoomName())) {
            setRoomPart(partInfo,issueVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setFloorPart(partInfo,issueVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getUnitName()) && ObjectUtil.isEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setUnitPart(partInfo,issueVo);
        }else if (ObjectUtil.isNotEmpty(roomQuery.getBanName()) && ObjectUtil.isEmpty(roomQuery.getUnitName()) && ObjectUtil.isEmpty(roomQuery.getFloorName()) && ObjectUtil.isEmpty(roomQuery.getRoomName())) {
            setBanPart(partInfo,issueVo);
        }else {
            setRoomPart(partInfo,issueVo);
        }
    }

    public void setRoomPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        issueVo.setFloor(ObjectUtil.isNotEmpty(partInfo.getString("floor")) ? partInfo.getString("floor"):null);
        issueVo.setRoomCode(ObjectUtil.isNotEmpty(partInfo.getString("roomCode")) ? partInfo.getString("roomCode"):null);
        issueVo.setRoomName(ObjectUtil.isNotEmpty(partInfo.getString("rooms")) ? partInfo.getString("rooms"):null);
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("??????").concat(" - ").concat(partInfo.getString("rooms"))
                : issueVo.getBanName().concat(" - ").concat(partInfo.getString("rooms")));
        if (ObjectUtil.isNotEmpty(partInfo.getString("checkImageUrl"))) {
            issueVo.setCheckImageUrl(partInfo.getString("checkImageUrl"));
            issueVo.setHouseTypeId(partInfo.getLong("houseTypeId"));
            issueVo.setPointX(RandomUtil.randomDouble(0.0000000000000001,1));
            issueVo.setPointY(RandomUtil.randomDouble(0.0000000000000001,1));
        }
    }

    public void setFloorPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        issueVo.setFloor(ObjectUtil.isNotEmpty(partInfo.getString("floor")) ? partInfo.getString("floor"):null);
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("??????").concat(" - ").concat(partInfo.getString("floor")).concat("???") : issueVo.getBanName());
    }

    public void setUnitPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("??????") : issueVo.getBanName());
    }

    public void setBanPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setPartName(issueVo.getBanName());
    }

    /**
     * ????????????????????????????????????
     * @param sectionName
     * @param sectionType
     */
    public void updateSectionInfo(String sectionName,Integer sectionType){

        sectionInfo = processMapper.getSectionInfo(sectionName,null,sectionType);
        sectionType = sectionInfo.getSectionType();
        contractorList = ucMapper.getSupplierUsers(sectionInfo.getContractorGuid());
        constructorList = ucMapper.getSupplierUsersByList(sectionInfo.getConstructionGuid());
        supervisorList = ucMapper.getSupplierUsers(sectionInfo.getSupervisorGuid());
        String category = setCategory(sectionType);
        managerUserList = ucMapper.getUsersByIdList(processMapper.getBatchUserId(sectionInfo.getStageCode(), RoleTypeEnum.ManageUser.getRoleCode(), category),Constant.PS_SOURCE);

        inspectorCompanyList = new ArrayList<>();
        inspectorCompanyList.addAll(constructorList);
        inspectorCompanyList.addAll(contractorList);
        acceptorCompanyList = new ArrayList<>();
        acceptorCompanyList.addAll(supervisorList);
        acceptorCompanyList.addAll(managerUserList);
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
                throw new IllegalStateException("??????????????????! sectionType:" + sectionType);
        }
        return category;
    }

    public ProcessDetailVo buildOneDetail(String inspectorName, String lastCheckName,String parentCheckName,String banName, String floorName, String unitName, String roomName) {
        ProcessDetailVo detail = new ProcessDetailVo();
        //????????????????????????
        setSectionInfo(detail,sectionInfo);
        //?????????????????????
        setCheckInfo(lastCheckName, parentCheckName,detail);
        //?????????-?????????????????????
        setCheckPoint(detail);
        //?????????????????? ???????????? ??? ???????????????
        UcUser inspector = CommonUtils.findTargetUser(inspectorName,inspectorCompanyList);
        detail.setCompanyName(inspector.getProviderName());
        detail.setCompanyGuid(inspector.getProviderGuid());
        detail.setRealName(inspector.getRealName());
        //?????????-?????????????????? (1:??????,2:??????,3:??????)
        setCheckFlow(detail);
        //???????????????????????? ??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)
        RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
        int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
        log.info("??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)  ?????????????????????: [{}]",checkPartType);
        if (checkPartType >= 4) {
            setCheckPart45(checkPartType,roomQuery,detail.getCheckId(),detail.getSectionId(),detail);
        }else {
            setCheckPart123(checkPartType,roomQuery,detail.getCheckId(),detail.getSectionId(),detail);
        }
        //???????????? ???????????? ????????????
        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.CreateProcess.getProcessDesc())
                .append(" ?????????:").append(detail.getCheckPathName()).append(" ????????????: ").append(detail.getPartName()).append(" ????????????").append(detail.getRealName()).append(DateUtils.now());
        detail.setComment(stringBuilder.toString());
        detail.setSubmitTime(DateUtils.now());
        return detail;
    }

    public ProcessDetailVo buildDetailCommon(String inspectorName) {
        ProcessDetailVo detail = new ProcessDetailVo();
        //????????????????????????
        setSectionInfo(detail,sectionInfo);
        //?????????????????? ???????????? ??? ???????????????
        UcUser inspector = CommonUtils.findTargetUser(inspectorName,inspectorCompanyList);
        detail.setCompanyName(inspector.getProviderName());
        detail.setCompanyGuid(inspector.getProviderGuid());
        detail.setRealName(inspector.getRealName());
        if (ObjectUtil.isNotEmpty(detail.getCheckId())){
            //??????-???????????????????????? (1:??????,2:??????,3:??????)
            setCheckFlow(detail);
        }
        return detail;
    }

    public List<ProcessDetailVo> createBatchDetail(String inspectorName, String lastCheckName,String parentCheckName, String banName, String floorName, String unitName, String roomName) {
        List<ProcessDetailVo> detailDtoList = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(lastCheckName)) {
            //??????????????????????????? ???????????????????????????????????????

            //???????????? ????????????-?????????-?????????????????????
            ProcessDetailVo detail = buildDetailCommon(inspectorName);
            //?????????????????????
            setCheckInfo(lastCheckName,parentCheckName ,detail);

            //???????????????????????? ??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)
            RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
            int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
            log.info("??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)  ?????????????????????: [{}]",checkPartType);
            List<JSONObject> batchCheckPart = getBatchCheckPart(checkPartType, roomQuery, detail.getCheckId(), detail.getSectionId());
            try {
                batchCheckPart.forEach(o -> {
//                    taskExecutor.execute(() -> {
                        ProcessDetailVo detailTemp = new ProcessDetailVo();
                        detailTemp = setDetailTemp(detail, o, checkPartType);
                        //??????????????????????????????
                        setCheckFlow(detailTemp);
                        detailDtoList.add(detailTemp);
//                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //????????????????????????,????????????????????????????????????

            //???????????? ????????????-?????????-?????????????????????
            List<JSONObject> lastChecks = processMapper.getLastCheck(null,null);
            lastChecks.forEach(lastCheck -> {
                ProcessDetailVo detail = buildDetailCommon(inspectorName);
                BeanUtil.copyProperties(lastCheck,detail);
                String pathCode = lastCheck.getString("pathCode");
                List<String> checkPathId = Arrays.stream(pathCode.split(",")).collect(Collectors.toList());
                detail.setCheckPathName(processMapper.getCheckPathName(checkPathId).getString("checkPathName"));
                //??????-???????????????????????? (1:??????,2:??????,3:??????)
                setCheckFlow(detail);
                //???????????????????????? ??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)
                RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
                int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
                log.info("??????????????????(1:??????,2:?????????-??????,3:????????????-??????,4:??????,5:??????????????????)  ?????????????????????: [{}]",checkPartType);
                List<JSONObject> batchCheckPart = null;
                try {
                    batchCheckPart = getBatchCheckPart(checkPartType, roomQuery, detail.getCheckId(), detail.getSectionId());
                    batchCheckPart.forEach(o -> {
                        taskExecutor.execute(() -> {
                            ProcessDetailVo detailTemp = setDetailTemp(detail, o, checkPartType);
                            detailDtoList.add(detailTemp);
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        return detailDtoList;
    }

    //????????????????????????
    private void setSectionInfo(ProcessDetailVo processDetailVo,SectionInfo sectionInfo){
        if (ObjectUtil.isNotEmpty(sectionInfo.getSectionId())) {
            BeanUtil.copyProperties(sectionInfo,processDetailVo);
        }else {
            throw new BusinessException("????????????????????????! ?????????????????????");
        }
    }

    //???????????????????????????
    private void setCheckInfo(String lastCheckName,String parentCheckName,ProcessDetailVo processDetailVo) {
        //SQL ?????????????????????1???
        List<JSONObject> lastCheckList = processMapper.getLastCheck(lastCheckName,parentCheckName);
        JSONObject check;
        if (CollectionUtil.isNotEmpty(lastCheckList)) {
            check = lastCheckList.get(RandomUtil.randomInt(lastCheckList.size()));
            BeanUtil.copyProperties(check,processDetailVo);
            String pathCode = check.getString("pathCode");
            List<String> checkPathId = Arrays.stream(pathCode.split(",")).collect(Collectors.toList());
            processDetailVo.setCheckPathName(processMapper.getCheckPathName(checkPathId).getString("checkPathName"));
        }else {
            throw new BusinessException("???????????????????????????! ????????????????????????");
        }
    }

    //??????????????????
    private void setPartCode(JSONObject checkPart,int checkPartType,ProcessDetailVo processDetailVo){
        if (checkPartType >= 4) {
            processDetailVo.setPartCode(checkPart.getString("partCode"));
            processDetailVo.setPartName(checkPart.getString("partName"));
        }else {
            processDetailVo.setPartCode(checkPart.getString("partCode"));
            processDetailVo.setPartName(checkPart.getString("partName"));
            processDetailVo.setBanCode(checkPart.getString("banCode"));
            processDetailVo.setBanName(checkPart.getString("banName"));
        }
    }

    //???????????????????????????????????????
    private void setCheckPoint(ProcessDetailVo processDetailVo){
        List<JSONObject> checkPointList = processMapper.getCheckPoint(processDetailVo.getCheckId());
        List<ProcessDetailVo.PointDto> pointDtoList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(checkPointList)) {
            checkPointList.forEach(i -> {
                pointDtoList.add(ProcessDetailVo.PointDto
                        .builder()
                        .pointId(i.getLong("pointId"))
                        .title(i.getString("title"))
                        .remark(i.getString("remark"))
                        .picture(TestDataUtils.getPicture(RandomUtil.randomInt(6)))
                        .build());
            });
        }else {
            throw new BusinessException("???????????????????????????! ???????????????????????????????????? checkId:" +processDetailVo.getCheckId());
        }
        processDetailVo.setDetailsPoint(pointDtoList);
    }

    //?????????????????? ??????????????????(??????????????????????????????)
    private void setCheckFlow(ProcessDetailVo processDetailVo){
        List<JSONObject> checkFlowList = processMapper.getCheckFlow(processDetailVo.getCheckId());
        List<ProcessDetailVo.PersonnelDto> acceptorList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(checkFlowList)) {
            JSONObject node = checkFlowList.stream().filter(i -> i.getInteger("node") == 1).findAny().orElse(null);
            List<JSONObject> nextNodeList = checkFlowList.stream().filter(i -> i.getInteger("node") == 2).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(nextNodeList)) {
                nextNodeList = checkFlowList.stream().filter(i -> i.getInteger("node") == 3).collect(Collectors.toList());
            }

            if (CollectionUtil.isEmpty(nextNodeList)){
                throw new BusinessException("?????????????????????????????????! ????????????node= 2 ??? 3 ???????????????");
            }

            List<String> selectedUsers = new ArrayList<>();
            selectedUsers.add(processDetailVo.getRealName());

            nextNodeList.forEach(i -> {
                int roleType = i.getIntValue("roleType");
                UcUser target = new UcUser();
                //????????????(2:??????(?????????????????????????????????),3:????????????,4:????????????)
                if (roleType == 2){
                    target = CommonUtils.findTargetUser(null,supervisorList);
                }

                if (roleType == 3 || roleType == 4) {
                    do {
                        target = CommonUtils.findTargetUser(null,managerUserList);
                    }while (selectedUsers.contains(target.getRealName()));
                }
                setPersonalDto(target,i.getBoolean("ifAppoint"),i.getLong("flowId"),acceptorList);
                selectedUsers.add(target.getRealName());
            });
            processDetailVo.setFlowId(node.getLong("flowId"));
        }else {
            throw new BusinessException("??????????????????????????????????????????! ?????????????????? checkId:" +processDetailVo.getCheckId());
        }
        processDetailVo.setAcceptor(acceptorList);
    }

    /**
     * ???????????????????????? ???????????? ??????????????????
     * @param checkPartType  1-???????????? 2-?????????-?????? 3-????????????-?????? 4-???????????? 5-???????????????
     * @param roomQuery ?????????????????????:??????-?????? ???????????????-?????? ??????????????????-?????? ??????-????????? ??????????????????-?????????&??????
     * @param checkId
     * @param sectionId
     * @return
     */
    private List<JSONObject> getBatchCheckPart(int checkPartType,RoomQuery roomQuery,Long checkId,Long sectionId){
        List<JSONObject> checkPartList = new ArrayList<>();

        switch (checkPartType) {
            //1-???????????? 2-?????????-?????? 3-????????????-?????? ??????partCode???partName???banCode???banName
            case 1:
                checkPartList = processMapper.getPartByRoom(sectionId,roomQuery,checkId,false);
                break;
            case 2:
                checkPartList = processMapper.getPartByUnitFloor(sectionId,roomQuery,checkId,false);
                break;
            case 3:
                checkPartList = processMapper.getPartByFloor(sectionId,roomQuery,checkId,false);
                break;
            //4-???????????? 5-??????????????? ??????partCode???partName
            case 4:
                checkPartList = processMapper.getPartByBan(sectionId,roomQuery,checkId,false);
                break;
            case 5:
                checkPartList = processMapper.getPartByCustom(sectionId,checkId);
                break;
            default:
                break;
        }

        if (CollectionUtil.isNotEmpty(checkPartList)) {
            return checkPartList;
        }else {
            throw new BusinessException("????????????????????????! ?????????????????????(???????????? ????????????)");
        }
    }

    //1-???????????? 2-?????????-?????? 3-????????????-?????? ??????partCode???partName???banCode???banName
    private void setCheckPart123(int checkPartType,RoomQuery roomQuery,Long checkId,Long sectionId,ProcessDetailVo processDetailVo){
        List<JSONObject> checkPart = new ArrayList<>();

        if (checkPartType == 1) {
            checkPart = processMapper.getPartByRoom(sectionId,roomQuery,checkId,false);
        }else if (checkPartType == 2) {
            checkPart = processMapper.getPartByUnitFloor(sectionId,roomQuery,checkId,false);
        }else if (checkPartType == 3) {
            checkPart = processMapper.getPartByFloor(sectionId,roomQuery,checkId,false);
        }

        JSONObject target = null;
        if (CollectionUtil.isNotEmpty(checkPart)) {
            target = checkPart.get(RandomUtil.randomInt(checkPart.size()));
            log.info("target????????????: [{}] ,[{}] ",target.getString("partName"),target.getString("partCode"));
        }else {
            throw new BusinessException("????????????????????????! ?????????????????????(???????????? ????????????)");
        }
        processDetailVo.setPartCode(target.getString("partCode"));
        processDetailVo.setPartName(target.getString("partName"));
        processDetailVo.setBanCode(target.getString("banCode"));
        processDetailVo.setBanName(target.getString("banName"));
    }

    //4-???????????? 5-??????????????? ??????partCode???partName 0620 ?????????????????????banName banCode
    private void setCheckPart45(int checkPartType,RoomQuery roomQuery,Long checkId,Long sectionId,ProcessDetailVo processDetailVo){
        List<JSONObject> checkPart = checkPartType == 4 ?
                processMapper.getPartByBan(sectionId,roomQuery,checkId,false)
                : processMapper.getPartByCustom(sectionId,checkId);
        JSONObject target = null;
        if (CollectionUtil.isNotEmpty(checkPart)) {
            target = checkPart.get(RandomUtil.randomInt(checkPart.size()));
            log.info("target????????????: [{}] ,[{}] ",target.getString("partName"),target.getString("partCode"));
        }else {
            throw new BusinessException("????????????????????????! ?????????????????????(???????????? ????????????)");
        }
        processDetailVo.setPartCode(target.getString("partCode"));
        processDetailVo.setPartName(target.getString("partName"));
        processDetailVo.setBanCode(ObjectUtil.isNotEmpty(target.getString("banCode")) ? target.getString("banCode") : null);
        processDetailVo.setBanName(ObjectUtil.isNotEmpty(target.getString("banName")) ? target.getString("banName") : null);
    }

    private void setPersonalDto(UcUser user,boolean ifAppoint,Long flowId,List<ProcessDetailVo.PersonnelDto> personnelDtoList){

        personnelDtoList.add(ProcessDetailVo.PersonnelDto.builder()
                .userId(user.getUserId())
                .realName(user.getRealName())
                .companyName(ObjectUtil.isNotEmpty(user.getProviderName()) ? user.getProviderName() : "")
                .companyGuid(ObjectUtil.isNotEmpty(user.getProviderGuid()) ? user.getProviderGuid() : "")
                .nextFlowId(flowId)
                .ifAppoint(ifAppoint)
                .build());
    }

    public ProcessDetailVo setDetailTemp(ProcessDetailVo commonDetail,JSONObject lastCheck,int checkPartType){
        ProcessDetailVo detailTemp = new ProcessDetailVo();
        BeanUtil.copyProperties(commonDetail,detailTemp);
        //??????????????????checkPartCode
        setPartCode(lastCheck,checkPartType,detailTemp);
        //?????????-?????????????????????
        setCheckPoint(detailTemp);
        //???????????? ???????????? ????????????
        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.CreateProcess.getProcessDesc())
                .append(" ?????????:").append(detailTemp.getCheckPathName()).append(" ????????????: ").append(detailTemp.getPartName()).append(" ????????????").append(detailTemp.getRealName()).append(DateUtils.now());
        detailTemp.setComment(stringBuilder.toString());
        detailTemp.setSubmitTime(DateUtils.now());
        return detailTemp;
    }
}
