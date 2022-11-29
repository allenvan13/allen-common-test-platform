package com.allen.testplatform.modules.databuilder.model.pile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Fan QingChuan
 * @since 2022/3/14 10:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssertDetailPoint {
    private Long pointId;
    private String title;
    private String remark;
    private List<String> picture;
}
