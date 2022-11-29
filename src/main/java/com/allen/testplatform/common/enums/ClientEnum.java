package com.allen.testplatform.common.enums;

import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.xiaoleilu.hutool.codec.Base64;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Fan QingChuan
 *
 * 登录涉及的应用信息
 */
@Getter
@AllArgsConstructor
public enum ClientEnum {

    UatJxManageBackSystem("匠星检查服务","供应商管理平台","nhdc-cloud-quality-check-service","app_7ab8c333","3569c12cb2c154f7df1ade4f79f827ea027055f2d3aeacb6b55cdad59a2c9f4a","UAT"),
    UatJxCheckDataSourceClient("匠星检查服务(数据源)","供应商管理平台(供应商数据源)","nhdc-cloud-quality-check-service","app_7ab8c333_SUPPLIER","c4c5276cefdf068b8c73ffdc01bbb03596a9cd32abf40f467da5ddc823f94523","UAT"),
    UatJxAlarmSystem("匠星预警系统","匠星预警","cloud-prov-alarm-service","jx_alarm","3315cf0ede92a7af5be212a90445b8f63c34421fd9325511d913421c78095214","UAT"),
    UatJxSupplierPortal("匠星供应商服务","供应商门户","nhdc-cloud-provider-platform","app_ea58b1e8","9b9513a0c2f69f0157ecdeee0ad8ef601d1b45c9fd3c4ed45311cb067ac3f483","UAT"),
    UatJxMaterialSystem("匠星材料系统","材料系统","podm-common-api","Material","f74bfd211a503839e1a1b0dd4c222454ed846cb3ff8d7279eb1bab5ba123b10a3070ce7bc5f8c0dc4ff5cf6836383b96","UAT"),
    UatJxAppAndroid("匠星APP-Android端","xh_professional_android","xh_professional_android","xh_professional_android","80d11090cbf133d8dd8245e44306399e50855add60f1eff691c0be44fbb7c945fe17fda4b2b8c922645dcc70061f4531","UAT"),
    UatJxAppIOS("匠星APP-IOS端","xh_professional_ios","xh_professional_ios","xh_professional_ios","aa8abf59955d857e2df7777370998f179d993c59caba0e2c24dc75e461876490d5e61bafb8953c43a89a5eb6611f8238","UAT"),
    UatUc("用户中心","用户中心","","upms","e2136616086c4993f0883a5c715788cf","UAT"),

    ProJxManageBackSystem("匠星检查服务","","nhdc-cloud-quality-check-service","app_1285530054763077633","d45d0d86de4df852828721d72c2f8012d1e9a8d01c94fc09fc25c5851c26e8ec","PRO"),
    ProJxCheckDataSourceClient("匠星检查服务(数据源)","","nhdc-cloud-quality-check-service","app_1285530054763077633_SUPPLIER","0dbbcbf49939be1488f2eead21e81c3d8b5f9dbc3b5317280800221308ffc2fc","PRO"),
    ProJxAlarmSystem("匠星预警系统","","cloud-prov-alarm-service","jx_alarm","fdb7b417943a4d7ee81af1b218d6c3dc5dfea1d0a35383f24324a21a6a050f73","PRO"),
    ProJxSupplierPortal("匠星供应商服务","","nhdc-cloud-provider-platform","app_1285529660468064258","c78a7771ec469a471741631c1dbebaafca0731527ce4d0684ad2bec668867230","PRO"),
    ProJxMaterialSystem("匠星材料系统","","podm-common-api","Material","ab568839bb8f27fd822f97a59b5da71a0e8949ac467199bde53359df25b9206493f66f897da3f9b74cfc1927355a5e4f","PRO"),
    ProJxAppAndroid("匠星APP-Android端","xh_professional_android","xh_professional_android","xh_professional_android","80d11090cbf133d8dd8245e44306399e50855add60f1eff691c0be44fbb7c945fe17fda4b2b8c922645dcc70061f4531","PRO"),
    ProJxAppIOS("匠星APP-IOS端","xh_professional_ios","xh_professional_ios","xh_professional_ios","aa8abf59955d857e2df7777370998f179d993c59caba0e2c24dc75e461876490d5e61bafb8953c43a89a5eb6611f8238","PRO"),
    ProUc("用户中心","用户中心","","upms","e2136616086c4993f0883a5c715788cf","PRO"),
    ;

    public static String getClientBasic(String businessName,String env){
        for (ClientEnum clientEnum : ClientEnum.values()) {
            if (clientEnum.getBusinessName().contains(businessName) && clientEnum.getEnv().equalsIgnoreCase(env)) {
                return Constant.BASIC_KEY.concat(Base64.encode(clientEnum.getClientId().concat(":").concat(EncryptUtils.decrypt(clientEnum.getClientSecret()))));
            }
        }
        return null;
    }

    public static ClientEnum getTargetClient(String businessName, String env){
        for (ClientEnum clientEnum : ClientEnum.values()) {
            if (clientEnum.getBusinessName().contains(businessName) && clientEnum.getEnv().equalsIgnoreCase(env)) {
                return clientEnum;
            }
        }
        return null;
    }

    /**
     * 业务系统名称(常用名)
     */
    private String businessName;
    /**
     * 用户中心中 应用中文名称
     */
    private String clientName;
    /**
     * 应用后端工程名称
     */
    private String projectName;
    private String clientId;
    /**
     * 加密后
     */
    private String clientSecret;
    private String env;
}
