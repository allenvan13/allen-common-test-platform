package com.allen.testplatform.modules.databuilder.enums;

import cn.nhdc.common.enums.IEnum;

/**
 * @author Fan QingChuan
 * @since 2022/4/25 16:08
 */
public enum TicketProcessEnum implements IEnum<String> {

    Create(0,"创建问题"),
    Assign(55,"指派问题"),
    Rectify(56,"整改问题"),
    ReviewPass(51,"核销通过问题"),
    ReviewNoPass(52,"核销不通过问题"),
    Cancel(53,"作废问题"),
    CreateProcess(1,"报验"),
    AcceptProcess(2,"验收"),
    SpotCheckProcess(3,"抽检"),
    ReAssign(1,"二次指派问题"),
    ReRectify(2,"重新整改问题"),
    CompleteRectify(3,"完成整改问题"),
    UnNormalClose(4,"非正常关闭问题"),
    NormalClose(5,"正常关闭问题"),
    BACK(7,"撤回问题"),
    CANCEL(8,"作废问题"),
    AGAIN_SUBMIT(9,"重新提交问题")
    ;

    private Integer code;
    private String desc;

    TicketProcessEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getTargetDesc(Integer code) {
        for (TicketProcessEnum t : TicketProcessEnum.values()) {
            if (code.equals(t.code)) {
                return t.desc;
            }
        }
        return null;
    }

    public Integer getProcessCode() {
        return this.code;
    }

    public String getProcessDesc() {
        return this.desc;
    }

    @Override
    public String getValue() {
        return this.desc;
    }
}
