package com.allen.testplatform.testscripts.testcase;

import cn.hutool.core.date.StopWatch;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.utils.AesUtils;
import com.allen.testplatform.common.utils.TokenUtils;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Fan QingChuan
 * @since 2022/7/1 10:08
 */

public class DebugDemoTest {


    @Test
    void encode() {
        String clientId = "LH0001";
        String clientSecret  = "yr88bq70ui39dsst";

        String before = clientId + ":" + clientSecret;

        String after = Base64.getEncoder().encodeToString(before.getBytes(StandardCharsets.UTF_8));

        System.out.println(after);

        System.out.println(AesUtils.encrypt("a123456"));
    }

    @Test
    void test00() throws UnsupportedEncodingException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println(TokenUtils.getJxAppAndroidToken("ATE004", "a123456", "UAT"));
        Thread.sleep(1000);
        stopWatch.stop();
        Thread.sleep(1000);

        stopWatch.start();
        Thread.sleep(1000);
        stopWatch.stop();

        stopWatch.start();
        Thread.sleep(1000);
        stopWatch.stop();

        stopWatch.start();
        Thread.sleep(1000);
        stopWatch.stop();

        stopWatch.start();
        Thread.sleep(1000);
        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
//        System.out.println(TokenUtils.getJxAppAndroidToken("chengxm1", "a123456", "UAT"));
    }

    @Test
    void test001() {
        System.out.println(TokenUtils.getJxAppAndroidToken("111", "111", Constant.PRO_ENV));
    }
}
