package com.allen.testplatform.testscripts.testcase.jx;

import com.allen.testplatform.modules.databuilder.enums.ZxxjTemplateTypeEnum;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import cn.nhdc.common.util.CollectionUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * 专项巡检-渲染打分区域回归测试用例
 *
 * @author Fan QingChuan
 * @since 2022/7/13 16:40
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class ZxxjPrintInputBoxTest extends CommonZxxj {

    private static final ReportLog reportLog = new ReportLog(ZxxjPrintInputBoxTest.class);

    @AfterTest
    void tearDown() {
        try {
            closeModule();
            logout();
        } catch (Exception e) {
        }
    }

    @Test(priority = 1,dataProvider = "regressionTestBatchList"
            ,description = " ======== >> 遍历各类型模板详情打分页面 断言输入框渲染是否符合预期")
    void testScoreAreaExsit(AssertBatch assertBatch){

        loginAndChooseBatch(assertBatch.getTestUserName(),assertBatch.getTestPassword(),assertBatch.getBatchName());

        List<AssertTemplateCheckItem> assertTemplateCheckItemList = assertBatch.getAssertTemplateCheckItemList();

        assertTemplateCheckItemList.forEach(checkItem -> {

            reportLog.info(" ======== >> 当前测试批次[{}],测试模板[{}],类型[{}],检查项路径[{}][{}]",
                    assertBatch.getBatchName(),checkItem.getTemplateName(),checkItem.getTemplateType(),checkItem.getParentCheckItemName(),checkItem.getLastCheckItemName());

            //组合模板 则回到模板首页
            if (assertBatch.getIsGroupTemplate() && !currentTemplate.get().equals(checkItem.getTemplateName()) && !currentTemplate.get().equals("null")) {
                pageBackModule();
            }

            //组合模板 需再次点击模板
            if (assertBatch.getIsGroupTemplate()) {
                clickText(checkItem.getTemplateName());
            }
            currentTemplate.set(checkItem.getTemplateName());

            if (CollectionUtils.isNotEmpty(checkItem.getParentCheckItemName())) {
                checkItem.getParentCheckItemName().forEach(item -> {
                    clickText(item,10);
                });
            }

            /**
             * 执行不同模板下检查项的断言
             *
             * 可打分加扣分 直接点末级项 进入
             * 定档打分  存在分数-点击末级项进入  不存在分数-点击扣分进入
             * 实测实量 直接点末级项 进入
             * 无打分  直接点末级项 进入
             * 可打分+权重  存在分数-点击末级项进入  不存在分数-点击扣分进入
             */
            if (checkItem.getTemplateType().equals(ZxxjTemplateTypeEnum.weightScore.getCode()) || checkItem.getTemplateType().equals(ZxxjTemplateTypeEnum.decideLevel.getCode())) {
                jumpIntoWeightScoreCheckItem(checkItem.getLastCheckItemName());
            }else {
                clickText(checkItem.getLastCheckItemName());
            }

            //断言
            assertInputBox(checkItem.getExtension(), checkItem.getTemplateType());
            //每次断言完成后 需退回到初始界面 准备下一次测试  其中: 如果测试模板没变 则不退出到模板选择页面  如果测试账号没变  则 不退出登录
            pageBackModule();

            if (CollectionUtils.isNotEmpty(checkItem.getParentCheckItemName())) {
                Collections.reverse(checkItem.getParentCheckItemName());
                checkItem.getParentCheckItemName().forEach(item -> {
                    clickText(item);
                });
            }
        });

        //每个批次测试完成后,重置模板名称
        currentTemplate.set("null");
    }

    void testSubmitProblem() {

    }
}
