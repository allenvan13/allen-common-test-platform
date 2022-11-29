package com.allen.testplatform.testscripts.scripts;

import com.allen.testplatform.TestPlatformApplication;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.enums.TestTypeEnum;
import com.allen.testplatform.common.handler.*;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.allen.testplatform.modules.casemanage.service.IUiTestCaseStepService;
import com.allen.testplatform.testscripts.config.ReportLog;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.List;

@SpringBootTest(classes = TestPlatformApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExportTestcase extends AbstractTestNGSpringContextTests {

    private static final ReportLog reportLog = new ReportLog(ExportTestcase.class);

    @Resource
    private IUiTestCaseStepService testCaseStepService;

    @Test(description = "本地启动服务------导出目标测试用例到服务器(windows)本地桌面路径  测试用例类型  1-接口测试 2-Web测试 3-安卓APP测试 4-IOS APP测试 9-其他混合测试")
    public void exportToLocalDesktop(@Optional("3")Integer caseType ) {
        exportCaseExcel(caseType);
    }

    public void exportCaseExcel(Integer caseType) {

        List<ExcelCaseStepVo> dataList = testCaseStepService.getCaseStepList(caseType);

        if (CollectionUtils.isNotEmpty(dataList)) {
            String type;
            SheetWriteHandler sheetWriteHandler = null;
            RowWriteHandler rowWriteHandler = null;
            TestTypeEnum typeEnum = TestTypeEnum.getInstance(caseType);

            switch (typeEnum) {
                case API_TEST: type = typeEnum.getName();
                    sheetWriteHandler = new ApiTestSheetWriteHandler();
                    rowWriteHandler = new ApiTestHeadWriteHandler();
                    break;
                case WEB_TEST: type = typeEnum.getName();
                    sheetWriteHandler = new WebTestSheetWriteHandler();
                    rowWriteHandler = new WebTestHeadWriteHandler();
                    break;
                case ANDROID_APP_TEST:
                    type = typeEnum.getName();
                    sheetWriteHandler = new AndroidTestSheetWriteHandler();
                    rowWriteHandler = new AndroidTestHeadWriteHandler();
                    break;
                case IOS_APP_TEST: type = typeEnum.getName();
                    sheetWriteHandler = new IOSTestSheetWriteHandler();
                    rowWriteHandler = new IOSTestHeadWriteHandler();
                    break;
                case ALL_TEST:
                default:
                    type = "所有类型";
                    break;
            }
            writeExcel(dataList,type,sheetWriteHandler,rowWriteHandler);
        }else {
            reportLog.info("不存在目标测试用例!");
        }
    }

    private void writeExcel(List<ExcelCaseStepVo> dataList,String type,SheetWriteHandler sheetWriteHandler,RowWriteHandler rowWriteHandler) {
        File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
        String desktopPath = desktopDir.getAbsolutePath();
        String fileName = desktopPath + System.getProperty("file.separator") + Constant.AUTO_TEST + type + Constant.EXPORT_CASE + DateUtils.getTimeSuffix() + ".xlsx";

        if (sheetWriteHandler != null && rowWriteHandler != null) {
            EasyExcel.write(fileName, ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)             //这里要注意inMemory 要设置为true，才能支持批注。目前没有好的办法解决 不在内存处理批注。这个需要自己选择
                    .registerWriteHandler(rowWriteHandler)
                    .registerWriteHandler(sheetWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else if (sheetWriteHandler != null && rowWriteHandler == null) {
            EasyExcel.write(fileName, ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .registerWriteHandler(sheetWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else if (sheetWriteHandler == null && rowWriteHandler != null) {
            EasyExcel.write(fileName, ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .registerWriteHandler(rowWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else {
            EasyExcel.write(fileName, ExcelCaseStepVo.class)
                    .sheet("testcase")
                    .doWrite(dataList);
        }
    }
}
