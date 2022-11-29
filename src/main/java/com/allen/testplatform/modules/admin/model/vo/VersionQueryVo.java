package com.allen.testplatform.modules.admin.model.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VersionQueryVo {

    /** 当前页码 */
    @NotNull(message = "当前页码不能为空")
    private Integer current = 1;
    /** 每页显示条数 */
    @NotNull(message = "每页显示条数不能为空")
    private Integer size = 20;

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
}
