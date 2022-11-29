package com.allen.testplatform.common.enums;

import cn.nhdc.common.enums.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Fan QingChuan
 * @since 2022/3/16 9:33
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum TestTeamEnum implements IEnum<String> {

    JX("匠星","JX"),
    XWH("希望云","XWH"),
    DWWB("地网无边","DWWB"),
    JSPT("技术平台","JSPT"),
    DSJPT("大数据平台","DSJPT"),
    ALL("所有团队","ALL");

    private String teamName;
    private String teamCode;

    @Override
    public String getValue() {
        return this.teamName;
    }

    public static String getCodeByName(String teamName) {
        return getInstance(teamName).getTeamCode();
    }

    public static TestTeamEnum getInstance(String teamName) {
        TestTeamEnum target = null;
        for (TestTeamEnum value : values()) {
            if (teamName.equalsIgnoreCase(value.getTeamName())) {
                target = value;
                break;
            }
        }
        return target;
    }

    public static List<String> getAllTeamCode() {
        return Arrays.stream(TestTeamEnum.values()).map(TestTeamEnum::getTeamCode).collect(Collectors.toList());
    }

    public static List<String> getAllTeamName() {
        return Arrays.stream(TestTeamEnum.values()).map(TestTeamEnum::getTeamName).collect(Collectors.toList());
    }
}
