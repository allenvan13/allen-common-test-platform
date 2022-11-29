package com.allen.testplatform.modules.admin.model.dto;

import lombok.Data;

@Data
public class VersionManageDto {
    /**
     * 版本code
     */
    private String versionCode;
    /**
     * 版本更新内容
     */
    private String versionContent;
    /**
     * 版本类型 DEBUG/PROD
     */
    private String versionType;
    /**
     * 团队code
     */
    private String teamCode;
    /**
     * 团队name
     */
    private String teamName;
    /**
     * 客户端code
     */
    private String terminalCode;
    /**
     * 客户端名称
     */
    private String terminalName;
    /**
     * 是否是最新版本
     */
    private Boolean latestVersionFlag;
    /**
     * 是否已发版
     */
    private Boolean hasReleased;
    /**
     * 发版时间
     */
    private String releaseTime;
    /**
     * 开发者姓名
     */
    private String developer;
    /**
     * 测试人员姓名
     */
    private String tester;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 阿里云Key
     */
    private String ossKey;
    /**
     * 文件地址
     */
    private String url;

    private String createDate;
}
