package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 问题工单
 *
 * @author Lu
 * @since 2021-05-24 16:17:47
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZxxjProblemVo {
    /**
     * 楼栋code
     */
    @NotBlank(message = "楼栋信息不能为空")
    private String banCode;
    /**
     * 单元
     */
    private String unit;
    /**
     * 楼层
     */
    private String floor;
    /**
     * 房屋code
     */
    private String roomCode;
    /**
     * 批次id
     */
    @NotNull(message = "批次id不能为空")
    private Long batchId;
    /**
     * 模板id
     */
    @NotNull(message = "模板id不能为空")
    private Long templateId;
    /**
     * 问题检查项
     */
    @NotNull(message = "问题检查项不能为空")
    private Long checkItemId;
    /**
     * 问题图片
     */
    private String[] imageUrls;
    /**
     * 重要程度(1-一般,2-重大3-紧急)
     */
    private Integer importance;
    /**
     * 整改时长
     */
    private Integer writeOffDays;
    /**
     * 整改人
     */
    private Processor processor;
    /**
     * 复验人
     */
    private Processor reProcessor;
    /**
     * 抄送人
     */
    private List<Processor> copyProcessors;
    /**
     * 责任单位
     */
    private String providerName;
    private String providerGuid;
    /**
     * 补充说明
     */
    private String content;
    /**
     * 是否推送整改
     */
    @NotNull(message = "是否推送整改不能为空")
    private Boolean ifPush;

    private ProblemInHouse problemInHouse;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Processor {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProblemInHouse {
        private Long houseTypeId;
        private String checkImageUrl;
        private Double pointX;
        private Double pointY;
    }
}
