package com.allen.testplatform.feign;

import com.allen.testplatform.config.FeignConfig;
import com.allen.testplatform.feign.fallback.QualityCheckServiceFeignFactory;
import com.allen.testplatform.feign.vo.OrgTreeSltVo;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import cn.nhdc.common.dto.ResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "NHDC-CLOUD-QUALITY-CHECK-SERVICE", configuration = FeignConfig.class, fallbackFactory = QualityCheckServiceFeignFactory.class)
public interface QualityCheckServiceFeign {

    /**
     * 获取工序验收明细
     * @param detailId
     * @return
     */
    @GetMapping("/qc/process/mobile/acceptance/detail")
    ResponseData<List<ProcessDetailDto>> getDetail(@RequestParam Long detailId);

    /**
     * 获取用户权限组织树
     *
     * @return 单条数据
     */
    @GetMapping("/qc/proView/orgTree")
    ResponseData<List<OrgTreeSltVo>> getOrgTree();


}
