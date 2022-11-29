package com.allen.testplatform.modules.casemanage.service;

import com.allen.testplatform.modules.casemanage.model.entity.TestCase;
import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * <p>
 * 测试平台-测试用例 服务类
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
public interface ITestCaseService extends IService<TestCase> {

    Long save(TestCaseVo caseVo);

    Long update(TestCaseVo caseVo);

    Map<String, Object> importBatchTestCase(InputStream inputStream, String fileName,String sheetName,Integer caseType);

    void exportAll(HttpServletResponse response,Integer caseType) throws IOException;
}
