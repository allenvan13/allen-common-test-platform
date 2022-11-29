package com.allen.testplatform.common.enums;

import com.allen.testplatform.common.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum TestTypeEnum {

    API_TEST("Api",1,"接口类型", Constant.API_AUTO_TEST),
    WEB_TEST("Web",2,"Web类型",Constant.WEB_AUTO_TEST),
    ANDROID_APP_TEST("Android",3,"安卓APP类型",Constant.ANDROID_AUTO_TEST),
    IOS_APP_TEST("IOS",4,"苹果APP类型",Constant.IOS_AUTO_TEST),
    ALL_TEST("ALL",9,"所有类型",Constant.AUTO_TEST);

    private String code;
    private Integer typeNumber;
    private String name;
    private String prefix;


    public static String getTagetName(Integer typeNumber) {
        TestTypeEnum instance = getInstance(typeNumber);
        if (instance != null) {
            return instance.getName();
        }
        return "未知类型";
    }

    public static TestTypeEnum getInstance(Integer typeNumber) {
        for (TestTypeEnum value : values()) {
            if (Objects.equals(typeNumber, value.getTypeNumber())) {
                return value;
            }
        }
        return null;
    }
}
