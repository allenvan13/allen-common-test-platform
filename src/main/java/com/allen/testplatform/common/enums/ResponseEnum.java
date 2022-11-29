package com.allen.testplatform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * api测试 断言 通用返回code枚举
 *
 */

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum ResponseEnum {

    SUCCESS("0000","操作成功");

    private String code;
    private String message;
}
