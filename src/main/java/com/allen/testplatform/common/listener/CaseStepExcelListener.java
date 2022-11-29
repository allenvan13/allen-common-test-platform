package com.allen.testplatform.common.listener;

import com.allen.testplatform.modules.casemanage.model.entity.UiTestCaseStep;
import com.allen.testplatform.modules.casemanage.service.IUiTestCaseStepService;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;

import javax.annotation.Resource;
import java.util.List;

/**
 * 直接用map接收数据
 *
 * @author Jiaju Zhuang
 */
public class CaseStepExcelListener implements ReadListener<UiTestCaseStep> {

    private static final ReportLog reportLog = new ReportLog(CaseStepExcelListener.class);

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;
    /**
     * 缓存的数据
     */
    private List<UiTestCaseStep> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    /**
     * 假设这个是一个DAO，当然有业务逻辑这个也可以是一个service。当然如果不用存储这个对象没用。
     */
    @Resource
    private IUiTestCaseStepService iUiTestCaseStepService;

    /**
     * 如果使用了spring,请使用这个构造方法。每次创建Listener的时候需要把spring管理的类传进来
     *
     * @param iUiTestCaseStepService
     */
    public CaseStepExcelListener(IUiTestCaseStepService iUiTestCaseStepService) {
        this.iUiTestCaseStepService = iUiTestCaseStepService;
    }

    public CaseStepExcelListener(){

    }

    /**
     * 异常时
     * @param exception
     * @param context
     * @throws Exception
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        reportLog.error("解析失败，但是继续解析下一行:{}", exception.getMessage());
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            reportLog.error("第{}行，第{}列解析异常，数据为:{}", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());
        }
    }

    @Override
    public void invoke(UiTestCaseStep uiTestCaseStep, AnalysisContext analysisContext) {
        reportLog.info("解析到一条数据:{}", JSON.toJSONString(uiTestCaseStep));
        cachedDataList.add(uiTestCaseStep);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param analysisContext
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        reportLog.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        reportLog.info("{}条数据，开始存储数据库！", cachedDataList.size());
        iUiTestCaseStepService.saveBatch(cachedDataList);
        reportLog.info("存储数据库成功！");
    }
}
