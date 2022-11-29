package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.enums.TestTeamEnum;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.modules.databuilder.enums.FeishuOpenIdEnum;
import com.allen.testplatform.modules.databuilder.mapper.HeartCheckMapper;
import com.allen.testplatform.modules.databuilder.model.feishu.FeishuCardMessage;
import com.allen.testplatform.modules.databuilder.model.test.entity.HeartCheckTarget;
import com.allen.testplatform.modules.databuilder.model.test.vo.HeartCheckTargetVo;
import com.allen.testplatform.modules.databuilder.service.HeartCheckService;
import cn.nhdc.common.util.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fan QingChuan
 * @since 2022/4/16 22:51
 */
@Slf4j
@Service("HeartCheckService")
public class HeartCheckServiceImpl extends ServiceImpl<HeartCheckMapper, HeartCheckTarget> implements HeartCheckService {

    @Resource
    private HeartCheckMapper heartCheckMapper;

    @Override
    public boolean checkServiceByTarget(String url, String params, String serviceName,Boolean hasPushUsers, String noticeUsers,Boolean hasPushGroups, String noticeGroups, String username, String password, String terminalCode, String env){

        boolean isHealth = true;

        boolean isOpenGateWay = CommonUtils.isOpenGateWay(terminalCode);
        String token;
        Map<String,String> header;
        if (isOpenGateWay) {
            token = TokenUtils.getJxAppAndroidToken(username, password,env);
        }else {
            token = TokenUtils.getJxCheckAuthToken(username,password,env);
        }
        header = TokenUtils.getHeader(token);

        String response = HttpUtils.doGet(url, header, params);
        response = response.replaceAll("\t", "").replaceAll("\n", "");
        if (response.contains("type=Service Unavailable, status=503")
                || response.contains("Service Unavailable")
                ||response.contains("status=503")) {//actual.equals(expectValue) ||
            isHealth = false;

            FeishuUtils feishuUtils = new FeishuUtils();
            FeishuCardMessage cardMessage = feishuUtils.buildHeartCheckCard(serviceName, DateUtils.now(), url, noticeUsers, response, url.concat("?").concat(params).concat("&token=").concat(token.substring(7)));
            Map<String, String> feishuHeaders = feishuUtils.getTenantTokenHeader();

            if (hasPushUsers && ObjectUtil.isNotEmpty(noticeUsers)) {
                List<String> noticeUsersOpenId = new ArrayList<>();
                String[] users = noticeUsers.split(",");
                for (String user : users) {
                    String userOpenId = FeishuOpenIdEnum.getOpenId(user);
                    noticeUsersOpenId.add(userOpenId);
                }
                feishuUtils.sendFeiShuMessage(cardMessage,feishuHeaders,noticeUsersOpenId,FeishuUtils.OPEN_ID_TYPE);
            }else {
                log.info("LOG--------未设置通知用户");
            }

            if (hasPushGroups && ObjectUtil.isNotEmpty(noticeGroups)) {
                List<String> groupIds = new ArrayList<>();
                String[] groups = noticeGroups.split(",");
                for (String group : groups) {
                    String groupChatId = FeishuOpenIdEnum.getOpenId(group);
                    groupIds.add(groupChatId);
                }
                feishuUtils.sendFeiShuMessage(cardMessage,feishuHeaders, groupIds,FeishuUtils.CHAT_ID_TYPE);
//                feishuUtils.sendFeiShuMessage(cardMessage,feishuHeaders, groupIds,FeishuUtils.CHAT_ID_TYPE);
            }else {
                log.info("LOG--------未设置通知群组");
            }
        }else {
            log.info("LOG---------------------> 服务正常!");
        }
        return isHealth;
    }

    @Override
    public int[] checkServiceByServiceAndEnv(String env,String serviceName,String teamCode) {
        List<HeartCheckTarget> heartCheckTargets;
        LambdaQueryWrapper<HeartCheckTarget> queryWrapper = new LambdaQueryWrapper<HeartCheckTarget>()
                .eq(HeartCheckTarget::getEnv, env)
                .eq(HeartCheckTarget::getDel_flag, false);

        if (ObjectUtil.isNotEmpty(serviceName)) {
            queryWrapper = teamCode.equals(TestTeamEnum.ALL.getTeamCode()) ? queryWrapper.eq(HeartCheckTarget::getServiceName,serviceName) :
                    queryWrapper.eq(HeartCheckTarget::getServiceName, serviceName).eq(HeartCheckTarget::getTeamCode,teamCode);
            heartCheckTargets = heartCheckMapper.selectList(queryWrapper);
        }else {
            if (!teamCode.equals(TestTeamEnum.ALL.getTeamCode())) {
                queryWrapper.eq(HeartCheckTarget::getTeamCode,teamCode);
            }
            heartCheckTargets = heartCheckMapper.selectList(queryWrapper);
        }
        AtomicInteger success = new AtomicInteger(0);
        if (CollectionUtils.isNotEmpty(heartCheckTargets)) {
            log.info("LOG----------------> 获取到[{}]个测试目标",heartCheckTargets.size());
            heartCheckTargets.forEach(o -> {
                boolean result = checkServiceByTarget(o.getUrl(), o.getParams(), o.getServiceName(),
                        false,o.getNoticeUsers(),true, o.getNoticeGroups(),
                        o.getUsername(), o.getPassword(), o.getTerminalCode(), o.getEnv());
                if (result) {
                    success.getAndIncrement();
                }
            });
        }else {
            log.info("LOG----------------> 未获取到被测目标");
        }
        return new int[]{heartCheckTargets.size(),success.get()};
    }

    public boolean checkServiceByVo(HeartCheckTargetVo heartCheck) {
        return checkServiceByTarget(heartCheck.getUrl(),heartCheck.getParams(),heartCheck.getServiceName(),
                heartCheck.getHasPushUsers(),heartCheck.getNoticeUsers(),heartCheck.getHasPushGroups(),heartCheck.getNoticeGroups(),
                heartCheck.getUsername(),heartCheck.getPassword(),heartCheck.getTerminalCode(),heartCheck.getEnv());
    }
}

