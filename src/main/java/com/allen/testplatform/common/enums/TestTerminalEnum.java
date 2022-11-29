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
public enum TestTerminalEnum implements IEnum<String> {

    JX_APP_ANDROID("匠星APP-安卓端","JX_APP_ANDROID"),
    JX_APP_IOS("匠星APP-苹果端","JX_APP_IOS"),
    JX_PC_GYS("匠星供应商门户","JX_PC_GYS"),
    JX_PC_BM("匠星后台管理","JX_PC_BM"),
    JX_XCX("匠星小程序","JX_XCX"),
    JX_H5("匠星H5","JX_H5");

    private String terminalName;
    private String terminalCode;

    @Override
    public String getValue() {
        return this.terminalName;
    }

    public static String getCodeByName(String terminalName) {
        return getInstance(terminalName).getTerminalCode();
    }

    public static TestTerminalEnum getInstance(String terminalName) {
        TestTerminalEnum target = null;
        for (TestTerminalEnum value : values()) {
            if (terminalName.equalsIgnoreCase(value.getTerminalName())) {
                target =  value;
                break;
            }
        }
        return target;
    }
}
