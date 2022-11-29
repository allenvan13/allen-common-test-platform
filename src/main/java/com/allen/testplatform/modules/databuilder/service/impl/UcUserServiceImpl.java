package com.allen.testplatform.modules.databuilder.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.modules.databuilder.mapper.UserCenterMapper;
import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.allen.testplatform.modules.databuilder.service.UcUserService;
import cn.nhdc.common.exception.BusinessException;
import cn.nhdc.common.util.CollectionUtils;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/6/2 13:35
 */
@Service("UcUserService")
@Slf4j
@DS("uc")
public class UcUserServiceImpl extends ServiceImpl<UserCenterMapper, UcUser> implements UcUserService {

    @Resource
    private UserCenterMapper ucMapper;

    @Override
    public UcUser getCreateUser(String createUserName) {
        UcUser user;
        if (ObjectUtil.isNotEmpty(createUserName)) {
            List<UcUser> users = ucMapper.getUserByOthers(null,createUserName,null,null, Constant.PS_SOURCE);
            if (CollectionUtils.isNotEmpty(users)) {
                user = users.get(RandomUtil.randomInt(users.size()));
            }else {
                throw new BusinessException("(姓名全匹配)未匹配到目标用户-->"+createUserName);
            }
        }else {
            user = ucMapper.getUserByOthers("chengxm1", "程星铭", null, null, Constant.PS_SOURCE).get(0);
        }

        return user;
    }
}
