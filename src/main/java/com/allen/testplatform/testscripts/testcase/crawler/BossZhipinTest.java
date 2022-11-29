package com.allen.testplatform.testscripts.testcase.crawler;

import com.allen.testplatform.testscripts.config.ReportLog;
import com.allen.testplatform.testscripts.testcase.base.WebTestBase;
import org.openqa.selenium.Cookie;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * @author Fan QingChuan
 * @since 2022/11/6 13:51
 */

public class BossZhipinTest extends WebTestBase {

    private static final ReportLog reportLog = new ReportLog(BossZhipinTest.class);

    @Test
    void testTemp() {
//        String API_LIST = "https://www.zhipin.com/wapi/zpgeek/search/joblist.json?scene=1&query=%E8%87%AA%E5%8A%A8%E5%8C%96%E6%B5%8B%E8%AF%95&city=101270100&experience=&degree=&industry=&scale=&stage=&position=&salary=&multiBusinessDistrict=&page=1&pageSize=30";
//        String API_DETAIL = "https://www.zhipin.com/wapi/zpgeek/job/card.json?securityId=P8DKQzWtudl9Y-Y1korGJuxVg_rIQFfs2axEaO3AX5_nMNfARgukEa14vzRWfISGxh_hagRQ3tk_YQAZfpTSlG1dfH3U8RiacdB7-a8YvZtIzgfL7HDDvtEmB_f2jozrw__0HgoGhA~~";
//
//        Map<String,String> headers = new HashMap<>();
//        headers.put("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
//        headers.put("referer","https://www.zhipin.com/web/geek/job?query=%E8%87%AA%E5%8A%A8%E5%8C%96%E6%B5%8B%E8%AF%95&city=101270100");
//        headers.put("cookie","lastCity=101270100; __zp_seo_uuid__=8fc4b520-c4e1-4a64-bd70-dbf1c646622d; __g=-; Hm_lvt_194df3105ad7148dcf2b98a91b5e727a=1667704654; wd_guid=00a50b5d-d9d7-4d28-890c-d1748a3ea756; historyState=state; _bl_uid=6RlXwaC844LsdvcFtdn6m8mu62eU; Hm_lpvt_194df3105ad7148dcf2b98a91b5e727a=1667711559; wt2=DziGyF4DYG2vZs0A3YRvTxtAAEd7ZfmyxYWSX0EOOehYYYmM2CtCLoVpOiA-QmfmF0oajqIcNgaqH0ZhCsiZfbQ~~; wbg=0; __c=1667704654; __l=r=https%3A%2F%2Fwww.baidu.com%2Flink%3Furl%3DXxfoGO1dyvAQajfZZpxzWwhq_heGf5R3wmRDY7EUSGrq8aCNljjt2ynyzrGXTpZC%26wd%3D%26eqid%3Df1bd95360013bded0000000663672742&l=%2Fwww.zhipin.com%2Fweb%2Fgeek%2Fjob%3Fquery%3D%25E8%2587%25AA%25E5%258A%25A8%25E5%258C%2596%25E6%25B5%258B%25E8%25AF%2595%26city%3D101270100%26page%3D2&s=3&g=&friend_source=0&s=3&friend_source=0; __a=55182397.1667704654..1667704654.15.1.15.15; __zp_stoken__=2d9feaUx8aCVVL2xfPDZPYyIieRhZaT01SlJqVFc3a05bOxN6Vi4feDheNVU6P0ZlF3t0BxY6IGQxfiRkUSVzdR5xWyF2XHFQIA4GK3AuJVBVHDAiaTYDWi9OSUMlFwMuVUJGDgxbC0d4bCE%3D; geek_zp_token=V1QtMhEef-2VxgXdNvyBoZKyyy7DrQzA~~");
//        headers.put("sec-ch-ua","\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"");
//        headers.put("sec-ch-ua-platform","\"Windows\"");
//        headers.put("token","WhktpsO95oHiGMs");
//        headers.put("zp_token","V1QtMhEef-2VxgXdNvyBoZLSKy6TLQww~~");
//
//        String rs = HttpUtils.doGet(API_LIST, headers);
//
//        reportLog.info(" ======== >> {}", JSONObject.parseObject(rs));

        initBaseBrowser("chrome:106");
        baseWebDriver.get("https://www.zhipin.com/web/geek/job?query=%E8%87%AA%E5%8A%A8%E5%8C%96%E6%B5%8B%E8%AF%95&city=101270100");
        Set<Cookie> cookies = baseWebDriver.manage().getCookies();


    }
}
