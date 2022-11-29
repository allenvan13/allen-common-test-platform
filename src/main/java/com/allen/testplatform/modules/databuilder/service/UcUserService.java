package com.allen.testplatform.modules.databuilder.service;

import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Fan QingChuan
 * @since 2022/6/2 13:34
 */
@DS("uc")
@Transactional
public interface UcUserService extends IService<UcUser> {

    UcUser getCreateUser(String createUserName);
}
