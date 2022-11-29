package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.constant.HostCommon;
import com.allen.testplatform.common.utils.*;
import com.allen.testplatform.modules.databuilder.enums.TicketProcessEnum;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.process.entity.ProcessDetail;
import com.allen.testplatform.testscripts.api.ApiProcess;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Fan QingChuan
 * @since 2022/8/4 9:59
 */

public class ConmonGxys extends CommonAndroid {

    private static final ReportLog reportLog = new ReportLog(ConmonGxys.class);

    // 暂写死(因为每次测试都删除测试数据 可反复使用)  可查询数据 取待验收的目标进行操作

    public String fatherOfLastCheckName = "二级-工程-分户";
    public String lastCheckName = "三级-分户3-单验收&单抽检";
    public String sectionName = super.sectionName;
    public String banName = "A-1精装高层";
    public String unitNumber;
    public String floorNumber;
    public String roomNumber = "101";
    public boolean hasDownload = false;


    public UcUser currentUser;
    public List<String> accepterList;
    public List<String> spotcheckList;
    public String content;
    public Long checkId;
    public JSONObject lastCheck;
    public List<JSONObject> checkPointList;
    public List<JSONObject> allCheckList;

    public void onceAcceptanceOrSpotCheck(int type) {
        loginAndChooseProcessCheckPart();
        takePhotosV2(checkPointList);
        swipeUp(1D,1);

        String content = getContent(type);
        inputText(LocateType.ID,"cn.host.qc:id/extraDescEt", content);
        reportLog.info(" ======== >> 输入备注[{}]", content);

        //选择下一步验收人
        chooseNextOperatorUser();

        //提交
        switch (type) {
            case 1:
                click(LocateType.ID,"cn.host.qc:id/submitTv");
                reportLog.info(" ======== >> 点击[提交]");
                break;
            case 2:
            case 3:
                click(LocateType.ID,"cn.host.qc:id/passTv");
                click(LocateType.ID,"cn.host.qc:id/confirm_tv");
                reportLog.info(" ======== >> 点击[通过]");
                break;
            default:
                throw new IllegalArgumentException("操作类型不合法!");
        }

//

        boolean subStatus = isToastHasAppeared(10, "提交成功");
        Assertion.verifyEquals(subStatus,true,"测试不通过!");

        //点击退出 到工作台
        closeModule();
        logout();
    }

    public void login(String username, String password) {
        boolean hasAgree = this.content == null;
        login(username,password,hasAgree);
    }

    /**
     * 登录 选择报验工序检查项 选择报验区域
     */
    public void loginAndChooseProcessCheckPart() {
        reportLog.info(" ======== >> 当前操作人[{}][{}]",currentUser.getRealName(),currentUser.getSource());

        login(currentUser.getSource().equals(Constant.SUPPLIER_SOURCE) ? currentUser.getPhone():currentUser.getUserName(), EncryptUtils.decrypt(currentUser.getPassword()));
        chooseStage(cityName,stageName);
        enterModule("工序验收");

        //TODO 拆分方法 -> 进入时 判断是否需要下载数据(等待进度条) 需要再等待 “下载完成”
        if (hasDownload) {
            Boolean until = false;
            int time = 1;
            do {

                try {
                    until = new WebDriverWait(baseAndroidDriver, Duration.ofSeconds(20), Duration.ofMillis(100))
                            .until(ExpectedConditions.textToBePresentInElementLocated(By.id("cn.host.qc:id/tipsTv"),"下载完成"));
                } catch (Exception e) {
                    reportLog.info(" ======== >> 本次等待中出现异常!,已等待[{}]次,每次最长等待20秒",time);
                }

                if (until) {
                    reportLog.info(" ======== >> 本次等待中完成下载,已等待[{}]次,每次最长等待20秒",time);
                    break;
                }else {
                    reportLog.info(" ======== >> 本次等待过程,未完成下载 已等待[{}]次,每次最长等待20秒",time);
                    reportLog.info(" ======== >> 检查进度条是否存在!(是否已错过捕获进度展示)");
                    Boolean isExsit = isElementExsit(baseAndroidDriver,2,LocateType.ID,"cn.host.qc:id/progressIv");
                    if (!isExsit) {
                        reportLog.info(" ======== >> 进度条不存在 退出捕获 执行下一步测试!");
                        break;
                    }
                }

                time ++ ;

            }while (!until || time <= 4);

            reportLog.info(" ======== >> 下载进度完成,执行后续步骤");
        }else {
            reportLog.info(" ======== >> 不存在待下载数据,直接执行后续步骤");
        }


        chooseCheckItems(lastCheck,allCheckList);

        chooseCheckPartName(sectionName,banName,null,null,roomNumber);
    }

    /**
     * 拍照V2 -根据检查项的检查点配置自动完成所有拍照(自动遍历所有检查点 下翻搜索目标进行拍照) V1版本不支持多个检查点时的配置
     * @param checkPointList
     */
    public void takePhotosV2(List<JSONObject> checkPointList) {
        //每个检查点上传拍照
        checkPointList.forEach(checkPoint -> {
            boolean isDone = false;

            do {
                List<WebElement> pointTitles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/nameTv")));
                WebElement titleEle = pointTitles.stream().filter(title -> title.getText().contains(checkPoint.getString("title"))).findAny().orElse(null);

                List<WebElement> pointDescs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/descTv")));
                WebElement remarkEle = pointDescs.stream().filter(remark -> remark.getText().contains(checkPoint.getString("remark"))).findAny().orElse(null);

                if (titleEle != null && remarkEle != null) {
                    String titleEleText = titleEle.getText();
                    String remarkEleText = remarkEle.getText();
                    WebElement photo = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='"+ remarkEleText +"']/parent::android.widget.LinearLayout/androidx.recyclerview.widget.RecyclerView/android.widget.RelativeLayout/android.widget.ImageView")));
                    photo.click();

                    do {
                        threadSleep("2");
                        pressKey("CAMERA");
                    } while (!wait.until(ExpectedConditions.presenceOfElementLocated(By.id("com.huawei.camera:id/done_button"))).isDisplayed());
                    click(LocateType.ID,"com.huawei.camera:id/done_button");

                    threadSleep("1");

                    //照片上标记涂鸦
                    drawMarkInScreen(RandomUtil.randomInt(1,4));
                    click(LocateType.ID,"cn.host.qc:id/dispatch_tv");
                    reportLog.info(" ======== >> 拍照成功! 检查点标题[{}] ",titleEleText);

                    isDone = true;
                }else {
                    swipePageDownByRatio(2D,1);
                    reportLog.info(" ======== >> 未找到检查点,页面向下翻页再次查找 检查点标题[{}] ",checkPoint.getString("title"));
                }
            }while (!isDone);

        });
    }

    public void swipePageDownByRatio(Double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        TouchAction action = new TouchAction(baseAndroidDriver);

        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width / 2, height * 3 / 4)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width / 2, height * 2 / 4)).release().perform();
        }
    }

    /**
     * 拍照V1
     * @param hasPermisson 是否需要点击授权按钮
     */
    public void takePhotosV1(boolean hasPermisson) {
        List<WebElement> uploadImgs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/photoIv")));
        if (hasPermisson) {
            uploadImgs.get(0).click();
            clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");
        }

        int startSize = uploadImgs.size();

        int[] indexArray = new int[startSize];
        for (int i = 0; i < indexArray.length; i++) {
            indexArray[i] = i;
        }

        for (int i = 0; i < indexArray.length; i++) {
            uploadImgs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/photoIv")));
            uploadImgs.get(indexArray[i]).click();

            takePhotoAndMark(wait, RandomUtil.randomInt(1,4));
            reportLog.info(" ======== >> 检查点拍照 共计[{}]个检查点,完成第[{}]个检查点拍照",indexArray.length,i+1);

            for (int j = i; j < indexArray.length; j++) {
                indexArray[j]++;
            }

            if (i >= 1) {
                uploadImgs = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/photoIv")));
                uploadImgs.get(indexArray[i]).click();
                takePhotoAndMark(wait, 3);
                reportLog.info(" ======== >> 检查点拍照 共计[{}]个检查点,完成第[{}]个检查点第[{}]张照片",indexArray.length,i+1,2);
                for (int j = i; j < indexArray.length; j++) {
                    indexArray[j]++;
                }
            }
        }
    }

    /**
     * 页面操作随机 选择下一验收人或抽检人
     */
    public void chooseNextOperatorUser() {
        List<WebElement> roleTypeList = new ArrayList<>();
        try {
            roleTypeList = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/userLabelTv")));
        } catch (Exception e) {

        }finally {
            if (CollectionUtils.isNotEmpty(roleTypeList)) {
                for (int i = 0; i < roleTypeList.size(); i++) {
                    roleTypeList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/userLabelTv")));
                    WebElement element = roleTypeList.get(i);
                    String accepterType = element.getText();

                    String accepterTypeTemp = null;
                    if (accepterType.contains("验收人") && !accepterType.contains("共同")) {
                        accepterTypeTemp = StringUtils.substringBefore(accepterType,"验收人");
                    }else if (accepterType.contains("抽检人")) {
                        accepterTypeTemp = StringUtils.substringBefore(accepterType,"抽检人");
                    }
                    if (accepterTypeTemp.contains("监理")) accepterTypeTemp += "单位人员";
                    else accepterTypeTemp += "人员";

                    List<WebElement> nextButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/userNameTv")));
                    nextButtons.get(i).click();

                    //选择具体人员
                    clickText(accepterTypeTemp);
                    List<WebElement> userElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("cn.host.qc:id/nameTv")))
                            .stream().filter(e -> !e.getText().contains("最近选择") && !e.getText().contains("人员")).collect(Collectors.toList());
                    WebElement user = userElements.get(RandomUtil.randomInt(0, userElements.size()));
                    String userRealName = null;

                    if (accepterType.contains("验收人")) {
                        if (accepterType.contains("监理")) {
                            userRealName = StringUtils.substringBefore(user.getText(),"-新希望供应商B公司-自动化测试");
                        }else {
                            userRealName = user.getText();
                        }
                        accepterList.add(userRealName);
                    }

                    if (accepterType.contains("抽检人")) {
                        if (accepterType.contains("监理")) {
                            userRealName = StringUtils.substringBefore(user.getText(),"-新希望供应商B公司-自动化测试");
                        }else {
                            userRealName = user.getText();
                        }
                        spotcheckList.add(userRealName);
                    }

                    clickText(user.getText());
                    reportLog.info(" ======== >> 选择[{}] [{}]",accepterType,userRealName);
                    click(LocateType.ID,"cn.host.qc:id/commitTv");
                }
            }else {
                reportLog.info(" ======== >> 当前步骤是最终步骤!不存在下一步人选");
            }
        }
    }

    /**
     * 根据操作类型 拼接content
     * @param type
     * @return
     */
    public String getContent(int type) {
        String content;
        switch (type) {
            case 1:
                content = TestDataUtils.getTestContent(3,Constant.GXYS, TicketProcessEnum.CreateProcess.getProcessDesc());
                break;
            case 2:
                content = TestDataUtils.getTestContent(3,Constant.GXYS, TicketProcessEnum.AcceptProcess.getProcessDesc());
                break;
            case 3:
                content = TestDataUtils.getTestContent(3,Constant.GXYS, TicketProcessEnum.SpotCheckProcess.getProcessDesc());
                break;
            default:
                throw new IllegalArgumentException("操作类型不合法!");
        }
        return content;

    }

    /**
     * 级联点击各级检查项 （检查项层级不限制）
     * @param lastCheck
     * @param allCheckList
     */
    public void chooseCheckItems(JSONObject lastCheck,List<JSONObject> allCheckList) {

        click(LocateType.ID,"cn.host.qc:id/checkItemFilterLt");

        List<String> parentNames = queryParentNames(lastCheck.getLong("checkId"), allCheckList);
        //因末级项特殊-拼接报验方式
        String appLastCheckName = getAppLastCheckName(lastCheck);
        parentNames.set(0,appLastCheckName);

        //顺序翻转下 目标顺序为 一级检查项 二级检查项...
        ListUtil.reverse(parentNames);
        parentNames.forEach(checkName -> {
            try {
                clickText(checkName,TIME_OUT);
            } catch (Exception e) {
                pageDownCheckItemList();
                clickText(checkName,TIME_OUT);
            }
        });

        clickConfirm();

        reportLog.info(" ======== >> 级联点击检查项 [{}]",parentNames);
    }

    /**
     * 根据父级、末级检查项名称 匹配目标检查项 不填则随机取
     * @param lastCheckName
     * @param fatherNameOfLastCheck
     * @param allCheckList
     * @return
     */
    public JSONObject getTargetLastCheck(String lastCheckName,String fatherNameOfLastCheck,List<JSONObject> allCheckList) {
        List<JSONObject> lastCheckList;
        JSONObject lastCheck;

        if (ObjectUtil.isEmpty(lastCheckName)) {
            lastCheckList = allCheckList.stream().filter(item -> item.getBoolean("ifLast")).collect(Collectors.toList());
        }else {
            lastCheckList = allCheckList.stream().filter(item -> item.getString("checkName").equals(lastCheckName) && item.getBoolean("ifLast")).collect(Collectors.toList());
        }

        if (CollectionUtils.isEmpty(lastCheckList)) {
            throw new BusinessException("未匹配到末级检查项 请检查配置!");
        }

        if (ObjectUtil.isNotEmpty(fatherNameOfLastCheck)) {
            List<Long> fatherIdList = allCheckList.stream().filter(item -> item.getString("checkName").equals(fatherNameOfLastCheck)).map(item -> item.getLong("checkId")).collect(Collectors.toList());

            Assert.assertTrue(fatherIdList.size() > 0 ,"未匹配到父级检查项 请检查配置!");
            lastCheck = getTargetLastCheck(lastCheckList,fatherIdList);
        }else {
            lastCheck = lastCheckList.get(RandomUtil.randomInt(lastCheckList.size()));
        }

        reportLog.info(" ======== >> 根据末级检查项[{}]父级检查项[{}]匹配,获取到目标末级检查项 [{}]",lastCheckName,fatherNameOfLastCheck,lastCheck);
        return lastCheck;
    }

    private JSONObject getTargetLastCheck(List<JSONObject> lastCheckList,List<Long> fatherIdList) {
        AtomicReference<JSONObject> lastCheck = new AtomicReference<>();
        lastCheckList.forEach(check -> {
            if (fatherIdList.contains(check.getLong("parentId"))) {
                lastCheck.set(check);
            }
        });
        return lastCheck.get();
    }

    public String getAppLastCheckName(JSONObject lastCheck) {
        String appLastCheckName = null;
        String checkName = lastCheck.getString("checkName");
        switch (lastCheck.getIntValue("checkPartType")) {
            case 1:
                appLastCheckName = checkName.concat(" (分户)");
                break;
            case 2:
                appLastCheckName = checkName.concat(" (分单元-整层)");
                break;
            case 3:
                appLastCheckName = checkName.concat(" (不分单元-整层)");
                break;
            case 4:
                appLastCheckName = checkName.concat(" (整栋)");
                break;
            case 5:
                appLastCheckName = checkName.concat(" (自定义检验批)");
                break;
            default:
                break;
        }

        return appLastCheckName;
    }

    /**
     * 根据配置 点击选择具体查验区域 若页面不存在 则下翻页面查找 (检查区域楼栋房源页面 )
     * @param sectionName
     * @param banName
     * @param unitNumber
     * @param floorNumber
     * @param roomNumber
     */
    public void chooseCheckPartName(String sectionName,String banName,String unitNumber,String floorNumber,String roomNumber) {
        click(LocateType.ID,"cn.host.qc:id/sectionFilterTv");
        clickText(sectionName);
        clickText(banName);
        clickConfirm();

        //目前只实现了根据传入值查找点击目标报验区域，未进行报验方式与传入房源信息正确性校验  如果传入值存在业务上不能匹配 则会出现页面反复查找 并查找不到目标房源
        // TODO 根据报验方式不同  房源选择颗粒度不同 根据检查项配置，读取对应传入参数 例如: 按整栋方式报验 则 不读取传入的 单元楼层房号等信息
        if (ObjectUtil.isNotEmpty(unitNumber)) {
            unitNumber = unitNumber.concat("单元");
            clickText(unitNumber);
        }

        if (ObjectUtil.isNotEmpty(floorNumber)) {
            clickTargetFloor(floorNumber);
        }

        if (ObjectUtil.isNotEmpty(roomNumber)) {
            clickTargetRoom(roomNumber);
        }

        reportLog.info(" ======== >> 选择标段[{}],报验区域 [{}]-[{}]单元-[{}]层-[{}]",sectionName,banName,unitNumber,floorNumber,roomNumber);
    }

    public void clickTargetFloor(String floorNumber) {
        try {
            clickText(String.format("%s层",floorNumber));
        } catch (Exception e) {
            pageDownCheckPartList();
            clickTargetFloor(floorNumber);
        }
    }

    public void clickTargetRoom(String roomNumber) {
        try {
            clickText(roomNumber);
        } catch (Exception e) {
            pageDownCheckPartList();
            clickText(roomNumber);
        }
    }

    public void pageDownCheckPartList() {
        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        TouchAction action = new TouchAction(baseAndroidDriver);

        action.press(PointOption.point(width / 2, height * 8 / 10)).waitAction(WaitOptions.waitOptions(Duration.ofSeconds(2)))
                .moveTo(PointOption.point(width / 2, height * 3 / 10)).release().perform();
    }

    public void pageDownCheckItemList() {
        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        swipeToPoint(width/2,height *8/10,width/2,height * 7/10);
    }

    /**
     * 物理删除测试数据
     */
    public int[] deleteTestData(String content) {
        List<String> snList = deleteDataMapper.getProcessDetailSnList(Arrays.asList(content));
        int count1 = deleteDataMapper.removeBacklogBySn(snList);
        int count2 = deleteDataMapper.removeProcessDetailAndProblemByContent(Arrays.asList(content));
        reportLog.info(" ======== >> 删除测试数据 待办[{}]条,业务数据[{}]条",count1,count2);
        return new int[]{count1,count2};
    }

    /**
     * 业务接口-逻辑删除测试数据
     * @param content
     */
    public void deleteApiTestData(String content) {
        ProcessDetail detail = processV2Mapper.assertDetail(content);
        Map<String,String> header_admin = TokenUtils.getHeader(TokenUtils.getJxCheckAuthToken("ATE001","a123456","UAT"));
        String params = "id=".concat(detail.getId().toString());
        String rs = HttpUtils.doGet(HostCommon.UAT.concat(ApiProcess.PC_DETAIL_DELETE), header_admin, params);
        Assert.assertNotNull(JsonPath.read(rs,"$.message"),"接口未返回值");
        reportLog.info(" ======== >> 调用工序验收删除接口 删除工序验收测试数据[{}]",detail);
    }

}
