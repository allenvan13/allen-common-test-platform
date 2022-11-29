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
 * 1、工序验收  报验&验收&抽检 流程
 * 匠星后台配置工序验收检查项（检查项下 检查点、验收流程节点）、标段配置（关联房源）,  涉及权限 项目分期权限、标段及验收查询权限、验收（问题提出-整改）-抽检操作权限
 *  报验人（施工方、总包公司） 发起报验-   根据配置的流程  由监理单位、甲方、城市平台人员进行验收、抽检  或只验收 或只抽检 ,其中验收、抽检节点可多方共同参与
 *  报验区域 根据配置的报验类型,存在不同形态  比如: 分户、不分单元-分层、分单元-分层、整栋、自定义检验批
 * 2、工序验收问题 提出-整改-复验流程
 *  待验收、待抽检状态的工序验收下 才可创建问题
 *  完成验收的前提 是 验收下所有问题均关闭（复验关闭）
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
        Assert.assertEquals(detailVos.size(),1,"验收明细不唯一!");
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

        //锁定提交人
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
                                throw new RuntimeException("新增问题失败异常: " + n);
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
                                            throw new RuntimeException("新增问题失败异常: " + n);
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
                        throw new BusinessException("该工序验收下不存在[待整改][重新整改]状态的问题");
                    }
                }
            }

        }else {
            throw  new BusinessException("未匹配到房源信息! 停止后续步骤");
        }
    }

    @Override
    public void submitAndAcceptOrSpotCheckOne(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                              String banName,String floorName,String unitName,String roomName,
                                              Integer nodeLimit){
        if (ObjectUtil.isNotEmpty(sectionName)) {
            updateSectionInfo(sectionName,sectionType);
        }else {
            throw new BusinessException("未指定标段,停止后续步骤");
        }

        ProcessDetailVo detail = buildOneDetail(inspectorName,lastCheckName,parentCheckName,banName,floorName,unitName,roomName);
        UcUser inspector = ucMapper.getUserByOthers(null, detail.getRealName(), null, detail.getCompanyGuid(), Constant.SUPPLIER_SOURCE).get(0);
        Map<String,String> header_inspector = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(inspector.getUserName(),EncryptUtils.decrypt(inspector.getPassword()),currentEnv.getENV()));
        if (nodeLimit >= 1) {
            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_inspector, JSONObject.toJSONString(detail));
            if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.body"))) {
                Long detailId = Long.valueOf(JsonPath.read(rs,"$.body"));
                detail.setDetailId(detailId);
                log.info("当前处理人: [{}] 报验提交成功! 报验ID:[{}],报验区域 [{}] 检查项path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
            }else {
                log.info("报验失败,接口未返回ID  报验区域 [{}] 检查项path:[{}],提交参数:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
            }

            if (nodeLimit >= 2 ) {
                callAcceptOrSpotCheck(detail);
            }

            if (nodeLimit >= 3) {
                callAcceptOrSpotCheck(detail);
            }
        }else {
            throw new BusinessException("未配置nodeLimit!");
        }
    }

    @Override
    public void submitAndAcceptOrSpotCheckBatch(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                                String banName,String floorName,String unitName,String roomName,
                                                Integer nodeLimit) {
        if (ObjectUtil.isNotEmpty(sectionName)) {
            updateSectionInfo(sectionName,sectionType);
        }else {
            throw new BusinessException("未指定标段,停止后续步骤");
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
                        log.info("当前处理人: [{}] 报验提交成功! 报验ID:[{}],报验区域 [{}] 检查项path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
                    }else {
                        log.info("报验失败,接口未返回ID  报验区域 [{}] 检查项path:[{}],提交参数:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
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
//                        log.info("当前处理人: [{}] 报验提交成功! 报验ID:[{}],报验区域 [{}] 检查项path:[{}]",inspector.getUserName(),detail.getDetailId(),detail.getPartName(),detail.getCheckPathName());
//                    }else {
//                        log.info("报验失败,接口未返回ID  报验区域 [{}] 检查项path:[{}],提交参数:{}",detail.getPartName(),detail.getCheckPathName(),JSONObject.toJSONString(detail));
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
            log.info("未配置nodeLimit!");
        }
    }

    //根据验收detailId整改关联的所有【待整改】问题
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
                throw new BusinessException("操作类型不正确!");
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
            throw new BusinessException("该工序验收下不存在当前状态的问题");
        }
        log.info("工序验收[{}]下共计[{}],[{}]个问题",detaiLId,operateTypeName,issues.size());
    }

    public String recitfyOrReviewOne(ProcessDetailIssue issue,Integer operateType,String operateTypeName,String secondRecitifyName,Integer pictureNum ){
        ProcessDetailIssueRectifyVo rectifyVo = new ProcessDetailIssueRectifyVo();
        UcUser user;
        switch (operateType) {
            //1:二次派单
            case 1:
                user = ucMapper.getUserById(issue.getRectifyUserId());
                UcUser reRecitify = CommonUtils.findTargetUser(secondRecitifyName, inspectorCompanyList);
                if (ObjectUtil.isNotEmpty(reRecitify)) {
                    rectifyVo.setRectifyUserId(reRecitify.getUserId());
                    rectifyVo.setRectifyRealName(reRecitify.getRealName());
                    rectifyVo.setRectifyCompanyGuid(reRecitify.getProviderGuid());
                    rectifyVo.setRectifyCompanyName(reRecitify.getProviderName());
                }else {
                    throw new BusinessException("未匹配到二次整改人!");
                }
                break;
            //3:完成整改
            case 3:
                user = ucMapper.getUserById(issue.getRectifyUserId());
                break;
            //2:重新整改 4:非正常关闭,5:正常关闭 均由复验人提起
            case 2:
            case 4:
            case 5:
                user = ucMapper.getUserById(issue.getReviewUserId());

                break;
            default:
                throw new BusinessException("操作类型不正确!");
        }

        if (ObjectUtil.isNotEmpty(user)) {
            if (Constant.SUPPLIER_SOURCE.equals(user.getSource())) {
                user = ucMapper.getUserByIdSource(user.getUserId(), Constant.SUPPLIER_SOURCE);
            }
        }else {
            throw new BusinessException("操作用户不存在!");
        }

        rectifyVo.setCompanyName(user.getProviderName());
        rectifyVo.setCompanyGuid(user.getProviderGuid());

        rectifyVo.setId(issue.getId());
        rectifyVo.setRealName(user.getRealName());
        rectifyVo.setUserId(user.getUserId());

        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS_PROBLEM.getMsg()).append("-").append(operateTypeName)
                .append(" 操作人：").append(user.getRealName()).append(DateUtils.now());
        rectifyVo.setRemark(stringBuilder.toString());
        rectifyVo.setPicture(TestDataUtils.getPicture(ObjectUtil.isNotEmpty(pictureNum) && pictureNum > 0 ? pictureNum : RandomUtil.randomInt(6)));
        rectifyVo.setSubmitTime(DateUtils.now());
        rectifyVo.setStatus(operateType);

        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getSource().equals(Constant.SUPPLIER_SOURCE) ? user.getPhone():user.getUserName(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));
        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_ISSUE_RECTIFY), header, JSONObject.toJSONString(rectifyVo));
        return rs;
    }

    //验收及抽检流程操作 因操作一致  封装在同个方法中
    @Override
    public void callAcceptOrSpotCheckById(Long detaiId, Integer operateType) {

        if (ObjectUtil.isEmpty(operateType)) {
            throw new BusinessException("操作类型不能为空");
        }
        if (operateType != 1 && operateType != 2) {
            throw new BusinessException("操作类型不正确! 1-验收 2-抽检");
        }
        int detailStatus = operateType; //验收状态    1 待验收   2 待抽检
        int lastNode = operateType;     //上一节点类型
        int currentNode = operateType + 1;//当前节点类型 1-报验 2-验收 3-抽检

        //验收状态(0:重新报验,1:待验收,2:待抽检,3:已完成)
        ProcessDetail processDetail = processMapper.selectOne(new QueryWrapper<ProcessDetail>().lambda().eq(ProcessDetail::getId, detaiId));
        if (ObjectUtil.isEmpty(processDetail)) {
            throw new BusinessException("未匹配到验收数据,请检查验收ID");
        }
        if (processDetail.getStatus() != detailStatus && processDetail.getStatus() != detailStatus-1) {
            throw new BusinessException("验收状态不正确");
        }

        List<ProcessDetailCheckFlow> detailCheckFlows = flowMapper.selectList(
                new QueryWrapper<ProcessDetailCheckFlow>().lambda().eq(ProcessDetailCheckFlow::getDetailId,detaiId).
                        eq(ProcessDetailCheckFlow::getNode, currentNode).eq(ProcessDetailCheckFlow::getDelFlag, false));
//        List<JSONObject> detailCheckFlows = processMapper.getDetailHandleFlow(detaiId, null, currentNode);

        if (CollectionUtils.isEmpty(detailCheckFlows)) {
            throw new BusinessException("ProcessDetailCheckFlow数据为空!");
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

                //设置业务人员不重复
                List<String> selectedUsers = new ArrayList<>();
                //报验人
                selectedUsers.add(processDetail.getCreateUserName());
                handlers.forEach(handler -> {
                    //验收及抽检人
                    selectedUsers.add(handler.getRealName());
                });

                List<ProcessDetailVo.PersonnelDto> nowAcceptorList = new ArrayList<>(nextFlows.size());
                nextFlows.forEach(next -> {
                    int roleType = next.getRoleType();
                    UcUser target = null;

                    //角色类型(2:监理(供应商登录手账号为机号),3:项目甲方,4:城市平台)
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

//            //只有验收时才需要设置下一步人   抽检为完结操作
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
//                    //角色类型(2:监理(供应商登录手账号为机号),3:项目甲方,4:城市平台)
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

            //设置检查点报验信息
            setCheckPoint(processDetailVo);
            processDetailVo.setUserId(nowHandler.getUserId());
            processDetailVo.setRealName(nowHandler.getRealName());
            processDetailVo.setCompanyGuid(ObjectUtil.isNotEmpty(nowHandler.getCompanyGuid()) ? nowHandler.getCompanyGuid() : null);
            processDetailVo.setCompanyName(ObjectUtil.isNotEmpty(nowHandler.getCompanyName()) ? nowHandler.getCompanyName() : null);
            processDetailVo.setFlowId(flow.getFlowId());

            StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
            if (flow.getNode() == TicketProcessEnum.AcceptProcess.getProcessCode()) {
                stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.AcceptProcess.getProcessDesc())
                        .append(" 报验人：").append(processDetail.getCreateUserName()).append(" 当前处理人：").append(nowHandler.getRealName()).append(" ").append(DateUtils.now());
            }else if (flow.getNode() == TicketProcessEnum.SpotCheckProcess.getProcessCode()) {
                stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.SpotCheckProcess.getProcessDesc())
                        .append(" 报验人：").append(processDetail.getCreateUserName()).append(" 当前处理人：").append(nowHandler.getRealName()).append(" ").append(DateUtils.now());
            }
            processDetailVo.setComment(stringBuilder.toString());
            processDetailVo.setSubmitTime(DateUtils.now());

            UcUser user = ucMapper.getUserByIdSource(processDetailVo.getUserId(),ObjectUtil.isNotEmpty(processDetailVo.getCompanyGuid()) ? Constant.SUPPLIER_SOURCE:Constant.PS_SOURCE);
            Assert.assertNotNull(user.getUserName(),"测试账号不存在!");
            log.info("当前处理人: [{}] 当前流程节点node=[{}] 报验ID:[{}],报验区域 [{}] 检查项path:[{}]",processDetailVo.getRealName(),currentNode,detaiId,processDetailVo.getPartName(),processDetailVo.getCheckPathName());
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
            throw new BusinessException("未匹配到待操作的验收");
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

    //验收及抽检流程操作 因操作一致  封装在同个方法中
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
                //设置下一流程节点验收或抽检人 当前节点 当前操作人是否指定下一流程节点人
                if (i.getIfAppoint()) {
                    //当前节点= 2-验收 时 下一节点为 3-抽检
                    if (thisFlow.getIntValue("node") == 2) {
                        List<JSONObject> nextFlowList = processMapper.getDetailCheckFlow(submitDetailVo.getDetailId(), null, 3);
                        if (CollectionUtils.isNotEmpty(nextFlowList)) {
                            List<ProcessDetailVo.PersonnelDto> nowAcceptorList = new ArrayList<>();
                            nextFlowList.forEach(x -> {
                                int roleType = x.getIntValue("roleType");
                                UcUser target = null;

                                //角色类型(2:监理(供应商登录手账号为机号),3:项目甲方,4:城市平台)
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
                            log.info("不存在下一流程节点,当前检查项-验收(node=2)为最终节点!");
                        }
                    }else {
                        log.info("当前流程节点node=[{}] ,为最终流程节点", thisFlow.getIntValue("node"));
                    }
                }else {
                    //不设置下一节点验收或抽检人
                    submitDetailVo.setAcceptor(null);
                }
                //设置检查点报验信息
                setCheckPoint(submitDetailVo);
                //设置其他 flowId 提交人公司信息 补充说明 提交时间
                if (ObjectUtil.isNotEmpty(i.getCompanyGuid())) {
                    submitDetailVo.setCompanyGuid(i.getCompanyGuid());
                    submitDetailVo.setCompanyName(i.getCompanyName());
                }else {
                    submitDetailVo.setCompanyName(null);
                    submitDetailVo.setCompanyGuid(null);
                }
                submitDetailVo.setFlowId(i.getNextFlowId());
                if (thisFlow.getIntValue("node") == 2) {
                    submitDetailVo.setComment("自动化测试-工序验收-验收".concat(submitDetailVo.getCheckPathName()).concat("-").concat(i.getRealName()).concat("-").concat(DateUtils.now()));
                }else {
                    submitDetailVo.setComment("自动化测试-工序验收-抽检:".concat(submitDetailVo.getCheckPathName()).concat("-").concat(i.getRealName()).concat("-").concat(DateUtils.now()));
                }
                submitDetailVo.setSubmitTime(DateUtils.now());

                //设置共同验收人信息
//            submitDetailDto.setCommonAcceptor();
                //设置抄送人信息
//            submitDetailDto.setCarbonCopy();

                UcUser user = ucMapper.getUserByIdSource(i.getUserId(),ObjectUtil.isNotEmpty(i.getCompanyGuid()) ? Constant.SUPPLIER_SOURCE:Constant.PS_SOURCE);
                Assert.assertNotNull(user.getUserName(),"测试账号不存在!");
                log.info("当前处理人: [{}] 当前流程节点node=[{}] 报验ID:[{}],报验区域 [{}] 检查项path:[{}]",i.getRealName(),thisFlow.getIntValue("node"),submitDetailVo.getDetailId(),submitDetailVo.getPartName(),submitDetailVo.getCheckPathName());
                Map<String,String> header_acceptor = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getUserName(), EncryptUtils.decrypt(user.getPassword()),currentEnv.getENV()));
                String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiProcess.APP_PROCESS_SUBMIT), header_acceptor, JSONObject.toJSONString(submitDetailVo));
                log.info("{}",JSON.parseObject(rs));
            });
        }else {
            log.info("无下一流程节点! ");
        }
    }

    /**
     * 删除工序验收工单
     * @param detailIdList 明细ID List
     */
    void deleteList(List<Long> detailIdList){

        Map<String,String> header_admin = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001","a123456","UAT"));

        if (CollectionUtils.isNotEmpty(detailIdList)) {
            Map<String, String> finalHeader_admin = header_admin;

            detailIdList.forEach(i -> {
                taskExecutor.execute(() -> {
                    String params = "id=".concat(i.toString());
                    String rs = HttpUtils.doGet(HostCommon.UAT.concat(ApiProcess.PC_DETAIL_DELETE), finalHeader_admin, params);
                    Assert.assertNotNull(JsonPath.read(rs,"$.message"),"接口未返回值");
                });
            });
            log.info("共计删除[{}]条数据",detailIdList.size());
        }else {
            throw new BusinessException("未匹配到验收数据");
        }
    }

    public void setIssueUser(String rectifyName, String reviewName, ProcessIssueVo issueVo) {
        //整改人
        UcUser rectify = CommonUtils.findTargetUser(rectifyName, inspectorCompanyList);
        issueVo.setRectifyUserId(rectify.getUserId());
        issueVo.setRectifyRealName(rectify.getRealName());
        issueVo.setRectifyCompanyName(ObjectUtil.isNotEmpty(rectify.getProviderName()) ? rectify.getProviderName() : null);
        issueVo.setRectifyCompanyGuid(ObjectUtil.isNotEmpty(rectify.getProviderGuid()) ? rectify.getProviderGuid() : null);

        //复验人
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
        stringBuilder.append(CheckTypeEnum.PROCESS_PROBLEM.getMsg()).append(TicketProcessEnum.Create.getProcessDesc()).append("整改人-").append(issueVo.getRectifyRealName()).append(" 复验人-").append(issueVo.getReviewRealName()).append(DateUtils.current());
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
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("单元").concat(" - ").concat(partInfo.getString("rooms"))
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
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("单元").concat(" - ").concat(partInfo.getString("floor")).concat("层") : issueVo.getBanName());
    }

    public void setUnitPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setUnit(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? partInfo.getString("unit"):null);
        issueVo.setPartName(ObjectUtil.isNotEmpty(partInfo.getString("unit")) ? issueVo.getBanName().concat(" - ").concat(partInfo.getString("unit")).concat("单元") : issueVo.getBanName());
    }

    public void setBanPart(JSONObject partInfo, ProcessIssueVo issueVo){
        issueVo.setBanCode(ObjectUtil.isNotEmpty(partInfo.getString("banCode")) ? partInfo.getString("banCode"):null);
        issueVo.setBanName(ObjectUtil.isNotEmpty(partInfo.getString("banName")) ? partInfo.getString("banName"):null);
        issueVo.setPartName(issueVo.getBanName());
    }

    /**
     * 更新初始化标段及人员信息
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
                throw new IllegalStateException("标段类型错误! sectionType:" + sectionType);
        }
        return category;
    }

    public ProcessDetailVo buildOneDetail(String inspectorName, String lastCheckName,String parentCheckName,String banName, String floorName, String unitName, String roomName) {
        ProcessDetailVo detail = new ProcessDetailVo();
        //设置标段相关信息
        setSectionInfo(detail,sectionInfo);
        //设置检查项信息
        setCheckInfo(lastCheckName, parentCheckName,detail);
        //检查项-检查点报验信息
        setCheckPoint(detail);
        //设置报验人员 从施工方 和 总包中选取
        UcUser inspector = CommonUtils.findTargetUser(inspectorName,inspectorCompanyList);
        detail.setCompanyName(inspector.getProviderName());
        detail.setCompanyGuid(inspector.getProviderGuid());
        detail.setRealName(inspector.getRealName());
        //检查项-验收节点信息 (1:报验,2:验收,3:抽检)
        setCheckFlow(detail);
        //设置报验区域信息 根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)
        RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
        int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
        log.info("根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)  本次报验类型为: [{}]",checkPartType);
        if (checkPartType >= 4) {
            setCheckPart45(checkPartType,roomQuery,detail.getCheckId(),detail.getSectionId(),detail);
        }else {
            setCheckPart123(checkPartType,roomQuery,detail.getCheckId(),detail.getSectionId(),detail);
        }
        //设置其他 补充说明 提交时间
        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.CreateProcess.getProcessDesc())
                .append(" 检查项:").append(detail.getCheckPathName()).append(" 报验区域: ").append(detail.getPartName()).append(" 报验人：").append(detail.getRealName()).append(DateUtils.now());
        detail.setComment(stringBuilder.toString());
        detail.setSubmitTime(DateUtils.now());
        return detail;
    }

    public ProcessDetailVo buildDetailCommon(String inspectorName) {
        ProcessDetailVo detail = new ProcessDetailVo();
        //设置标段相关信息
        setSectionInfo(detail,sectionInfo);
        //设置报验人员 从施工方 和 总包中选取
        UcUser inspector = CommonUtils.findTargetUser(inspectorName,inspectorCompanyList);
        detail.setCompanyName(inspector.getProviderName());
        detail.setCompanyGuid(inspector.getProviderGuid());
        detail.setRealName(inspector.getRealName());
        if (ObjectUtil.isNotEmpty(detail.getCheckId())){
            //设置-验收节点人员信息 (1:报验,2:验收,3:抽检)
            setCheckFlow(detail);
        }
        return detail;
    }

    public List<ProcessDetailVo> createBatchDetail(String inspectorName, String lastCheckName,String parentCheckName, String banName, String floorName, String unitName, String roomName) {
        List<ProcessDetailVo> detailDtoList = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(lastCheckName)) {
            //若指定了末级检查项 则只批量处理该检查项下报验

            //批量报验 将报验人-验收人-抽检人设置统一
            ProcessDetailVo detail = buildDetailCommon(inspectorName);
            //设置检查项信息
            setCheckInfo(lastCheckName,parentCheckName ,detail);

            //设置报验区域信息 根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)
            RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
            int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
            log.info("根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)  本次报验类型为: [{}]",checkPartType);
            List<JSONObject> batchCheckPart = getBatchCheckPart(checkPartType, roomQuery, detail.getCheckId(), detail.getSectionId());
            try {
                batchCheckPart.forEach(o -> {
//                    taskExecutor.execute(() -> {
                        ProcessDetailVo detailTemp = new ProcessDetailVo();
                        detailTemp = setDetailTemp(detail, o, checkPartType);
                        //设置报验流程节点人员
                        setCheckFlow(detailTemp);
                        detailDtoList.add(detailTemp);
//                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            //未指定末级检查项,则全量遍历检查项进行报验

            //批量报验 将报验人-验收人-抽检人设置统一
            List<JSONObject> lastChecks = processMapper.getLastCheck(null,null);
            lastChecks.forEach(lastCheck -> {
                ProcessDetailVo detail = buildDetailCommon(inspectorName);
                BeanUtil.copyProperties(lastCheck,detail);
                String pathCode = lastCheck.getString("pathCode");
                List<String> checkPathId = Arrays.stream(pathCode.split(",")).collect(Collectors.toList());
                detail.setCheckPathName(processMapper.getCheckPathName(checkPathId).getString("checkPathName"));
                //设置-验收节点人员信息 (1:报验,2:验收,3:抽检)
                setCheckFlow(detail);
                //设置报验区域信息 根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)
                RoomQuery roomQuery = new RoomQuery(banName,floorName,unitName,roomName);
                int checkPartType = processMapper.getLastCheckById(detail.getCheckId()).getIntValue("checkPartType");
                log.info("根据类型区分(1:分户,2:分单元-整层,3:不分单元-整层,4:整栋,5:自定义检验批)  本次报验类型为: [{}]",checkPartType);
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

    //设置标段相关信息
    private void setSectionInfo(ProcessDetailVo processDetailVo,SectionInfo sectionInfo){
        if (ObjectUtil.isNotEmpty(sectionInfo.getSectionId())) {
            BeanUtil.copyProperties(sectionInfo,processDetailVo);
        }else {
            throw new BusinessException("未匹配到标段信息! 请核对标段配置");
        }
    }

    //设置检查项基础信息
    private void setCheckInfo(String lastCheckName,String parentCheckName,ProcessDetailVo processDetailVo) {
        //SQL 根据配置随机取1条
        List<JSONObject> lastCheckList = processMapper.getLastCheck(lastCheckName,parentCheckName);
        JSONObject check;
        if (CollectionUtil.isNotEmpty(lastCheckList)) {
            check = lastCheckList.get(RandomUtil.randomInt(lastCheckList.size()));
            BeanUtil.copyProperties(check,processDetailVo);
            String pathCode = check.getString("pathCode");
            List<String> checkPathId = Arrays.stream(pathCode.split(",")).collect(Collectors.toList());
            processDetailVo.setCheckPathName(processMapper.getCheckPathName(checkPathId).getString("checkPathName"));
        }else {
            throw new BusinessException("未匹配到末级检查项! 请核对检查项配置");
        }
    }

    //设置报验区域
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

    //设置检查项下检查点验收信息
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
            throw new BusinessException("未匹配到对应检查点! 请核对检查项下检查点配置 checkId:" +processDetailVo.getCheckId());
        }
        processDetailVo.setDetailsPoint(pointDtoList);
    }

    //设置检查项下 报验流程信息(下一个验收、抽检节点)
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
                throw new BusinessException("报验节点配置数据不正确! 必须存在node= 2 或 3 的节点信息");
            }

            List<String> selectedUsers = new ArrayList<>();
            selectedUsers.add(processDetailVo.getRealName());

            nextNodeList.forEach(i -> {
                int roleType = i.getIntValue("roleType");
                UcUser target = new UcUser();
                //角色类型(2:监理(供应商登录手账号为机号),3:项目甲方,4:城市平台)
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
            throw new BusinessException("检查项报验流程节点配置不存在! 请检查项配置 checkId:" +processDetailVo.getCheckId());
        }
        processDetailVo.setAcceptor(acceptorList);
    }

    /**
     * 根据不同报验类型 获取批量 可报验的区域
     * @param checkPartType  1-分户报验 2-分单元-分层 3-不分单元-分层 4-整栋报验 5-自定义报验
     * @param roomQuery 注意最小颗粒度:分户-房号 分单元分层-层号 不分单元分层-层号 整栋-楼栋名 自定义检验批-检查项&标段
     * @param checkId
     * @param sectionId
     * @return
     */
    private List<JSONObject> getBatchCheckPart(int checkPartType,RoomQuery roomQuery,Long checkId,Long sectionId){
        List<JSONObject> checkPartList = new ArrayList<>();

        switch (checkPartType) {
            //1-分户报验 2-分单元-分层 3-不分单元-分层 传参partCode、partName、banCode、banName
            case 1:
                checkPartList = processMapper.getPartByRoom(sectionId,roomQuery,checkId,false);
                break;
            case 2:
                checkPartList = processMapper.getPartByUnitFloor(sectionId,roomQuery,checkId,false);
                break;
            case 3:
                checkPartList = processMapper.getPartByFloor(sectionId,roomQuery,checkId,false);
                break;
            //4-整栋报验 5-自定义报验 传参partCode、partName
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
            throw new BusinessException("未匹配到房源信息! 请检查房源配置(未匹配到 或已报验)");
        }
    }

    //1-分户报验 2-分单元-分层 3-不分单元-分层 传参partCode、partName、banCode、banName
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
            log.info("target报验区域: [{}] ,[{}] ",target.getString("partName"),target.getString("partCode"));
        }else {
            throw new BusinessException("未匹配到房源信息! 请检查房源配置(未匹配到 或已报验)");
        }
        processDetailVo.setPartCode(target.getString("partCode"));
        processDetailVo.setPartName(target.getString("partName"));
        processDetailVo.setBanCode(target.getString("banCode"));
        processDetailVo.setBanName(target.getString("banName"));
    }

    //4-整栋报验 5-自定义报验 传参partCode、partName 0620 整栋验收时加上banName banCode
    private void setCheckPart45(int checkPartType,RoomQuery roomQuery,Long checkId,Long sectionId,ProcessDetailVo processDetailVo){
        List<JSONObject> checkPart = checkPartType == 4 ?
                processMapper.getPartByBan(sectionId,roomQuery,checkId,false)
                : processMapper.getPartByCustom(sectionId,checkId);
        JSONObject target = null;
        if (CollectionUtil.isNotEmpty(checkPart)) {
            target = checkPart.get(RandomUtil.randomInt(checkPart.size()));
            log.info("target报验区域: [{}] ,[{}] ",target.getString("partName"),target.getString("partCode"));
        }else {
            throw new BusinessException("未匹配到房源信息! 请检查房源配置(未匹配到 或已报验)");
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
        //设置报验区域checkPartCode
        setPartCode(lastCheck,checkPartType,detailTemp);
        //检查项-检查点报验信息
        setCheckPoint(detailTemp);
        //设置其他 补充说明 提交时间
        StringBuilder stringBuilder = new StringBuilder(Constant.AUTO_TEST);
        stringBuilder.append(CheckTypeEnum.PROCESS.getMsg()).append("-").append(TicketProcessEnum.CreateProcess.getProcessDesc())
                .append(" 检查项:").append(detailTemp.getCheckPathName()).append(" 报验区域: ").append(detailTemp.getPartName()).append(" 报验人：").append(detailTemp.getRealName()).append(DateUtils.now());
        detailTemp.setComment(stringBuilder.toString());
        detailTemp.setSubmitTime(DateUtils.now());
        return detailTemp;
    }
}
