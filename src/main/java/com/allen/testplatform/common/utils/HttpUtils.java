package com.allen.testplatform.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.allen.testplatform.testscripts.config.ReportLog;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * HutoolHttpUtils
 * 再次封装Hutool的HttpUtils
 * @author FanQingChuan
 * @since 2021/8/27 17:57
 */
@Slf4j
public class HttpUtils extends HttpUtil {

    private static final ReportLog reportLog = new ReportLog(HttpUtils.class);

    public static String doGet(String url, Map<String,String> headers,String params){
        reportLog.info("请求地址: {},请求头: {},请求参数: {}",url,headers,params);
        return HttpRequest.get(url)
                .addHeaders(headers)
                .body(params)
                .execute().body();
    }

    public static String doGet(String url, Map<String,String> headers){
        reportLog.info("请求地址: {}",url,headers);
        return HttpRequest.get(url)
                .addHeaders(headers)
                .execute().body();
    }

    public static String doGet(String url){
        reportLog.info("请求地址: {}",url);
        return HttpRequest.get(url)
                .execute().body();
    }

    public static String doPost(String url){
        reportLog.info("请求地址: {}",url);
        return HttpRequest.post(url)
                .execute().body();
    }

    public static String doPost(String url,String params){
        reportLog.info("请求地址: {},请求参数: {}",url,params);
        return HttpRequest.post(url)
                .body(params)
                .execute().body();
    }

    public static String doPost(String url, Map<String,String> headers){
        reportLog.info("请求地址: {},请求头: {}",url,headers);
        return HttpRequest.post(url)
                .addHeaders(headers)
                .execute().body();
    }

    public static String doPost(String url, Map<String,String> headers,String params){
        reportLog.info("请求地址: {},请求头: {},请求参数: {}",url,headers,params);
        return HttpRequest.post(url)
                .addHeaders(headers)
                .body(params)
                .execute().body();
    }
}
