package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.modules.databuilder.service.PileService;
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
 * @since 2022/4/11 10:25
 */
@Slf4j
@RestController
@RequestMapping("/pile")
@Validated
public class PileController {

    @Resource
    private PileService pileService;

    @GetMapping("/addBatch")
    public void addBatch(@RequestParam String checkTypeName,@RequestParam String pileAreaName,
                         @RequestParam String reportName,@RequestParam String acceptorName,
                         @RequestParam @NotBlank(message = "标段不能为空") String sectionName,@RequestParam String ccorName,
                         @RequestParam Integer pictureNum,@RequestParam Double pointX,
                         @RequestParam Double pointY,@RequestParam String pileSn,@RequestParam(defaultValue = "1") Integer testCount,@RequestParam(defaultValue = "2")Integer commitType) {
        pileService.submitBatchDetail(checkTypeName,pileAreaName,reportName,acceptorName,sectionName,ccorName,pictureNum,pointX,pointY,pileSn,testCount,commitType);
    }

    @GetMapping("/deleteBatch")
    public void deleteDetails(@RequestParam String orgName,@RequestParam String projectName,@RequestParam String stageName,
                              @RequestParam String sectionName,@RequestParam String partName,@RequestParam String typePath,
                              @RequestParam String pileSn,@RequestParam String createUserName,@RequestParam Integer commitType) {
        pileService.deleteDetails(orgName,projectName,stageName,sectionName,partName,typePath,pileSn,createUserName,commitType);
    }



}
