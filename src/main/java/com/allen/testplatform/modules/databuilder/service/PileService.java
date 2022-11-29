package com.allen.testplatform.modules.databuilder.service;

/**
 * @author Fan QingChuan
 * @since 2022/5/18 15:09
 */
public interface PileService {

    void submitBatchDetail(String checkTypeName,String pileAreaName,String reportName,String acceptorName,String sectionName,String ccorName,Integer pictureNum,Double pointX,Double pointY,String pileSn,Integer testCount,Integer commitType);

    void deleteDetails(String orgName,String projectName,String stageName,String sectionName,String partName,String typePath,String pileSn,String createUserName,Integer commitType);
}
