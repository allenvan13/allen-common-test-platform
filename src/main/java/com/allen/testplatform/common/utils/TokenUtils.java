package com.allen.testplatform.common.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.allen.testplatform.common.constant.Constant;
import com.allen.testplatform.common.constant.HostCommon;
import com.allen.testplatform.common.enums.ClientEnum;
import com.allen.testplatform.testscripts.api.ApiCommon;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * GetToken
 * token工具类
 * @author FanQingChuan
 * @since 2021/8/27 11:14
 */
@Slf4j
public class TokenUtils {

    public static String getResBySysAndPass(String systemName, String username, String password, String env){
        Map<String,String> header = new HashMap<>();
        header.put("Authorization", ClientEnum.getClientBasic(systemName,env));
        header.put("Content-Type","application/x-www-form-urlencoded");
        String params = "grant_type=password&scope=server&username="+username+"&password="+AesUtils.encrypt(password);
        switch (env) {
            case Constant.FAT_ENV:
                return HttpUtils.doPost(HostCommon.FAT.concat(HostCommon.AUTH_TOKEN),header,params);
            case Constant.UAT_ENV:
                return HttpUtils.doPost(HostCommon.UAT.concat(HostCommon.AUTH_TOKEN),header,params);
            case Constant.PRO_ENV:
                return HttpUtils.doPost(HostCommon.PRO.concat(HostCommon.AUTH_TOKEN),header,params);
            default:
                throw new IllegalStateException("不可识别的环境！ " + env);
        }
    }

    public static String getTokenBySysAndPass(String systemName,String username, String password, String env){
        String response = getResBySysAndPass(systemName, username, password, env);
        return JsonPath.read(response,"$.body.access_token").toString();
    }

    public static String getResByAuth(String systemName, String username, String password, String host, String formApi, String redirectUrl, String env){

        StringBuilder body = new StringBuilder("username=");
        body.append(username)
                .append("&password=")
                .append(AesUtils.encrypt(password))
                .append("&saved_request_url=");

        body.append(redirectUrl);
        HttpResponse rs = HttpRequest.post(formApi)
                .setFollowRedirects(false)
                .body(body.toString())
                .executeAsync();
        String code = getLocationCode(rs);
        rs = HttpRequest.post(host.concat(HostCommon.AUTH_TOKEN).concat("?grant_type=authorization_code&code=").concat(code))
                .header("Authorization",ClientEnum.getClientBasic(systemName,env))
                .execute();

        return rs.body();
    }

    public static String getTokenByAuth(String systemName,String username, String password, String host, String formApi, String redirectUrl,String env){
        String response = getResByAuth(systemName, username, password, host, formApi, redirectUrl,env);
        return JsonPath.read(response,"$.body.access_token").toString();
    }

    /**
     * 匠星APP端-密码模式登录取token  调用匠星后端封装后的登录接口获取token
     * @param username
     * @param password 加密前密码
     * @param env
     * @return
     */
    public static String getJxAppAndroidToken(String username, String password, String env){
        String rs = null;
        Map<String,String> params = new HashMap<>();
        params.put("username",username);
        params.put("password",AesUtils.encrypt(password));
        params.put("grant_type","password");
        params.put("Authorization",ClientEnum.getClientBasic("匠星APP-Android端", env));

        switch (env) {
            case Constant.FAT_ENV:
                rs = HttpUtils.doPost(HostCommon.FAT_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            case Constant.UAT_ENV:
                rs =  HttpUtils.doPost(HostCommon.UAT_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            case Constant.PRO_ENV:
                rs = HttpUtils.doPost(HostCommon.PRO_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            default:
                break;
        }

        return JsonPath.read(rs,"$.body.access_token").toString();
    }

    /**
     * 匠星APP端-密码模式登录取token  调用匠星后端封装后的登录接口获取token
     * @param username
     * @param password 加密前密码
     * @param env
     * @return
     */
    public static String getJxAppIOSToken(String username, String password, String env){
        String rs = null;
        Map<String,String> params = new HashMap<>();
        params.put("username",username);
        params.put("password",AesUtils.encrypt(password));
        params.put("grant_type","password");
        params.put("Authorization",ClientEnum.getClientBasic("匠星APP-IOS端", env));

        switch (env) {
            case Constant.FAT_ENV:
                rs = HttpUtils.doPost(HostCommon.FAT_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            case Constant.UAT_ENV:
                rs =  HttpUtils.doPost(HostCommon.UAT_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            case Constant.PRO_ENV:
                rs = HttpUtils.doPost(HostCommon.PRO_OPEN.concat(ApiCommon.API_APP_TOKEN), JSONObject.toJSONString(params));
                break;
            default:
                break;
        }

        return JsonPath.read(rs,"$.body.access_token").toString();
    }

    /**
     * 匠星供应商门户-密码模式登录获取token
     * @param username
     * @param password
     * @param env
     * @return
     */
    public static String getJxSupplierToken(String username, String password, String env) {
        String rs = getJxSupplierResponse(username, password, env);
        return JsonPath.read(rs,"$.body.access_token").toString();
    }

    /**
     * 匠星供应商门户-密码模式登录获取登录响应body
     * @param username
     * @param password  加密前密码
     * @param env  环境 FAT UAT PRO
     * @return
     */
    public static String getJxSupplierResponse(String username, String password, String env) {
        return getResBySysAndPass(ClientEnum.UatJxSupplierPortal.getBusinessName(), username, password, env);
    }

    /**
     * 内部员工匠星后台-认证码模式 登录获取登录响应body (匠星后台登录-AUTH认证模式),获取整个响应体
     * @param username
     * @param password  加密前密码
     * @param env   环境  UAT PRO
     * @return
     */
    public static String getJxCheckAuthResponse(String username, String password, String env) {
        String rs;
        switch (env) {
            case Constant.FAT_ENV:
                rs = getResByAuth(ClientEnum.UatUc.getBusinessName(),username,password,HostCommon.FAT,HostCommon.FAT_AUTH_FORM,HostCommon.FAT_REDIRECT_URL,env);
                return convertToken(rs,ClientEnum.UatJxManageBackSystem.getClientId(),HostCommon.FAT);
            case Constant.UAT_ENV:
                rs = getResByAuth(ClientEnum.UatUc.getBusinessName(),username,password,HostCommon.UAT,HostCommon.UAT_AUTH_FORM,HostCommon.UAT_REDIRECT_URL,env);
                return convertToken(rs,ClientEnum.UatJxManageBackSystem.getClientId(),HostCommon.UAT);
            case Constant.PRO_ENV:
                rs = getResByAuth(ClientEnum.UatUc.getBusinessName(),username,password,HostCommon.PRO,HostCommon.PRO_AUTH_FORM,HostCommon.PRO_REDIRECT_URL,env);
                return convertToken(rs,ClientEnum.ProJxManageBackSystem.getClientId(),HostCommon.PRO);
            default:
                throw new IllegalStateException("不可识别的环境！ " + env);
        }
    }

    public static String convertToken(String responseBody,String clientId,String host){
        Map<String,String> header = new HashMap<>();
        header.put("Authorization",subToken(JsonPath.read(responseBody,"$.body.access_token").toString()));
        return HttpRequest.post(host.concat("/auth/user/token/convert"))
                .addHeaders(header)
                .body("{\"clientId\":\"" + clientId + "\"}")
                .execute().body();
    }

    /**
     * 内部员工匠星后台-认证码模式 登录获取token
     * @param username
     * @param password  加密前密码
     * @param env  环境   UAT UAT FAT
     * @return
     */
    public static String getJxCheckAuthToken(String username, String password, String env){
        String response = getJxCheckAuthResponse(username, password, env);
        return JsonPath.read(response,"$.body.access_token").toString();
    }

    public static String getLocationCode(HttpResponse rs){
        rs = HttpRequest.get(rs.header("Location"))
                .setFollowRedirects(false)
                .cookie(rs.getCookies())
                .executeAsync();
        return rs.header("Location").split("code=")[1];
    }

    public static String subToken(String token){
        return "Bearer " +token;
    }

    /**
     * 根据token获取当前登录系统Code
     *
     * @return 用户ID
     */
    public static String getUserCode(String token) {
        Object userCode = getValue("userCode",token);
        return userCode != null ? userCode.toString() : null;
    }

    /**
     * 根据token获取当前登录系统Code
     *
     * @return 用户ID
     */
    public static String getAppCode(String token) {
        Object appCode = getValue("appCode",token);
        return appCode != null ? appCode.toString() : null;
    }

    /**
     * 根据token获取用户ID
     *
     * @return 用户ID
     */
    public static String getUserId(String token) {
        Object userId = getValue("userId",token);
        return userId != null ? userId.toString() : null;
    }

    /**
     * 根据token获取用户名
     *
     * @return 用户ID
     */
    public static String getUsername(String token) {
        Object userName = getValue("userName",token);
        return userName != null ? userName.toString() : null;
    }

    /**
     * 根据token获取用户的真实姓名
     * @return
     */
    public static String getRealName(String token){
        Object realName = getValue("realName",token);
        return realName!=null ? realName.toString():null;
    }

    /**
     * 根据name在token中获取参数
     *
     * @param name
     * @return
     */
    private static Object getValue(String name,String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String key = Base64.getEncoder().encodeToString(Constant.SIGN_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
        return claims.get(name);
    }

    public static Map<String,String> getHeader(String token){
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", TokenUtils.subToken(token));
        return headers;
    }
}
