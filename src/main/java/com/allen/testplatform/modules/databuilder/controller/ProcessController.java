package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.modules.databuilder.service.ProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

/**
 * @author Fan QingChuan
 * @since 2022/5/7 9:13
 */
@Slf4j
@RestController
@RequestMapping("/process")
@Validated
public class ProcessController {

    @Resource
    private ProcessService processService;

    @GetMapping("/addBatchProblem")
    public void addBatchProblem(@RequestParam Long detailId, @RequestParam String acceptorName,
                                @RequestParam String rectifyName, @RequestParam String reviewName,
                                @RequestParam String banName, @RequestParam String floorName,
                                @RequestParam String unitName, @RequestParam String roomName,
                                @RequestParam Integer severity, @RequestParam Integer deadlineDay, @RequestParam Integer pictureNum,
                                @RequestParam(defaultValue = "10") Integer testCount,@RequestParam(defaultValue = "3") Integer nodeLimit) {
        processService.addBatchProblem(detailId,acceptorName,rectifyName,reviewName,banName,floorName,unitName,roomName,severity,deadlineDay,pictureNum,testCount,nodeLimit);
    }

    @GetMapping("/submitOne")
    public void submitAndAcceptOrSpotCheckOne(@RequestParam String sectionName,@RequestParam Integer sectionType,@RequestParam String inspectorName,@RequestParam String lastCheckName,@RequestParam String parentCheckName,
                      @RequestParam String banName,@RequestParam String floorName,@RequestParam String unitName,@RequestParam String roomName,
                      @RequestParam(defaultValue = "3") Integer nodeLimit) {
        processService.submitAndAcceptOrSpotCheckOne(sectionName,sectionType,inspectorName,lastCheckName,parentCheckName,banName,floorName,unitName,roomName,nodeLimit);
    }

    @GetMapping("/submitBatch")
    public void submitAndAcceptOrSpotCheckBatch(@RequestParam String sectionName,@RequestParam Integer sectionType,@RequestParam String inspectorName,@RequestParam String lastCheckName,@RequestParam String parentCheckName,
                                              @RequestParam String banName,@RequestParam String floorName,@RequestParam String unitName,@RequestParam String roomName,
                                              @RequestParam(defaultValue = "3") Integer nodeLimit) {
        processService.submitAndAcceptOrSpotCheckBatch(sectionName,sectionType,inspectorName,lastCheckName,parentCheckName,banName,floorName,unitName,roomName,nodeLimit);
    }

    @GetMapping("/handleDetailProblem")
    public void handleDetailProblem(@RequestParam Long detaiLId,@RequestParam Integer operateType,@RequestParam Integer pictureNum,@RequestParam String secondRecitifyName){
        processService.recitfyOrReview(detaiLId,operateType,pictureNum,secondRecitifyName);
    }

    @GetMapping("/handleDetail")
    public void handleDetail(@RequestParam Long detaiLId,@RequestParam @NotBlank(message = "操作类型不能为空") Integer operateType){
        processService.callAcceptOrSpotCheckById(detaiLId,operateType);
    }

    @GetMapping("/handleDetailsByTarget")
    public void handleDetailsByTarget(@RequestParam @NotBlank(message = "城市公司不能为空") String orgName,@RequestParam @NotBlank(message = "项目名称不能为空") String projectName,@RequestParam String stageName,@RequestParam String sectionName,
                              @RequestParam Integer checkType,@RequestParam String checkPathName,@RequestParam String partName,
                              @RequestParam String submitCompanyName,@RequestParam String acceptCompanyName,@RequestParam @NotBlank(message = "操作类型不能为空") Integer operateType){
        processService.handleDetailByTarget(orgName,projectName,stageName,sectionName,checkType,checkPathName,partName,submitCompanyName,acceptCompanyName,operateType);
    }

    @GetMapping("/deleteDetails")
    public void deleteDetails(@RequestParam @NotBlank(message = "城市公司不能为空") String orgName,@RequestParam @NotBlank(message = "项目名称不能为空") String projectName,@RequestParam String stageName,@RequestParam String sectionName,
                              @RequestParam Integer checkType,@RequestParam String checkPathName,@RequestParam String partName,
                              @RequestParam String submitCompanyName,@RequestParam String acceptCompanyName,@RequestParam String status){
        processService.deleteDetailByTarget(orgName,projectName,stageName,sectionName,checkType,checkPathName,partName,submitCompanyName,acceptCompanyName,status);
    }

}
