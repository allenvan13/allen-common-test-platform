package com.allen.testplatform.modules.databuilder.model.pile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fan QingChuan
 * @since 2022/3/14 10:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssertDetail {

    private Long detailId;

//    private String orgCode;
//    private String orgName;
//    private String projectCode;
//    private String projectName;
//    private String stageCode;
//    private String stageName;

    private Long sectionId;
    private String sectionName;
    private String pileAreaName;
    private String pileAreaCode;

    private Long typeId;
    private String typePath;
    private String typeName;
    private String pileSn;

    private Integer commitType;
}
