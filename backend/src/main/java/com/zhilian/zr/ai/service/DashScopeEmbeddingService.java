package com.zhilian.zr.ai.service;

import com.alibaba.fastjson2.JSON;
import com.zhilian.zr.ai.config.DashScopeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DashScopeEmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(DashScopeEmbeddingService.class);

    private final DashScopeProperties properties;
    private final RestTemplate restTemplate;

    public DashScopeEmbeddingService(DashScopeProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public List<Float> embed(String text) {
        try {
            String url = properties.getBaseUrl() + "/services/embeddings/text-embedding/text-embedding-v2";

            Map<String, Object> request = new HashMap<>();
            request.put("model", properties.getEmbeddingModel());
            request.put("input", new String[]{text});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("output");
                if (data != null && data.containsKey("embeddings")) {
                    List<List<Float>> embeddings = (List<List<Float>>) data.get("embeddings");
                    if (!embeddings.isEmpty()) {
                        return embeddings.get(0);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error embedding text: {}", text, e);
        }
        return Collections.emptyList();
    }

    public List<List<Float>> embedBatch(List<String> texts) {
        try {
            String url = properties.getBaseUrl() + "/services/embeddings/text-embedding/text-embedding-v2";

            Map<String, Object> request = new HashMap<>();
            request.put("model", properties.getEmbeddingModel());
            request.put("input", texts);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("output");
                if (data != null && data.containsKey("embeddings")) {
                    return (List<List<Float>>) data.get("embeddings");
                }
            }
        } catch (Exception e) {
            log.error("Error embedding texts: {}", texts, e);
        }
        return Collections.emptyList();
    }
}
