package com.allen.testplatform.modules.databuilder.model.processreport;

import lombok.Data;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/5/27 17:39
 */
@Data
public class ProcessRiskVo {
    private String orgCode;
    private String orgName;
    private String projectCode;
    private String projectName;
    private String stageCode;
    private String stageName;
    /**
     * 类型 1-工程 2-装饰 3-景观
     */
    private Integer type;
    private Long sectionId;
    private List<String> images;
    /**
     * 风险与销项情况
     */
    private String riskAndSale;
}
