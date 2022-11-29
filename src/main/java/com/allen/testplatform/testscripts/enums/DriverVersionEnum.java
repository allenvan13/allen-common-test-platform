package com.allen.testplatform.testscripts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DriverVersionEnum {

    Chrome_102("chromedriver102.exe","102","chrome"),
    Chrome_H5_102("chromedriver102.exe","102","chromeh5"),
    Chrome_104("chromedriver104.exe","104","chrome"),
    Chrome_106("chromedriver106.exe","106","chrome"),
    IE_420("IEDriverServer4_2_0.exe","4.2.0","ie"),
    FireFox_0310("geckodriver0_31_0.exe","0.31.0","firefox"),
    Edge_104("msedgedriver104.exe","104","edge"),
    Edge_102("msedgedriver102.exe","102","edge"),
    Chrome_88("chromedriver88.exe","88","chrome");

    private String fileName;
    private String version;
    private String type;

    public static String getTargetFileName(String type,String version) {
        for (DriverVersionEnum versionEnum : DriverVersionEnum.values()) {
            if (versionEnum.getVersion().equalsIgnoreCase(version) && versionEnum.getType().equalsIgnoreCase(type)) {
                return versionEnum.fileName;
            }
        }
        return null;
    }

}
