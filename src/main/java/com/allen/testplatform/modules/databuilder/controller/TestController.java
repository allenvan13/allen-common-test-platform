package com.allen.testplatform.modules.databuilder.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.allen.testplatform.common.enums.ResponseEnum;
import com.allen.testplatform.common.utils.FeishuUtils;
import com.allen.testplatform.common.utils.HttpUtils;
import com.allen.testplatform.config.CurrentEnvironmentConfig;
import com.allen.testplatform.config.SpringContextConfig;
import com.allen.testplatform.job.MyDimension;
import com.allen.testplatform.modules.databuilder.enums.FeishuOpenIdEnum;
import com.allen.testplatform.modules.databuilder.model.feishu.FeishuTextMessage;
import com.allen.testplatform.modules.databuilder.model.test.vo.HeartCheckTargetVo;
import com.allen.testplatform.modules.databuilder.service.HeartCheckService;
import com.allen.testplatform.common.utils.IpUtils;
import cn.nhdc.common.dto.ResponseData;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

/**
 * @author Fan QingChuan
 * @since 2022/1/12 10:06
 */

@RestController
@RequestMapping("/v1")
@Slf4j
@Validated
public class TestController {

    @Resource
    private HeartCheckService heartCheckService;

    @Resource
    private CurrentEnvironmentConfig environmentConfig;

    @GetMapping("/heartcheck/all")
    public ResponseData doAllHeartCheck(@RequestParam(defaultValue = "PRO") String env,
                                        @RequestParam String serviceName,
                                        @RequestParam(defaultValue = "ALL") String teamCode){
        int[] result = heartCheckService.checkServiceByServiceAndEnv(env,serviceName,teamCode);
        return new ResponseData(true, ResponseEnum.SUCCESS.getMessage(),"被测服务总数: ".concat(String.valueOf(result[0])).concat(" 服务正常数量:  ").concat(String.valueOf(result[1])),null);
    }


    @PostMapping("/heartcheck/target")
    public ResponseData doTargetHeartCheck(@RequestBody @Valid HeartCheckTargetVo heartCheck){
        return new ResponseData(heartCheckService.checkServiceByVo(heartCheck),ResponseEnum.SUCCESS.getMessage(),"",null);
    }

    @GetMapping("/ip")
    public ResponseData getRemoteIp(HttpServletRequest request) {
        ResponseData responseData = new ResponseData();
        responseData.setBody(IpUtils.getIpAddr(request));
        responseData.setCode("0001");
        responseData.setMessage(request.getHeader("user-agent"));
        return responseData;
    }

    @GetMapping("/testAppPile")
    public ResponseData testAppPile() throws Exception {
//        TestNG t = new TestNG();
//        testNG.setTestClasses(new Class[]{TestAppProcess.class});
//        testNG.run();

        return ResponseData.success(SpringContextConfig.getActiveProfile()+ environmentConfig.getOPEN_HOST() +environmentConfig.getHOST() +environmentConfig.getENV());
    }
}
