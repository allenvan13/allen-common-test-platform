package com.allen.testplatform.modules.databuilder.service;

/**
 * @author Fan QingChuan
 * @since 2022/5/27 17:41
 */
public interface ProcessReportService {

    void saveRisks(String sectionName,Boolean hasRisk,String riskContent,Integer pictureNum,String createUserName,Integer testCount);
}
