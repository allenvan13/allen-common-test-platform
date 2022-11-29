package com.allen.testplatform.modules.databuilder.model.feishu;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/4/17 11:16
 */
@Data
public class FeishuUser {
    private String name;
    private String user_id;
    private String open_id;
    private String phone;
    private String email;
    private String union_id;
    private List<String> department_ids;
}
