package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.common.utils.TestDataUtils;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketStatusType;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import cn.nhdc.common.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 分户查验-主流程测试用例
 *
 * @author Fan QingChuan
 * @since 2022/7/9 15:58
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class FhcyMainProcessTest extends CommonFhcy {

    private static final ReportLog reportLog = new ReportLog(FhcyMainProcessTest.class);

    private UcUser currentUser;

    private String banName;
    private String roomName;
    private String projectSite;
    private String content;

    private String checkName = "房屋天花 - 混凝土顶棚 - 开裂";

    @AfterTest(enabled = false)
    void tearDown() {
        try {
            TkTicket ticket = fhcyV2Mapper.assertTicket(content);
            int count1 = deleteDataMapper.removeBacklogBySn(Arrays.asList(ticket.getSn()));
            int count2 = deleteDataMapper.removeTicketByTypeContent(Constant.FHCY, Arrays.asList(content));
            reportLog.info(" ======== >> 删除测试数据 待办[{}]条,业务数据[{}]条",count1,count2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(description = " ======== >> 查验人创建提交问题-断言1、出现toast消息[提交成功] <br> 断言2、根据content(唯一)查询数据库 核对数据对象(含状态)是否正确")
    void testSubmitProblem(@Optional("ATE002") String username,@Optional("a123456") String password,
                       @Optional("南京分户多楼层")String batchName,
                       @Optional("四墙")String directionName) {
        login(username,password,true);
        chooseStage(cityName,stageName);

        enterModule("分户查验");
        boolean elementExsit = true;
        do {
            // 正在下载数据…100.00%
            elementExsit = isElementExsit(baseAndroidDriver, 2, LocateType.ID, "cn.host.qc:id/proTvProject");
        }while (elementExsit);

        clickText(batchName);
        reportLog.info(" ======== >> 点击查验批次[{}]",batchName);

        List<String> banList = Arrays.asList("B-11底商", "A-12精装高层", "A-3精装高层", "A-9精装高层", "A-4精装高层", "A-5精装高层", "A-14底商", "A-10精装高层");
        banName = banList.get(RandomUtil.randomInt(banList.size()));
        threadSleep("1");

        clickText(banName);
        reportLog.info(" ======== >> 点击楼栋[{}]",banName);

        threadSleep("1");
        WebElement title = null;

        title = getTitleEle();

        while(title.getText().trim().equals("选择楼栋")) {
            clickText(banName);
            reportLog.info(" ======== >> 页面刷新 再次点击楼栋 [{}]",banName);
            threadSleep("1");
            title = getTitleEle();
        }

        title = getTitleEle();

        if (title.getText().contains("选择房号")) {
            List<WebElement> roomList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvRoomShow")));
            WebElement room = roomList.get(RandomUtil.randomInt(roomList.size()));
            roomName = room.getText();
            room.click();
            reportLog.info(" ======== >> 点击房号[{}]",roomName);
        }

        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;

        //判断页面是否 新增问题 非新增页面则返回重新点 因为随机tap某点可能点到已存在问题的点位
        boolean isCreateNewPage = false;
        do {
            threadSleep("1");
            clickRandomPointInCustomArea(width*2/5,height*2/5,width*4/5,height*3/5);
            threadSleep("1");
            title = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(LocateType.ID,"cn.host.qc:id/tv_toolbar_title")));
            if (title.getText().equals("新增问题")) {
                reportLog.info(" ======== >> 页面title[新增问题],继续下一步");
                isCreateNewPage = true;
            }else {
                reportLog.info(" ======== >> 页面title非[新增问题],实际为:[{}],退回上一页面",title.getText());
                pageBack();
            }
        }while (isCreateNewPage == false);

        //部位
        click(LocateType.ID,"cn.host.qc:id/tvPlaceAddQst");
        List<WebElement> projectSiteList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvShowPlace")));
        WebElement site = projectSiteList.get(RandomUtil.randomInt(projectSiteList.size()));
        projectSite = site.getText();
        site.click();
        reportLog.info(" ======== >> 随机点击选择部位[{}]",projectSite);

        //检查项
        click(LocateType.ID,"cn.host.qc:id/tvCheckOneAddQst");
        clickText(checkName);
        //方位类型
        click(LocateType.ID,"cn.host.qc:id/tvOriAddQst");
        clickText(directionName);
        //输入补充说明
        content = TestDataUtils.getTestContent(3,BusinessType.FHCY,TicketProcessEnum.Create.getProcessDesc());
        inputText(LocateType.ID,"cn.host.qc:id/edtExplainAddQst", content);
        reportLog.info(" ======== >> 点选 检查项[{}],方位:[{}],输入补充说明",checkName,directionName,content);

        //上传图片
        click(LocateType.ID,"cn.host.qc:id/photoIv");
        clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");
        do {
            threadSleep("3");
            pressKey("CAMERA");
        } while (!wait.until(ExpectedConditions.presenceOfElementLocated(By.id("com.huawei.camera:id/done_button"))).isDisplayed());
        click(LocateType.ID,"com.huawei.camera:id/done_button");
        threadSleep("1");

        //照片上标记涂鸦
        drawMarkInScreen(RandomUtil.randomInt(1,4));
        click(LocateType.ID,"cn.host.qc:id/dispatch_tv");
        threadSleep("1");

        //向下翻页
        swipeUp(1D,1);
        String realName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/tvtvPrincipalAddQst"))).getText();
        List<UcUser> userByOthers = ucMapper.getUserByOthers(null, realName, null, null, null);
        assert  userByOthers.size() == 1;
        this.currentUser = userByOthers.get(0);

        //提交
        click(LocateType.ID,"cn.host.qc:id/tvUpdateAddQst");
        reportLog.info(" ======== >> 点击提交");
        threadSleep("1");
        assertToastHasAppeared(TIME_OUT,"问题提交成功");

        //退出到工作台
        closeModule();

        logout();

        reportLog.info(" ======== >> 准备断言:数据库工单数据是否符合预期");
        assertTicketAndStatus(content,TicketStatusType.FHCY_PROCESSING.getCode());
    }

    @Test(dependsOnMethods = "testSubmitProblem",
            description = " ======== >> 整改人整改问题-断言1、是否存在对应的待办 待办状态是否为待整改 及详情页面整改按钮是否存在 <br> 断言2、从模块进入详情页面 是否存在完成整改按钮,且提交整改后 toast消息[完成整改成功] <br> 断言3、查询数据库 状态是否变为已整改")
    void testRecitifyButtonExist() {
        login(currentUser.getSource().equals(Constant.SUPPLIER_SOURCE) ? currentUser.getPhone():currentUser.getUserName(), EncryptUtils.decrypt(currentUser.getPassword()),false);
//        login("13988999991","a123456",false);
        chooseStage(cityName,stageName);

        //待办
        click(LocateType.ID,"cn.host.qc:id/tab_task");
        clickText("分户查验");
        int time = 0;
        List<WebElement> elements;
        do {
            elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/descTv")));
            threadSleep("2");
            time++;
        }while (elements.size() == 0 || time <= 10);
        Assert.assertTrue(elements.size() > 0,"测试不通过! 应存在对应待办消息 实际不存在");
        elements.get(0).click();

        //断言是否是目标待办
        assertElementText(LocateType.ID,"cn.host.qc:id/tvMoreAloneQstDetail",content);
        //断言按钮是否存在 完成整改
        assertElementText(LocateType.ID,"cn.host.qc:id/tvRightAloneQstDetail","完成整改");

        //点击退出到 待办主页->工作台
        click(LocateType.ID,"cn.host.qc:id/iv_toolbar_left_sub");
        click(LocateType.ID,"cn.host.qc:id/tab_work");

        //点击模块进入进行整改
        enterModule("分户查验");

        //判断唯一性  PS: 这里数据量大时需算法算出工单唯一 暂未实现 思路: 遍历查出符合条件的List 一个一个点进详情页判断 不是则返回选下一个 特别多还需翻页下滑
        List<WebElement> ticketList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvTitleChangeAlone")));
        for (int i = 0; i < ticketList.size(); i++) {
            if (ticketList.get(i).getText().equals(projectSite.concat("-").concat(StringUtils.cleanBlank(checkName)))) {
                ticketList.get(i).click();
                break;
            }
        }

        //断言是否是目标待办
        assertElementText(LocateType.ID,"cn.host.qc:id/tvMoreAloneQstDetail",content);
        //断言按钮是否存在 完成整改
        assertElementText(LocateType.ID,"cn.host.qc:id/tvRightAloneQstDetail","完成整改");

        //点击完成整改
        click(LocateType.ID,"cn.host.qc:id/tvRightAloneQstDetail");
        click(LocateType.ID,"cn.host.qc:id/tvSure");

        //断言toast
        assertToastHasAppeared(TIME_OUT,"完成整改成功");

        //点击退出到工作台
        closeModule();
        logout();

        reportLog.info(" ======== >> 准备断言:数据库工单数据是否符合预期");
        assertTicketAndStatus(content,TicketStatusType.FHCY_COMPLATE.getCode());

        List<UcUser> users = ucMapper.getUserByOthers("ATE002", "NHATE-员工B", null, null, null);
        assert users.size() == 1;
        this.currentUser = users.get(0);
    }

    @Test(dependsOnMethods = "testRecitifyButtonExist"
            ,description = " ======== >> 复验人核销通过问题-从待办跳转详情页面提交核销通过 断言1、页面待办是否转已办,且状态是否为已通过  <br> 断言2、查询数据库 状态是否为已通过")
    void testReviewPass() {
        login(currentUser.getSource().equals(Constant.SUPPLIER_SOURCE) ? currentUser.getPhone():currentUser.getUserName(), EncryptUtils.decrypt(currentUser.getPassword()),false);
        chooseStage(cityName,stageName);

        //待办
        reportLog.info(" ======== >> 点击进入待办Tab");
        click(LocateType.ID,"cn.host.qc:id/tab_task");
        reportLog.info(" ======== >> 点击进入[分户查验]");
        clickText("分户查验");
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/descTv")));
        elements.get(0).click();

        reportLog.info(" ======== >> 准备断言:是否是目标待办");
        assertElementText(LocateType.ID,"cn.host.qc:id/tvCheckOneAloneQstDetail",projectSite.concat("-").concat(StringUtils.cleanBlank(checkName)));
        reportLog.info(" ======== >> 准备断言:[核销通过][核销不通过]按钮是否存在");
        assertElementText(LocateType.ID,"cn.host.qc:id/tvLeftAloneQstDetail","核销通过");
        assertElementText(LocateType.ID,"cn.host.qc:id/tvRightAloneQstDetail","核销不通过");

        click(LocateType.ID,"cn.host.qc:id/tvLeftAloneQstDetail");
        click(LocateType.ID,"cn.host.qc:id/tvConfirmDispose");

        //点击退出到 待办主页->工作台
        closeModule();
        click(LocateType.ID,"cn.host.qc:id/tab_work");

        logout();
        reportLog.info(" ======== >> 准备断言:数据库工单数据是否符合预期");
        assertTicketAndStatus(content,TicketStatusType.FHCY_PASSED.getCode());

    }

}
