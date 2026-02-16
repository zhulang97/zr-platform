package com.zhilian.zr.policy.service;

import com.alibaba.fastjson2.JSON;
import com.zhilian.zr.ai.config.DashScopeProperties;
import com.zhilian.zr.policy.dto.ConditionDiff;
import com.zhilian.zr.policy.dto.PolicyConditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyAnalysisService {
    
    private final DashScopeProperties dashScopeProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 分段大小
    private static final int SEGMENT_SIZE = 4000;
    // 重叠大小
    private static final int OVERLAP_SIZE = 500;
    
    /**
     * 分析政策文本（支持长文本分段）
     */
    public PolicyConditions analyzePolicy(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("政策内容不能为空");
        }
        
        // 如果内容较短，直接分析
        if (content.length() <= SEGMENT_SIZE) {
            return analyzeSegment(content, null);
        }
        
        // 长文本分段分析
        return analyzeLongPolicy(content);
    }
    
    /**
     * 长政策分段分析（串行）
     */
    private PolicyConditions analyzeLongPolicy(String content) {
        List<String> segments = splitIntoSegments(content);
        log.info("Policy content split into {} segments", segments.size());
        
        List<PolicyConditions> segmentConditions = new ArrayList<>();
        List<String> previousSummaries = new ArrayList<>();
        
        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i);
            log.info("Analyzing segment {}/{}", i + 1, segments.size());
            
            // 构建带上下文的prompt
            String context = buildContext(previousSummaries);
            PolicyConditions conditions = analyzeSegment(segment, context);
            
            segmentConditions.add(conditions);
            previousSummaries.add(extractSummary(conditions));
        }
        
        // 合并所有段落的条件
        return mergeConditions(segmentConditions);
    }
    
    /**
     * 分析单段文本
     */
    private PolicyConditions analyzeSegment(String segment, String context) {
        String prompt = buildAnalysisPrompt(segment, context);
        
        try {
            String url = dashScopeProperties.getBaseUrl() + "/services/aigc/text-generation/generation";
            
            Map<String, Object> request = new HashMap<>();
            request.put("model", dashScopeProperties.getModel());
            request.put("messages", Arrays.asList(
                Map.of("role", "system", "content", prompt)
            ));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + dashScopeProperties.getApiKey());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> output = (Map<String, Object>) response.getBody().get("output");
                if (output != null && output.containsKey("text")) {
                    String aiResponse = (String) output.get("text");
                    return parseAIResponse(aiResponse);
                }
            }
            
            throw new RuntimeException("AI分析返回无效响应");
        } catch (Exception e) {
            log.error("Error analyzing policy segment", e);
            throw new RuntimeException("政策分析失败", e);
        }
    }
    
    /**
     * 构建分析Prompt
     */
    private String buildAnalysisPrompt(String content, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个残疾人救助政策分析专家。请分析以下政策文本，提取享受补贴的条件。\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("前文摘要：").append(context).append("\n\n");
        }
        
        prompt.append("政策文本：\n").append(content).append("\n\n");
        
        prompt.append("请提取以下条件（JSON格式）：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"政策标题\",\n");
        prompt.append("  \"conditions\": {\n");
        prompt.append("    \"districtIds\": [\"区县代码\"],\n");
        prompt.append("    \"disabilityCategories\": [\"LIMB\", \"VISION\", \"HEARING\", \"SPEECH\", \"INTELLECTUAL\", \"MENTAL\"],\n");
        prompt.append("    \"disabilityLevels\": [\"1\", \"2\", \"3\", \"4\"],\n");
        prompt.append("    \"hasCar\": true/false/null,\n");
        prompt.append("    \"hasMedicalSubsidy\": true/false/null,\n");
        prompt.append("    \"hasPensionSubsidy\": true/false/null,\n");
        prompt.append("    \"hasBlindCard\": true/false/null,\n");
        prompt.append("    \"ageMin\": 18,\n");
        prompt.append("    \"ageMax\": 60\n");
        prompt.append("  },\n");
        prompt.append("  \"explanation\": \"用中文解释这些条件\"\n");
        prompt.append("}\n\n");
        
        prompt.append("注意：\n");
        prompt.append("1. 如果政策中未提及某项条件，设为null\n");
        prompt.append("2. 区县代码：浦东新区=310115，黄浦区=310101，静安区=310106，徐汇区=310104，长宁区=310105，普陀区=310107，虹口区=310109，杨浦区=310110，闵行区=310112，宝山区=310113，嘉定区=310114，浦东新区=310115，金山区=310116，松江区=310117，青浦区=310118，奉贤区=310120，崇明区=310151\n");
        prompt.append("3. 残疾类别代码：LIMB(肢体), VISION(视力), HEARING(听力), SPEECH(言语), INTELLECTUAL(智力), MENTAL(精神)\n");
        prompt.append("4. 如果无法确定某个条件，不要猜测，设为null\n");
        prompt.append("5. 只返回JSON格式，不要其他文字");
        
        return prompt.toString();
    }
    
    /**
     * 将文本分段
     */
    private List<String> splitIntoSegments(String content) {
        List<String> segments = new ArrayList<>();
        int start = 0;
        
        while (start < content.length()) {
            int end = Math.min(start + SEGMENT_SIZE, content.length());
            
            // 尽量在句子或段落边界分割
            if (end < content.length()) {
                int lastPeriod = content.lastIndexOf("。", end);
                int lastNewline = content.lastIndexOf("\n", end);
                int splitPoint = Math.max(lastPeriod, lastNewline);
                if (splitPoint > start) {
                    end = splitPoint + 1;
                }
            }
            
            segments.add(content.substring(start, end));
            start = end - OVERLAP_SIZE; // 重叠部分
        }
        
        return segments;
    }
    
    /**
     * 构建上下文
     */
    private String buildContext(List<String> previousSummaries) {
        if (previousSummaries.isEmpty()) {
            return null;
        }
        
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < previousSummaries.size(); i++) {
            context.append("段落").append(i + 1).append("：").append(previousSummaries.get(i)).append("\n");
        }
        return context.toString();
    }
    
    /**
     * 提取摘要
     */
    private String extractSummary(PolicyConditions conditions) {
        StringBuilder summary = new StringBuilder();
        if (conditions.getTitle() != null) {
            summary.append("政策：").append(conditions.getTitle()).append("；");
        }
        if (conditions.getDistrictIds() != null && !conditions.getDistrictIds().isEmpty()) {
            summary.append("涉及").append(conditions.getDistrictIds().size()).append("个区县；");
        }
        if (conditions.getDisabilityCategories() != null && !conditions.getDisabilityCategories().isEmpty()) {
            summary.append("残疾类别：").append(String.join(",", conditions.getDisabilityCategories())).append("；");
        }
        return summary.toString();
    }
    
    /**
     * 解析AI响应
     */
    private PolicyConditions parseAIResponse(String aiResponse) {
        try {
            // 提取JSON部分
            int jsonStart = aiResponse.indexOf("{");
            int jsonEnd = aiResponse.lastIndexOf("}") + 1;
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw new RuntimeException("AI响应中未找到JSON");
            }
            String json = aiResponse.substring(jsonStart, jsonEnd);
            
            Map<String, Object> result = JSON.parseObject(json);
            PolicyConditions conditions = new PolicyConditions();
            
            // 解析标题
            if (result.containsKey("title")) {
                conditions.setTitle((String) result.get("title"));
            }
            
            // 解析条件
            if (result.containsKey("conditions")) {
                Map<String, Object> condMap = (Map<String, Object>) result.get("conditions");
                
                if (condMap.containsKey("districtIds")) {
                    List<Object> ids = (List<Object>) condMap.get("districtIds");
                    List<Long> districtIds = new ArrayList<>();
                    for (Object id : ids) {
                        if (id instanceof Number) {
                            districtIds.add(((Number) id).longValue());
                        } else if (id instanceof String) {
                            districtIds.add(Long.parseLong((String) id));
                        }
                    }
                    conditions.setDistrictIds(districtIds);
                }
                
                if (condMap.containsKey("disabilityCategories")) {
                    conditions.setDisabilityCategories((List<String>) condMap.get("disabilityCategories"));
                }
                
                if (condMap.containsKey("disabilityLevels")) {
                    conditions.setDisabilityLevels((List<String>) condMap.get("disabilityLevels"));
                }
                
                if (condMap.containsKey("hasCar")) {
                    conditions.setHasCar((Boolean) condMap.get("hasCar"));
                }
                
                if (condMap.containsKey("hasMedicalSubsidy")) {
                    conditions.setHasMedicalSubsidy((Boolean) condMap.get("hasMedicalSubsidy"));
                }
                
                if (condMap.containsKey("hasPensionSubsidy")) {
                    conditions.setHasPensionSubsidy((Boolean) condMap.get("hasPensionSubsidy"));
                }
                
                if (condMap.containsKey("hasBlindCard")) {
                    conditions.setHasBlindCard((Boolean) condMap.get("hasBlindCard"));
                }
                
                if (condMap.containsKey("ageMin")) {
                    conditions.setAgeMin(((Number) condMap.get("ageMin")).intValue());
                }
                
                if (condMap.containsKey("ageMax")) {
                    conditions.setAgeMax(((Number) condMap.get("ageMax")).intValue());
                }
            }
            
            // 解析解释
            if (result.containsKey("explanation")) {
                conditions.setExplanation((String) result.get("explanation"));
            }
            
            return conditions;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponse, e);
            throw new RuntimeException("解析AI响应失败", e);
        }
    }
    
    /**
     * 合并多个段落的条件
     */
    private PolicyConditions mergeConditions(List<PolicyConditions> conditionsList) {
        PolicyConditions merged = new PolicyConditions();
        
        // 使用第一个段落的标题
        if (!conditionsList.isEmpty() && conditionsList.get(0).getTitle() != null) {
            merged.setTitle(conditionsList.get(0).getTitle());
        }
        
        // 合并所有条件（取并集）
        Set<Long> districtIds = new HashSet<>();
        Set<String> categories = new HashSet<>();
        Set<String> levels = new HashSet<>();
        StringBuilder explanation = new StringBuilder();
        
        for (PolicyConditions cond : conditionsList) {
            if (cond.getDistrictIds() != null) {
                districtIds.addAll(cond.getDistrictIds());
            }
            if (cond.getDisabilityCategories() != null) {
                categories.addAll(cond.getDisabilityCategories());
            }
            if (cond.getDisabilityLevels() != null) {
                levels.addAll(cond.getDisabilityLevels());
            }
            if (cond.getExplanation() != null) {
                explanation.append(cond.getExplanation()).append("\n");
            }
            
            // 对于布尔值，如果有任何一个段落要求，就设为true
            if (Boolean.TRUE.equals(cond.getHasCar())) {
                merged.setHasCar(true);
            }
            if (Boolean.TRUE.equals(cond.getHasMedicalSubsidy())) {
                merged.setHasMedicalSubsidy(true);
            }
            if (Boolean.TRUE.equals(cond.getHasPensionSubsidy())) {
                merged.setHasPensionSubsidy(true);
            }
            if (Boolean.TRUE.equals(cond.getHasBlindCard())) {
                merged.setHasBlindCard(true);
            }
        }
        
        merged.setDistrictIds(new ArrayList<>(districtIds));
        merged.setDisabilityCategories(new ArrayList<>(categories));
        merged.setDisabilityLevels(new ArrayList<>(levels));
        merged.setExplanation(explanation.toString().trim());
        
        return merged;
    }
    
    /**
     * 计算版本差异
     */
    public List<ConditionDiff> calculateDiff(PolicyConditions oldVersion, PolicyConditions newVersion) {
        List<ConditionDiff> diffs = new ArrayList<>();
        
        // 对比各个字段
        compareField(diffs, "districtIds", "户籍区县", oldVersion.getDistrictIds(), newVersion.getDistrictIds());
        compareField(diffs, "disabilityCategories", "残疾类别", oldVersion.getDisabilityCategories(), newVersion.getDisabilityCategories());
        compareField(diffs, "disabilityLevels", "残疾等级", oldVersion.getDisabilityLevels(), newVersion.getDisabilityLevels());
        compareField(diffs, "hasCar", "残车补贴", oldVersion.getHasCar(), newVersion.getHasCar());
        compareField(diffs, "hasMedicalSubsidy", "医疗补贴", oldVersion.getHasMedicalSubsidy(), newVersion.getHasMedicalSubsidy());
        compareField(diffs, "hasPensionSubsidy", "养老补贴", oldVersion.getHasPensionSubsidy(), newVersion.getHasPensionSubsidy());
        compareField(diffs, "hasBlindCard", "盲人证", oldVersion.getHasBlindCard(), newVersion.getHasBlindCard());
        compareField(diffs, "ageMin", "最小年龄", oldVersion.getAgeMin(), newVersion.getAgeMin());
        compareField(diffs, "ageMax", "最大年龄", oldVersion.getAgeMax(), newVersion.getAgeMax());
        
        return diffs;
    }
    
    private void compareField(List<ConditionDiff> diffs, String field, String fieldName, Object oldVal, Object newVal) {
        if (Objects.equals(oldVal, newVal)) {
            return;
        }
        
        ConditionDiff diff = new ConditionDiff();
        diff.setField(field);
        diff.setFieldName(fieldName);
        diff.setOldValue(oldVal);
        diff.setNewValue(newVal);
        
        if (oldVal == null && newVal != null) {
            diff.setDiffType("ADDED");
        } else if (oldVal != null && newVal == null) {
            diff.setDiffType("REMOVED");
        } else {
            diff.setDiffType("MODIFIED");
        }
        
        diffs.add(diff);
    }
}
