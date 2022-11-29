package com.allen.testplatform.testscripts.testcase.base.common;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.utils.HttpUtils;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.TestBase;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.bean.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface TestCommon{

    int COMMON_TIME_OUT = TestBase.TIME_OUT;
    ReportLog reportLog = new ReportLog(TestCommon.class);



    default void threadSleep(String seconds) {
        if (seconds == null || Long.parseLong(seconds) < 1 ) {
            throw new IllegalArgumentException("threadSleep 未设置休眠秒数 即输入值(秒值)不能为空 且 值需大于0");
        }
        long second = Long.parseLong(seconds);
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析Excel UI测试用例 <br>
     * 解析逻辑: 按测试用例编码(caseCode)进行分组,将所有步骤封装到对应用例编码下 <br>
     * 与测试用例-测试步骤数据结构对应, {@link TestCaseVo.UiCaseStepVo} 一对多关系
     *
     * @param filePath 文件绝对路径
     * @param sheetName 需解析的sheetName 默认Sheet1
     * @param headerRowNumber 解析case的标题行数 默认1行
     * @return a List of {@link TestCaseVo}
     */
    default List<TestCaseVo> analysisExcelUiCase(String filePath, String sheetName, Integer headerRowNumber){

        List<ExcelCaseStepVo> caseStepVos = EasyExcel.read(filePath).head(ExcelCaseStepVo.class)
                .sheet(sheetName != null ? sheetName : "Sheet1")
                .headRowNumber(ObjectUtil.isNotEmpty(headerRowNumber) && headerRowNumber > 0 ? headerRowNumber : 1).doReadSync();

        reportLog.info(" ======== >> 解析到[{}]条数据",caseStepVos.size());
        List<String> caseCodeList = caseStepVos.stream().map(ExcelCaseStepVo::getCaseCode).distinct().collect(Collectors.toList());
        List<TestCaseVo> caseVoList = new ArrayList<>(caseCodeList.size());
        reportLog.info(" ======== >> 重新组装成[{}]条测试用例",caseCodeList.size());

        caseCodeList.forEach(caseCode -> {
            TestCaseVo testCaseVo = new TestCaseVo();
            testCaseVo.setCaseCode(caseCode);

            List<TestCaseVo.UiCaseStepVo> caseSteps = caseStepVos.stream().filter(o -> caseCode.equals(o.getCaseCode())).map(excelCaseStepVo -> {
                TestCaseVo.UiCaseStepVo uiCaseStepVo = new TestCaseVo.UiCaseStepVo();
                BeanUtil.copyProperties(excelCaseStepVo,uiCaseStepVo);
                return uiCaseStepVo;
            }).collect(Collectors.toList());
            testCaseVo.setCaseSteps(caseSteps);
            caseVoList.add(testCaseVo);
        });

        return caseVoList;
    }

    default JSONObject doGet(String url, Map<String,String> headers, String params) {
        String str = HttpUtils.doGet(url, headers, params);

        if (str == null) {
            reportLog.error(" ======== >> 响应为null");
        }

        JSONObject rs = null;
        try {
            rs = JSONObject.parseObject(str);
        } catch (Exception e) {
            rs = JSON.parseObject(str);
        }finally {
            if (rs == null) {
                reportLog.error(" ======== >> 响应体解析JSON失败!");
            }
            reportLog.info(" ======== >> 响应body [{}]",JSONObject.toJSONString(rs));
        }
        return rs;
    }
}
