package com.allen.testplatform.modules.databuilder.model.zxxj.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专项巡检问题人员表
 *
 * @author yangchao
 * @email
 * @date 2021-05-07 14:52:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("zxxj_order_processor")
public class ZxxjOrderProcessor extends Model<ZxxjOrderProcessor> {
    private static final long serialVersionUID = 1945547147996431143L;

    /**
     * 工单id
     */
    private Long orderId;
    /**
     * 00:未受理  01:已受理 10:已分配 11:处理中 21:处理完成 90:无效工单 91:非正常关闭 92:正常关闭  99:工单完成
     */
    private String status;
    /**
     * 处理人ID
     */
    private Long processorId;
    /**
     * 处理人姓名
     */
    private String processorName;
    /**
     * 处理人用户名
     */
    private String processorCode;
    /**
     * 人员类型 1-整改人，2-复验人,3-抄送人
     */
    private Integer roleType;
}
