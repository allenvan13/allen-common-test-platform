package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.modules.databuilder.mapper.*;
import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.fhcy.entity.TkTicket;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetail;
import com.allen.testplatform.testscripts.config.AppiumCapabilities;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.SpringAndroidTestBase;
import com.alibaba.fastjson.JSONObject;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeSuite;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 匠星APP-安卓端 通用操作统一封装
 *
 * @author Fan QingChuan
 * @since 2022/7/11 16:26
 */

public class CommonAndroid extends SpringAndroidTestBase {

    private static final ReportLog reportLog = new ReportLog(CommonAndroid.class);

    public WebDriverWait wait;

    //全局属性
    public static String cityName = "南京公司";
    public static String projectName = "南京紫樾府";
    public static String stageName = "南京紫樾府-1期";
    public static String sectionName = "南京紫樾府1标段";

    public static String cityCode = "FCNJGS001";
    public static String projectCode = "FCSYBPJ30675D46-F7BE-E911-80D6-91E68D9E0BEC";
    public static String stageCode = "NJJYZYYXGS.NJJKQ36M.1Q";
    public static String sectionId = "1498845212762828801";
    //测试手机
    public static String testPhone = "HJS5T19718010374";   //PQY0221B17001204  HJS5T19718010374

    @Resource
    protected FhcyV2Mapper fhcyV2Mapper;

    @Resource
    protected UserCenterMapper ucMapper;

    @Resource
    protected QcDeleteDataMapper deleteDataMapper;

    @Resource
    protected ProcessV2Mapper processV2Mapper;

    @Resource
    protected ZxxjV2Mapper zxxjV2Mapper;

    @BeforeSuite
    @Override
    public void beforeAllSuitesSetUp(){
        reportLog.info("准备执行测试 ========== >>  配置相关资源及环境");

        startDefaultBaseService();
        initBaseAndroidDriver(AppiumCapabilities.getConnectDevicesWithJxApp(testPhone));
        setBaseTimeOut(TIME_OUT);
        wait = new WebDriverWait(baseAndroidDriver, Duration.ofSeconds(TIME_OUT),Duration.ofMillis(SLEEP_TIME));
    }

    public void login(String username, String password,Boolean hasAgree) {
        if (hasAgree) {
            clickNegatively(LocateType.ID,"cn.host.qc:id/tv_privacy_agree");
        }
        inputText(LocateType.ID,"cn.host.qc:id/etAccount",username);
        inputText(LocateType.ID,"cn.host.qc:id/etPassword",password);
        click(LocateType.ID,"cn.host.qc:id/protocolCb");
        click(LocateType.ID,"cn.host.qc:id/btnLogin");

        reportLog.info(" ======== >> 登录账号 用户名:[{}],密码:[{}]",username,password);
    }

    public void logout() {
        //点击我的
        click(LocateType.ID,"cn.host.qc:id/tab_profile");
        //点击退出登录
        click(LocateType.ID,"cn.host.qc:id/logoutTv");
        //点击确认
        click(LocateType.ID,"cn.host.qc:id/confirm_tv");

        reportLog.info(" ======== >> 退出登录 ");
    }

    //目前写死 南京公司 南京紫樾府1期
    public void chooseStage(String cityName,String stageName) {

        //V4 点击 页面未跳转项目列表选择页面 则再次点击
        boolean isChooseProjectListPage = false;
        do {
            //点击[请选择项目分期]
            click(LocateType.ID,"cn.host.qc:id/projectTv");
            isChooseProjectListPage = isChooseProjectListPage();

        }while (!isChooseProjectListPage);

        //保留机制 防止其他意外进入 无数据页面时,退出重新进入 (不用于断言是)
        while (isElementsExsit(baseAndroidDriver,1,LocateType.ID,"cn.host.qc:id/emptyTv")) {
            pageBack();
            //点击[请选择项目分期]
            click(LocateType.ID,"cn.host.qc:id/projectTv");
        }

        //选择[南京公司]
        clickInElementsByText(LocateType.ID,"cn.host.qc:id/cityNameTv",cityName);
        click(LocateType.XPATH,"/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.RelativeLayout/android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView[2]/android.widget.LinearLayout[4]/android.widget.TextView");

        assertElementText(LocateType.ID,"cn.host.qc:id/projectTv",stageName);

        reportLog.info(" ======== >> 选择分期 [{}][{}]",cityName,stageName);
    }

    public boolean isChooseProjectListPage(){
        return isElementTextToBe(baseAndroidDriver,1,LocateType.ID,"cn.host.qc:id/tv_toolbar_title","项目筛选");
    }

    public void assertTicketAndStatus(String content,String expectStatus) {
        TkTicket tkTicket = fhcyV2Mapper.assertTicket(content);
        reportLog.info("查询数据库 ======== >> 工单数据最新为[{}]", JSONObject.toJSONString(tkTicket));
        reportLog.info("断言1 ======== >> 工单数据是否存在", JSONObject.toJSONString(tkTicket));
        Assertion.verifyNotNulls(tkTicket,"测试不通过! 数据库不存在该工单");
        reportLog.info("断言2 ======== >> 工单状态是否为[{}]", expectStatus);
        Assertion.verifyEquals(tkTicket.getStatus(), expectStatus,"测试不通过! 数据库工单状态不为 ["+expectStatus+"] 实际为: [" +tkTicket.getStatus()+ "]");
    }

    public void assertProcessDetail(String content, Integer expectStatus) {
        ProcessDetail processDetail = processV2Mapper.assertDetail(content);
        reportLog.info("查询数据库 ======== >> 工单数据最新为[{}]", JSONObject.toJSONString(processDetail));
        reportLog.info("断言1 ======== >> 工单数据是否存在", JSONObject.toJSONString(processDetail));
        Assertion.verifyNotNulls(processDetail,"测试不通过! 数据库不存在该工单");
        reportLog.info("断言2 ======== >> 工单状态是否为[{}]", expectStatus);
        Assertion.verifyEquals(processDetail.getStatus(), expectStatus,"测试不通过! 数据库工单状态不为 ["+expectStatus+"] 实际为: [" +processDetail.getStatus()+ "]");
    }

    public void takePhotoAndMark(WebDriverWait wait,int markType) {
        do {
            threadSleep("3");
            baseAndroidDriver.pressKey(new KeyEvent(AndroidKey.CAMERA));
        } while (!wait.until(ExpectedConditions.presenceOfElementLocated(By.id("com.huawei.camera:id/done_button"))).isDisplayed());
        click(LocateType.ID,"com.huawei.camera:id/done_button");
        threadSleep("1");

        //照片上标记涂鸦
        drawMarkInScreen(markType);
        click(LocateType.ID,"cn.host.qc:id/dispatch_tv");
        threadSleep("1");
    }

    public void enterModule(String moduleName) {
        click(LocateType.ANDROID_UIAUTOMATOR,"new UiSelector().text(\""+moduleName+"\")");
        reportLog.info(" ======== >> 进入[{}]业务模块",moduleName);
    }

    public void closeModule() {
        click(LocateType.ID,"cn.host.qc:id/iv_toolbar_left_sub");
        reportLog.info(" ======== >> 关闭页面退出到工作台或其他tab");
    }

    public void clickConfirm() {
        click(LocateType.ID,"cn.host.qc:id/confirmTv");
        reportLog.info(" ======== >> 点击确定");
    }

    public void clickCancel() {
        click(LocateType.ID,"cn.host.qc:id/cancelTv");
        reportLog.info(" ======== >> 点击取消");
    }

    public void pageBackModule() {
        click(LocateType.ID,"cn.host.qc:id/iv_toolbar_left_main");
        reportLog.info(" ======== >> 返回上一页");
    }

    public UcUser getUcUser(CheckUser checkUser) {
        return ucMapper.getUserById(checkUser.getUserId());
    }

    /**
     * 获取检查项id的所有父级Id直到根节点id
     * @param treeId 目标检查项id
     * @param trees 所有检查项
     * @return
     */
    public List<String> queryParentNames(Long treeId, List<JSONObject> trees) {
        //递归获取父级检查项名称,不包含自己
        List<String> parentNames = new ArrayList<>();
        treeOrgParent(trees, treeId, parentNames);
        return parentNames;
    }

    /**
     * 递归遍历获取检查项家族name
     * @param trees 所有检查项
     * @param treeId 目标检查项id
     * @param parentItemNames
     */
    public void treeOrgParent(List<JSONObject> trees, Long treeId, List<String> parentItemNames) {
        for (JSONObject tree : trees) {
            if (null == tree.getLong("parentId")) {
                continue;
            }
            //判断是否有父节点
            if (treeId.equals(tree.getLong("checkId"))) {

                String parentName = trees.stream().filter(item -> item.getLong("checkId").equals(treeId)).map(item -> item.getString("checkName")).findFirst().orElse(null);
//                if (ObjectUtil.isNotEmpty(parentName) && !tree.getBoolean("ifLast")) {
                if (ObjectUtil.isNotEmpty(parentName)) {
                    parentItemNames.add(parentName);
                }
                treeOrgParent(trees,tree.getLong("parentId"),parentItemNames);
            }
        }
    }

}
