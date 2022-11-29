package com.allen.testplatform.modules.casemanage.controller;


import com.allen.testplatform.modules.casemanage.model.vo.TestCaseVo;
import com.allen.testplatform.modules.casemanage.service.Add;
import com.allen.testplatform.modules.casemanage.service.ITestCaseService;
import com.allen.testplatform.modules.casemanage.service.Update;
import cn.nhdc.common.exception.BusinessException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * 测试平台-测试用例 前端控制器
 * </p>
 *
 * @author Fan QingChuan
 * @since 2022-06-13
 */
@Validated
@RestController
@RequestMapping("/testcase")
public class TestCaseController {

    @Resource
    public ITestCaseService testCaseService;

    /**
     * 保存测试用例
     * @param caseVo
     */
    @PostMapping("/save")
    public Long save(@RequestBody @Validated({Add.class}) TestCaseVo caseVo) {
        return testCaseService.save(caseVo);
    }

    /**
     * 更新测试用例
     * @param caseVo
     */
    @PostMapping("/update")
    public Long update(@RequestBody @Validated({Update.class}) TestCaseVo caseVo) {
        return testCaseService.update(caseVo);
    }

    /**
     * 导入
     * @return
     */
    @PostMapping("/import")
    public Map<String, Object> importTestCase(@RequestParam(value = "file", required = true) MultipartFile file,
                                              @RequestParam(defaultValue = "testcase") String sheetName,
                                              @RequestParam(defaultValue = "2") Integer caseType) throws IOException {
        if((!file.getOriginalFilename().endsWith("xls"))&& (!file.getOriginalFilename().endsWith("xlsx"))){
            throw new BusinessException("文件类型错误! 只支持excel类型");
        }
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename.substring(0, originalFilename.indexOf("."));
        Map<String, Object> result = testCaseService.importBatchTestCase(file.getInputStream(), fileName, sheetName ,caseType);
        return result;
    }

    /**
     * 导出测试用例
     */
    @GetMapping("/export")
    public void exportTestCase(HttpServletResponse response, @RequestParam(defaultValue = "9")Integer caseType) throws IOException {
        testCaseService.exportAll(response,caseType);
    }

}
