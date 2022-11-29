package com.allen.testplatform.modules.databuilder.model.cjcy.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/9/21 13:44
 */

@Data
public class AddCjcyOrderVo {

    private Long  id;
    @NotNull(message = "批次ID不能为空")
    private Long checkBatchId;
    /**
     * 房屋编码
     */
    @NotNull(message = "房屋编码不能为空")
    private String roomCode;
    /**
     * 检查部位
     */
    @NotNull(message = "一级检查项不能为空")
    private Long ckOneId;
    private String ckOneName;

    /**
     * 检查部位
     */
    @NotNull(message = "二级检查项不能为空")
    private Long ckTwoId;
    private String ckTwoName;
    /**
     * 检查项
     */
    @NotNull(message = "检查项不能为空")
    private Long ckItemId;
    private String ckItemName;
    /**
     * 问题描述
     */
    @NotBlank(message = "问题描述不能为空")
    private String ckDescName;
    /**
     * 重大程度 0一般 1重大 2 紧急
     */
    @NotBlank(message = "重要成都不能为空")
    private String importance;

    /**
     * 通知整改
     */
    @NotNull(message = "是否通知整改不能为空")
    private Boolean isInform;
    /**
     * 补充说明
     */
    private String content;

    /**
     * 图片
     */
    private List<String> imgs;


    private String banCode;
    private String unit;
    /**
     * 创建时间
     */
    @NotNull
    private Long addTime;


    //2021-03-09 新增
    /**
     * 承建商GUID
     */
    private String providerGuid;
    /**
     * 承建商中文名
     */
    private String providerName;
    /**
     * 责任人
     */
    private Long dutyUserId;
    /**
     * 责任人Code
     */
    private String dutyUserCode;
    /**
     * 责任人中文名
     */
    private String dutyUserName;
    /**
     * 方位类型
     */
    private String bearingType;

    /**
     * 唯一标记
     */
    @NotBlank(message = "唯一标识不能为空")
    private String checkSign;
}
