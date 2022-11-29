package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

/**
 * @author Fan QingChuan
 * @since 2022/4/25 17:59
 */
@Data
public class CheckUser {
    private Long userId;
    private String realName;
    /**
     * 登录username 内部员工 username = usercode  供应商登录userName为手机号
     */
    private String userName;
    private String userCode;
    /**
     * 角色类型
     */
    private Integer roleType;
    private String roleName;
    /**
     * 用户类型 PS SUPPLIER
     */
    private String userType;
    /**
     * 供应商用户类型  责任人、团队成员
     */
    private String empType;
    private String providerGuid;
    private String providerName;
    /**
     * 职位
     */
    private String position;
}
