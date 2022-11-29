package com.allen.testplatform.testscripts.testcase.demo;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.EncryptUtils;
import com.allen.testplatform.common.utils.HttpUtils;
import com.allen.testplatform.common.utils.StringUtils;
import com.allen.testplatform.common.utils.TokenUtils;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.config.SpringContextConfig;
import com.allen.testplatform.modules.databuilder.mapper.PileMapper;
import com.allen.testplatform.modules.databuilder.mapper.ProcessV2Mapper;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.SectionInfo;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.model.pile.AssertDetail;
import com.allen.testplatform.modules.databuilder.model.pile.PileDetailsVO;
import com.allen.testplatform.modules.databuilder.service.ProcessReportService;
import com.allen.testplatform.modules.databuilder.service.impl.PileServiceImpl;
import com.allen.testplatform.testscripts.api.ApiPile;
import com.allen.testplatform.testscripts.config.Assertion;
import com.allen.testplatform.testscripts.config.LocateType;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.SpringWebTestBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testng.annotations.*;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;

/**
 * @author Fan QingChuan
 * @since 2022/6/6 15:30
 */

@Slf4j
@Test(description = "SpringBootTest中 测试demo")
public class WebAndSpringTestDemo extends SpringWebTestBase {

    private static final ReportLog reportLog = new ReportLog(WebAndSpringTestDemo.class);

    @Resource
    private CurrentEnvironmentConfig currentEnv;

    @Resource
    private PileServiceImpl pileServiceImpl;

    @Resource
    private ProcessReportService processReportService;

    @Resource
    private UserCenterMapper ucMapper;

    @Resource
    private PileMapper pileMapper;

    @Resource
    private ProcessV2Mapper processV2Mapper;

    @Resource
    @Qualifier(value = "callerRunsThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @BeforeTest
    void setUp() {
        initBaseBrowser("chrome:102");
        baseWebDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        baseWebDriver.manage().window().setSize(new Dimension(800,400));
        baseWebDriver.manage().window().setPosition(new Point(100,100));
    }

    @AfterTest
    void tearDown() {
    }

    @Test(description = "demo1-启动spring环境进行测试")
    void testDemo001() {
        //获取用户
        UcUser user = ucMapper.getUserByIdSource(1130391013415436290L, "PS");
        reportLog.info("{}",JSONObject.toJSONString(user));
        Assertion.verifyNotNulls(user,"测试失败! user为空!");

        //获取标段
        SectionInfo sectionInfo = processV2Mapper.getSectionInfo("杭州装饰测试标段", null, null);
        reportLog.info("{}",JSONObject.toJSONString(sectionInfo));
        Assertion.verifyEquals(sectionInfo.getSectionName(),"杭州装饰测试标段","测试失败! 标段名实际为: " +sectionInfo.getSectionName());

        reportLog.info("目前运行环境 -> {} , 读取apollo环境配置 -> {} ",StringUtils.upperCase(SpringContextConfig.getActiveProfile().toUpperCase()),currentEnv.getENV());
        Assertion.verifyEquals(StringUtils.upperCase(SpringContextConfig.getActiveProfile().toUpperCase()),Constant.UAT_ENV,"测试失败! 实际环境不为UAT");

        //保存进度报告
        processReportService.saveRisks("杭州装饰测试标段",false,null,4,"NHATE-员工A",1);

    }

    @Test(description = "demo2- 一般接口测试完整流程")
    void testDemo002() {

        //获取报验人
        UcUser reporter = pileServiceImpl.getSectionReportUsers("杭州悦潮府1期标段", "C-员工D-AutoTest");
        Map<String,String> reportHeader = TokenUtils.getHeader(TokenUtils.getJxAppAndroidToken(reporter.getSource().equals(Constant.SUPPLIER_SOURCE) ? reporter.getPhone() : reporter.getUserName(), EncryptUtils.decrypt(reporter.getPassword()),currentEnv.getENV()));

        //设置预期断言对象 -桩基验收
        AssertDetail expectDetail = AssertDetail.builder()
                .sectionName("杭州悦潮府1期标段").sectionId(1498840877194670082L)
                .pileAreaName("桩基区域-三图ABC").pileAreaCode("1498841537805938689")
                .typeName("锤击预制桩").typeId(1427876963416317954L).typePath("桩基工程-锤击预制桩")
                .commitType(1).pileSn("指定编号001").build();

        //提交接口-报验过程提交(未完成)
        PileDetailsVO pileDetailsVO = pileServiceImpl.buildDetailVo(expectDetail.getTypeName(), expectDetail.getPileAreaName(), null, expectDetail.getSectionName(), null, 5, 0.55, 0.55, expectDetail.getPileSn());
        String rs = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiPile.SAVE_DETAIL), reportHeader, JSONObject.toJSONString(pileDetailsVO));
        reportLog.info("{}" , JSON.parseObject(rs));

        Long detailId = null;
        try {
            detailId = Long.valueOf(JsonPath.read(rs, "$.body").toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //断言
        Assertion.verifyNotNulls(detailId,"测试失败! 接口未响应detailId");
        expectDetail.setDetailId(detailId);
        //从数据库取实际对象
        AssertDetail actualDetail = pileMapper.assertDetail(detailId);
        Assertion.verifyEquals(actualDetail,expectDetail,"测试失败! 实际对象为: " + JSONObject.toJSONString(actualDetail));


        //继续提交接口-报验提交最终完成
        if (ObjectUtil.isNotEmpty(detailId)) {
            pileDetailsVO.setId(detailId);
            String params = "id=".concat(pileDetailsVO.getId().toString());
            String rs2 = HttpUtils.doPost(currentEnv.getOPEN_HOST().concat(ApiPile.COMMIT_PILE_DETAIL), reportHeader, params);
            reportLog.info("{}" , JSON.parseObject(rs2));

            //预期状态更新为2(已完成)
            expectDetail.setCommitType(2);
            actualDetail = pileMapper.assertDetail(detailId);
            Assertion.verifyEquals(actualDetail,expectDetail,"测试失败! 实际对象为: " + JSONObject.toJSONString(actualDetail));
        }else {
            reportLog.info("保存失败,接口未返回ID");
        }
    }

    @Test(description = "demo3- Web测试")
    void testDemo003(@Optional("ATE001")String username,
                     @Optional("a123456")String password,
                     @Optional("NHATE-员工A")String expectRealname) {
        //登录
        openUrl("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        inputText("id","username",username);
        inputText("id","password",password);
        click("id","login-button");
        //断言
        assertElementText(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",expectRealname);
        //退出登录
        clickAndHold(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span");
        clickInElementsByText(LocateType.CLASS_NAME,"el-dropdown-menu__item","退出登录");
    }


    @Test(description = "demo4- spring环境下 Web测试 前置数据需从数据库取或从其他接口、远程调用等来")
    void testDemo004(){
        //数据库取用户信息
        UcUser user = ucMapper.getUserById(1434711468133150722L);
        //登录
        openUrl("http://uat-jxadmin.host.cn/#/cooperation-manage/home");
        inputText("id","username", Constant.PS_SOURCE.equalsIgnoreCase(user.getSource()) ? user.getUserName() : user.getPhone());
        inputText("id","password",EncryptUtils.decrypt(user.getPassword()));
        click("id","login-button");
        //断言
        assertElementText(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span",user.getRealName());
        //退出登录
        clickAndHold(LocateType.XPATH,"/html/body/div[1]/div/div[1]/div/div[3]/div[2]/span");
        clickInElementsByText(LocateType.CLASS_NAME,"el-dropdown-menu__item","退出登录");
    }

    @BeforeMethod
    void beforeMethod(){
        reportLog.info("每个方法前执行操作");
    }

    @AfterMethod
    void afterMethod(){
        reportLog.info("每个方法后执行操作");
    }
}
