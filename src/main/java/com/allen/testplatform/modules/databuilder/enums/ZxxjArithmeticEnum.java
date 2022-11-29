package com.allen.testplatform.modules.databuilder.enums;

import lombok.Getter;

/**
 * @author FanQingChuan
 * @since 2021/11/17 10:52
 */
@Getter
public enum ZxxjArithmeticEnum {

    totalArithmetic("totalArithmetic", "组合模板总分算法"),
    weightRule("weightRule", "权重规则"),
    computeMode("computeMode", "计算方式"),
    fullMarkRule("fullMarkRule", "满分规则"),
    computeRule("computeRule", "计算规则"),
    awardDeductRule("awardDeductRule", "带*加扣规则"),
    ;

    private String code;

    private String desc;


    ZxxjArithmeticEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
