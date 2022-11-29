package com.allen.testplatform.modules.databuilder.enums;

import cn.nhdc.common.enums.IEnum;

import java.util.HashMap;
import java.util.Map;

public enum TicketStatusType implements IEnum<String> {
    //工程检查
    GCJC_WAIT_COMPLATE("10", "待整改"),
    GCJC_RE_COMPLATE("21", "重新整改"),
    GCJC_WAIT_VERIFY("31", "待复验"),
    GCJC_NO_NORMAL_CLOSE("91", "非正常关闭"),
    GCJC_NORMAL_CLOSE("92", "正常关闭"),

    //分户
    FHCY_PROCESSING("50", "待整改"),
    FHCY_PASSED("51", "已通过"),
    FHCY_CANCELLATION("52", "已作废"),
    //2021-03-23 处理中修改为待整改，新增已整改类型
    FHCY_COMPLATE("53", "已整改"),
    FHCY_APPOINT("5400", "待指派"),
    FHCY_REJECT("5600", "驳回"),

    //承接查验
    CJCY_PROCESSING("50", "待整改"),
    CJCY_PASSED("51", "已通过"),
    CJCY_CANCELLATION("52", "已作废"),
    CJCY_COMPLATE("53", "已整改"),
    CJCY_APPOINT("5400", "待指派"),
    CJCY_SUBMIT("5500", "已提交"),
    CJCY_REJECT("5600", "驳回"),

    //
    SJXJ_NO_DISTRIBUTE("1", "待分配"),
    SJXJ_NO_CONFIRM("2", "待确认"),
    SJXJ_NO_RECTIFY("3", "待整改"),
    SJXJ_NO_REVIEW("4", "待复验"),
    SJXJ_NORMAL_CLOSE("5", "正常关闭"),
    SJXJ_NO_NORMAL_CLOSE("6", "非正常关闭"),

    //材料到货
    CLDH_NO_CONFIRM("100", "待确认"),
    CLDH_REJECTED("101", "已拒绝"),
    CLDH_CONFIRMED("102", "已确认"),
    CLDH_INVALIDATED("103", "已作废"),

    //材料验货
    CLYH_NO_RECEIVING("200", "待收货"),
    CLYH_RECEIVING("201", "验货中"),
    CLYH_RECEIVED("202", "已收货"),
    CLYH_INVALIDATE("203", "已作废"),
    CLYH_REJECTED("204", "已拒绝"),
    CLYH_RECEIVED_INSTALL("205", "已验收"),

    /**
     * 成本系统状态
     */
    COST_TODO("COST50", "待处理"),
    COST_PASSED("COST51", "已通过"),
    COST_CANCEL("COST52", "作废"),
    COST_RECEIVE("COST53", "已接收"),
    COST_DO("COST54", "已处理"),

    //材料退货
    CLTH_NO_CONFIRM("300", "待确认"),
    CLTH_REJECTED("301", "已拒绝"),
    CLTH_CONFIRMED("302", "已确认"),
    CLTH_INVALIDATED("303", "已作废"),


    ZXXJ_SUBMIT("ZX01", "已提交"),
    ZXXJ_WAIT_COMPLATE("ZX02", "待整改"),
    ZXXJ_RE_COMPLATE("ZX04", "重新整改"),
    ZXXJ_WAIT_VERIFY("ZX03", "待复验"),
    ZXXJ_NORMAL_CLOSE("ZX05", "正常关闭"),
    ZXXJ_UN_NORMAL_CLOSE("ZX06", "非正常关闭"),
    ZXXJ_WAIT_APPOINT("ZX07", "待指派"),
    ZXXJ_CANCEL("ZX08", "作废"),

    /**
     * 工序验收状态
     */
    GXYS_ACCEPTANCE("GXYS01", "待验收"),
    GXYS_FINISH("GXYS02", "已验收"),
    GXYS_ANEW("GXYS03", "重新报验"),
    GXYS_TRANSFER("GXYS04", "已转派"),
    /**
     * 工序验收问题状态
     */
    GXYS_PROBLEM_WAIT_COMPLATE("GXYSP01", "待整改"),
    GXYS_PROBLEM_RE_COMPLATE("GXYSP02", "重新整改"),
    GXYS_PROBLEM_WAIT_VERIFY("GXYSP03", "待复验"),
    GXYS_PROBLEM_NO_NORMAL_CLOSE("GXYSP04", "非正常关闭"),
    GXYS_PROBLEM_NORMAL_CLOSE("GXYSP05", "正常关闭"),
    /**
     * 样板 状态
     */
    YBYS_SUBMIT("YBYS01", "待验收"),
    YBYS_ACEEPT("YBYS02", "已验收"),
    YBYS_ANEW("YBYS03", "重新报验"),
    YBYS_BACK("YBYS04", "已退回"),
    /**
     * 样板验收问题状态
     */
    YBYS_PROBLEM_WAIT_COMPLATE("YBYSP01", "待整改"),
    YBYS_PROBLEM_RE_COMPLATE("YBYSP02", "重新整改"),
    YBYS_PROBLEM_WAIT_VERIFY("YBYSP03", "待复验"),
    YBYS_PROBLEM_NO_NORMAL_CLOSE("YBYSP04", "非正常关闭"),
    YBYS_PROBLEM_NORMAL_CLOSE("YBYSP05", "正常关闭"),

    /**
     * 供应商满意度调研
     */
    GYSDY_NOT_EVALUATION("GYSDY01", "待评价"),
    GYSDY_EVALUATION("GYSDY02", "已评价"),
    ;
    private String code;

    private String msg;

    private static final Map<String, TicketStatusType> ALL_MAP = new HashMap<>();

    TicketStatusType(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    @Override
    public String getValue() {
        return code;
    }

    public static TicketStatusType getTicketStatus(String code) {
        if (ALL_MAP.isEmpty()) {
            for (TicketStatusType type : values()) {
                ALL_MAP.put(type.getCode(), type);
            }
        }
        return ALL_MAP.get(code);
    }

    public static String getMsg(String code) {
        for (TicketStatusType t : TicketStatusType.values()) {
            if (code.equals(t.getCode())) {
                return t.getMsg();
            }
        }
        return null;
    }

}