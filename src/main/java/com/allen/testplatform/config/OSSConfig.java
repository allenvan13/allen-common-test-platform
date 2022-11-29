package com.allen.testplatform.config;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.InputStream;

@Configuration
@Data
public class OSSConfig {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKey;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucketName;

    @Resource
    private OSSClient ossClient;

    public String putObject(String ossKey, InputStream upInputStream) {
        ossClient.deleteObject(getBucketName(), ossKey);
        ossClient.putObject(getBucketName(), ossKey, upInputStream);
        String ossUrl = "http://" + getBucketName() + "." + getEndpoint() + "/" + ossKey;
        return ossUrl;
    }

    public OSSObject getObject(String ossKey) {
        return ossClient.getObject(getBucketName(), ossKey);
    }
}