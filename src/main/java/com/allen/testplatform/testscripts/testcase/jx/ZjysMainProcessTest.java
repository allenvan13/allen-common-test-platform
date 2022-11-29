package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.listener.AssertListener;
import com.allen.testplatform.testscripts.listener.ExtentTestNGIReporterListener;
import com.alibaba.fastjson.JSONObject;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 桩基验收-主流程测试用例
 *
 * @author Fan QingChuan
 * @since 2022/7/25 17:07
 */

@Listeners(value = {ExtentTestNGIReporterListener.class, AssertListener.class})
public class ZjysMainProcessTest extends CommonZjys {

    private static final ReportLog reportLog = new ReportLog(ZjysMainProcessTest.class);

    @Test(priority = 0,description = " ======== >> 初始化数据:测试标段、验收区域、验收检查项、验收点")
    void initData() {
        List<JSONObject> pileAreas = pileMapper.getPileSection(sectionName);
        assert pileAreas.size() > 0;
        reportLog.info(" ======== >> 标段下所有桩基验收区域 [{}]",pileAreas);
        JSONObject checkPart = pileAreas.stream().filter(area -> area.getString("banName").equals(checkPartName)).findAny().orElse(null);
        if (ObjectUtil.isEmpty(checkPart)) {
            checkPart = pileAreas.get(RandomUtil.randomInt(pileAreas.size()));
        }
        checkPartCode = checkPart.getString("banCode");
        checkPartName = checkPart.getString("banName");
        sectionId = checkPart.getLong("sectionId");
        reportLog.info(" ======== >> 本次测试 桩基验收区域为 [{}]",checkPartName);

        List<JSONObject> pileCheckType = pileMapper.getPileCheckType();
        JSONObject checkType = pileCheckType.stream().filter(json -> json.getString("typeName").equals(checkTypeNameLv1)).findAny().orElse(null);
        if (ObjectUtil.isEmpty(checkType)) {
            checkType = pileCheckType.get(RandomUtil.randomInt(pileCheckType.size()));
        }
        checkTypeNameLv1 = checkType.getString("typeName");
        checkTypeId = checkType.getLong("typeId");
        reportLog.info(" ======== >> 本次测试 桩基验收检查项为 [{}] ID[{}]",checkTypeNameLv1,checkTypeId);

        checkPointList = new ArrayList<>();
        checkPointList = pileMapper.getPoint(checkTypeId);
        reportLog.info(" ======== >> 本次测试 桩基验收检查项对应检查点 [{}]",checkPointList);

        user = pileService.getSectionReportUsers(sectionName, reportName);
        reportLog.info(" ======== >> 本次测试账号 [{}],[{}]",user.getRealName(),user.getUserName());
    }

    @Test(priority = 1,description = " ======== >> 发起报验,首次保存 断言1-toast消息\"提交成功\",断言2-点击坐标,进入详情页面,桩号符合预期,断言3-详情页面,存在暂存、保存、完成按钮,断言4-数据库验收明细数据(含状态)符合预期")
    void saveCheckFirst() {

        login(user.getSource().equals(Constant.PS_SOURCE) ? user.getUserName():user.getPhone(), EncryptUtils.decrypt(user.getPassword()),true);
        chooseStage(cityName,stageName);
        enterModule("桩基验收");

        try {
            //进入标段
            enterSection(sectionName);
            //选择目标验收区域
            chooseCheckPart(checkPartName);
            //点击图片点位 进行详情页面开始查验
            jumpIntoNewCheckPage();

            //选择桩基检查类型
            click(LocateType.ID,"cn.host.qc:id/typeNameTv");

            //断言检查项选项是否存在(”桩基工程“)
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/categoryTv");
            clickText(checkTypeNameLv0);
            //断言一级检查项是否为7个(固定配置)
            assertElementsCount(baseAndroidDriver,7,TIME_OUT,LocateType.ID,"cn.host.qc:id/categoryTv");
            clickText(checkTypeNameLv1);
            //点击确定
            clickConfirm();

            //输入桩基编号
            pileSn = String.format("%s%s桩号", Constant.ANDROID_AUTO_TEST,DateUtils.getTimeStampSuffix() );
            inputText(LocateType.ID,"cn.host.qc:id/pileSnTv",pileSn);

            //相机权限开启
            click(LocateType.ID,"cn.host.qc:id/photoIv");
            clickNegatively(LocateType.ID,"com.android.permissioncontroller:id/permission_allow_button");

            //每个检查点拍照
            takePhotoInPointList(checkPointList);

            //点击保存
            swipePageDownByRadio(1D,1);
            saveCheck();

            //断言 1 toast断言 提交成功
            assertToastHasAppeared(TIME_OUT,"提交成功");

            clickTab("待完成");

            clickPoint(point);
            //断言 2 点击坐标 进入详情页面 读取桩号 正确
            assertElementText(LocateType.ID,"cn.host.qc:id/pileSnTv",pileSn);
            //断言 3 详情页面 暂存 保存 完成按钮存在
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/saveTv");
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/submitTv");
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/finishTv");

            //断言数据库
            AssertPileDetail actualDetail = pileMapper.assertPileDetail(pileSn, stageCode);
            assertPileDetail(actualDetail,1);
        } finally {
            //退出到工作台
            closeModule();
        }

        checkCount();
    }

    @Test(priority = 3,description = " ======== >> 发起报验,再次保存 断言1-toast消息\"提交成功\",断言2-点击坐标,进入详情页面,桩号符合预期,断言3-详情页面,存在暂存、保存、完成按钮,断言4-数据库验收明细数据(含状态)符合预期")
    void saveCheckTAgain() {
        enterModule("桩基验收");
        try {
            enterSection(sectionName);
            chooseCheckPart(checkPartName);
            clickTab("待完成");
            clickPoint(point);
            //断言 2 点击坐标 进入详情页面 读取桩号 正确
            assertElementText(LocateType.ID,"cn.host.qc:id/pileSnTv",pileSn);
            //断言 3 详情页面 暂存 保存 完成按钮存在
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/saveTv");
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/submitTv");
            assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/finishTv");

            //点击保存
            swipeUp(1D,1);
            saveCheck();

            //断言 1 toast断言 提交成功
            assertToastHasAppeared(TIME_OUT,"提交成功");

            //断言数据库
            AssertPileDetail actualDetail = pileMapper.assertPileDetail(pileSn, stageCode);
            assertPileDetail(actualDetail,1);
        } finally {
            //退出到工作台
            closeModule();
        }
        checkCount();
    }

    @Test(priority = 5,description = " ======== >> 发起报验,提交验收 断言1-toast消息\"提交成功\",断言2-点击坐标,进入详情页面,桩号符合预期,断言3-详情页面,不存在暂存、保存、完成按钮,断言4-数据库验收明细数据(含状态)符合预期")
    void submitCheck() {
        enterModule("桩基验收");
        try {
            enterSection(sectionName);
            chooseCheckPart(checkPartName);
            clickTab("待完成");
            clickPoint(point);
            finishCheck();

            //断言 1 toast 提交成功
            assertToastHasAppeared(TIME_OUT,"提交成功");

            clickTab("已验收");
            clickPoint(point);
            //断言 2 点击坐标 进入详情页面 读取桩号 正确
            assertElementText(LocateType.ID,"cn.host.qc:id/pileSnTv",pileSn);

            //TODO 断言3 按钮应该不存在
            AssertPileDetail actualPileDetail = pileMapper.assertPileDetail(pileSn, stageCode);
            assertPileDetail(actualPileDetail,2);
        } finally {
            closeModule();
            logout();
        }
    }

}
