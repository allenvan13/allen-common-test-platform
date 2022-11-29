package com.allen.testplatform.modules.databuilder.enums;


import com.allen.testplatform.common.constant.Constant;

/**
 * @author Fan QingChuan
 * @since 2022/2/22 16:14
 */
public enum CheckTypeEnum {

    HOUSEHOLD(Constant.FHCY, "分户查验"),
    UNDERTAKE(Constant.CJCY, "承接查验"),
    PROJECT(Constant.GCJC, "工程检查"),
    SCENERY(Constant.JGJC, "景观检查"),
    DECORATE(Constant.ZSJC, "装饰检查"),
    REPAIR(Constant.ZXFX, "在线房修"),
    DESIGN(Constant.SJXJ, "设计巡检"),
    RETURN(Constant.CLTH, "材料退货"),
    DEMAND(Constant.CLDH, "材料订货"),
    RECEIVE(Constant.CLYS, "材料验收"),
    FOCUS(Constant.JZJF, "集中交付"),
    COST(Constant.COST, "成本审核"),
    SPECIAL(Constant.ZXXJ, "专项巡检"),
    PROCESS(Constant.GXYS, "工序验收"),
    PROCESS_PROBLEM(Constant.GXYS_PROBLEM, "工序验收问题"),
    TEMPLATE(Constant.YBYS, "样板验收"),
    TEMPLATE_PROBLEM(Constant.YBYS_PROBLEM, "样板验收问题"),
    REV_PROV_EVALUATION(Constant.GYSDY, "供应商满意度调研"),
    PROCESS_REPORT("NULL暂时无标识", "进度报告"),
    ;

    private String code;

    private String msg;

    CheckTypeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public static CheckTypeEnum getCheckType(String code) {
        for (CheckTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
