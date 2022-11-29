
package com.allen.testplatform;

import com.allen.testplatform.config.SpringContextConfig;
import cn.nhdc.common.autoconfig.ConfigRefresher;
import cn.nhdc.common.autoconfig.WebMvcConfig;
import cn.nhdc.common.config.RedisCacheConfig;
import cn.nhdc.common.web.advice.GlobalControllerAdvice;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 新希望项目 - 测试平台
 *
 * @author lpeng
 * @version 1.0.0
 * @since 2018/12/05 17:32
 */
@EnableScheduling
@EnableEurekaClient
@EnableAsync
@EnableFeignClients(basePackages = "com.allen.testplatform.feign")
@ComponentScan(basePackages = {"com.allen.testplatform", "cn.nhdc.common.service", "cn.nhdc.common.feignclient"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class, MongoAutoConfiguration.class})
@Import({RedisCacheConfig.class, WebMvcConfig.class, ConfigRefresher.class, SpringContextConfig.class, GlobalControllerAdvice.class})
public class TestPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestPlatformApplication.class, args);
    }

}
