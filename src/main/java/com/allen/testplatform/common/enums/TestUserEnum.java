package com.allen.testplatform.common.enums;

import com.allen.testplatform.common.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author Fan QingChuan
 * @since 2021/12/4 17:02
 */

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public enum TestUserEnum {

    EMPLOYEE_A("NHATE-员工A","ATE001","13655666661","1434711468133150722","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_B("NHATE-员工B","ATE002","13655666662","1434714042345287681","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_C("NHATE-员工C","ATE003","13655666663","1434714452170731522","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_D("NHATE-员工D","ATE004","13655666664","1454819064429010946","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_E("NHATE-员工E","ATE005","13655666665","1454819962052009985","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_F("NHATE-员工F","ATE006","13655666666","1454820691760242689","14f47e684733e4811b718840a1fee894","PS"),
    EMPLOYEE_G("NHATE-员工G","ATE007","13655666667","1454821035504427009","14f47e684733e4811b718840a1fee894","PS"),
SUPPLIER_AA("A-员工A-AutoTest","a-yga-autotest","13877888881","1434414339435098113","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_AB("A-员工B-AutoTest","a-ygb-autotest","13877888882","1434414897445302274","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_AC("A-员工C-AutoTest","a-ygc-autotest","13877888883","1434415080182738945","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_AD("A-员工D-AutoTest","a-ygd-autotest","13877888884","1434415125607051265","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_BA("B-员工A-AutoTest","b-yga-autotest","13766777771","1434440410574450689","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_BB("B-员工B-AutoTest","b-ygb-autotest","13766777772","1434440916122300418","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_BC("B-员工C-AutoTest","b-ygc-autotest","13766777773","1434440975954046977","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_BD("B-员工D-AutoTest","b-ygd-autotest","13766777774","1434441043616559105","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_CA("C-员工A-AutoTest","c-yga-autotest","13988999991","1434443859735179265","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_CB("C-员工B-AutoTest","c-ygb-autotest","13988999992","1434443979415449601","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_CC("C-员工C-AutoTest","c-ygc-autotest","13988999993","1434444059409215490","14f47e684733e4811b718840a1fee894","SUPPLIER"),
SUPPLIER_CD("C-员工D-AutoTest","c-ygd-autotest","13988999994","1434444170898010114","14f47e684733e4811b718840a1fee894","SUPPLIER");

    private String name;
    private String username;
    private String phone;
    private String userId;
    private String password;
    private String source;

    public static TestUserEnum getInstance(String realName) {
        TestUserEnum target = null;
        for (TestUserEnum value : values()) {
            if (realName.equalsIgnoreCase(value.getName())) {
                target = value;
                break;
            }
        }
        return target;
    }

    public static String getUsername(String realName) {

        TestUserEnum userEnum = getInstance(realName);
        if (Constant.PS_SOURCE.equals(userEnum.getSource())) {
            return userEnum.getUsername();
        }else {
            return userEnum.getPhone();
        }
    }

}
