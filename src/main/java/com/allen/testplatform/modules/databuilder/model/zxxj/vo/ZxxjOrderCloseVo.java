package com.allen.testplatform.modules.databuilder.model.zxxj.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author ht
 * @since 2021-05-24
 */
@Data
public class ZxxjOrderCloseVo implements Serializable {

    private static final long serialVersionUID = -9061806954482165962L;

    /** 工单id */
    @NotEmpty(message = "工单id不能为空")
    private Long id;
    /** 问题图片地址 */
    private List<String> imageUrls;
    /** 内容描述 */
    private String content;
}
