package com.allen.testplatform.modules.databuilder.model.test.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Fan QingChuan
 * @since 2022/4/19 12:15
 */
@Data
@TableName("heart_check_target")
@EqualsAndHashCode
@AllArgsConstructor
public class HeartCheckTarget implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String url;

    private String params;

    @TableField("service_name")
    private String serviceName;

    @TableField("notice_users")
    private String noticeUsers;

    @TableField("notice_groups")
    private String noticeGroups;

    private String username;
    private String password;

    @TableField("terminal_code")
    private String terminalCode;
    private String env;

    @TableField("team_code")
    private String teamCode;
    private Boolean del_flag;
}
