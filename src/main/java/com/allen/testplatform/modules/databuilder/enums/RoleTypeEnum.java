package com.allen.testplatform.modules.databuilder.enums;

/**
 * @author Fan QingChuan
 * @since 2022/4/27 10:51
 */
public enum RoleTypeEnum {
    RectifyUser(1,"整改人员",CheckTypeEnum.HOUSEHOLD),
    CheckAndReview(0,"查验人员",CheckTypeEnum.HOUSEHOLD),
    ManageUser(4,"管理人员",CheckTypeEnum.HOUSEHOLD),
    SpotCheckUser(3,"抽检人员",CheckTypeEnum.HOUSEHOLD),
    ;

    private Integer roleCode;
    private String roleName;
    private CheckTypeEnum businessType;

    RoleTypeEnum(Integer roleCode, String roleName, CheckTypeEnum businessType){
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.businessType = businessType;
    }

    public Integer getRoleCode() {
        return this.roleCode;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public CheckTypeEnum getBusinessType() {
        return this.businessType;
    }

    public static RoleTypeEnum getRoleTypeEnum(Integer roleType) {
        for (RoleTypeEnum value : RoleTypeEnum.values()) {
            if (value.getRoleCode() == roleType) {
                return value;
            }
        }
        return null;
    }
}
