package com.allen.testplatform.modules.databuilder.model.test.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Fan QingChuan
 * @since 2022/4/19 12:15
 */
@Data
@AllArgsConstructor
public class HeartCheckTargetVo implements Serializable {

    private static final long serialVersionUID = 23456543234561L;

    @NotBlank(message = "被测URL不能为空")
    @NotNull(message = "缺少url字段")
    private String url;

    @NotNull(message = "缺少params字段")
    private String params;

    @NotNull(message = "缺少serviceName字段")
    private String serviceName;

    /**
     * 人员名称,多个用,相隔 消息卡片中会@配置的人 人员open_id需在枚举FeishuOpenIdEnum中配置
     */
    @NotNull(message = "缺少noticeUsers字段")
    private String noticeUsers;
    /**
     * 是否单独发送卡片消息至用户
     */
    @NotNull(message = "缺少hasPushUsers字段")
    private Boolean hasPushUsers;

    /**
     * 群组名称
     */
    @NotNull(message = "缺少noticeGroups字段")
    private String noticeGroups;
    /**
     * 是否通知群组 机器人需在群组内 且群组chat_id需在枚举FeishuOpenIdEnum中配置
     */
    @NotNull(message = "缺少hasPushGroups字段")
    private Boolean hasPushGroups;

    @NotBlank(message = "username不能为空")
    @NotNull(message = "缺少username字段")
    private String username;

    @NotBlank(message = "password不能为空")
    @NotNull(message = "缺少password字段")
    private String password;

    /**
     * 被测端口code 需在TestTerminalEnum中配置
     */
    @NotBlank(message = "terminalCode被测端口Code不能为空")
    @NotNull(message = "缺少terminalCode字段")
    private String terminalCode;

    @NotBlank(message = "测试环境env不能为空")
    @NotNull(message = "缺少env字段")
    private String env;


}
