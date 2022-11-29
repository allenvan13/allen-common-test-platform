package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.modules.databuilder.service.FhcyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;

/**
 * @author Fan QingChuan
 * @since 2022/5/10 17:15
 */
@Slf4j
@RestController
@RequestMapping("/fhcy")
@Validated
public class FhcyController {

    @Resource
    private FhcyService fhcyService;

    @GetMapping("/addPros")
    public void addBatchProblems(@RequestParam String batchName, @RequestParam String firstCheckName, @RequestParam String secondCheckName, @RequestParam String lastCheckName,
                    @RequestParam String banName, @RequestParam String unitName, @RequestParam String floorName, @RequestParam String roomName, @RequestParam String projectSiteName,
                    @RequestParam String rectifyUserName, @RequestParam String checkUserName, @RequestParam String importance,@RequestParam(defaultValue = "1")  Integer checkImageIndex, @RequestParam(defaultValue = "随机") String positionName, @RequestParam String nearDirection,
                    @RequestParam String nearPercent,@RequestParam  Double x,@RequestParam  Double y,@RequestParam(defaultValue = "1") Integer testCount  ) throws ParseException {
        fhcyService.addBatchProblems(batchName,firstCheckName,secondCheckName,lastCheckName,banName,unitName,floorName,roomName,projectSiteName,rectifyUserName,checkUserName,importance,checkImageIndex,positionName,nearDirection,nearPercent,x,y,testCount);
    }

    @GetMapping("/rectifyPros")
    public void rectifyBatchProblems(@RequestParam Long bactchId,@RequestParam String stageCode) {
        fhcyService.rectifyBatchProblems(bactchId,stageCode);
    }

    @GetMapping("/reviewPros")
    public void reviewBatchPassOrNot(@RequestParam String stageCode,@RequestParam Long bactchId,@RequestParam(defaultValue = "核销通过") String reviewTypeName  ) {
        fhcyService.reviewBatchPassOrNot(stageCode,bactchId,reviewTypeName);
    }

    @GetMapping("/returnPros")
    public void returnBatchProblems(@RequestParam Long bactchId,@RequestParam String stageCode) {
        fhcyService.returnBatchProblems(bactchId,stageCode);
    }

    @GetMapping("/handlePros")
    public void handleProblems(@RequestParam String batchName, @RequestParam String firstCheckName, @RequestParam String secondCheckName, @RequestParam String lastCheckName,
                                 @RequestParam String banName, @RequestParam String unitName, @RequestParam String floorName, @RequestParam String roomName, @RequestParam String projectSiteName,
                                 @RequestParam String rectifyUserName, @RequestParam String checkUserName, @RequestParam String importance,@RequestParam(defaultValue = "1") Integer checkImageIndex,@RequestParam(defaultValue = "随机") String positionName, @RequestParam String nearDirection,
                                 @RequestParam String nearPercent,@RequestParam  Double x,@RequestParam  Double y,@RequestParam(defaultValue = "1") Integer testCount,@RequestParam(defaultValue = "3") int nodeLimit) throws ParseException {
        fhcyService.addAndRectifyAndReview(batchName,firstCheckName,secondCheckName,lastCheckName,banName,unitName,floorName,roomName,projectSiteName,rectifyUserName,checkUserName,importance,checkImageIndex,positionName,nearDirection,nearPercent,x,y,testCount,nodeLimit);
    }
}
