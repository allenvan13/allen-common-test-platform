package com.allen.testplatform.modules.databuilder.enums;

import cn.nhdc.common.enums.IEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fan QingChuan
 * @since 2022/4/17 22:57
 */
public enum FeishuOpenIdEnum implements IEnum<String> {

    FanYu("樊宇","17671645585","ou_aa42afe5ee6b9e36fddba0a219c97922"),
    ChengXingMing("程星铭","18820797532","ou_ed7828ceb9cdc5c355df6b3bb43c7cfe"),
    LuJiaXing("鲁家星","18620228979","ou_2930b6fa3eb709a8b89aa0a27023b16c"),
    HeJunLong("何俊龙","15906604221","ou_2fbc3c3e15c87de8bf3198d0ad32f2ae"),
    ChenPing("陈平","18428003636","ou_be72bff085a12232f3b78c78c6fad9e2"),
    FanQingChuan("范青川","13627664629","ou_adf29d2fd723749f8101103f0a32a80b"),
    ChenChao("陈超","17623555008","ou_21cab9a84316050454044ac797539908"),
    LuoJiaLi("罗家利","18108093537","ou_5a49c93678ef3875f78e8c78772c78ea"),
    LiZhiMing("李志明","18583279499","ou_070c624012112d2a63dbe9c7525b2808"),
    JxTestGroup("匠星-测试大佬","all","oc_7639d9b7bf55e5e4b96e5d4c58691652"),
    JxInerGroup("匠星内部群","all","oc_aff2f984b6a63d6b801ce0391dd865bf"),
    ;

    private String realName;
    private String mobile;
    private String openId;

    @Override
    public String getValue() {
        return openId;
    }

    private static final Map<String, FeishuOpenIdEnum> ALL_MAP = new HashMap<>();

    FeishuOpenIdEnum(String realName, String mobile, String openId) {
        this.realName = realName;
        this.mobile = mobile;
        this.openId = openId;
    }

    public String getRealName() {
        return this.realName;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getOpenId() {
        return this.openId;
    }

    public static FeishuOpenIdEnum getOpenIdUser(String realName) {
        if (ALL_MAP.isEmpty()) {
            for (FeishuOpenIdEnum user : values()) {
                ALL_MAP.put(user.getRealName(), user);
            }
        }
        return ALL_MAP.get(realName);
    }

    public static String getOpenId(String realName) {
        for (FeishuOpenIdEnum t : FeishuOpenIdEnum.values()) {
            if (realName.equals(t.getRealName())) {
                return t.openId;
            }
        }
        return null;
    }
}
