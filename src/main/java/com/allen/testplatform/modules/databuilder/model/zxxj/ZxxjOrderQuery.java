package com.allen.testplatform.modules.databuilder.model.zxxj;

import com.allen.testplatform.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZxxjOrderQuery implements Serializable {

    //    城市公司 name
    private String orgName;
    //    项目name
    private String projectName;
    //    分期code
    private String stageCode;
    //
    private String banName;
    private String unit;
    private String floor;
    private String roomName;
    private String providerName;
    private String lastCheckName;
    private String creatorName;

    private Long batchId;

    private Date submitTimeStart;
    //    报验时间结束
    private Date submitTimeEnd;
    //    验收时间开始
    private Date updateTimeStart;
    //    验收时间结束
    private Date updateTimeEnd;
    ///验收状态（多选用,隔开）  问题状态 ZX01-已提交 ZX02-待整改 ZX03-待复验 ZX04-重新整改 ZX05-正常关闭 ZX06-非正常关闭
    private String status;

    public void endDate() {
        this.submitTimeEnd = submitTimeEnd == null ? null : DateUtils.addDate(submitTimeEnd, 1);
        this.updateTimeEnd = updateTimeEnd == null ? null : DateUtils.addDate(updateTimeEnd, 1);
    }
}