package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.feign.QualityCheckServiceFeign;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.MockDemoService;
import com.allen.testplatform.modules.databuilder.service.UcUserService;
import cn.nhdc.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.bind.ValidationException;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/6/8 9:56
 */

@Service("MockDemoService")
public class MockDemoServiceImpl implements MockDemoService {

    @Resource
    private UcUserService ucUserService;

    @Resource
    private QualityCheckServiceFeign processFeign;

    @Resource
    private UserCenterMapper ucMapper;

    @Override
    public int sum(int a, int b) {
        return a +b;
    }

    @Override
    public UcUser getUcUser(String realName) throws ValidationException {

        if (ObjectUtil.isEmpty(realName)) {
            throw new ValidationException("realName 不能为空!");
        }
        return ucUserService.getCreateUser(realName);
    }

    @Override
    public int getSupplierEmployeeCount(String providerGuid) {

        if (ObjectUtil.isEmpty(providerGuid)) {
            throw new RuntimeException("providerGuid 不能为空!");
        }
        return ucMapper.getSupplierUsers(providerGuid).size();
    }

    @Override
    public List<ProcessDetailDto> getProcessDetails(Long detailId) {
        if (ObjectUtil.isEmpty(detailId)) {
            throw new BusinessException("detailId不能为空!");
        }
        return processFeign.getDetail(detailId).getBody();
    }
}
