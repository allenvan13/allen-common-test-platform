package com.allen.testplatform.modules.databuilder.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UcUser implements Serializable {

    private static final long serialVersionUID = 1001L;
    private Long userId;
    private String userName;
    private String realName;
    private String phone;
    private String password;
    private String source;
    private String position;
    private String providerGuid;
    private String providerName;
}
