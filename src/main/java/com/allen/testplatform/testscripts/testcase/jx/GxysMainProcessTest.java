package com.allen.testplatform.testscripts.testcase.jx;

import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import cn.nhdc.common.util.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 工序验收-主流程测试
 *
 * @author Fan QingChuan
 * @since 2022/7/9 13:48
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class GxysMainProcessTest extends ConmonGxys {

    private static final ReportLog reportLog = new ReportLog(GxysMainProcessTest.class);

    @AfterTest(description = " ======== >> 测试完毕后,删除业务数据")
    void tearDown() {
        try {
            deleteApiTestData(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 1,description = " ======== >> 初始化数据: 报验人,报验区域、报验检查项")
    void initData() {
        List<UcUser> supplierUsers = ucMapper.getSupplierUsersByList(Arrays.asList("035EBA63-204B-4DDD-9960-C9DC9ED22B5F", "3FEC3C8D-6AA5-4B0C-82D1-C7580C9B8FB0","60046A99-FE80-46E7-A7F3-DE23E8DDA46B"));
        assert  supplierUsers.size() > 0; //平时使用的 已配置好的供应商账号 不可能为空
        currentUser = supplierUsers.parallelStream().filter(p -> p.getProviderName().contains("A公司") || p.getProviderName().contains("C公司")).filter(p -> p.getRealName().contains("员工")).findAny().get();
        accepterList = new ArrayList<>();
        spotcheckList = new ArrayList<>();
        checkPointList = new ArrayList<>();
        allCheckList = new ArrayList<>();

        allCheckList = processV2Mapper.getAllCheckList();
        lastCheck = getTargetLastCheck(lastCheckName, fatherOfLastCheckName, allCheckList);
        assert lastCheck != null;
        checkId = lastCheck.getLong("checkId");
        checkPointList = processV2Mapper.getCheckPoint(checkId);

        int count = processV2Mapper.countDetail(stageCode);
        if (count > 0) hasDownload = true;
        reportLog.info(" ======== >> 初始化数据完毕: 检查项[{}] 检查点[{}]  报验人[{}]",lastCheck,checkPointList,currentUser);
    }

    @Test(priority = 3,
            description = " ======== >> 提交报验: 断言1:详情页面提交按钮是否存在; 断言2: 报验状态是否为[待报验]; 断言3: 报验区域展示是否正确; " +
                    "断言4: 报验提交是否成功(toast未捕获到则进入详情页面查看状态是否更新); 断言5: 查看数据库对应验收工单数据状态是否正确 ")
    void testSubmitInspection() {

        loginAndChooseProcessCheckPart();

        //断言是否存在按钮[提交]
        Boolean submitExsit = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/submitTv"))).isDisplayed();
        Assertion.verifyEquals(submitExsit,true,"测试不通过! ");

        assertElementText(LocateType.ID,"cn.host.qc:id/statusTv","待报验");
//        assertElementText(LocateType.ID,"cn.host.qc:id/checkAreaTv",partName);  //TODO 断言报验区域 分类情况 后续补充

        //相机权限开启
        click(LocateType.ID,"cn.host.qc:id/photoIv");
        clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");

        //拍照上传图片
        takePhotosV2(checkPointList);

        swipeUp(1D,1);

        //输入备注
        String content = getContent(1);
        this.content = content;
        inputText(LocateType.ID,"cn.host.qc:id/extraDescEt",content);
        reportLog.info(" ======== >> 输入备注[{}]",content);

        //选择下一步验收人
        chooseNextOperatorUser();

        //提交
        click(LocateType.ID,"cn.host.qc:id/submitTv");
        reportLog.info(" ======== >> 点击提交");

        boolean subStatus = isToastHasAppeared(10, "提交成功");
        if (!subStatus) {
            clickText(roomNumber);
            WebElement status = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/statusTv")));
            String statusText = status.getText();
            Assertion.verifyNotEquals(statusText,"待报验","测试失败! 状态应不为待报验");
            reportLog.info(" ======== >> 未捕获到toast消息 则进入详情页面断言状态是否不为[待报验] 实际为[{}]",statusText);
        }

        //点击退出 到工作台
        closeModule();
        logout();

        assertProcessDetail(content, TicketProcessEnum.CreateProcess.getProcessCode());
    }

    @Test(priority = 5,
            description = " ======== >> 当前流程存在验收时: 提交验收: 断言1:验收通过提交是否成功(toast); 断言2: 查看数据库对应验收工单数据状态是否正确 ")
    void testSubmitAcception() {
        if (CollectionUtils.isNotEmpty(accepterList)) {
            accepterList.forEach(accept -> {
                List<UcUser> userByOthers = ucMapper.getUserByOthers(null, accept, null, null, null);
                assert userByOthers.size() == 1;
                this.currentUser = userByOthers.get(0);

                onceAcceptanceOrSpotCheck(2);
            });

            assertProcessDetail(content, TicketProcessEnum.AcceptProcess.getProcessCode());
        }
    }

    @Test(priority = 7,
            description = " ======== >> 当前流程存在抽检时: 提交抽检: 断言1:抽检通过提交是否成功(toast); 断言2: 查看数据库对应验收工单数据状态是否正确 ")
    void testSubmitSpotCheck() {

        if (CollectionUtils.isNotEmpty(spotcheckList)) {
            spotcheckList.forEach(spotcheck -> {
                List<UcUser> userByOthers = ucMapper.getUserByOthers(null, spotcheck, null, null, null);
                assert userByOthers.size() == 1;
                this.currentUser = userByOthers.get(0);

                onceAcceptanceOrSpotCheck(3);
            });

            assertProcessDetail(content, TicketProcessEnum.SpotCheckProcess.getProcessCode());
        }

    }




}
