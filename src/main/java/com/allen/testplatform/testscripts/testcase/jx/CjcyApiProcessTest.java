package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketStatusType;
import com.allen.testplatform.modules.databuilder.mapper.CjcyMapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.cjcy.entity.OrderQuery;
import com.allen.testplatform.modules.databuilder.model.cjcy.entity.ProcessUserOrder;
import com.allen.testplatform.modules.databuilder.model.cjcy.vo.AddCjcyOrderVo;
import com.allen.testplatform.modules.databuilder.model.cjcy.vo.RecitifyProblemVo;
import com.allen.testplatform.modules.databuilder.model.cjcy.vo.ReviewProblemVo;
import com.allen.testplatform.modules.databuilder.model.common.CheckBatch;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.testscripts.api.ApiCjcy;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.SpringTestBase;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.xiaoleilu.hutool.bean.BeanUtil;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * 承接查验-问题处理接口脚本
 * @author Fan QingChuan
 * @since 2022/9/21 9:34
 */

public class CjcyApiProcessTest extends SpringTestBase {

    private static final ReportLog reportLog = new ReportLog(CjcyApiProcessTest.class);

    @Resource
    public CjcyMapper cjcyMapper;

    @Resource
    public CurrentEnvironmentConfig currentEnv;

    @Resource
    public UserCenterMapper ucMapper;

    @Test
    void addProblemV2() {

        String username = "ATE003";
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(username, "a123456", "UAT"));

        Long batchId = 1570627011694747650L;

        CheckBatch batch = cjcyMapper.getCheckBatch(batchId);
        if (ObjectUtil.isEmpty(batch)) {
            throw new BusinessException("未获取到批次");
        }

        String banName="楼栋-整栋公区";
        String roomName="401";
        String unit="";
        String floor="";


        List<JSONObject> batchRoomList = cjcyMapper.getBatchRoom(batchId, banName, roomName, unit, floor);

        if (CollectionUtils.isEmpty(batchRoomList)) {
            throw new BusinessException("未获取到批次下房源");
        }

        JSONObject room = batchRoomList.get(0);
        Date payDate = room.getDate("payDate");
        String banCode = room.getString("ban_code");
        String roomCode = room.getString("room_code");

        AddCjcyOrderVo commonOrderVo = new AddCjcyOrderVo();
        commonOrderVo.setCheckBatchId(batchId);
        commonOrderVo.setBanCode(banCode);
        if (ObjectUtil.isNotEmpty(room.getString("unit"))) {
            commonOrderVo.setUnit(room.getString("unit"));
        }
        commonOrderVo.setRoomCode(roomCode);

        List<JSONObject> checkList = cjcyMapper.getRoomCheckItem(batchId, roomCode);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<String>> futureList = new LinkedList<>();
        checkList.forEach(item -> {
            Callable<String> callable = () -> {
                try {
                    JSONObject parentCheckItem = cjcyMapper.getParentCheckItem(item.getLong("id"));

                    AddCjcyOrderVo addCjcyOrderVo = new AddCjcyOrderVo();
                    BeanUtil.copyProperties(commonOrderVo,addCjcyOrderVo);

                    addCjcyOrderVo.setCkItemId(item.getLong("id"));
                    addCjcyOrderVo.setCkItemName(item.getString("name"));
                    addCjcyOrderVo.setCkDescName(item.getString("item_desc"));

                    addCjcyOrderVo.setCkOneId(parentCheckItem.getLong("ckOneId"));
                    addCjcyOrderVo.setCkOneName(parentCheckItem.getString("ckOneName"));
                    addCjcyOrderVo.setCkTwoId(parentCheckItem.getLong("ckTwoId"));
                    addCjcyOrderVo.setCkTwoName(parentCheckItem.getString("ckTwoName"));

                    JSONObject dutyUser = cjcyMapper.getDutyUser(batch.getStageCode(), banCode, item.getLong("id"), 1);
                    addCjcyOrderVo.setDutyUserId(dutyUser.getLong("emp_id"));
                    addCjcyOrderVo.setDutyUserName(dutyUser.getString("emp_name"));
                    addCjcyOrderVo.setDutyUserCode(dutyUser.getString("emp_code"));
                    addCjcyOrderVo.setProviderGuid(dutyUser.getString("provider_guid"));
                    addCjcyOrderVo.setProviderName(dutyUser.getString("provider_name"));

                    addCjcyOrderVo.setImportance(String.valueOf(RandomUtil.randomInt(0, 3)));
                    addCjcyOrderVo.setIsInform(true);
                    addCjcyOrderVo.setContent(TestDataUtils.getApiTestContent(BusinessType.CJCY, TicketProcessEnum.Create.getProcessDesc()) +
                            "-创建人-"+username + "检查项序号：" +item.getString("code") + item.getString("name"));
                    addCjcyOrderVo.setImgs(TestDataUtils.getPicture(RandomUtil.randomInt(1,6)));

                    String checkSign = IdWorker.getId() + DateUtils.currentSeconds() + UUID.randomUUID().toString();
                    addCjcyOrderVo.setCheckSign(checkSign);

                    addCjcyOrderVo.setAddTime(DateUtils.current());

                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ADD_PROBLEM), header, JSONObject.toJSONString(addCjcyOrderVo));
                    reportLog.info("{}", JSON.parseObject(rs));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "Success";
            };
            Future<String> future = executorService.submit(callable);
            futureList.add(future);
        });


        futureList.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        int size = futureList.size();

        reportLog.info("共计请求: {}",size);

    }

    /**
     * 指定范围 进行批量整改:
     * 范围参数 分期 批次 楼栋 单元 房号 检查项 工单sn 整改人 供应商公司 检查项
     */
    @Test
    void rectifyBatchProblems() {

        String stageCode = "";
        String roomName = "";
        Long batchId = 1570627011694747650L;
        String roomCode = "66f377ce-1e24-4ed1-a484-a24517078bfe";
        String sn = "";
        String location = "";
        Long dutyUserId = 1434443859735179265L;
        String status = TicketStatusType.CJCY_PROCESSING.getCode();

        OrderQuery orderQuery = new OrderQuery();
//        orderQuery.setStageCode(stageCode);
//        orderQuery.setRoomName(roomName);
        orderQuery.setRoomCode(roomCode);
//        orderQuery.setSn(sn);
        orderQuery.setCheckBatchId(batchId);
        orderQuery.setLocation(location);
        orderQuery.setStatus(status);
        orderQuery.setDutyUserId(dutyUserId);

        List<Long> idList = cjcyMapper.getTargetOrderIdList(orderQuery);
        if (idList.size() == 0) {
            throw new BusinessException("不存在对应工单!");
        }

        if (idList.size() < 100) {
            List<ProcessUserOrder> processUserList = cjcyMapper.getRecitifyProcessUser(idList);
            processUserList.forEach(processor -> {
                UcUser user = ucMapper.getUserByIdSource(processor.getProcessorId(), Constant.SUPPLIER_SOURCE);
                Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getPhone(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));

                processor.getProcessorOrderIds().forEach(order -> {
                    RecitifyProblemVo recitifyProblemVo = new RecitifyProblemVo();
                    recitifyProblemVo.setId(Long.valueOf(order));
                    recitifyProblemVo.setImgs(TestDataUtils.getPicture(RandomUtil.randomInt(1,4)));
                    recitifyProblemVo.setAddTime(DateUtils.now());
                    recitifyProblemVo.setContent(TestDataUtils.getApiTestContent(BusinessType.CJCY,TicketProcessEnum.CompleteRectify.getProcessDesc()) +" orderId："+ order +" 整改人：" + processor.getProcessorName());
                    String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ADD_COMPLETERECT), header, JSONObject.toJSONString(recitifyProblemVo));
                    reportLog.info("{}", JSON.parseObject(rs));
                });
            });
        }else {
            List<List<Long>> targetList = ListUtils.splitList(idList,100);
            targetList.forEach(idSonList -> {
                List<ProcessUserOrder> processUserList = cjcyMapper.getRecitifyProcessUser(idSonList);
                processUserList.forEach(processor -> {
                    UcUser user = ucMapper.getUserByIdSource(processor.getProcessorId(), Constant.SUPPLIER_SOURCE);
                    Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getPhone(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));

                    processor.getProcessorOrderIds().forEach(order -> {
                        RecitifyProblemVo recitifyProblemVo = new RecitifyProblemVo();
                        recitifyProblemVo.setId(Long.valueOf(order));
                        recitifyProblemVo.setImgs(TestDataUtils.getPicture(RandomUtil.randomInt(1,4)));
                        recitifyProblemVo.setAddTime(DateUtils.now());
                        recitifyProblemVo.setContent(TestDataUtils.getApiTestContent(BusinessType.CJCY,TicketProcessEnum.CompleteRectify.getProcessDesc()) +" orderId："+ order +" 整改人：" + processor.getProcessorName());
                        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ADD_COMPLETERECT), header, JSONObject.toJSONString(recitifyProblemVo));
                        reportLog.info("{}", JSON.parseObject(rs));
                    });
                });
            });
        }
    }

    @Test
    void reSubmitProblems() {
        String stageCode = "";
        String roomName = "";
        Long batchId = 1570627011694747650L;
        String roomCode = "";
        String sn = "";
        String location = "";
        Long dutyUserId = null;//C-A
        Long orderId= null;
        String status = TicketStatusType.CJCY_APPOINT.getCode();

        OrderQuery orderQuery = new OrderQuery();
//        orderQuery.setStageCode(stageCode);
//        orderQuery.setRoomName(roomName);
//        orderQuery.setRoomCode(roomCode);
//        orderQuery.setSn(sn);
        orderQuery.setCheckBatchId(batchId);
//        orderQuery.setLocation(location);
        orderQuery.setStatus(status);
        orderQuery.setDutyUserId(dutyUserId);
        orderQuery.setId(orderId);

        List<Long> idList = cjcyMapper.getTargetOrderIdList(orderQuery);


        if (CollectionUtils.isEmpty(idList)) {
            throw new BusinessException("不存在对应状态的工单!");
        }

        List<ProcessUserOrder> createOrderUserList = cjcyMapper.getCreateOrderUsers(idList);


        createOrderUserList.stream().parallel().forEach(processUserOrder -> {

            UcUser user = ucMapper.getUserByIdSource(processUserOrder.getProcessorId(), Constant.PS_SOURCE);
            Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(user.getPhone(), EncryptUtils.decrypt(user.getPassword()), currentEnv.getENV()));

            processUserOrder.getProcessorOrderIds().forEach(order -> {
                String param = "id=" + order;
                String rs = HttpUtils.doGet(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ORDER_DETAILS), header, param);

                String body = JSONObject.parseObject(rs).getString("body");

                AddCjcyOrderVo addCjcyOrderVo = JSON.parseObject(body, AddCjcyOrderVo.class);
                addCjcyOrderVo.setCheckBatchId(JSONObject.parseObject(body).getLong("batchId"));
                addCjcyOrderVo.setAddTime(DateUtils.current());
                addCjcyOrderVo.setContent(TestDataUtils.getApiTestContent(BusinessType.CJCY, TicketProcessEnum.AGAIN_SUBMIT.getProcessDesc()) +
                        "_创建人-"+user.getRealName() +"_检查项: "+ addCjcyOrderVo.getCkOneName()+"-"+addCjcyOrderVo.getCkTwoName()+"-"+addCjcyOrderVo.getCkItemName());
                rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ADD_PROBLEM), header, JSONObject.toJSONString(addCjcyOrderVo));
                reportLog.info("{}", JSON.parseObject(rs));
            });
        });
    }

    @Test
    void reviewPass() {
        String stageCode = "";
        String roomName = "";
        Long batchId = 1570627011694747650L;
        String roomCode = "";
        String sn = "";
        String location = "";
        Long dutyUserId = 1434443859735179265L;//C-A
        String status = TicketStatusType.CJCY_COMPLATE.getCode();

        OrderQuery orderQuery = new OrderQuery();
        orderQuery.setStageCode(stageCode);
        orderQuery.setRoomName(roomName);
        orderQuery.setRoomCode(roomCode);
        orderQuery.setSn(sn);
        orderQuery.setCheckBatchId(batchId);
        orderQuery.setLocation(location);
        orderQuery.setStatus(status);
        orderQuery.setDutyUserId(dutyUserId);

        List<Long> idList = cjcyMapper.getTargetOrderIdList(orderQuery);
        if (CollectionUtils.isEmpty(idList)) {
            throw new BusinessException("不存在对应状态的工单!");
        }

        String username = "ATE001";
        Map<String, String> header = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(username, "a123456", "UAT"));

        List<JSONObject> orderCheckTypeList = cjcyMapper.getOrderCheckType(idList);

        orderCheckTypeList.stream().parallel().forEach(orderType -> {

            ReviewProblemVo reviewProblemVo = new ReviewProblemVo();
            if (orderType.getString("checkType").equals("A")) {
                reviewProblemVo.setAttachments(TestDataUtils.getPicture(RandomUtil.randomInt(1,4)));
            }
            reviewProblemVo.setContent(TestDataUtils.getApiTestContent(BusinessType.CJCY, TicketProcessEnum.ReviewPass.getProcessDesc()) +
                    "_复验人-"+username );
            reviewProblemVo.setAddTime(DateUtils.now());
            reviewProblemVo.setOrderId(orderType.getLong("orderId"));

            String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiCjcy.APP_ADD_REVIEWPASS), header, JSONObject.toJSONString(reviewProblemVo));
            reportLog.info("{}", JSON.parseObject(rs));
        });

    }

    private OrderQuery setOrderQuery() {
        String stageCode = "";
        String roomName = "";
        Long batchId = 1570627011694747650L;
        String roomCode = "";
        String sn = "";
        String location = "";
        Long dutyUserId = 1434443859735179265L;//C-A
        String status = TicketStatusType.CJCY_COMPLATE.getCode();

        OrderQuery orderQuery = new OrderQuery();
        orderQuery.setStageCode(stageCode);
        orderQuery.setRoomName(roomName);
        orderQuery.setRoomCode(roomCode);
        orderQuery.setSn(sn);
        orderQuery.setCheckBatchId(batchId);
        orderQuery.setLocation(location);
        orderQuery.setStatus(status);
        orderQuery.setDutyUserId(dutyUserId);

        return orderQuery;
    }

}
