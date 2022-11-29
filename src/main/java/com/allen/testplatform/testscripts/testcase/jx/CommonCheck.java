package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.common.utils.TestDataUtils;
import com.allen.testplatform.modules.databuilder.enums.RoleTypeEnum;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.mapper.CommonCheckMapper;
import com.allen.testplatform.modules.databuilder.mapper.QcDeleteDataMapper;
import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.alibaba.fastjson.JSONObject;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;

/**
 * 工程检查、装饰检查、景观检查 操作逻辑类似，局部存在差异，通用方法统一封装
 *
 * @author Fan QingChuan
 * @since 2022/7/29 17:07
 */

public class CommonCheck extends CommonAndroid {

    private static final ReportLog reportLog = new ReportLog(CommonCheck.class);

    public JSONObject lastCheck;
    public JSONObject secondCheck;
    public JSONObject firstCheck;
    public List<JSONObject> checkItemList;

    public CheckUser checkUser;
    public CheckUser recitifyUser;
    public CheckUser reviewUser;

    public String categoryCode;
    public String categoryName;
    public String desContent;
    public String modifyContent;

    public UcUser currentUser;

    @Resource
    public CommonCheckMapper checkMapper;

    @Resource
    public QcDeleteDataMapper deleteDataMapper;

    protected void setLastCheck(String lastCheckName) {

        checkItemList = checkMapper.getCheckItems(categoryCode, cityCode);

        if (ObjectUtil.isNotEmpty(lastCheckName)) {
            reportLog.info(" ======== >> 指定末级检查项[{}]",lastCheckName);
            String finalLastCheckName = lastCheckName;
            lastCheck = checkItemList.stream().filter(item -> item.getString("name").equals(finalLastCheckName)).findAny().orElse(null);
            if (lastCheck == null) {
                lastCheck = checkItemList.get(RandomUtil.randomInt(checkItemList.size()));
                reportLog.info(" ======== >> 未匹配到末级检查项,随机取末级检查项[{}][{}]",lastCheck.getString("code"),lastCheck.getString("name"));
            }else {
                reportLog.info(" ======== >> 匹配到末级检查项[{}][{}]",lastCheck.getString("code"),lastCheck.getString("name"));
            }
        }else {
            lastCheck = checkItemList.stream().filter(item -> item.getIntValue("level") == 3).parallel().findAny().get();
            reportLog.info(" ======== >> 未指定末级检查项,随机取末级检查项[{}][{}]",lastCheck.getString("code"),lastCheck.getString("name"));
        }
    }

    protected void setSecondCheck() {
        secondCheck = checkItemList.stream().filter(item -> item.getLong("id").equals(lastCheck.getLong("parent_id"))).findAny().orElse(null);
        assert secondCheck != null;
    }

    protected void setFirstCheck() {
        firstCheck = checkItemList.stream().filter(item -> item.getLong("id").equals(secondCheck.getLong("parent_id"))).findAny().orElse(null);
        assert firstCheck != null;
    }

    protected void setUsers(String checkUserName,String recitifyUserName,String reviewUserName) {
        //指定本次测试 检查人、整改人、复验人
        List<CheckUser> checkUsers = checkMapper.getCheckUsers(stageCode, categoryCode, null);
        checkUser = getTargetCheckUser(checkUserName,0,checkUsers);
        recitifyUser = getTargetCheckUser(recitifyUserName,1,checkUsers);
        reviewUser = getTargetCheckUser(reviewUserName,0,checkUsers);
    }

    protected void assertTabTicketCount(String stageCode) {
        JSONObject ticketCount = checkMapper.assertTicketCount(categoryCode, stageCode);
        Dimension size = baseAndroidDriver.manage().window().getSize();

        List<WebElement> titleList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/titleTv")));
        titleList.forEach(element -> {
            if (!element.getText().equals("待复验")) {
                assertCount(ticketCount,element);
            }
        });

        WebElement ele = baseAndroidDriver.findElement(getBy(LocateType.ANDROID_UIAUTOMATOR, "new UiSelector().text(\"待复验\")"));
        Point point = ele.getLocation();
        swipeToPoint(point.getX(), point.getY(), 100, size.height / 5);

        titleList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/titleTv")));
        titleList.forEach(element -> {
            if (!element.getText().equals("重新整改")) {
                assertCount(ticketCount,element);
            }
        });

        ele = baseAndroidDriver.findElement(getBy(LocateType.ANDROID_UIAUTOMATOR, "new UiSelector().text(\"待复验\")"));
        point = ele.getLocation();
        swipeToPoint(point.getX(), point.getY(), size.width - 100, size.height / 5);
    }

    protected void assertCount(JSONObject ticketCount,WebElement element) {

        String title = element.getText();
        String count = null;
        switch (title) {
            case "全部": count = ticketCount.getString("all"); break;
            case "待整改": count = ticketCount.getString("toRecitify"); break;
            case "重新整改": count = ticketCount.getString("reRecitify"); break;
            case "待复验": count = ticketCount.getString("toReview"); break;
            case "非正常关闭": count = ticketCount.getString("abnormalClose"); break;
            case "正常关闭": count = ticketCount.getString("normalClose"); break;
            default: break;
        }
        String expectText = String.format("(%s)",count);

        try {
            WebElement actualElement = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='"+ title +"']/parent::android.view.ViewGroup/android.widget.TextView[2]")));
            reportLog.info(" ======== >> 定位到tab[{}],准备断言",title);
            assertElementText(actualElement,expectText);
        } catch (Exception e) {
            reportLog.info(" ======== >> 定位tab出现异常,暂不断言");
        }
    }

    protected CheckUser getTargetCheckUser(String userRealName, Integer roleType , List<CheckUser> checkUsers) {
        RoleTypeEnum roleTypeEnum = RoleTypeEnum.getRoleTypeEnum(roleType);
        CheckUser targetUser;
        if (ObjectUtil.isNotEmpty(userRealName)) {
            reportLog.info(" ======== >> 指定[{}][{}]",roleTypeEnum.getRoleName(),userRealName);
            targetUser = checkUsers.stream().filter(user -> user.getRoleType() == roleType && user.getRealName().contains(userRealName)).findAny().orElse(null);
            if (targetUser == null) {
                targetUser = checkUsers.stream().filter(user -> user.getRoleType() == roleType).parallel().findAny().get();
                reportLog.info(" ======== >> 未匹配到[{}],随机取用户[{}]",roleTypeEnum.getRoleName(),targetUser);
            }
        }else {
            targetUser = checkUsers.stream().filter(user -> user.getRoleType() == roleType).parallel().findAny().get();
            reportLog.info(" ======== >> 未指定[{}],随机取用户[{}]",roleTypeEnum.getRoleName(),targetUser);
        }

        return targetUser;
    }

    protected void loginAndTestAssertTicketCount() {
        currentUser = getUcUser(checkUser);
        login(currentUser.getSource().equals(Constant.PS_SOURCE) ? currentUser.getUserName():currentUser.getPhone(), EncryptUtils.decrypt(currentUser.getPassword()),true);

        chooseStage(cityName,stageName);
        enterModule(categoryName);
        waitLoading();
        if (categoryCode.equals(Constant.GCJC)) {
            //断言tab展示数量
            reportLog.info(" ======== >> 准备断言tab各状态工单数量是否符合预期");
            assertTabTicketCount(stageCode);
        }
        closeModule();
    }

    protected void waitLoading() {
        reportLog.info(" ======== >> 等待数据加载");
        boolean isDowmloading = true;
        do {
            isDowmloading = isElementExsit(baseAndroidDriver, 2, LocateType.ID, "cn.host.qc:id/proTvProject");
        }while (isDowmloading);
        threadSleep("1");
        reportLog.info(" ======== >> 数据加载完成");
    }

    protected void createProblem() {
        enterModule(categoryName);
        //等待页面加载完成
        waitLoading();
        //点击登记问题
        click(LocateType.ID,"cn.host.qc:id/recordTv");

        //上传图片
        click(LocateType.ID,"cn.host.qc:id/photoIv");
        if (categoryCode.equals(Constant.JGJC)) {
            click(LocateType.ID,"cn.host.qc:id/camaraTv");
        }
        clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");
        if (categoryCode.equals(Constant.JGJC)) {
            click(LocateType.ID,"cn.host.qc:id/photoIv");
            click(LocateType.ID,"cn.host.qc:id/camaraTv");
        }
        takePhotoAndMark(wait, RandomUtil.randomInt(1,4));

        //点击设置 检查区域
        click(LocateType.ID,"cn.host.qc:id/areaTv");
        clickText("A-14底商");
        clickText("02");
        Dimension size = baseAndroidDriver.manage().window().getSize();

        do {
            threadSleep("1");
            clickRandomPointInCustomArea(size.width/4,size.height/2 - 100,size.width *3/4,size.height/2 +100);
            threadSleep("1");
            //点击确定
            clickConfirm();
        }while (isToastHasAppeared(1,"请在户型图上标点！"));

        threadSleep("1");

        //点击设置 检查项
        click(LocateType.ID,"cn.host.qc:id/checkItemTv");
        clickTargetItem(firstCheck.getString("code"),firstCheck.getString("name"));
        clickTargetItem(secondCheck.getString("code"),secondCheck.getString("name"));
        clickTargetItem(lastCheck.getString("code"),lastCheck.getString("name"));
//        clickText(String.format("%s %s",firstCheck.getString("code"),firstCheck.getString("name")));
//        clickText(String.format("%s %s",secondCheck.getString("code"),secondCheck.getString("name")));
//        clickText(String.format("%s %s",lastCheck.getString("code"),lastCheck.getString("name")));

        //输入补充说明
        desContent = TestDataUtils.getTestContent(3, categoryCode, TicketProcessEnum.Create.getProcessDesc(), "补充说明-创建人:" + currentUser.getRealName());
        inputText(LocateType.ID,"cn.host.qc:id/extraDescEt",desContent);

        //下翻页面
        swipeUp(1D,1);

        //随机选择严重程度
        String important = String.format("cn.host.qc:id/levelRb%s",RandomUtil.randomInt(1,4));
        click(LocateType.ID,important);

        //输入整改说明
        modifyContent = TestDataUtils.getTestContent(3, categoryCode, TicketProcessEnum.Create.getProcessDesc(), "整改说明-创建人:" + currentUser.getRealName());
        inputText(LocateType.ID,"cn.host.qc:id/modifyDescEt",modifyContent);

        //点击选择整改人
        click(LocateType.ID,"cn.host.qc:id/modifyPersonTv");
        String user = ObjectUtil.isEmpty(recitifyUser.getPosition()) ? recitifyUser.getRealName() : recitifyUser.getRealName().concat("-").concat(recitifyUser.getPosition());
        clickText(user);
        clickConfirm();

        //点击选择复验人
        click(LocateType.ID,"cn.host.qc:id/checkPersonTv");
        user = ObjectUtil.isEmpty(reviewUser.getPosition()) ? reviewUser.getRealName() : reviewUser.getRealName().concat("-").concat(reviewUser.getPosition());
        clickText(user);
        clickConfirm();

        //点击提交
        clickConfirm();

        //断言toast
        assertToastHasAppeared(10,"问题提交成功");

        if (categoryCode.equals(Constant.GCJC)) {
            reportLog.info(" ======== >> 准备断言tab各状态工单数量是否符合预期");
            //断言tab展示数量
            assertTabTicketCount(stageCode);
        }
        closeModule();

    }

    protected void clickTargetItem(String itemCode,String itemName) {
        try {
            clickText(String.format("%s %s",itemCode,itemName));
        } catch (Exception e) {
            pageDownTicketList();
            clickTargetItem(itemCode,itemName);
        }
    }

    protected void searchTargetTicketAndSubmitRecitify(String checkName, String desContent,boolean isLastSearch) {

        if (isTargetToRecitifyTicket(checkName,desContent,0)) {
            submitRecitify();
        } else {
            if (!isLastSearch) {
                reportLog.info(" ======== >> 往下翻页");
                pageDownTicketList();

                if (isToastHasAppeared(TIME_OUT, "没有更多数据啦！")) {
                    searchTargetTicketAndSubmitRecitify(checkName,desContent,false);
                }else {
                    searchTargetTicketAndSubmitRecitify(checkName,desContent,true);
                }
            }
        }
    }

    protected void searchTargetTicketAndSubmitReview(String checkName, String desContent,boolean isLastSearch) {

        if (isTargetToReviewTicket(checkName,desContent,0)) {
            submitReview();
        } else {
            if (!isLastSearch) {
                reportLog.info(" ======== >> 往下翻页");
                pageDownTicketList();

                if (isToastHasAppeared(TIME_OUT, "没有更多数据啦！")) {
                    searchTargetTicketAndSubmitReview(checkName,desContent,false);
                }else {
                    searchTargetTicketAndSubmitReview(checkName,desContent,true);
                }
            }
        }
    }

    protected void pageDownTicketList() {
        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        TouchAction action = new TouchAction(baseAndroidDriver);

        action.press(PointOption.point(width / 2, height * 8 / 10)).waitAction(WaitOptions.waitOptions(Duration.ofSeconds(2)))
                .moveTo(PointOption.point(width / 2, height * 3 / 10)).release().perform();
    }

    protected boolean isTargetToRecitifyTicket(String checkName, String desContent, int startIndex) {

        boolean isTargetTicket = false;
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/descTv")));
        assert startIndex < elements.size();

        for (int i = startIndex; i < elements.size(); i++) {
            WebElement element = elements.get(i);
            String elementText = element.getText();

            reportLog.info(" ======== >> 检查第[{}]个工单,检查项[{}]",i+1,elementText);

            if (elementText.equals(checkName)) {
                element.click();
                reportLog.info(" ======== >> 点击进入");

                threadSleep("1");
                //先判断是否是目标工单 判断依据为 补充说明
                isTargetTicket = isElementTextToBe(baseAndroidDriver,2, LocateType.ID, "cn.host.qc:id/descTv", desContent);

                if (!isTargetTicket) {
                    pageBackModule();
                    if (i + 1 == elements.size()) {
                        reportLog.info(" ======== >> 遍历完毕");
                    }else {
                        isTargetTicket = isTargetToRecitifyTicket(checkName, desContent,i + 1);
                    }
                }
                break;
            }
        }

        return isTargetTicket;
    }

    protected boolean isTargetToReviewTicket(String checkName, String desContent, int startIndex) {

        boolean isTargetTicket = false;
        List<WebElement> elements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/descTv")));
        assert startIndex < elements.size();

        for (int i = startIndex; i < elements.size(); i++) {
            WebElement element = elements.get(i);
            String elementText = element.getText();

            reportLog.info(" ======== >> 检查第[{}]个工单,检查项[{}]",i+1,elementText);

            if (elementText.equals(checkName)) {
                element.click();
                reportLog.info(" ======== >> 点击进入");

                threadSleep("1");
                //先判断是否是目标工单 判断依据为 补充说明
                isTargetTicket = isElementTextToBe(baseAndroidDriver,LocateType.XPATH,"//*[@text='"+ checkUser.getRealName() +"']/parent::android.view.ViewGroup/android.widget.LinearLayout/android.widget.TextView[2]",desContent);

                if (!isTargetTicket) {
                    pageBackModule();
                    if (i + 1 == elements.size()) {
                        reportLog.info(" ======== >> 遍历完毕");
                    }else {
                        isTargetTicket = isTargetToRecitifyTicket(checkName, desContent,i + 1);
                    }
                }
                break;
            }
        }

        return isTargetTicket;
    }

    protected void submitRecitify() {
        //断言按钮是否存在 二次派单、完成整改
        reportLog.info(" ======== >> 准备断言按钮是否存在 二次派单、完成整改");
        assertElementExsit(baseAndroidDriver, TIME_OUT, LocateType.ID, "cn.host.qc:id/rebuildTv");
        assertElementExsit(baseAndroidDriver, TIME_OUT, LocateType.ID, "cn.host.qc:id/finishTv");

        //点击[完成整改]
        click(LocateType.ID, "cn.host.qc:id/finishTv");
        click(LocateType.ID, "cn.host.qc:id/confirmTv");
        assertToastHasAppeared(2, "请上传图片");

        //完成拍照
        click(LocateType.ID, "cn.host.qc:id/photoIv");
        takePhotoAndMark(wait, RandomUtil.randomInt(1, 4));
        //输入补充说明
        String content = TestDataUtils.getTestContent(3, categoryCode, TicketProcessEnum.CompleteRectify.getProcessDesc(), "补充说明-整改人:" + currentUser.getRealName());
        inputText(LocateType.ID, "cn.host.qc:id/descEt", content);
        //点击确认
        click(LocateType.ID, "cn.host.qc:id/confirmTv");

        assertToastHasAppeared(TIME_OUT, "处理成功");
        closeModule();
    }

    private void submitReview() {
        //断言按钮是否存在 非正常关闭、重新整改、正常关闭
        reportLog.info(" ======== >> 准备断言按钮是否存在 非正常关闭、重新整改、正常关闭");
        assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/abnormalTv");
        assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/rebuildTv");
        assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/finishTv");

        //点击[正常关闭]
        click(LocateType.ID,"cn.host.qc:id/finishTv");

        if (categoryCode.equals(Constant.GCJC)) {
            //点击确认
            clickConfirm();
            assertToastHasAppeared(2,"请上传图片");
        }

        //完成拍照
        click(LocateType.ID,"cn.host.qc:id/photoIv");
        takePhotoAndMark(wait, RandomUtil.randomInt(1,4));
        //输入补充说明
        String content = TestDataUtils.getTestContent(3, categoryCode, TicketProcessEnum.NormalClose.getProcessDesc(), "补充说明-复验人:" + currentUser.getRealName());
        inputText(LocateType.ID,"cn.host.qc:id/descEt",content);
        //点击确认
        clickConfirm();

        assertToastHasAppeared(TIME_OUT,"处理成功");
        closeModule();
    }

}
