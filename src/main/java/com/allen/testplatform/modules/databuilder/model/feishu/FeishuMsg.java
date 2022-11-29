package com.allen.testplatform.modules.databuilder.model.feishu;

import lombok.Data;

/**
 * @author Fan QingChuan
 * 文档地址: https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/im-v1/message/create_json
 */
@Data
public class FeishuMsg {

    /**
     * 依据receive_id_type的值，填写对应的消息接收者id 示例值："ou_7d8a6e6df7621556ce0d21922b676706ccs"
     */
    private String receive_id;
    /**
     * 消息内容，json结构序列化后的字符串。不同msg_type对应不同内容。消息类型
     * 包括：text、post、image、file、audio、media、sticker、interactive、share_chat、share_user
     *
     */
    private String content;
    /**
     * 消息类型 包括：text、post、image、file、audio、media、sticker、interactive、share_chat、share_user
     */
    private String msg_type;
    /**
     * 消息接收者id类型（参数拼接在URL里） open_id/user_id/union_id/email/chat_id
     */
//    private String receive_id_type;

}
