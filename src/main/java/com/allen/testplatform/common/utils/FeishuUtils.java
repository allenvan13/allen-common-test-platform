package com.allen.testplatform.common.utils;

import cn.hutool.core.util.ObjectUtil;
import com.allen.testplatform.modules.databuilder.enums.FeishuOpenIdEnum;
import com.allen.testplatform.modules.databuilder.model.feishu.FeishuCardMessage;
import com.allen.testplatform.modules.databuilder.model.feishu.FeishuMsg;
import com.allen.testplatform.modules.databuilder.model.feishu.FeishuTextMessage;
import com.allen.testplatform.testscripts.config.ReportLog;
import cn.nhdc.common.exception.BusinessException;
import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fan QingChuan
 * @since 2022/4/17 0:06
 */
public class FeishuUtils {

    private static final ReportLog reportLog = new ReportLog(FeishuUtils.class);

    public static String APP_ID = "123456789";
    public static String APP_SECRET = "qwertyuiop123456789";
    public static String APP_TOKEN = "asdfghjkl1234567890";
    public static String API_APP_TOKEN = "https://open.feishu.cn/open-apis/auth/v3/app_access_token";
    public static String API_TENANT_TOKEN = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/";
    //指定单个用户 或 群组送消息 POST
    public static String API_SEND_MESSAGE = "https://open.feishu.cn/open-apis/im/v1/messages";
    //指定多个用户或部门发送消息 POST 不能发送群组
    public static String API_SEND_BATCH_MESSAGE = "https://open.feishu.cn/open-apis/message/v4/batch_send/";
    //获取用户或机器人所在的群列表信息 需机器人在群里 GET
    public static String API_GET_GROUPID = "https://open.feishu.cn/open-apis/im/v1/chats";
    //通过手机号或邮箱获取用户ID  批量查询 POST {String[] emails, String[] mobiles}
    public static String API_POST_USERID = "https://open.feishu.cn/open-apis/contact/v3/users/batch_get_id";
    //通过手机号或邮箱获取用户ID  批量查询 GET &拼接 emails=lisi@z.com&emails=wangwu@z.com&mobiles=13812345678&mobiles=+12126668888
    public static String API_GET_USERID = "https://open.feishu.cn/open-apis/user/v1/batch_get_id";
    //根据用户名搜索用户 只能用user_token
    public static String API_GET_USER_BY_USERNAME = "https://open.feishu.cn/open-apis/search/v1/user";
    public static String API_GET_USER_BY_DEPARTMENT = "https://open.feishu.cn/open-apis/contact/v3/users/find_by_department";
    //人员open_id 统一用open_id发送消息
    public static String OPEN_ID_TYPE = "open_id";
    //群组id 统一用群组id发送消息
    public static String CHAT_ID_TYPE = "chat_id";
    //飞书开发者文档地址
    // token相关 https://open.feishu.cn/document/ukTMukTMukTM/uMTNz4yM1MjLzUzM
    // 发送消息相关 https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/create
    // 使用手机号或邮箱获取用户 ID https://open.feishu.cn/document/ukTMukTMukTM/uUzMyUjL1MjM14SNzITN

    public Map<String,String> getTenantTokenHeader() {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        Map<String,String> params = new LinkedHashMap<>();
        params.put("app_id",APP_ID);
        params.put("app_secret",APP_SECRET);
        String rs = HttpUtils.doPost(API_TENANT_TOKEN, headers, JSONObject.toJSONString(params));
        headers.put("Authorization","Bearer ".concat(JsonPath.read(rs,"$.tenant_access_token")));
        return headers;
    }

    public Map<String,String> getTenantTokenCustom(String appId,String appSecret) {
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json; charset=utf-8");
        Map<String,String> params = new LinkedHashMap<>();
        params.put("app_id",appId);
        params.put("app_secret",appSecret);
        String rs = HttpUtils.doPost(API_TENANT_TOKEN, headers, JSONObject.toJSONString(params));
        headers.put("Authorization","Bearer ".concat(JsonPath.read(rs,"$.tenant_access_token")));
        return headers;
    }

    public List<String> getUserIdsByPhoneOrEmailV3(List<String> emails,List<String> mobiles,Map<String,String> headers){
        Map<String,Object> params = new HashMap<>();
        String[] emailsStr = ListUtils.toArray(emails);
        String[] mobilesStr = ListUtils.toArray(mobiles);
        params.put("emails",emailsStr);
        params.put("mobiles",mobilesStr);
        String rs = HttpUtils.doPost(API_POST_USERID, headers, JSONObject.toJSONString(params));
        return JsonPath.read(rs,"$..user_id");
    }

    public List<String> getUserOpenIdsByPhoneOrEmailV1(List<String> emails,List<String> mobiles,Map<String,String> headers){
        StringBuilder params = new StringBuilder();
        if (CollectionUtils.isNotEmpty(emails) && CollectionUtils.isNotEmpty(mobiles)) {
            for (int i = 0; i < emails.size(); i++) {
                params.append("emails=").append(emails.get(i)).append("&");
            }
            setStringParams(1,mobiles,params);
        }else if (CollectionUtils.isEmpty(emails) && CollectionUtils.isNotEmpty(mobiles)) {
            setStringParams(1,mobiles,params);
        }else if (CollectionUtils.isNotEmpty(emails) && CollectionUtils.isEmpty(mobiles)) {
            setStringParams(2,emails,params);
        }else if (CollectionUtils.isEmpty(emails) && CollectionUtils.isEmpty(mobiles)) {
            throw new BusinessException("email 与 mobile 均为空,停止查询");
        }

        String rs = HttpUtils.doGet(API_GET_USERID, headers, params.toString());
        List<String> userIdList = new ArrayList<>();
        userIdList = JsonPath.read(rs, "$..open_id");
        return userIdList;
    }

    /**
     * 获取群Id
     * @param userIdType 可选值有：open_id：用户的 open id union_id：用户的 union id user_id：用户的 user id
     * @return
     */
    public List<String> getGroupChatId(String userIdType,Map<String, String> headers){
        String rs = HttpUtils.doGet(API_GET_GROUPID, headers, "user_id_type".concat(userIdType).concat("&page_size=100"));
        List<String> userIdList = new ArrayList<>();
        if (rs.contains("ok")) {
            userIdList = JsonPath.read(rs, "$..chat_id");
        }
        return userIdList;
    }

    public static void main(String[] args) {

        FeishuUtils feishuUtils = new FeishuUtils();
        Map<String, String> tokenHeader = feishuUtils.getTenantTokenHeader();
        List<String> mobiles = Arrays.asList("18820797532","13627664629");//"","","","","","","");
        List<String> emails = Arrays.asList("chengxm1@newhope.cn");//"","","","","","","");
        StringBuilder params = new StringBuilder();
//        feishuUtils.setStringParams(1,mobiles,params);
        feishuUtils.setStringParams(2,emails,params);
        List<String> userIdsByPhoneOrEmailV3 = feishuUtils.getUserOpenIdsByPhoneOrEmailV1(emails, null, tokenHeader);
        System.out.println(userIdsByPhoneOrEmailV3);

//        //ou_adf29d2fd723749f8101103f0a32a80b
//        FeishuMsg feishuMsg = new FeishuMsg();
//        feishuMsg.setMsg_type("interactive");
//        String rsponse =  "{\"body\":{\"category\":\"\",\"ckList\":[],\"mgrList\":[{\"avatar\":\"\",\"orgId\":\"1394941355783741442\",\"orgName\":\"\\u88C5\\u9970\\u5DE5\\u7A0B\\u7EC4\",\"position\":\"\",\"realName\":\"\\u738B\\u6842\\u658C\",\"userCode\":\"wanggb\",\"userId\":\"1108256536016261122\"}],\"rectifyList\":[],\"stageCode\":\"\"},\"code\":\"0000\",\"message\":\"\\u64CD\\u4F5C\\u6210\\u529F\",\"status\":true}";
//        FeishuCardMessage cardMessage = feishuUtils.buildHeartCheckCard("nnhdc-cloud-provider-data-platform", DateUtils.now(), "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/"
//                , "@测试 @开发 @产品", rsponse, "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal/");
//        feishuMsg.setContent(JSONObject.toJSONString(cardMessage));
//        feishuMsg.setReceive_id("ou_adf29d2fd723749f8101103f0a32a80b");
//        String rs2 = HutoolHttpUtils.doPost(API_SEND_MESSAGE.concat("?receive_id_type=").concat(OPEN_ID_TYPE), tokenHeader, JSONObject.toJSONString(feishuMsg));
//        System.out.println(rs2);
//        feishuUtils.sendFeiShuBatchMessage(cardMessage,tokenHeader,list);
//        feishuUtils.sendFeiShuMessage(cardMessage,tokenHeader,list,"open_id");

    }


    /**
     * 一次一次循环推送消息
     * @param cardMessage
     * @param headers
     * @param targetIdList 目标id
     * @param idType 指定idType open_id/user_id/union_id/email/chat_id
     */
    public void sendFeiShuMessage(FeishuCardMessage cardMessage, Map<String,String> headers, List<String> targetIdList, String idType){
        FeishuMsg feishuMsg = new FeishuMsg();
        feishuMsg.setMsg_type("interactive");
        feishuMsg.setContent(JSONObject.toJSONString(cardMessage));
        AtomicInteger success = new AtomicInteger(0);
        reportLog.info("Send meessage 共计需发送飞书消息[{}]条 ",targetIdList.size());
//        targetIdList.forEach(o -> {
//            feishuMsg.setReceive_id(o);
//            String rs = HutoolHttpUtils.doPost(API_SEND_MESSAGE.concat("?receive_id_type=").concat(idType), headers, JSONObject.toJSONString(feishuMsg));
//            if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.code")) && (int)JsonPath.read(rs,"$.code") == 0) {
//                success.getAndIncrement();
//            }
//        });

        for (int i = 0; i < targetIdList.size(); i++) {
            feishuMsg.setReceive_id(targetIdList.get(i));
            String rs = HttpUtils.doPost(API_SEND_MESSAGE.concat("?receive_id_type=").concat(idType), headers, JSONObject.toJSONString(feishuMsg));
            if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.code")) && (int)JsonPath.read(rs,"$.code") == 0) {
                success.getAndIncrement();
            }
        }
        reportLog.info("Send meessage 成功发送飞书消息[{}]条 ",success.get());
    }

    public void sendFeiShuMessageText(FeishuTextMessage textMessage, Map<String,String> headers, List<String> targetIdList, String idType){
        FeishuMsg feishuMsg = new FeishuMsg();
        feishuMsg.setMsg_type("text");
        feishuMsg.setContent(JSONObject.toJSONString(textMessage));
        AtomicInteger success = new AtomicInteger(0);
        reportLog.info("Send meessage 共计需发送飞书消息[{}]条 ",targetIdList.size());

        for (int i = 0; i < targetIdList.size(); i++) {
            feishuMsg.setReceive_id(targetIdList.get(i));
            String rs = HttpUtils.doPost(API_SEND_MESSAGE.concat("?receive_id_type=").concat(idType), headers, JSONObject.toJSONString(feishuMsg));
            if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.code")) && (int)JsonPath.read(rs,"$.code") == 0) {
                success.getAndIncrement();
            }
        }
        reportLog.info("Send meessage 成功发送飞书消息[{}]条 ",success.get());
    }


    /**
     * 给多个用户批量发消息
     * 给一个或多个部门的成员批量发消息
     * 只能发送给用户，无法发送给群组
     * @param cardMessage
     * @param headers
     * @param targetIdList 目标id 只按open_id发送
     */
    public void sendFeiShuBatchMessage(FeishuCardMessage cardMessage,Map<String,String> headers,List<String> targetIdList){
        //接口文档 https://open.feishu.cn/document/ukTMukTMukTM/ucDO1EjL3gTNx4yN4UTM
        Map<String,Object> params = new HashMap<>();
        params.put("msg_type","interactive");
        params.put("card",cardMessage);  //卡片消息的请求体不使用content字段，使用card字段 且不需要转义
        //content, card字段两个字段至少填一个
        //department_ids, open_ids, user_ids, union_ids 四个字段至少填一个
        params.put("open_ids",targetIdList);

        String rs = HttpUtils.doPost(API_SEND_BATCH_MESSAGE, headers, JSONObject.toJSONString(params));
        System.out.println(rs);
//        if (ObjectUtil.isNotEmpty(JsonPath.read(rs,"$.code")) && String.valueOf(JsonPath.read(rs,"$.code")).equals("0")) {
//            reportLog.info("Send meessage success 成功发送飞书消息!");
//        }else {
//            log.error("Send meessage fail! 发送飞书消息失败!");
//        }
    }

    /**
     * 构造心跳检测messageCard
     * @param serviceName
     * @param testTime
     * @param testUrl
     * @param noticeUsers
     * @param response
     * @param requestApi
     * @return
     */
    public FeishuCardMessage buildHeartCheckCard(String serviceName,String testTime,String testUrl,String noticeUsers,String response,String requestApi){
        FeishuCardMessage cardMessage = new FeishuCardMessage();
        cardMessage.setConfig(FeishuCardMessage.Config.builder().wide_screen_mode(true).build());
        FeishuCardMessage.Text text = FeishuCardMessage.Text.builder().tag("plain_text").content("\uD83D\uDCCCNewHope数科测试平台心跳检查").build();
        cardMessage.setHeader(FeishuCardMessage.Header.builder().template("green").title(text).build());

        List<FeishuCardMessage.Element> elementsAll = new ArrayList<>();

        //备注图片
        List<FeishuCardMessage.Img> Imgs = new ArrayList<>();
        FeishuCardMessage.Text alt = FeishuCardMessage.Text.builder().tag("plain_text").content("图片").build();
        Imgs.add(FeishuCardMessage.Img.builder().tag("img").img_key("img_e344c476-1e58-4492-b40d-7dcffe9d6dfg").alt(alt).build());
        Imgs.add(FeishuCardMessage.Img.builder().tag("plain_text").content("注意:出现此消息代表服务503或响应不符合期望").build());
        FeishuCardMessage.Element note = new FeishuCardMessage.Element();
        note.setTag("note");
        note.setElements(Imgs);
        elementsAll.add(note);

        //lark_md div卡片
        FeishuCardMessage.Element divUp = new FeishuCardMessage.Element();
        divUp.setTag("div");
        List<FeishuCardMessage.Field> fields = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            FeishuCardMessage.Field field = new FeishuCardMessage.Field();
            field.setIs_short(true);
            FeishuCardMessage.Text te = new FeishuCardMessage.Text();
            te.setTag("lark_md");
            switch (i) {
                case 0:
                    te.setContent("**\uD83D\uDD3A被测服务：**\n".concat(serviceName));
                    break;
                case 1:
                    te.setContent("**\uD83D\uDD50测试时间：**\n".concat(testTime));
                    break;
                case 2:
                    te.setContent("**\uD83D\uDDF3测试环境及链接：**\n".concat(testUrl));
                    break;
                case 3:
                    if (ObjectUtil.isNotEmpty(noticeUsers)) {
                        List<String> noticeUsersOpenId = new ArrayList<>();
                        String[] users = noticeUsers.split(",");
                        for (int j = 0; j < users.length; j++) {
                            String userOpenId = FeishuOpenIdEnum.getOpenId(users[j]);
                            noticeUsersOpenId.add(userOpenId);
                        }
                        te.setContent(setAtUser("**\uD83D\uDC64通知人员: **\n",noticeUsersOpenId));
                    }else {
                        te.setContent("**\uD83D\uDC64请相关人员及时处理**\n");
                    }
                    break;
                default:
                    break;
            }
            field.setText(te);
            fields.add(field);
        }
        divUp.setFields(fields);
        elementsAll.add(divUp);

        //响应体div
        FeishuCardMessage.Element divDown = new FeishuCardMessage.Element();
        divDown.setTag("div");
        divDown.setText(FeishuCardMessage.Text.builder().tag("lark_md").content("**响应信息: **\n".concat(response)).build());
        elementsAll.add(divDown);

        //按钮Button
        FeishuCardMessage.Element action = new FeishuCardMessage.Element();
        action.setTag("action");
        List<FeishuCardMessage.Button> buttonList = new ArrayList<>();
        buttonList.add(FeishuCardMessage.Button.builder().tag("button").type("primary").url(requestApi).text(FeishuCardMessage.Text.builder().tag("plain_text").content("点击再次调用服务尝试").build()).build());
        action.setActions(buttonList);
        elementsAll.add(action);

        cardMessage.setElements(elementsAll);

        return cardMessage;

    }

    public FeishuTextMessage buildTextMessage(String noticeUsers,String warningTitle,String message) {
        FeishuTextMessage textMessage = new FeishuTextMessage();
        if (ObjectUtil.isNotEmpty(noticeUsers)) {
            textMessage.setText(setAtTextUser("**\uD83D\uDC64"+warningTitle+": **\n"+message,noticeUsers));
        }else {
            textMessage.setText("**\uD83D\uDC64"+warningTitle+"**\n"+message);
        }
        return textMessage;
    }

    public List<String> splitUsers(String noticeUsers){
        if (ObjectUtil.isNotEmpty(noticeUsers)) {
            List<String> users = Arrays.asList(noticeUsers.split(","));
            return users;
        }else {
            throw new BusinessException("未设置通知用户!");
        }
    }

    public String setAtUser(String sourceStr,List<String> openIds) {
        for (String openId : openIds) {
            sourceStr = sourceStr + "<at id=" + openId + "></at>";
        }
        return sourceStr;
    }

    public String setAtTextUser(String sourceStr,String noticeUsers) {
        String[] users = noticeUsers.split(",");
        for (int j = 0; j < users.length; j++) {
            FeishuOpenIdEnum feishuUser = FeishuOpenIdEnum.getOpenIdUser(users[j]);
            sourceStr = sourceStr + "<at user_id=\"" + feishuUser.getOpenId()+"\">"+feishuUser.getRealName()+"</at>";
        }
        return sourceStr;
    }

    /**
     *
     * @param type 1-mobiles 2-emails
     * @param mobilesOrEmails
     * @param params
     */
    public void setStringParams(int type,List<String> mobilesOrEmails,StringBuilder params){
        if (1 == type) {
            for (int i = 0; i < mobilesOrEmails.size(); i++) {
                if (i == mobilesOrEmails.size()-1) {
                    params.append("mobiles=").append(mobilesOrEmails.get(i));
                }else {
                    params.append("mobiles=").append(mobilesOrEmails.get(i)).append("&");
                }
            }
        }else {
            for (int i = 0; i < mobilesOrEmails.size(); i++) {
                if (i == mobilesOrEmails.size()-1) {
                    params.append("emails=").append(mobilesOrEmails.get(i));
                }else {
                    params.append("emails=").append(mobilesOrEmails.get(i)).append("&");
                }
            }
        }

    }

    private void privateMethod(){
        System.out.println("privateMethod");
    }

}
