package com.allen.testplatform.modules.databuilder.model.feishu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Fan QingChuan
 * @since 2022/4/24 14:08
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeishuTextMessage {
    private String text;
}
