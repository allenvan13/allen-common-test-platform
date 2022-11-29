package com.allen.testplatform.feign.vo;

import com.allen.testplatform.modules.databuilder.model.process.entity.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author hejunlong
 * 2021-8-9 14:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProcessDetailDto extends ProcessDetail {
    private static final long serialVersionUID = 4633341642348363878L;

    private Long updateTime;
    /**
     * 节点
     */
    private List<CheckFlowVo> checkFlows;

    /**
     * 验收点列表
     */
    private List<ProcessDetailCheckPoint> detailCheckPoints;

    /**
     * 流程信息
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CheckFlowVo extends ProcessDetailCheckFlow {

        private static final long serialVersionUID = 3085891453242861026L;

        /**
         * 验收人
         */
        private List<ProcessDetailHandler> acceptor;

        /**
         * 共同验收人
         */
        private List<ProcessDetailHandler> commonAcceptor;

        /**
         * 抄送人
         */
        private List<ProcessDetailHandler> carbonCopy;

        /**
         * 当前流程验收点信息
         */
        private List<ProcessDetailPoint> detailsPoints;
    }
}
