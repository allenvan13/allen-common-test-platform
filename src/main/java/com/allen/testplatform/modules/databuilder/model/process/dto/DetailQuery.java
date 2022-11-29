package com.allen.testplatform.modules.databuilder.model.process.dto;

import com.allen.testplatform.common.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Fan QingChuan
 * @since 2022/3/29 17:47
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetailQuery implements Serializable {
    private Integer current;
    private Integer size;
    //    城市公司 name
    private String orgName;
    //    项目name
    private String projectName;
    //    分期name
    private String stageName;
    //    工序类型 1工程，2装饰，3景观
    private Integer checkType;
    //    标段name
    private String sectionName;
    //    报验工序checkPathName
    private String checkPathName;
    //    报验区域partName
    private String partName;
    //    施工单位submitCompanyName
    private String submitCompanyName;
    //    报验人
    private String submitUser;
    //    监理单位acceptCompanyName
    private String acceptCompanyName;
    //    验收人
    private String acceptUser;
    //    抽检人
    private String checkUser;
    //    报验时间开始
    private Date submitTimeStart;
    //    报验时间结束
    private Date submitTimeEnd;
    //    验收时间开始
    private Date acceptTimeStart;
    //    验收时间结束
    private Date acceptTimeEnd;
    //    抽检时间开始
    private Date checkTimeStart;
    //    抽检时间结束
    private Date checkTimeEnd;
    ///验收状态（多选用,隔开）   0:/重新报验,1:待验收,2:待抽检,3:已关闭
    private String status;

    public void endDate() {
        this.submitTimeEnd = submitTimeEnd == null ? null : DateUtils.addDate(submitTimeEnd, 1);
        this.acceptTimeEnd = acceptTimeEnd == null ? null : DateUtils.addDate(acceptTimeEnd, 1);
        this.checkTimeEnd = checkTimeEnd == null ? null : DateUtils.addDate(checkTimeEnd, 1);
    }
}