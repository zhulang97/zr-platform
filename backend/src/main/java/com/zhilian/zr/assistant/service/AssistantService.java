package com.zhilian.zr.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zr.assistant.dto.AssistantDsl;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssistantService {

  private final ObjectMapper objectMapper;
  private final PersonService personService;
  private final DataScopeService dataScopeService;

  public AssistantService(PersonService personService, DataScopeService dataScopeService) {
    this.objectMapper = new ObjectMapper();
    this.personService = personService;
    this.dataScopeService = dataScopeService;
  }

  public Map<String, Object> chat(String userInput, String currentPage) {
    try {
      AssistantDsl dsl = parseNaturalLanguage(userInput);
      return executeDsl(dsl);
    } catch (Exception e) {
      return Map.of(
          "action", "ANSWER_ONLY",
          "answer", "抱歉，我理解您的请求时遇到了问题。请尝试更具体地描述您的查询条件，例如：" +
                   "\"帮我查浦东新区二级肢体残疾的人\" 或 \"显示有残车补贴的人员名单\""
      );
    }
  }

  public Map<String, Object> parse(String userInput) {
    try {
      AssistantDsl dsl = parseNaturalLanguage(userInput);
      return Map.of(
          "action", "SET_FILTERS",
          "dsl", dsl
      );
    } catch (Exception e) {
      return Map.of(
          "action", "ANSWER_ONLY",
          "answer", "无法解析您的查询，请用更明确的条件描述。"
      );
    }
  }

  private AssistantDsl parseNaturalLanguage(String input) {
    AssistantDsl dsl = new AssistantDsl();
    dsl.setIntent("person_search");
    AssistantDsl.Filters filters = dsl.getFilters();

    String lower = input.toLowerCase();

    if (lower.contains("浦东") || lower.contains("pudong")) {
      filters.setDistrictIds(java.util.List.of(310115L));
    }

    if (lower.contains("肢体") || lower.contains("limb")) {
      filters.setDisabilityCategories(java.util.List.of("LIMB"));
    }

    if (lower.contains("二级") || lower.contains("2") || lower.contains("2级")) {
      filters.setDisabilityLevels(java.util.List.of("2"));
    }

    if (lower.contains("残车") || lower.contains("car")) {
      filters.setHasCar(true);
    }

    if (lower.contains("医疗") || lower.contains("medical")) {
      filters.setHasMedicalSubsidy(true);
    }

    if (lower.contains("养老") || lower.contains("pension")) {
      filters.setHasPensionSubsidy(true);
    }

    if (lower.contains("盲") || lower.contains("blind")) {
      filters.setHasBlindCard(true);
    }

    AssistantDsl.Page page = new AssistantDsl.Page();
    page.setNo(1);
    page.setSize(20);

    dsl.setFilters(filters);
    dsl.setPage(page);

    return dsl;
  }

  private Map<String, Object> executeDsl(AssistantDsl dsl) {
    if (dsl == null || dsl.getFilters() == null) {
      return Map.of("action", "ANSWER_ONLY", "answer", "无法识别查询条件");
    }

    var filters = dsl.getFilters();
    var page = dsl.getPage() != null ? dsl.getPage() : new AssistantDsl.Page();
    dsl.setPage(page);

    PersonDtos.PersonSearchRequest req = new PersonDtos.PersonSearchRequest(
        filters.getNameLike(),
        filters.getIdNo(),
        filters.getDisabilityCardNo(),
        dataScopeService.intersectDistrictIds(filters.getDistrictIds(), CurrentUser.userId(), CurrentUser.authorities()),
        filters.getStreetIds(),
        filters.getDisabilityCategories(),
        filters.getDisabilityLevels(),
        filters.getHasCar(),
        filters.getHasMedicalSubsidy(),
        filters.getHasPensionSubsidy(),
        filters.getHasBlindCard(),
        null,
        null,
        page.getNo(),
        page.getSize()
    );

    var result = personService.search(req);

    String explanation = generateExplanation(filters, result);

    return Map.of(
        "action", "SET_FILTERS",
        "dsl", dsl,
        "explanation", explanation,
        "total", result.total(),
        "data", result.items()
    );
  }

  private String generateExplanation(AssistantDsl.Filters filters, var result) {
    StringBuilder sb = new StringBuilder();
    sb.append("查询结果：共找到 ").append(result.total()).append(" 条记录。");

    if (filters.getDistrictIds() != null && !filters.getDistrictIds().isEmpty()) {
      sb.append(" 区县：已筛选。");
    }
    if (filters.getDisabilityCategories() != null && !filters.getDisabilityCategories().isEmpty()) {
      sb.append(" 残疾类别：").append(String.join(", ", filters.getDisabilityCategories())).append("。");
    }
    if (filters.getDisabilityLevels() != null && !filters.getDisabilityLevels().isEmpty()) {
      sb.append(" 残疾等级：").append(String.join(", ", filters.getDisabilityLevels())).append("级。");
    }
    if (Boolean.TRUE.equals(filters.getHasCar())) {
      sb.append(" 有残车。");
    }

    return sb.toString();
  }
}