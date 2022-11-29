package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.modules.databuilder.mapper.PileMapper;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.impl.PileServiceImpl;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.alibaba.fastjson.JSONObject;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidTouchAction;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fan QingChuan
 * @since 2022/8/4 9:53
 */

public class CommonZjys extends CommonAndroid {

    public static final ReportLog reportLog = new ReportLog(CommonZjys.class);

    @Resource
    protected PileMapper pileMapper;

    @Resource
    protected PileServiceImpl pileService;

    /**
     * 可自定义修改的测试属性 也可不指定则随机
     */
    public String checkTypeNameLv0 = "桩基工程";
    public String checkPartName = "A-2精装高层";
    public String checkTypeNameLv1 = "CFG桩";
    public String reportName = "C-员工D";

    /**
     * 无需定义及修改的属性
     */
    public Long checkTypeId;
    public List<JSONObject> checkPointList;
    public String pileSn;
    public UcUser user;
    public PointOption point;
    public String checkPartCode;
    public Long sectionId;

    public void enterSection(String sectionName) {
        clickText(sectionName);
        if (isToastHasAppeared(2,"请先下载离线数据")) {
            WebElement element = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='"+ sectionName +"']/parent::android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[1]")));
            String text = element.getText();
            element.click();
            reportLog.info(" ======== >> 点击按钮[{}]",text);

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
                    Boolean isExsit = isElementExsit(baseAndroidDriver,2, LocateType.ID,"cn.host.qc:id/progressIv");
                    if (!isExsit) {
                        reportLog.info(" ======== >> 进度条不存在 退出捕获 执行下一步测试!");
                        break;
                    }
                }

                time ++ ;
            }while (!until || time <= 3);

            clickText(sectionName);
            reportLog.info(" ======== >> 点击进入标段[{}] ",sectionName);
        }
    }

    public void chooseCheckPart(String checkPartName) {

        click(LocateType.ID,"cn.host.qc:id/tv_toolbar_title");
        clickText(checkPartName);
        reportLog.info(" ======== >> 点击选择桩基验收区域[{}] ",checkPartName);
    }

    public void jumpIntoNewCheckPage() {
        Dimension size = baseAndroidDriver.manage().window().getSize();
        //放大图片
//        enlargePoint(size.width / 2,size.height / 2);
        clickTab("暂存");
        boolean isCorrectPage = false;
        do {
            point = clickRandomPointInCustomArea(size.width * 0.1, size.height * 0.4, size.width * 0.9, size.height * 0.7);
            threadSleep("1");

            if (isElementExsit(baseAndroidDriver,2,LocateType.ID,"cn.host.qc:id/titleTv")) {
                WebElement startCheckButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/titleTv")));
                String text = startCheckButton.getText();
                startCheckButton.click();
                isCorrectPage = true;
                reportLog.info(" ======== >> 点击[{}]",text);
            }else {
                WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cn.host.qc:id/titleTv")));
                if (title.getText().equals(checkPartName)) {
                    reportLog.info(" ======== >> 该点位未在图片上,需重新点击");
                    continue;
                }

                if (title.getText().equals("桩基验收")) {
                    reportLog.info(" ======== >> 该点位已存在验收,需返回上一页重新点击");
                    pageBackModule();
                    continue;
                }
            }

        }while (!isCorrectPage);
    }

    public void swipePageDownByRadio(Double second, int num) {
        int nanos = (int) (second * 1000);
        Duration duration = Duration.ofNanos(nanos);
        int width = baseAndroidDriver.manage().window().getSize().width;
        int height = baseAndroidDriver.manage().window().getSize().height;
        TouchAction action = new TouchAction(baseAndroidDriver);

        for (int i = 0; i <= num; i++) {
            action.press(PointOption.point(width / 2, height * 3 / 4)).waitAction(WaitOptions.waitOptions(duration))
                    .moveTo(PointOption.point(width / 2, height / 4)).release().perform();
        }
    }

    public void takePhotoInPointList(List<JSONObject> checkPointList) {
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
                    swipePageDownByRadio(2D,1);
                    reportLog.info(" ======== >> 未找到检查点,页面向下翻页再次查找 检查点标题[{}] ",checkPoint.getString("title"));
                }
            }while (!isDone);

        });
    }

    public void clickPoint(PointOption point) {
        AndroidTouchAction action = new AndroidTouchAction(baseAndroidDriver);
        action.tap(point).release().perform();
    }

    public void saveCheck() {
        click(LocateType.ID,"cn.host.qc:id/submitTv");
        reportLog.info(" ======== >> 点击保存");
    }

    public void finishCheck() {
        click(LocateType.ID,"cn.host.qc:id/finishTv");
        reportLog.info(" ======== >> 点击完成");
    }

    public void clickTab(String tabName) {
        assert tabName != null;
        switch(tabName) {
            case "全部":
                reportLog.info(" ======== >> 点击tab[全部]");
                click(LocateType.ID,"cn.host.qc:id/allFilterTv");
                break;
            case "暂存":
                reportLog.info(" ======== >> 点击tab[暂存]");
                click(LocateType.ID,"cn.host.qc:id/draftFilterTv");
                break;
            case "待完成":
                reportLog.info(" ======== >> 点击tab[待完成]");
                click(LocateType.ID,"cn.host.qc:id/pendingFilterTv");
                break;
            case "已验收":
                reportLog.info(" ======== >> 点击tab[已验收]");
                click(LocateType.ID,"cn.host.qc:id/finishFilterTv");
                break;
            default:
                break;
        }

    }

    public void assertPileDetail(CommonZjys.AssertPileDetail actualDetail,int commitType) {

        reportLog.info(" ======== >> PileDetail数据 [{}]",actualDetail);

        reportLog.info(" ======== >> 断言1:detailId 不为空");
        Assertion.verifyNotNulls(actualDetail.getDetailId(),"测试不通过!");

        reportLog.info(" ======== >> 断言2:commitType 状态是否正确");
        Assertion.verifyEquals(actualDetail.getCommitType(),commitType);

        reportLog.info(" ======== >> 断言3:pileSn 是否符合预期");
        Assertion.verifyEquals(actualDetail.getPileSn(),pileSn);

        reportLog.info(" ======== >> 断言4:orgCode 是否符合预期");
        Assertion.verifyEquals(actualDetail.getOrgCode(),cityCode);

        reportLog.info(" ======== >> 断言5:orgName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getOrgName(),cityName);

        reportLog.info(" ======== >> 断言6:projectCode 是否符合预期");
        Assertion.verifyEquals(actualDetail.getProjectCode(),projectCode);

        reportLog.info(" ======== >> 断言7:projectName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getProjectName(),projectName);

        reportLog.info(" ======== >> 断言8:stageCode 是否符合预期");
        Assertion.verifyEquals(actualDetail.getStageCode(),stageCode);

        reportLog.info(" ======== >> 断言9:stageName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getStageName(),stageName.split("-")[1]);

        reportLog.info(" ======== >> 断言10:sectionName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getSectionName(),sectionName);

        reportLog.info(" ======== >> 断言11:sectionId 是否符合预期");
        Assertion.verifyEquals(actualDetail.getSectionId(),sectionId);

        reportLog.info(" ======== >> 断言12:TypeName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getTypeName(),checkTypeNameLv1);

        reportLog.info(" ======== >> 断言13:TypePath 是否符合预期");
        Assertion.verifyEquals(actualDetail.getTypePath(),checkTypeNameLv0+"-"+checkTypeNameLv1);

        reportLog.info(" ======== >> 断言14:TypeId 是否符合预期");
        Assertion.verifyEquals(actualDetail.getTypeId(),checkTypeId);

        reportLog.info(" ======== >> 断言15:PileAreaName 是否符合预期");
        Assertion.verifyEquals(actualDetail.getPileAreaName(),checkPartName);

        reportLog.info(" ======== >> 断言16:PileAreaCode 是否符合预期");
        Assertion.verifyEquals(actualDetail.getPileAreaCode(),checkPartCode);


        reportLog.info(" ======== >> 断言17:points.size() 是否符合预期");
        List<AssertPileDetailPoint> points = pileMapper.assertPileDetailPoint(actualDetail.getDetailId());
        Assertion.verifyEquals(points.size(),checkPointList.size());

        List<Long> checkPoints = checkPointList.stream().map(o -> o.getLong("pointId")).collect(Collectors.toList());

        reportLog.info(" ======== >> 断言18:points每个PointId 是否符合预期");
        points.forEach(point -> {
            Assertion.verifyTrue(checkPoints.contains(point.getPointId()),"测试不通过! pointId不匹配");
        });

        reportLog.info(" ======== >> 断言19、20:points每个title以及每个remark 是否符合预期");
        points.forEach(detailPoint -> {
            checkPointList.forEach(checkPoint -> {
                if (checkPoint.getLong("pointId").equals(detailPoint.getPointId())) {
                    Assertion.verifyEquals(checkPoint.getString("title"),detailPoint.getTitle());
                    Assertion.verifyEquals(checkPoint.getString("remark"),detailPoint.getRemark());
                }
            } );
        });
    }

    /**
     * 操作完成后,断言 移动端页面展示数量是否正确! 全部(XX)- 待完成(XX)- 已验收(XX)-
     */
    public void checkCount() {
        try {
            JSONObject countResult = pileMapper.countPileDetail(checkPartCode, this.sectionId);
            enterModule("桩基验收");
            enterSection(sectionName);
            chooseCheckPart(checkPartName);

            String expectAll = "全部(" +countResult.getString("all") + ")";
            String expectTodo = "待完成(" +countResult.getString("todo") + ")";
            String expectDone = "已验收(" +countResult.getString("done") + ")";

            assertElementText(LocateType.ID,"cn.host.qc:id/allFilterTv",expectAll);
            assertElementText(LocateType.ID,"cn.host.qc:id/pendingFilterTv",expectTodo);
            assertElementText(LocateType.ID,"cn.host.qc:id/finishFilterTv",expectDone);
        } finally {
            closeModule();
        }
    }

    @Data
    public static class AssertPileDetail{

        private Long detailId;
        private String orgCode;
        private String orgName;
        private String projectCode;
        private String projectName;
        private String stageCode;
        private String stageName;

        private String sectionName;
        private Long sectionId;
        private String pileAreaName;
        private String pileAreaCode;

        private Long typeId;
        private String typePath;
        private String typeName;
        private String pileSn;

        private Integer commitType;
    }

    @Data
    public static class AssertPileDetailPoint{
        private Long pointId;
        private String title;
        private String remark;
        private List<String> picture;
    }
}
