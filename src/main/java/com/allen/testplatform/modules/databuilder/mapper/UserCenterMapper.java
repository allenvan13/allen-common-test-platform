package com.allen.testplatform.modules.databuilder.mapper;

import com.allen.testplatform.modules.databuilder.model.common.UcUser;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author Fan QingChuan
 * @since 2021/11/22 11:30
 */

@Mapper
@DS("uc")
@Transactional
public interface UserCenterMapper extends BaseMapper<UcUser> {

    List<Map<String,Object>> getSupplierUserInfo(@Param("providerGuid") String providerGuid);

    List<JSONObject> getSupplierUserOld(@Param("providerGuid") String providerGuid);

    List<JSONObject> getSupplierListUser(@Param("providerGuidList")List<String> providerGuidList);

    JSONObject getUserIdByUserName(@Param("userName") String userName);

    JSONObject getTestUserById(@Param("userId") Long userId);

    /**
     * 获取多个目标供应商公司下员工
     * @param providerGuidList
     * @return
     */
    List<UcUser> getSupplierUsersByList(@Param("providerGuidList")List<String> providerGuidList);

    /**
     * 获取目标供应商公司下员工
     * @param providerGuid
     * @return
     */
    List<UcUser> getSupplierUsers(@Param("providerGuid") String providerGuid);

    /**
     * 获取指定源类型目标用户
     * @param userIdList
     * @param source PS、SUPPLIER
     * @return
     */
    List<UcUser> getUsersByIdList(@Param("userIdList")List<Long> userIdList,
                                  @Param("source")String source);

    /**
     * 获取指定源类型目标用户
     * @param userName
     * @param realName
     * @param phone
     * @param providerGuid
     * @param source PS、SUPPLIER
     * @return
     */
    List<UcUser> getUserByOthers(@Param("userName")String userName,
                                 @Param("realName")String realName,
                                 @Param("phone")String phone,
                                 @Param("providerGuid")String providerGuid,
                                 @Param("source")String source);

    UcUser getUserByIdSource(@Param("userId")Long userId,
                             @Param("source")String source);

    UcUser getUserById(@Param("userId")Long userId);

}
