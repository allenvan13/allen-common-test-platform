package com.allen.testplatform.feign.fallback;

import com.allen.testplatform.feign.QualityCheckServiceFeign;
import com.allen.testplatform.feign.vo.OrgTreeSltVo;
import com.allen.testplatform.feign.vo.ProcessDetailDto;
import cn.nhdc.common.dto.ResponseData;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class QualityCheckServiceFeignFactory implements FallbackFactory<QualityCheckServiceFeign> {
    @Override
    public QualityCheckServiceFeign create(Throwable cause) {
        return new QualityCheckServiceFeign() {

            @Override
            public ResponseData<List<ProcessDetailDto>> getDetail(Long detailId) {
                log.error("远程调用check服务 /qc/process/mobile/acceptance/detail 获取工序验收明细!", cause);
                return ResponseData.fail(cause.getMessage());
            }

            @Override
            public ResponseData<List<OrgTreeSltVo>> getOrgTree() {
                log.error("远程调用check服务 /qc/proView/orgTree 失败", cause.getMessage());
                return ResponseData.fail(cause.getMessage());
            }
        };
    }

}
