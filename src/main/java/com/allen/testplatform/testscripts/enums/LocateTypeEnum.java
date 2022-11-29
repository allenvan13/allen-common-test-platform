package com.allen.testplatform.testscripts.enums;

import com.allen.testplatform.testscripts.config.LocateType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum LocateTypeEnum {

    ID (LocateType.ID,"通用id定位方式"),
    NAME (LocateType.NAME,"通用name定位方式"),
    TAG_NAME (LocateType.TAG_NAME,"通用tagName定位方式"),
    CLASS_NAME (LocateType.CLASS_NAME,"通用className定位方式"),
    XPATH (LocateType.XPATH,"通用xpath定位方式"),
    CSS_SELECTOR (LocateType.CSS_SELECTOR,"通用cssSelector定位方式"),
    LINKTEXT (LocateType.LINKTEXT,"通用linkText定位方式"),
    PARTIAL_LINKTEXT (LocateType.PARTIAL_LINKTEXT,"通用partialLinkText定位方式"),

    APPIUM_ACCESSIBILITYID (LocateType.ACCESSIBILITYID,"Appium独有 Android对应content-desc属性,IOS对应name"),

    APPIUM_IMAGE (LocateType.APPIUM_IMAGE,"Appium通过图片定位"),
    APPIUM_ID (LocateType.APPIUM_ID,"AppiumBy.id"),
    APPIUM_NAME (LocateType.APPIUM_NAME,"AppiumBy.name"),
    APPIUM_TAGNAME (LocateType.APPIUM_TAGNAME,"AppiumBy.tagName"),
    APPIUM_CLASSNAME (LocateType.APPIUM_CLASSNAME,"AppiumBy.className"),
    APPIUM_CSSSELECTOR (LocateType.APPIUM_CSSSELECTOR,"AppiumBy.cssSelector"),
    APPIUM_LINKTEXT (LocateType.APPIUM_LINKTEXT,"AppiumBy.linkText"),
    APPIUM_PARTIALLINKTEXT (LocateType.APPIUM_PARTIALLINKTEXT,"AppiumBy.partialLinkText"),
    APPIUM_XPATH (LocateType.APPIUM_XPATH,"AppiumBy.xpath"),
    APPIUM_CUSTOM (LocateType.APPIUM_CUSTOM,"AppiumBy.custom"),

    ANDROID_UIAUTOMATOR (LocateType.ANDROID_UIAUTOMATOR,"Android uiautomator UI元素定位"),
    ANDROID_DATAMATCHER (LocateType.ANDROID_DATAMATCHER,"Android数据匹配器定位"),
    ANDROID_VIEWMATCHER (LocateType.ANDROID_VIEWMATCHER,"Android viewmatcher 视图匹配器"),
    ANDROID_VIEWTAG (LocateType.ANDROID_VIEWTAG,"android_viewtag 视图标签定位"),
    IOS_CLASSCHAIN (LocateType.IOS_CLASSCHAIN,"ios_classchain定位"),
    IOS_NSPREDICATESTRING (LocateType.IOS_NSPREDICATESTRING," ios_NsPredicate定位 语法参照 https://testerhome.com/topics/9405");

    private String name;
    private String description;

    public static LocateTypeEnum getTargetType(String locateType) {
        for (LocateTypeEnum value : LocateTypeEnum.values()) {
            if (value.getName().equalsIgnoreCase(locateType)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getAllType() {
        return Arrays.stream(LocateTypeEnum.values()).map(LocateTypeEnum::getName).collect(Collectors.toList());
    }

    public static List<String> getAllDescription() {
        return Arrays.stream(LocateTypeEnum.values()).map(LocateTypeEnum::getDescription).collect(Collectors.toList());
    }
}
