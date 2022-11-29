package com.allen.testplatform.modules.databuilder.model.fhcy.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class FhcyProblemVo implements Serializable {

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
    @NotNull(message = "检查部位不能为空")
    private Long checkPartId;
    private String checkPartName;
    /**
     * 检查项
     */
    @NotNull(message = "检查项不能为空")
    private Long checkItemId;
    private String checkItemName;
    /**
     * 问题描述
     */
    @NotNull(message = "问题描述不能为空")
    private Long checkDescId;
    private String checkDescName;
    /**
     * 重大程度 0一般 1重大 2 紧急
     */
    @NotNull(message = "重要程度不能为空")
    private String importance;
    /**
     * 补充说明
     */
    private String notes;

    /**
     * 图片
     */
    private List<String> imgs;

    /**
     * 分类
     */
    @NotNull
    private String category;

    private Double pointX;
    private Double pointY;
    @NotNull(message = "户型图不能为空")
    private Long houseTypeId;
    /**
     * 户型图
     */
    private String checkImageUrl;

    private String banCode;
    private String unit;
    /**
     * 创建时间
     */
    @NotNull
    private long addTime;

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
     * 部位id
     */
    private Long projectSiteId;
    /**
     * 部位名称
     */
    private String projectSiteName;

    /**
     * 唯一标记
     */
    private String checkSign;


}
