package com.zhilian.zr.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class DashScopeProperties {
    private String apiKey;
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";
    private String model = "qwen-turbo";
    private String embeddingModel = "text-embedding-v2";
    private Integer dimensions = 1536;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getDimensions() {
        return dimensions;
    }

    public void setDimensions(Integer dimensions) {
        this.dimensions = dimensions;
    }
}
