package com.allen.testplatform.modules.databuilder.model.common;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/3/17 14:06
 */
@Data
public class SectionInfo {

    private Long sectionId;
    private String sectionName;
    private String orgCode;
    private String orgName;
    private String projectName;
    private String projectCode;
    private String stageName;
    private String stageCode;
    private String contractor;
    private String contractorGuid;
    private String supervisor;
    private String supervisorGuid;
    private Integer sectionType;
    private String sectionPicture;
    private List<String> construction;
    private List<String> constructionGuid;

}
