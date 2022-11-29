package com.allen.testplatform.modules.databuilder.enums;

import lombok.Getter;

/**
 * @author FanQingChuan
 * @since 2021/11/17 10:52
 */
@Getter
public enum ZxxjTemplateTypeEnum {

    realMeasure("realMeasure", "实测实量"),
    realMeasureCustom("realMeasureCustom", "实测实量（自定义权重）"),
    qualityPoint("qualityPoint", "无打分，按合格计算点数/计算点总数，计算合格率"),
    weightScore("weightScore", "可打分，按“权重+得分率”计算总分"),
    awardDeduct("awardDeduct", "可打分，加/扣分"),
    decideLevel("decideLevel", "定档打分"),
    group("group", "组合模板"),
    ;

    private String code;

    private String desc;


    ZxxjTemplateTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static String getTypeName(String type) {
        for (ZxxjTemplateTypeEnum t : ZxxjTemplateTypeEnum.values()) {
            if (type.equals(t.getCode())) {
                return t.getDesc();
            }
        }
        return null;
    }

    public static ZxxjTemplateTypeEnum getType(String type) {
        for (ZxxjTemplateTypeEnum t : ZxxjTemplateTypeEnum.values()) {
            if (type.equals(t.getCode())) {
                return t;
            }
        }
        return null;
    }
}
