package com.allen.testplatform.modules.databuilder.service;

/**
 * @author Fan QingChuan
 * @since 2022/5/7 22:53
 */
public interface ProcessService {


    /**
     *
     * @param detailId
     * @param acceptorName
     * @param rectifyName
     * @param reviewName
     * @param banName
     * @param floorName
     * @param unitName
     * @param roomName
     * @param severity
     * @param deadlineDay
     * @param pictureNum
     * @param testCount
     */
    public void addBatchProblem(Long detailId, String acceptorName, String rectifyName, String reviewName,
                         String banName, String floorName, String unitName, String roomName,
                         Integer severity, Integer deadlineDay, Integer pictureNum, Integer testCount, Integer nodeLimit);

    public void submitAndAcceptOrSpotCheckOne(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                              String banName,String floorName,String unitName,String roomName,
                                              Integer nodeLimit);

    public void submitAndAcceptOrSpotCheckBatch(String sectionName,Integer sectionType,String inspectorName,String lastCheckName,String parentCheckName,
                                              String banName,String floorName,String unitName,String roomName,
                                              Integer nodeLimit);

    public void recitfyOrReview(Long detaiLId,Integer operateType,Integer pictureNum,String secondRecitifyName);

    public void callAcceptOrSpotCheckById(Long detaiId,Integer operateType);

    public void handleDetailByTarget(String orgName,String projectName,String stageName,String sectionName,
                                     Integer checkType,String checkPathName,String partName,
                                     String submitCompanyName,String acceptCompanyName,Integer operateType);

    public void deleteDetailByTarget(String orgName,String projectName,String stageName,String sectionName,
                                     Integer checkType,String checkPathName,String partName,
                                     String submitCompanyName,String acceptCompanyName,String status);
}
