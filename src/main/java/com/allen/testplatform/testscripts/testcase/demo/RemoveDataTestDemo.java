package com.allen.testplatform.testscripts.testcase.demo;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.constant.BusinessType;
import com.allen.testplatform.common.utils.CommonUtils;
import com.allen.testplatform.common.utils.PropertiesUtils;
import com.allen.testplatform.modules.databuilder.mapper.QcDeleteDataMapper;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.SpringTestBase;
import cn.nhdc.common.util.CollectionUtils;
import com.xiaoleilu.hutool.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Test(description = "删除测试数据类 测试数据删除方法可根据业务划分统一写在这里")
public class RemoveDataTestDemo extends SpringTestBase {

    private static final ReportLog reportLog = new ReportLog(RemoveDataTestDemo.class);

    @Resource
    private QcDeleteDataMapper deleteDataMapper;

    private boolean isDeleteFile = false;

    private PropertiesUtils prop;
    private String sourceName = "jxRemoveData";

    @BeforeTest
    void setUp() {
        prop = new PropertiesUtils(sourceName);
    }

    @AfterTest
    void tearDown() {
        //删除文件及文件夹
        if (isDeleteFile) {
            String filePath = CommonUtils.getResourceRootPath() + "properties" + CommonUtils.SEPARATOR + sourceName + ".properties";
            if (FileUtil.exist(filePath)) {
                FileUtil.del(filePath);
            }
        }
    }

    @Test(description = "专项巡检")
    void removeZxxjOrderData() {
        String contents = prop.getProperty(BusinessType.ZXXJ);
        if (ObjectUtil.isNotEmpty(contents)) {
            List<String> removeContentList = Arrays.stream(contents.split(",")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(removeContentList)) {
                //删除代办
                int backlogCount = deleteDataMapper.removeZxxjBacklogByContent(removeContentList);
                //删除业务数据
                int orderCount = deleteDataMapper.removeZxxjOrderByContent(removeContentList);
                reportLog.info("删除[专项巡检]数据 ========== >> [{}] 条 , 代办数据 [{}] 条",orderCount,backlogCount);
            }
        }else {
            reportLog.info(" ========== >> 不存在待删除的[专项巡检]数据");
        }
    }

    @Test(description = "工程检查、分户查验、景观检查、装饰检查、在线房修、设计巡检共用tk_ticket表,删除逻辑一致",dataProvider = "getBusinessType")
    void removeTicketData(String businessType) {
        String contents = prop.getProperty(businessType);
        if (ObjectUtil.isNotEmpty(contents)) {
            List<String> removeContentList = Arrays.stream(contents.split(",")).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(removeContentList)) {
                int backlogCount = deleteDataMapper.removeTicketBacklogByTypeContent(businessType,removeContentList);
                int ticketCount = deleteDataMapper.removeTicketByTypeContent(businessType,removeContentList);
                reportLog.info("删除[{}]数据 ========== >> [{}] 条 , 代办数据 [{}] 条",businessType,ticketCount,backlogCount);
            }
        }else {
            reportLog.info(" ========== >> 不存在待删除的[{}]数据",businessType);
        }
    }

    @DataProvider(name = "getBusinessType")
    public Object[][] businessTypeDataProvider() {
        return new Object[][] {
                {BusinessType.GCJC},
                {BusinessType.ZSJC},
                {BusinessType.JGJC},
                {BusinessType.ZXFX},
                {BusinessType.FHCY},
                {BusinessType.SJXJ}
        };
    }


}
