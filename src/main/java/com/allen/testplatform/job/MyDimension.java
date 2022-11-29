package com.allen.testplatform.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Fan QingChuan
 * @since 2022/3/23 14:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyDimension {

    private double width;
    private double height;
}
