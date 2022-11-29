package com.allen.testplatform.modules.databuilder.service;

import com.allen.testplatform.modules.databuilder.model.test.entity.HeartCheckTarget;
import com.allen.testplatform.modules.databuilder.model.test.vo.HeartCheckTargetVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Fan QingChuan
 * @since 2022/4/18 15:51
 */
public interface HeartCheckService extends IService<HeartCheckTarget> {

    /**
     * 统一封装 执行心跳检查
     * @param url  被测URL 不拼接后续参数  建议统一用GET请求的接口,且尽量能快速响应数据的接口
     * @param params  链接参数
     * @param serviceName 被测服务名
     * @param noticeUsers 消息卡片种 @的人员 多个用,分割 且均需在FeishuOpenIdEnum枚举中配置
     * @param noticeGroups 需通知的群名  多个用,分割 且均需在FeishuOpenIdEnum枚举中配置
     * @param username 测试账号用户名
     * @param password 测试账号密码
     * @param terminalCode 被测端code APP、PC_GYS、PC_BM、XCX、H5
     * @param env 被测环境 UAT\PRO\FAT
     * @return
     */
    boolean checkServiceByTarget(String url, String params,
                                 String serviceName, Boolean hasPushUsers, String noticeUsers,Boolean hasPushGroups, String noticeGroups,
                                 String username, String password, String terminalCode, String env);

    /**
     * 根据筛选环境、服务名 执行测试
     * @param env 环境 FAT/UAT/PRO
     * @param serviceName
     * @return [测试数量,成功数量]
     */
    int[] checkServiceByServiceAndEnv(String env,String serviceName,String teamCode);

    /**
     * 根据自定义执行测试
     * @param heartCheck
     * @return 服务是否正常
     */
    boolean checkServiceByVo(HeartCheckTargetVo heartCheck);

}
