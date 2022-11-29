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

//    @Scheduled(cron = "0 39 0/4 * * ?")
//    public void autoHeartCheck(){
//        int[] result = heartCheckService.checkServiceByServiceAndEnv("PRO",null,"ALL");
//        String logStr = "被测服务总数: ".concat(String.valueOf(result[0])).concat(" 服务正常数量:  ").concat(String.valueOf(result[1]));
//        log.info("LOG---------------->自动执行检查: [{}]",logStr);
//    }


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

    @Scheduled( cron = "0 35 8,18 * * 1/5")
    public void testAutoPost() throws InterruptedException {
        int randomInt = RandomUtil.randomInt(600);
        Thread.sleep(randomInt * 1000);
        commonPost();
    }

    @GetMapping("/post")
    public ResponseData testPost() throws InterruptedException {
        ResponseData responseData = commonPost();
        return responseData;
    }

    public ResponseData commonPost() throws InterruptedException {

        Map<String,String> header = new HashMap<>();
        Map<String,String> params = new HashMap<>();
        header.put("Content-Type","application/json");
        header.put("User-Agent","OneApp/1.17.0(Android;HUAWEI,other,10)");

        params.put("username","fanqc");
        params.put("password","72f9786e362851c362052f911d2b660e");
        params.put("authorization","Basic eGhfcHJvZmVzc2lvbmFsX2lvczpKSVZDUFVST0Y2SUVaRE1XTE84TjdIMTI2SEtHVFdBUw==");
        params.put("grant_type","password");

        String response = HttpUtils.doPost("https://app.newhope.cn/pdp/manage/auth/token/app", header, JSONObject.toJSONString(params));

        Object token = JsonPath.read(response, "$.body.access_token");
        Object userId = JsonPath.read(response, "$.body.userId");

        String params_1 = "userId=".concat(userId.toString()).concat("&dataInfo=userJob%2CuserPersonnel");
        header.remove("Content-Type");
        header.put("Authorization","Bearer ".concat(token.toString()));

        response = HttpUtils.doGet("http://app.newhope.cn/uc/user/detail?", header, params_1);
        Object oldUserId = JsonPath.read(response, "$.body.user.oldUserId");

        header.put("Content-Type","application/json");
        header.put("osVersion","10");
        header.put("deviceId","599013db-4098-4985-947f-032462fc3246");
        header.put("os","HUAWEI,other");
        header.put("agent.device","Android");
        header.put("deviceMode","NOH-AN00");
        header.put("appType","APP.XH");
        header.put("deviceType","1");

        Map<String, MyDimension> report = getDimension();
        Map<String,Object> params_2 = new HashMap<>();

        for (String key : report.keySet()) {
            MyDimension dimension = report.get(key);
            params_2.put("locationType","LBS");
            params_2.put("signTime", DateUtil.now());
            params_2.put("workDate", DateUtil.today());
            params_2.put("title", key);
            params_2.put("address", key);
            params_2.put("lat",dimension.getWidth());
            params_2.put("lng",dimension.getHeight());
            params_2.put("ifOutSide",false);
            params_2.put("handle","HAND");
            params_2.put("jobNumber",oldUserId);
        }

        response = HttpUtils.doPost("http://app.newhope.cn/signin/punch/data/signIn/v2", header, JSONObject.toJSONString(params_2));

        if (!JsonPath.read(response, "$.status").equals("0000") && !JsonPath.read(response, "$.message").equals(ResponseEnum.SUCCESS.getMessage())) {
            FeishuUtils feishuUtils = new FeishuUtils();
            FeishuTextMessage textMessage = feishuUtils.buildTextMessage("范青川", "请注意!!!出现非预期结果", response);
            Map<String, String> feishuHeaders = feishuUtils.getTenantTokenHeader();
            String openId = FeishuOpenIdEnum.getOpenId("范青川");
            feishuUtils.sendFeiShuMessageText(textMessage,feishuHeaders, Collections.singletonList(openId),FeishuUtils.OPEN_ID_TYPE);
            return new ResponseData(JsonPath.read(response, "$.status"),JsonPath.read(response, "$.message"),JsonPath.read(response, "$.body"),null);
            }else {
            Object body = JsonPath.read(response, "$.body");
            JSONObject response_body = JSON.parseObject(JSONObject.toJSONString(body));
            return new ResponseData(JsonPath.read(response, "$.status"),JsonPath.read(response, "$.message"),response_body,null);
        }
    }

    public Map<String, MyDimension> getDimension(){
        List<Map<String, MyDimension>> pointList = new ArrayList<>();
        Map<String,MyDimension> m1 = new HashMap<>();
        Map<String,MyDimension> m2 = new HashMap<>();
        Map<String,MyDimension> m3 = new HashMap<>();
        Map<String,MyDimension> m4 = new HashMap<>();
        Map<String,MyDimension> m5 = new HashMap<>();
        Map<String,MyDimension> m6 = new HashMap<>();
        Map<String,MyDimension> m7 = new HashMap<>();
        Map<String,MyDimension> m8 = new HashMap<>();
        Map<String,MyDimension> m9 = new HashMap<>();
        Map<String,MyDimension> m10 = new HashMap<>();
        Map<String,MyDimension> m11 = new HashMap<>();
        Map<String,MyDimension> m12 = new HashMap<>();
        m1.put("新希望中鼎国际",MyDimension.builder().width(30.592665).height(104.089919).build());
        m2.put("新希望中鼎国际",MyDimension.builder().width(30.592351).height(104.090386).build());
        m3.put("新希望中鼎国际",MyDimension.builder().width(30.592092).height(104.089941).build());
        m4.put("新希望中鼎国际",MyDimension.builder().width(30.592309).height(104.090428).build());
        m5.put("新希望中鼎国际",MyDimension.builder().width(30.592374).height(104.089855).build());

        m6.put("新希望中鼎国际",MyDimension.builder().width(30.592788).height(104.090121).build());
        m7.put("新希望中鼎国际",MyDimension.builder().width(30.592045).height(104.090563).build());
        m8.put("新希望中鼎国际",MyDimension.builder().width(30.592336).height(104.090239).build());
        m9.put("新希望中鼎国际",MyDimension.builder().width(30.592655).height(104.090079).build());
        m10.put("新希望中鼎国际",MyDimension.builder().width(30.591328).height(104.089496).build());
        m11.put("新希望中鼎国际",MyDimension.builder().width(30.592584).height(104.090548).build());
        m12.put("新希望中鼎国际",MyDimension.builder().width(30.591295).height(104.089565).build());
        pointList.add(m1);
        pointList.add(m2);
        pointList.add(m3);
        pointList.add(m4);
        pointList.add(m5);
        pointList.add(m6);
        pointList.add(m7);
        pointList.add(m8);
        pointList.add(m9);
        pointList.add(m10);
        pointList.add(m11);
        pointList.add(m12);

        Collections.shuffle(pointList);
        return pointList.get(RandomUtil.randomInt(pointList.size()));
    }

}
