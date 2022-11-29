package com.allen.testplatform.modules.databuilder.service;

import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;

import javax.xml.bind.ValidationException;
import java.util.List;

public interface MockDemoService {

    UcUser getUcUser(String realName) throws ValidationException;

    int getSupplierEmployeeCount(String providerGuid);

    int sum(int a, int b);

    List<ProcessDetailDto> getProcessDetails(Long detailId);
}
