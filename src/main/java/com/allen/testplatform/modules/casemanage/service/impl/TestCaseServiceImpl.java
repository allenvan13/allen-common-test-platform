package com.allen.testplatform.modules.casemanage.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.enums.TestTypeEnum;
import com.allen.testplatform.common.handler.*;
import com.allen.testplatform.common.listener.DataEasyExcelListener;
import com.allen.testplatform.common.utils.DateUtils;
import com.allen.testplatform.config.OSSConfig;
import com.allen.testplatform.modules.casemanage.mapper.TestCaseMapper;
import com.allen.testplatform.modules.casemanage.model.entity.TestCase;
import com.allen.testplatform.modules.casemanage.model.vo.ExcelCaseStepVo;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.modules.casemanage.service.ITestCaseService;
import com.allen.testplatform.modules.casemanage.service.IUiTestCaseStepService;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoleilu.hutool.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 测试平台-测试用例 服务实现类
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@Service
@Slf4j
public class TestCaseServiceImpl extends ServiceImpl<TestCaseMapper, TestCase> implements ITestCaseService {

    @Resource
    private IUiTestCaseStepService testCaseStepService;

    @Resource
    private OSSConfig ossConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(TestCaseVo caseVo) {

        if (ObjectUtil.isNotEmpty(caseVo.getCaseId())) {
            return update(caseVo);
        }else {
            //新增
            TestCase testCase = new TestCase();
            BeanUtil.copyProperties(caseVo,testCase);
            testCase.setId(IdWorker.getId());
            testCase.setCreateUser(caseVo.getTester());
            if (CollectionUtils.isNotEmpty(caseVo.getCaseSteps())) {
                List<TestCaseVo.UiCaseStepVo> caseSteps = caseVo.getCaseSteps().stream().sorted(Comparator.comparingInt(TestCaseVo.UiCaseStepVo::getSort)).collect(Collectors.toList());
                testCaseStepService.saveNewBatch(caseSteps,testCase.getId(),testCase.getCaseCode(),testCase.getCreateUser());
            }
            this.save(testCase);
            return testCase.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(TestCaseVo caseVo) {

        if (ObjectUtil.isEmpty(caseVo.getCaseId())) {
            throw new BusinessException("测试用例ID不能为空!");
        }

        TestCase testCase = this.baseMapper.selectById(caseVo.getCaseId());
        if (ObjectUtil.isEmpty(testCase)) {
            throw new BusinessException("测试用例不存在!");
        }
        BeanUtil.copyProperties(caseVo,testCase);
        testCase.setUpdateUser(caseVo.getTester());

        if (CollectionUtils.isNotEmpty(caseVo.getCaseSteps())) {
            testCaseStepService.updateBatchById(caseVo.getCaseSteps(),testCase.getUpdateUser());
        }

        this.updateById(testCase);
        return testCase.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importBatchTestCase(InputStream inputStream, String fileName,String sheetName,Integer caseType) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        DataEasyExcelListener<ExcelCaseStepVo> listener = new DataEasyExcelListener<>();
        EasyExcel.read(inputStream, ExcelCaseStepVo.class, listener).sheet(sheetName).doRead();
        List<ExcelCaseStepVo> caseStepVos = listener.getData();

        log.info("解析到{}条数据",caseStepVos.size());

        List<String> caseCodeList = caseStepVos.stream().map(ExcelCaseStepVo::getCaseCode).distinct().collect(Collectors.toList());
        List<Long> idList = new ArrayList<>(caseCodeList.size());

        log.info("重新组装成{}条测试用例",caseCodeList.size());

        caseCodeList.forEach(caseCode -> {
            TestCaseVo testCaseVo = new TestCaseVo();
            testCaseVo.setCaseCode(caseCode);
            testCaseVo.setType(caseType);
            testCaseVo.setSort(1);
            List<TestCaseVo.UiCaseStepVo> caseSteps = caseStepVos.stream().filter(o -> caseCode.equals(o.getCaseCode())).map(excelCaseStepVo -> {
                if (ObjectUtil.isEmpty(testCaseVo.getTester())) {
                    testCaseVo.setTester(ObjectUtil.isNotEmpty(excelCaseStepVo.getTester()) ? excelCaseStepVo.getTester() : null);
                }
                if (ObjectUtil.isEmpty(testCaseVo.getTeamCode())) {
                    testCaseVo.setTeamCode(ObjectUtil.isNotEmpty(excelCaseStepVo.getTeamCode()) ? excelCaseStepVo.getTeamCode() : null);
                }
                TestCaseVo.UiCaseStepVo uiCaseStepVo = new TestCaseVo.UiCaseStepVo();
                BeanUtil.copyProperties(excelCaseStepVo,uiCaseStepVo);
                return uiCaseStepVo;
            }).collect(Collectors.toList());
            testCaseVo.setCaseSteps(caseSteps);
            Long caseId = save(testCaseVo);
            idList.add(caseId);
        });

        resultMap.put("msg", String.format("成功导入%s条测试用例", idList.size()));
        resultMap.put("id", idList);
        return resultMap;
    }

    @Override
    public void exportAll(HttpServletResponse response,Integer caseType) throws IOException {
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
                case IOS_APP_TEST: type = typeEnum.getName();
                    sheetWriteHandler = new AndroidTestSheetWriteHandler();
                    rowWriteHandler = new AndroidTestHeadWriteHandler();
                    break;
                case ALL_TEST:
                default:
                    type = "所有类型";
                    break;
            }

            String fileName = Constant.AUTO_TEST + type + Constant.EXPORT_CASE + DateUtils.getTimeSuffix() + ".xlsx";
            response.setContentType("application/msexcel;charset=UTF-8");
            fileName = URLEncoder.encode(fileName,"UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            ServletOutputStream outputStream = null;
            try {

                outputStream = response.getOutputStream();
                writeExcel(dataList,sheetWriteHandler,rowWriteHandler,outputStream);
                if (outputStream != null) outputStream.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            log.info("不存在目标测试用例");
        }
    }

    private void writeExcel(List<ExcelCaseStepVo> dataList, SheetWriteHandler
            sheetWriteHandler, RowWriteHandler rowWriteHandler,ServletOutputStream outputStream){

        if (sheetWriteHandler != null && rowWriteHandler != null) {
            EasyExcel.write(outputStream,ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .registerWriteHandler(sheetWriteHandler)
                    .registerWriteHandler(rowWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else if (sheetWriteHandler != null && rowWriteHandler == null) {
            EasyExcel.write(outputStream,ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .registerWriteHandler(sheetWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else if (sheetWriteHandler == null && rowWriteHandler != null) {
            EasyExcel.write(outputStream,ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .registerWriteHandler(rowWriteHandler)
                    .sheet("testcase")
                    .doWrite(dataList);
        }else {
            EasyExcel.write(outputStream,ExcelCaseStepVo.class)
                    .inMemory(Boolean.TRUE)
                    .sheet("testcase")
                    .doWrite(dataList);
        }
    }

}
