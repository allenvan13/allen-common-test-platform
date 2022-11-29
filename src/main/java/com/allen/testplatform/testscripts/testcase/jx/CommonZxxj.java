package com.allen.testplatform.testscripts.testcase.jx;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.modules.databuilder.enums.ZxxjTemplateTypeEnum;
import com.allen.testplatform.modules.databuilder.model.common.CheckUser;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjBatch;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjBatchEntity;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjTemplateCheckItem;
import com.allen.testplatform.modules.databuilder.model.zxxj.entity.ZxxjTemplateRelation;
import com.allen.testplatform.modules.databuilder.model.zxxj.vo.*;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Fan QingChuan
 * @since 2022/8/4 12:22
 */

public class CommonZxxj extends CommonAndroid {

    private static final ReportLog reportLog = new ReportLog(CommonZxxj.class);

    //当前操作的 批次 模板 操作人
    public AtomicReference<String> currentBatch = new AtomicReference<>("null");
    public AtomicReference<String> currentTemplate = new AtomicReference<>("null");
    public AtomicReference<String> currentUser = new AtomicReference<>("null");

    @DataProvider(name = "regressionTestBatchList")
    public Iterator<Object[]> regressionTestBatchList() {

        List<ZxxjBatchEntity> batchList = zxxjV2Mapper.selectList(new QueryWrapper<ZxxjBatchEntity>()
                .like("name", "渲染回归测试")
                .eq("stage_code",stageCode)
                .eq("enable",1)
                .eq("del_flag", 0));

        assert batchList.size() > 0;

        List<AssertBatch> assertBatchList = new ArrayList<>(batchList.size());

        batchList.forEach(batch -> {
            //被测试 批次、批次涉及的检查人员(测试账号)
            AssertBatch assertBatch = new AssertBatch();
            assertBatch.setBatchName(batch.getName());

            List<CheckUser> batchUsers = zxxjV2Mapper.getBatchUsers(batch.getId(), 1);
            CheckUser checkUser = batchUsers.get(RandomUtil.randomInt(batchUsers.size()));
            UcUser user = ucMapper.getUserById(checkUser.getUserId());

            assertBatch.setTestUserName(user.getSource().equals(Constant.SUPPLIER_SOURCE) ? user.getPhone() : user.getUserName());
            assertBatch.setTestPassword(EncryptUtils.decrypt(user.getPassword()));

            //根据批次关联的模板类型进行组装   单 -> 取检查项及其父路径    组合 -> 遍历所有模板 取检查项及其父路径
            //判断需要 被测模板名称 模板类型    末级项名称 末级项对应所有父级路径名称 末级项配置
            List<AssertTemplateCheckItem> assertTemplateCheckItemList = new ArrayList<>();

            ZxxjBatch batchInfo = zxxjV2Mapper.getBatchInfo(batch.getName(), batch.getId());
            if (batchInfo.getTemplateType().equals(ZxxjTemplateTypeEnum.group.getCode())) {
                //组合模板
                List<ZxxjTemplateRelation> singleTemplateList = zxxjV2Mapper.getBatchSingleTemplate(batchInfo.getTemplateId());
                assertBatch.setIsGroupTemplate(true);

                singleTemplateList.forEach(singeTemplate -> {
                    setAssertTemplateList(singeTemplate, assertTemplateCheckItemList);
                });
            }else {
                assertBatch.setIsGroupTemplate(false);

                //单项模板
                JSONObject template = zxxjV2Mapper.getSingleTemplate(batchInfo.getTemplateId());
                ZxxjTemplateRelation singleTemplate = new ZxxjTemplateRelation();
                singleTemplate.setTemplateName(template.getString("name"));
                singleTemplate.setTemplateType(template.getString("type"));
                singleTemplate.setSingleTemplateId(template.getLong("id"));
                setAssertTemplateList(singleTemplate, assertTemplateCheckItemList);
            }

            assertBatch.setAssertTemplateCheckItemList(assertTemplateCheckItemList);
            assertBatchList.add(assertBatch);
        });

        List<Object[]> result = new ArrayList<>();

        for (AssertBatch assertBatch : assertBatchList) {
            result.add(new Object[]{assertBatch});
        }
        return result.iterator();
    }

    public void jumpIntoBatch(String batchName) {
        clickText(batchName);
        boolean toastHasAppeared = isToastHasAppeared(baseAndroidDriver, 1, "请先下载离线数据");

        if (toastHasAppeared) {
            List<WebElement> batchs = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("cn.host.qc:id/tvNamePatrol")));
            List<WebElement> downloads = baseAndroidDriver.findElements(By.id("cn.host.qc:id/ivDownloadPatrol"));
            for (int i = 0; i < batchs.size(); i++) {
                if (batchs.get(i).getText().equals(batchName)) {
                    downloads.get(i).click();
                    break;
                }
            }

            toastHasAppeared = isToastHasAppeared(baseAndroidDriver, TIME_OUT, "数据下载完成");
            if (toastHasAppeared) {
                boolean elementExsit = true;
                do {
                    // 正在下载数据…100.00%
                    elementExsit = isElementExsit(baseAndroidDriver, 2, LocateType.ID, "cn.host.qc:id/proTvProject");
                }while (elementExsit);

                clickText(batchName);
            }
        }
    }

    /**
     * 登录-选择分期,进入专项巡检模块，点击进入目标批次
     * @param username
     * @param password
     * @param batchName
     */
    public void loginAndChooseBatch(String username,String password,String batchName) {

        if (currentUser.get().equals("null")) {
            login(username,password, true);
            currentUser.set(username);
            chooseStage(cityName,stageName);
        }else {
            if (!currentUser.get().equals(username)) {
                closeModule();
                logout();
                login(username,password, false);
                currentUser.set(username);
                chooseStage(cityName,stageName);
            }else {
                closeModule();
            }
        }

        enterModule("专项巡检");
        jumpIntoBatch(batchName);
        currentBatch.set(batchName);
    }

    public void jumpIntoWeightScoreCheckItem(String lastCheckItemName) {
        //判断是否存在扣分的按钮 存在则点击  不存在 则点击检查项名称
        try {
            WebElement element = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@text='"+ lastCheckItemName +"']/parent::android.widget.LinearLayout/android.widget.LinearLayout/android.widget.TextView[1]")));
            String text = element.getText();
            element.click();
            reportLog.info(" ======== >> 点击按钮[{}]",text);
        } catch (Exception e) {
            clickText(lastCheckItemName);
            reportLog.info(" ======== >> 不存在[扣分]按钮,点击检查项名称[{}]",lastCheckItemName);
        }
    }

    /**
     * 不同类型模板 具体断言方式不一样
     *
     * @param extension 检查项模板配置
     * @param templateType 检查项模板类型
     */
    public void assertInputBox(String extension,String templateType) {
        ZxxjTemplateTypeEnum templateTypeEnum = ZxxjTemplateTypeEnum.getType(templateType);
        switch (templateTypeEnum) {
            case realMeasure:
                assertRealMeasureInputBox(extension);
                break;
            case realMeasureCustom:
                assertRealMeasureCustomInputBox(extension);
                break;
            case qualityPoint:
                assertQualityPointInputBox(extension);
                break;
            case weightScore:
                assertWeightScoreInputBox(extension);
                break;
            case decideLevel:
                assertDecideLevelInputBox(extension);
                break;
            case awardDeduct:
                assertAwardDeductInputBox(extension);
                break;
            default:
                break;
        }
    }

    public void assertRealMeasureCustomInputBox(String extension) {
        RealMeasureCustomVo realMeasureCustomVo = JSON.parseObject(extension, RealMeasureCustomVo.class);

        RealMeasureVo realMeasureVo = realMeasureCustomVo.getConditions().get(0);
        int max = realMeasureVo.getAreaCount().getCount() * realMeasureVo.getAreaCount().getNums();
        switch (realMeasureVo.getPassArithmetic()) {

            case 2:
                reportLog.info(" ======== >> 断言1:设计值输入框个数是否符合预期 [{}]个",realMeasureVo.getAreaCount().getNums());
                assertElementsCount(baseAndroidDriver,realMeasureVo.getAreaCount().getNums(),TIME_OUT,LocateType.ID,"cn.host.qc:id/standardEt");

                reportLog.info(" ======== >> 断言2:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameEt");
                break;
            case 1:
            case 3:
            case 4:
                reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameEt");
                break;
            case 5:
                reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameTv");
                break;
            case 6:
                reportLog.info(" ======== >> 断言1:是否存在[合格点数]文案");
                assertElementText(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/leftLabelTv","合格点数");

                reportLog.info(" ======== >> 断言2:是否存在[总点数]文案");
                assertElementText(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/rightLabelTv","总点数");

                reportLog.info(" ======== >> 断言3:是否存在[合格点数输入框]");
                assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/valueEt");

                reportLog.info(" ======== >> 断言4:是否存在[总点数输入框]");
                assertElementExsit(baseAndroidDriver,TIME_OUT,LocateType.ID,"cn.host.qc:id/totalValueEt");
                break;
            default:
                break;
        }
    }

    public void assertRealMeasureInputBox(String extension) {
        List<RealMeasureVo> realMeasureVoList = JSON.parseArray(extension, RealMeasureVo.class);
        RealMeasureVo realMeasureVo = realMeasureVoList.get(0);
        int max = realMeasureVo.getAreaCount().getCount() * realMeasureVo.getAreaCount().getNums();
        switch (realMeasureVo.getPassArithmetic()) {
            case 2:
                reportLog.info(" ======== >> 断言1:设计值输入框个数是否符合预期 [{}]个",realMeasureVo.getAreaCount().getNums());
                assertElementsCount(baseAndroidDriver,realMeasureVo.getAreaCount().getNums(),TIME_OUT, LocateType.ID,"cn.host.qc:id/standardEt");
                reportLog.info(" ======== >> 断言2:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameEt");
                break;
            case 1:
            case 3:
            case 4:
                reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameEt");
                break;
            case 5:
                reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
                assertElementsCount(baseAndroidDriver,max,TIME_OUT, LocateType.ID,"cn.host.qc:id/itemNameTv");
                break;
            default:
                break;
        }
    }

    public void assertDecideLevelInputBox(String extension) {
        DecideLevelVo decideLevelVo = JSON.parseObject(extension, DecideLevelVo.class);

        int max = decideLevelVo.getAreaCount() * decideLevelVo.getMarkStandard().getDeductRules().size();

        reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
        assertElementsCount(baseAndroidDriver,max ,TIME_OUT,LocateType.ID,"cn.host.qc:id/itemNameTv");
    }

    public void assertQualityPointInputBox(String extension) {
        QualityPointVo qualityPointVo = JSON.parseObject(extension, QualityPointVo.class);
        int max = qualityPointVo.getAreaCount().getCount() * qualityPointVo.getAreaCount().getNums();

        reportLog.info(" ======== >> 断言:测量值输入框个数是否符合预期 [{}]个",max);
        assertElementsCount(baseAndroidDriver,max,TIME_OUT,LocateType.ID, "cn.host.qc:id/itemNameTv");
    }

    public void assertWeightScoreInputBox(String extension) {
        WeightScoreVo weightScoreVo = JSON.parseObject(extension, WeightScoreVo.class);

        String str = String.format("此问题扣分（上限%s分）",weightScoreVo.getDeductLimit());

        reportLog.info(" ======== >> 断言1:备注文字及上限是否符合预期 [{}]个",str);
        assertElementText(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/descTv",str);

        reportLog.info(" ======== >> 断言2:测量值输入框个数是否符合预期 [{}]个",1);
        assertElementsCount(baseAndroidDriver,1,TIME_OUT,LocateType.ID, "cn.host.qc:id/valueEt");
    }

    public void assertAwardDeductInputBox(String extension) {
        AwardDeductVo awardDeductVo = JSON.parseObject(extension, AwardDeductVo.class);

        String str = String.format("此问题扣分（上限%s分）",awardDeductVo.getLimit());

        reportLog.info(" ======== >> 断言1:备注文字及上限是否符合预期 [{}]个",str);
        assertElementText(baseAndroidDriver,LocateType.ID,"cn.host.qc:id/descTv",str);

        reportLog.info(" ======== >> 断言2:测量值输入框个数是否符合预期 [{}]个",1);
        assertElementsCount(baseAndroidDriver,1,TIME_OUT,LocateType.ID, "cn.host.qc:id/valueEt");
    }

    public void setAssertTemplateList(ZxxjTemplateRelation singeTemplate, List<AssertTemplateCheckItem> assertTemplateCheckItemList) {
        //末级检查项(可打分的)
        List<ZxxjTemplateCheckItem> lastCheckList = zxxjV2Mapper.getLastCheckList(singeTemplate.getSingleTemplateId());
        //模板下所有检查项
        List<ZxxjTemplateCheckItem> allCheckList = zxxjV2Mapper.getCheckItemList(singeTemplate.getSingleTemplateId());
        //TODO 取检查项规则  要么指定写死名称    要么 根据数据结构类型 取 具备典型特征的检查项进行测试   非必须全量测试 每个模板按细节分类取1-5个检查项查看
        //实测实量 实测实量自定义 类型中有 详细类型分类 单独处理 每种细类型取1个
        if (singeTemplate.getTemplateType().equals(ZxxjTemplateTypeEnum.realMeasure.getCode()) || singeTemplate.getTemplateType().equals(ZxxjTemplateTypeEnum.realMeasureCustom.getCode())) {

            for (int i = 1; i <= 6; i++) {
                int finalI = i;
                ZxxjTemplateCheckItem checkItem = lastCheckList.stream().filter(item -> item.getExtension().contains("\"passArithmetic\":"+ finalI)).findAny().orElse(null);
                if (checkItem != null) {
                    setAssertTemplate(checkItem,allCheckList,singeTemplate, assertTemplateCheckItemList);
                }
            }
        }else {
            //其他类型模板 随机取3个
            Collections.shuffle(lastCheckList);
            int index = RandomUtil.randomInt(0, lastCheckList.size() - 3);
            List<ZxxjTemplateCheckItem> subList = lastCheckList.subList(index, index + 3);

            subList.forEach(checkItem -> {
                setAssertTemplate(checkItem,allCheckList,singeTemplate, assertTemplateCheckItemList);
            });
        }

    }

    public void setAssertTemplate(ZxxjTemplateCheckItem checkItem,List<ZxxjTemplateCheckItem> allCheckList,ZxxjTemplateRelation singeTemplate,List<AssertTemplateCheckItem> assertTemplateCheckItemList) {
        List<String> parentNames = getParentNames(checkItem.getId(), allCheckList);

        //顺序翻转下 目标顺序为 一级检查项 二级检查项...
        ListUtil.reverse(parentNames);

        AssertTemplateCheckItem assertCheckItem = new AssertTemplateCheckItem();
        assertCheckItem.setTemplateName(singeTemplate.getTemplateName());
        assertCheckItem.setLastCheckItemName(checkItem.getName());
        assertCheckItem.setParentCheckItemName(parentNames);
        assertCheckItem.setExtension(checkItem.getExtension());
        assertCheckItem.setTemplateType(singeTemplate.getTemplateType());
        assertTemplateCheckItemList.add(assertCheckItem);
    }

    /**
     * 获取检查项id的所有父级Id直到根节点id
     * @param treeId 目标检查项id
     * @param trees 所有检查项
     * @return
     */
    public List<String> getParentNames(Long treeId, List<ZxxjTemplateCheckItem> trees) {
        //递归获取父级检查项名称,不包含自己
        List<String> parentNames = new ArrayList<>();
        setParentNames(trees, treeId, parentNames);
        return parentNames;
    }

    /**
     * 递归遍历获取检查项家族name
     * @param trees 所有检查项
     * @param treeId 目标检查项id
     * @param parentItemNames
     */
    public void setParentNames(List<ZxxjTemplateCheckItem> trees, Long treeId, List<String> parentItemNames) {
        for (ZxxjTemplateCheckItem tree : trees) {
            if (null == tree.getParentId()) {
                continue;
            }
            //判断是否有父节点
            if (treeId.equals(tree.getId())) {
                String parentName = trees.stream().filter(item -> item.getId().equals(tree.getParentId())).map(ZxxjTemplateCheckItem::getName).findFirst().orElse(null);
                parentItemNames.add(parentName);
                setParentNames(trees,tree.getParentId(),parentItemNames);
            }
        }
    }

    @Data
    public static class AssertBatch {
        private String batchName;
        private String testUserName;
        private String testPassword;
        private Boolean isGroupTemplate;
        private List<AssertTemplateCheckItem> assertTemplateCheckItemList;
    }

    @Data
    public static class AssertTemplateCheckItem {

        private String templateType;
        private String templateName;
        private List<String> parentCheckItemName;
        private String lastCheckItemName;
        private String extension;
    }
}
