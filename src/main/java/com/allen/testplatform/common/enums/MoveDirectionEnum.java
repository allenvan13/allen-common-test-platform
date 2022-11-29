package com.allen.testplatform.common.enums;

import cn.nhdc.common.enums.IEnum;

/**
 * @author Fan QingChuan
 * @since 2021/12/22 15:03
 */
public enum MoveDirectionEnum implements IEnum<String> {

    LEFT_UP("LEFT_UP","左上"),
    LEFT_BOTTOM("LEFT_BOTTOM","左下"),
    RIGHT_UP("RIGHT_UP","右上"),
    RIGHT_BOTTOM("RIGHT_BOTTOM","右下"),
    CENTER("CENTER","中心"),
    ALL("ALL","随机"),
    ;

    private String code;
    private String desc;

    MoveDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getPositionCode() {
        return this.code;
    }

    public String getPositionDesc() {
        return this.desc;
    }

    public static MoveDirectionEnum getPositionEnum(String positionName) {
        for (MoveDirectionEnum t : MoveDirectionEnum.values()) {
            if (positionName.equals(t.getPositionDesc())) {
                return t;
            }
        }
        return null;
    }

    @Override
    public String getValue() {
        return code;
    }
}
