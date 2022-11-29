package com.allen.testplatform.common.enums;

import cn.nhdc.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author: Fan QingChuan
 * @since 2022/3/16 9:33
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum TestVersionEnum implements IEnum<String> {

    DEBUG("测试环境版本","DEBUG"),
    PROD("生产环境版本","PROD");

    private String versionName;
    private String code;

    @Override
    public String getValue() {
        return this.versionName;
    }

    public static TestVersionEnum getInstance(String versionCode) {
        TestVersionEnum target = null;
        for (TestVersionEnum value : values()) {
            if (versionCode.equalsIgnoreCase(value.getCode())) {
                target = value;
                break;
            }
        }
        return target;
    }
}
