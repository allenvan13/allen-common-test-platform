package com.allen.testplatform.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Fan QingChuan
 * @since 2022/5/27 9:43
 */

@Configuration
@Data
public class CurrentEnvironmentConfig {

    @Value("${http.environment.host.open}")
    private String OPEN_HOST;

    @Value("${http.environment.host}")
    private String HOST;

    @Value("${http.environment.env}")
    private String ENV;

    @Value("${zxxj.score.min.value}")
    public static double MIN_VALUE;              //测量打分值最小值

    @Value("${zxxj.score.max.value}")
    public static double MAX_VALUE;

}
