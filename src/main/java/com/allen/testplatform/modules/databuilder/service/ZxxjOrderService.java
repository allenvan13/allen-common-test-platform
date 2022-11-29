package com.allen.testplatform.modules.databuilder.service;


/**
 * @author Fan QingChuan
 * @since 2022/4/11 10:39
 */
public interface ZxxjOrderService {

    void addProblems(String batchName, Long batchId, String templateName, String lastCheckName,
                              String banName, String floorName, String unitName, String roomName,
                              Boolean hasPoint, Double pointX, Double pointY,
                              Boolean hasNotice, String rectifyName,String reviewName,String checkName,
                              Integer importance, Integer writeOffDays, Integer pictureNum, Integer testCount);

    void addPro(String batchName, Long batchId, String templateName, String lastCheckName,
                     String banName, String floorName, String unitName, String roomName,
                     Boolean hasPoint, Double pointX, Double pointY,
                     Boolean hasNotice, String rectifyName,String reviewName,String checkName,
                     Integer importance, Integer writeOffDays, Integer pictureNum );

    void recitifyOrReviewProblems(String batchName, Long batchId, String orgName, String projectName, String stageCode,
                                         String banName, String floorName, String unitName, String roomName,
                                         String providerName, String lastCheckName, String creatorName,Integer pictureNum, Integer operateType);

}
