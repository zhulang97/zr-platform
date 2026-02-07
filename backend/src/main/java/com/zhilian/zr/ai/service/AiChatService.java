package com.zhilian.zr.ai.service;

import com.zhilian.zr.ai.config.DashScopeProperties;
import com.zhilian.zr.ai.entity.VectorDoc;
import com.zhilian.zr.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    private final DashScopeProperties properties;
    private final VectorSearchService vectorSearchService;
    private final RestTemplate restTemplate;

    public AiChatService(DashScopeProperties properties, VectorSearchService vectorSearchService) {
        this.properties = properties;
        this.vectorSearchService = vectorSearchService;
        this.restTemplate = new RestTemplate();
    }

    public String chat(String query, Long personId) {
        List<VectorDoc> similarDocs = vectorSearchService.searchSimilar(query, 5);

        StringBuilder context = new StringBuilder();
        context.append("以下是一些相关信息：\n");
        for (int i = 0; i < similarDocs.size(); i++) {
            VectorDoc doc = similarDocs.get(i);
            context.append(String.format("%d. [%s] %s (相似度: %.2f)\n",
                i + 1, doc.getType(), doc.getContent(), doc.getScore()));
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
            "你是残疾人救助平台的智能助手。请基于提供的上下文信息回答用户问题。" +
            "如果上下文中没有相关信息，请诚实告知用户你不知道答案。" +
            "回答要简洁、准确、友好。"));
        messages.add(Map.of("role", "user", "content", query));

        if (!similarDocs.isEmpty()) {
            messages.add(1, Map.of("role", "system", "content", context.toString()));
        }

        try {
            String url = properties.getBaseUrl() + "/services/aigc/text-generation/generation";

            Map<String, Object> request = new HashMap<>();
            request.put("model", properties.getModel());
            request.put("messages", messages);

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
                Map<String, Object> output = (Map<String, Object>) response.getBody().get("output");
                if (output != null && output.containsKey("text")) {
                    String aiResponse = (String) output.get("text");
                    log.info("AI chat response generated for query: {}", query);
                    return aiResponse;
                }
            }

            log.error("Failed to get AI response for query: {}", query);
            return "抱歉，我暂时无法回答这个问题。";
        } catch (Exception e) {
            log.error("Error in AI chat", e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
    }

    public String chatWithPerson(String query, Long personId) {
        if (personId == null) {
            return chat(query, null);
        }

        List<VectorDoc> similarCases = vectorSearchService.searchByPerson(query, personId, 3);

        StringBuilder context = new StringBuilder();
        context.append("以下是该人员相关的异常案例：\n");
        for (int i = 0; i < similarCases.size(); i++) {
            VectorDoc doc = similarCases.get(i);
            context.append(String.format("%d. %s (相似度: %.2f)\n",
                i + 1, doc.getContent(), doc.getScore()));
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
            "你是残疾人救助平台的智能助手。用户正在查看一个特定人员的信息。" +
            "请基于提供的该人员历史案例回答用户问题。" +
            "如果历史案例中没有相关信息，请基于一般知识回答。" +
            "回答要简洁、准确、友好。"));
        messages.add(Map.of("role", "user", "content", query));

        if (!similarCases.isEmpty()) {
            messages.add(1, Map.of("role", "system", "content", context.toString()));
        }

        try {
            String url = properties.getBaseUrl() + "/services/aigc/text-generation/generation";

            Map<String, Object> request = new HashMap<>();
            request.put("model", properties.getModel());
            request.put("messages", messages);

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
                Map<String, Object> output = (Map<String, Object>) response.getBody().get("output");
                if (output != null && output.containsKey("text")) {
                    String aiResponse = (String) output.get("text");
                    log.info("AI chat response generated for person: {}, query: {}", personId, query);
                    return aiResponse;
                }
            }

            log.error("Failed to get AI response for person: {}, query: {}", personId, query);
            return "抱歉，我暂时无法回答这个问题。";
        } catch (Exception e) {
            log.error("Error in AI chat with person", e);
            return "抱歉，服务暂时不可用，请稍后再试。";
        }
    }
}
