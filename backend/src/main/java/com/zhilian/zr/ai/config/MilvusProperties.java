package com.zhilian.zr.ai.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "milvus")
public class MilvusProperties {
    private String host = "localhost";
    private Integer port = 19530;
    private String token = "";
    private String database = "zr_platform";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Bean
    public MilvusServiceClient milvusClient(MilvusProperties properties) {
        ConnectParam connectParam = ConnectParam.newBuilder()
            .withHost(properties.getHost())
            .withPort(properties.getPort())
            .withToken(properties.getToken())
            .withDatabaseName(properties.getDatabase())
            .build();
        return new MilvusServiceClient(connectParam);
    }
}
