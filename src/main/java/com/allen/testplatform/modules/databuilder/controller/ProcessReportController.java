package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.modules.databuilder.service.ProcessReportService;
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
 * @since 2022/5/27 23:28
 */
@Slf4j
@RestController
@RequestMapping("/pb")
@Validated
public class ProcessReportController {

    @Resource
    private ProcessReportService processReportService;

    @GetMapping("/saveBatch")
    public void saveBatch(@RequestParam @NotBlank(message = "标段名称不能为空") String sectionName,
                          @RequestParam(defaultValue = "false") Boolean hasRisk, @RequestParam String riskContent,
                          @RequestParam Integer pictureNum,@RequestParam String createUserName,
                          @RequestParam(defaultValue = "1") Integer testCount){
        processReportService.saveRisks(sectionName,hasRisk,riskContent,pictureNum,createUserName,testCount);
    }
}
