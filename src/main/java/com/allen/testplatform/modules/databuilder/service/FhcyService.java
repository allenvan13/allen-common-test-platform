package com.allen.testplatform.modules.databuilder.service;

import java.text.ParseException;

/**
 * @author Fan QingChuan
 * @since 2022/5/10 17:12
 */
public interface FhcyService {


    void addBatchProblems(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                          String banName, String unitName, String floorName, String roomName,
                          String projectSiteName, String rectifyUserName, String checkUserName,
                          String importance, Integer checkImageIndex, String positionName, String nearDirection,
                          String nearPercent, Double x, Double y, Integer testCount
    ) throws ParseException;

    void rectifyBatchProblems(Long batchId,String stageCode);

    void reviewBatchPassOrNot(String stageCode,Long bactchId,String reviewTypeName);

    void addAndRectifyAndReview(String batchName, String firstCheckName, String secondCheckName, String lastCheckName,
                                String banName, String unitName, String floorName, String roomName,
                                String projectSiteName, String rectifyUserName, String checkUserName,
                                String importance, Integer checkImageIndex, String positionName, String nearDirection,
                                String nearPercent, Double x, Double y, Integer testCount, int nodeLimit
    )throws ParseException;

    void returnBatchProblems(Long batchId,String stageCode);
}
