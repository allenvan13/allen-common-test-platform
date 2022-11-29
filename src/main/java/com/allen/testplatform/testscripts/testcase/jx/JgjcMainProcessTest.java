package com.allen.testplatform.testscripts.testcase.jx;

import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.modules.databuilder.enums.CheckTypeEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketStatusType;
import com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * 景观检查-主流程测试
 *
 * @author Fan QingChuan
 * @since 2022/7/29 11:19
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class JgjcMainProcessTest extends CommonCheck {

    private static final ReportLog reportLog = new ReportLog(JgjcMainProcessTest.class);

    private String lastCheckName;
    private String secondCheckName;
    private String firstCheckName;
    private String checkUserName;
    private String recitifyUserName;
    private String reviewUserName;

    @AfterTest
    void tearDown() {
        try {
            TkTicket ticket = checkMapper.getTicketByContent(desContent,categoryCode);
            int count1 = deleteDataMapper.removeBacklogBySn(Arrays.asList(ticket.getSn()));
            int count2 = deleteDataMapper.removeTicketByTypeContent(categoryCode, Arrays.asList(desContent));
            reportLog.info(" ======== >> 删除[{}]测试数据 待办[{}]条,业务数据[{}]条",categoryName,count1,count2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(priority = 0,description = " ======== >> 初始化测试数据:业务类型、检查项、检查人、整改人、复验人")
    void initData() {
        CheckTypeEnum checkType = CheckTypeEnum.getCheckType(Constant.JGJC);
        categoryName = checkType.getMsg();
        categoryCode = checkType.getCode();
        setLastCheck(lastCheckName);
        setSecondCheck();
        setFirstCheck();

        lastCheckName = lastCheck.getString("name");
        secondCheckName = secondCheck.getString("name");
        firstCheckName = firstCheck.getString("name");

        setUsers(checkUserName,recitifyUserName,reviewUserName);

        checkUserName = checkUser.getRealName();
        recitifyUserName = recitifyUser.getRealName();
        reviewUserName = reviewUser.getRealName();

        reportLog.info(" ======== >> 完成数据初始化: 业务类型[{}] 检查项一级[{}],二级[{}],三级[{}],检查人[{}],整改人[{}],复验人[{}]",categoryName,firstCheckName,secondCheckName,lastCheckName,checkUserName,recitifyUserName,reviewUserName);
    }

    @Test(priority = 1,enabled = false,description = " ======== >> 进入模块,断言tab展示工单数量是否与实际相符")
    void testAssertTicketCount() {
        loginAndTestAssertTicketCount();
    }

    @Test(priority = 3,description = " ======== >> 创建问题-断言1、toast消息\"问题提交成功\"，断言2、数据库工单ticket数据(含状态)符合预期")
    void testCreateProblem() {
        currentUser = getUcUser(checkUser);
        login(currentUser.getSource().equals(Constant.PS_SOURCE) ? currentUser.getUserName():currentUser.getPhone(), EncryptUtils.decrypt(currentUser.getPassword()),true);
        chooseStage(cityName,stageName);
        createProblem();
        logout();
        assertTicketAndStatus(desContent, TicketStatusType.GCJC_WAIT_COMPLATE.getCode());
    }

    @Test(priority = 5,description = " ======== >> 整改问题-断言1、详情页面是否存在按钮[二次派单、完成整改]，断言2、整改后toast消息\"处理成功\"，断言3、数据库工单ticket数据(含状态)符合预期")
    void testRecitifyProblem() {
        currentUser = getUcUser(recitifyUser);
        login(currentUser.getSource().equals(Constant.PS_SOURCE) ? currentUser.getUserName():currentUser.getPhone(), EncryptUtils.decrypt(currentUser.getPassword()),false);
        chooseStage(cityName,stageName);
        enterModule(categoryName);
        waitLoading();

        //点击tab[待整改]
        clickInElementsByText(LocateType.ID,"cn.host.qc:id/titleTv","待整改");
        String checkName = String.format("%s-%s-%s",firstCheckName,secondCheckName,lastCheckName);

        //点击搜索目标工单并提交整改
        threadSleep("1");
        searchTargetTicketAndSubmitRecitify(checkName,desContent,false);

        logout();
        assertTicketAndStatus(desContent, TicketStatusType.GCJC_WAIT_VERIFY.getCode());

    }

    @Test(priority = 7,description = " ======== >> 复验（正常关闭）问题-断言1、详情页面是否存在按钮[非正常关闭、重新整改、正常关闭]，断言2、整改后toast消息\"处理成功\"，断言3、数据库工单ticket数据(含状态)符合预期")
    void testReviewProblem() {
        currentUser = getUcUser(reviewUser);
        login(currentUser.getSource().equals(Constant.PS_SOURCE) ? currentUser.getUserName():currentUser.getPhone(), EncryptUtils.decrypt(currentUser.getPassword()),false);
        chooseStage(cityName,stageName);
        enterModule(categoryName);
        waitLoading();

        //点击tab[待复验]
        clickInElementsByText(LocateType.ID,"cn.host.qc:id/titleTv","待复验");
        String checkName = String.format("%s-%s-%s",firstCheckName,secondCheckName,lastCheckName);
        //点击搜索目标工单并提交复验
        threadSleep("1");
        searchTargetTicketAndSubmitReview(checkName,desContent,false);

        logout();
        assertTicketAndStatus(desContent, TicketStatusType.GCJC_NORMAL_CLOSE.getCode());
    }

}
