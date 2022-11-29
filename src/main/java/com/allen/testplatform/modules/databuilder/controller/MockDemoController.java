package com.allen.testplatform.modules.databuilder.controller;

import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.MockDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.xml.bind.ValidationException;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/6/26 22:00
 */

@RestController
@RequestMapping("/mock")
public class MockDemoController {

    @Resource
    private MockDemoService mockDemoService;

    @GetMapping("/user")
    public UcUser getUcUser(String realName) throws ValidationException {
        return mockDemoService.getUcUser(realName);
    }

    @GetMapping("/count")
    public int getSupplierEmployeeCount(String providerGuid) {
        return mockDemoService.getSupplierEmployeeCount(providerGuid);
    }

    @GetMapping("/sum")
    public int sum(int a, int b) {
        return mockDemoService.sum(a,b);
    }

    @GetMapping("/details")
    public List<ProcessDetailDto> getProcessDetails(Long detailId) {
        return mockDemoService.getProcessDetails(detailId);
    }

}
