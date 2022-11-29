package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.modules.databuilder.service.ZxxjAlgorithmService;
import com.allen.testplatform.modules.databuilder.service.ZxxjOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Fan QingChuan
 * @since 2022/4/11 10:25
 */
@Slf4j
@RestController
@RequestMapping("/zxxj")
@Validated
public class ZxxjController {

    @Resource
    private ZxxjAlgorithmService zxxjAlgorithmService;

    @Resource
    private ZxxjOrderService zxxjOrderService;

    @GetMapping("/resetScore")
    public void resetBatchScore(@RequestParam String batchName,
                                @RequestParam String templateName,
                                @RequestParam String checkItemName) {
        zxxjAlgorithmService.resetScore(batchName,templateName,checkItemName);
    }

    @GetMapping("/resetBatch")
    public void resetBatch(@RequestParam String batchName,@RequestParam String templateName) {
        zxxjAlgorithmService.resetBatch(batchName,templateName);
    }

    @GetMapping("/scoreItem")
    public void testScoreItem(@NotBlank(message = "是否需要断言 不能为空") @RequestParam(defaultValue = "true")Boolean isNeedAssert,
                              @RequestParam String batchName,
                              @RequestParam String templateName,
                              @RequestParam String checkItemName,
                              @RequestParam String itemName) {
        if (isNeedAssert) {
            zxxjAlgorithmService.testAssertScoreItem(batchName,templateName,checkItemName,itemName);
        }else {
            zxxjAlgorithmService.testScoreItem(batchName,templateName,checkItemName,itemName);
        }
    }

    @GetMapping("/scoreBatch")
    public void testScoreBatch(@NotNull(message = "是否需要断言 不能为空") @RequestParam(defaultValue = "true")Boolean isNeedAssert,
                               @RequestParam String batchName,
                               @RequestParam String templateName,
                               @RequestParam String checkItemName,
                               Boolean hasBeenScored) {
        if (isNeedAssert) {
            zxxjAlgorithmService.testAssertScoreBatch(batchName,templateName,checkItemName,hasBeenScored);
        }else {
            zxxjAlgorithmService.testScoreBatch(batchName,templateName,checkItemName,hasBeenScored);
        }
    }

    @GetMapping("/addBatchProblem")
    public void addProblems(@RequestParam String batchName,@RequestParam  Long batchId,@RequestParam  String templateName,@RequestParam  String lastCheckName,
                            @RequestParam  String banName, @RequestParam String floorName,@RequestParam  String unitName,@RequestParam  String roomName,
                            @RequestParam(defaultValue = "false")  Boolean hasPoint,@RequestParam  Double pointX,@RequestParam  Double pointY,
                            @RequestParam(defaultValue = "false") Boolean hasNotice, @RequestParam String rectifyName,@RequestParam String reviewName,@RequestParam String checkName,
                            @RequestParam Integer importance, @RequestParam Integer writeOffDays,@RequestParam Integer pictureNum, @RequestParam(defaultValue = "1") Integer testCount) {
        zxxjOrderService.addProblems(batchName,batchId,templateName,lastCheckName,banName,floorName,unitName,roomName,hasPoint,pointX,pointY,hasNotice,rectifyName,reviewName,checkName,importance,writeOffDays,pictureNum,testCount);
    }

    @GetMapping("/addOneProblem")
    public void addProblems(@RequestParam String batchName,@RequestParam  Long batchId,@RequestParam  String templateName,@RequestParam  String lastCheckName,
                            @RequestParam  String banName, @RequestParam String floorName,@RequestParam  String unitName,@RequestParam  String roomName,
                            @RequestParam(defaultValue = "false")  Boolean hasPoint,@RequestParam  Double pointX,@RequestParam  Double pointY,
                            @RequestParam(defaultValue = "false") Boolean hasNotice, @RequestParam String rectifyName,@RequestParam String reviewName,@RequestParam String checkName,
                            @RequestParam Integer importance, @RequestParam Integer writeOffDays,@RequestParam Integer pictureNum) {
        zxxjOrderService.addPro(batchName,batchId,templateName,lastCheckName,banName,floorName,unitName,roomName,hasPoint,pointX,pointY,hasNotice,rectifyName,reviewName,checkName,importance,writeOffDays,pictureNum);
    }

    @GetMapping("/handleProblems")
    public void recitifyProblems(@RequestParam String batchName, @RequestParam Long batchId, @RequestParam String orgName, @RequestParam  String projectName, @RequestParam String stageCode,
                                 @RequestParam String banName, @RequestParam String floorName, @RequestParam String unitName, @RequestParam String roomName,
                                 @RequestParam String providerName, @RequestParam String lastCheckName, @RequestParam String creatorName,
                                 @RequestParam Integer pictureNum, @RequestParam @NotNull(message = "操作类型不能为空 1-整改问题 2-正常关闭 3-非正常关闭") Integer operateType) {
        zxxjOrderService.recitifyOrReviewProblems(batchName,batchId,orgName,projectName,stageCode,banName,floorName,unitName,roomName,providerName,lastCheckName,creatorName,pictureNum,operateType);
    }
}
