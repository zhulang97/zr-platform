package com.zhilian.zr.assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zr.assistant.dto.AssistantDsl;
import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      
      // 根据意图执行不同操作
      String intent = dsl.getIntent();
      String action = dsl.getAction();
      
      switch (intent) {
        case "person_search":
          return executePersonSearch(dsl);
        case "anomaly_search":
          return executeAnomalySearch(dsl);
        case "navigate":
          return executeNavigation(dsl);
        case "refresh":
          return executeRefresh(dsl, currentPage);
        default:
          return executePersonSearch(dsl);
      }
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
          "action", dsl.getAction() != null ? dsl.getAction() : "SET_FILTERS",
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
    AssistantDsl.Filters filters = new AssistantDsl.Filters();
    String lower = input.toLowerCase();
    
    // 检测意图
    if (lower.contains("异常") || lower.contains("anomaly")) {
      dsl.setIntent("anomaly_search");
      dsl.setAction("NAVIGATE_AND_FILTER");
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("anomaly");
    } else if (lower.contains("跳转到") || lower.contains("打开") || lower.contains("查看")) {
      dsl.setIntent("navigate");
      dsl.setAction("NAVIGATE");
      dsl.setNavigation(parseNavigation(input));
    } else if (lower.contains("刷新") || lower.contains("refresh")) {
      dsl.setIntent("refresh");
      dsl.setAction("REFRESH");
    } else {
      dsl.setIntent("person_search");
      dsl.setAction("NAVIGATE_AND_FILTER");
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("query");
    }
    
    // 检测人员ID（用于查看详情）
    Pattern personIdPattern = Pattern.compile("(?:person|人员|id)[：:]?\\s*(\\d+)");
    Matcher personIdMatcher = personIdPattern.matcher(lower);
    if (personIdMatcher.find()) {
      Long personId = Long.parseLong(personIdMatcher.group(1));
      dsl.setNavigation(new AssistantDsl.Navigation());
      dsl.getNavigation().setPath("/person/" + personId);
      dsl.setAction("NAVIGATE");
    }
    
    // 检测姓名
    Pattern namePattern = Pattern.compile("(?:name|姓名)[：:]?\\s*([^\\s,，。]+)");
    Matcher nameMatcher = namePattern.matcher(lower);
    if (nameMatcher.find()) {
      filters.setNameLike(nameMatcher.group(1));
    }
    
    // 检测身份证号
    Pattern idNoPattern = Pattern.compile("\\b\\d{17}[\\dXx]\\b");
    Matcher idNoMatcher = idNoPattern.matcher(input);
    if (idNoMatcher.find()) {
      filters.setIdNo(idNoMatcher.group());
    }

    // 检测区县
    if (lower.contains("浦东") || lower.contains("pudong")) {
      filters.setDistrictIds(List.of(310115L));
    }

    // 检测残疾类别
    if (lower.contains("肢体") || lower.contains("limb")) {
      filters.setDisabilityCategories(List.of("LIMB"));
    } else if (lower.contains("视力") || lower.contains("eye") || lower.contains("盲")) {
      filters.setDisabilityCategories(List.of("VISION"));
    } else if (lower.contains("听力") || lower.contains("hearing")) {
      filters.setDisabilityCategories(List.of("HEARING"));
    } else if (lower.contains("言语") || lower.contains("speech")) {
      filters.setDisabilityCategories(List.of("SPEECH"));
    } else if (lower.contains("智力") || lower.contains("intellectual")) {
      filters.setDisabilityCategories(List.of("INTELLECTUAL"));
    } else if (lower.contains("精神") || lower.contains("mental")) {
      filters.setDisabilityCategories(List.of("MENTAL"));
    }

    // 检测残疾等级
    if (lower.contains("一级") || lower.contains("1级") || lower.contains("level 1")) {
      filters.setDisabilityLevels(List.of("1"));
    } else if (lower.contains("二级") || lower.contains("2级") || lower.contains("level 2")) {
      filters.setDisabilityLevels(List.of("2"));
    } else if (lower.contains("三级") || lower.contains("3级") || lower.contains("level 3")) {
      filters.setDisabilityLevels(List.of("3"));
    } else if (lower.contains("四级") || lower.contains("4级") || lower.contains("level 4")) {
      filters.setDisabilityLevels(List.of("4"));
    }

    // 检测补贴类型
    if (lower.contains("残车") || lower.contains("car")) {
      filters.setHasCar(true);
    }
    if (lower.contains("医疗") || lower.contains("medical")) {
      filters.setHasMedicalSubsidy(true);
    }
    if (lower.contains("养老") || lower.contains("pension")) {
      filters.setHasPensionSubsidy(true);
    }
    if (lower.contains("盲人证") || lower.contains("blind card")) {
      filters.setHasBlindCard(true);
    }
    
    // 检测异常类型
    if (lower.contains("人车分离") || lower.contains("person car separation")) {
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("anomaly");
      filters.setAnomalyType("PERSON_CAR_SEPARATION");
    } else if (lower.contains("无证补贴") || lower.contains("no card subsidy")) {
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("anomaly");
      filters.setAnomalyType("NO_CARD_SUBSIDY");
    } else if (lower.contains("注销") || lower.contains("cancelled")) {
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("anomaly");
      filters.setAnomalyType("CANCELLED_CARD_SUBSIDY");
    } else if (lower.contains("重复") || lower.contains("duplicate")) {
      dsl.setTargetPage(new AssistantDsl.TargetPage());
      dsl.getTargetPage().setPage("anomaly");
      filters.setAnomalyType("DUPLICATE_SUBSIDY");
    }

    AssistantDsl.Page page = new AssistantDsl.Page();
    page.setNo(1);
    page.setSize(20);

    dsl.setFilters(filters);
    dsl.setPage(page);

    return dsl;
  }

  private AssistantDsl.Navigation parseNavigation(String input) {
    AssistantDsl.Navigation nav = new AssistantDsl.Navigation();
    String lower = input.toLowerCase();

    if (lower.contains("首页") || lower.contains("home") || lower.contains("查询")) {
      nav.setPath("/home");
    } else if (lower.contains("统计") || lower.contains("stats")) {
      nav.setPath("/stats");
    } else if (lower.contains("异常") || lower.contains("anomaly")) {
      nav.setPath("/anomaly");
    } else if (lower.contains("系统") || lower.contains("system")) {
      nav.setPath("/sys");
    } else if (lower.contains("ai") || lower.contains("智能助手")) {
      nav.setPath("/ai");
    } else {
      nav.setPath("/home");
    }

    return nav;
  }

  private Map<String, Object> executePersonSearch(AssistantDsl dsl) {
    if (dsl == null || dsl.getFilters() == null) {
      return Map.of("action", "ANSWER_ONLY", "answer", "无法识别查询条件");
    }

    var filters = dsl.getFilters();
    var page = dsl.getPage() != null ? dsl.getPage() : new AssistantDsl.Page();
    dsl.setPage(page);

    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    List<Long> districtIds = DataScopeService.intersectDistrictIds(filters.getDistrictIds(), scope);

    PersonDtos.PersonSearchRequest req = new PersonDtos.PersonSearchRequest(
        filters.getNameLike(),
        filters.getIdNo(),
        filters.getDisabilityCardNo(),
        districtIds,
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
    String action = dsl.getAction() != null ? dsl.getAction() : "SET_FILTERS";

    return Map.of(
        "action", action,
        "dsl", dsl,
        "explanation", explanation,
        "total", result.total(),
        "data", result.items()
    );
  }

  private Map<String, Object> executeAnomalySearch(AssistantDsl dsl) {
    String explanation = "正在为您跳转到异常管理页面";
    if (dsl.getFilters() != null && dsl.getFilters().getAnomalyType() != null) {
      explanation += "，筛选类型：" + dsl.getFilters().getAnomalyType();
    }
    
    return Map.of(
        "action", "NAVIGATE_AND_FILTER",
        "dsl", dsl,
        "explanation", explanation,
        "targetPage", Map.of("page", "anomaly")
    );
  }

  private Map<String, Object> executeNavigation(AssistantDsl dsl) {
    String explanation = "正在为您跳转到指定页面";
    if (dsl.getNavigation() != null) {
      explanation = "正在为您跳转到：" + dsl.getNavigation().getPath();
    }
    
    return Map.of(
        "action", "NAVIGATE",
        "dsl", dsl,
        "explanation", explanation,
        "navigation", dsl.getNavigation()
    );
  }

  private Map<String, Object> executeRefresh(AssistantDsl dsl, String currentPage) {
    return Map.of(
        "action", "REFRESH",
        "dsl", dsl,
        "explanation", "正在刷新当前页面数据",
        "targetPage", Map.of("page", currentPage)
    );
  }

  private Map<String, Object> executeDsl(AssistantDsl dsl) {
    if (dsl == null || dsl.getFilters() == null) {
      return Map.of("action", "ANSWER_ONLY", "answer", "无法识别查询条件");
    }

    var filters = dsl.getFilters();
    var page = dsl.getPage() != null ? dsl.getPage() : new AssistantDsl.Page();
    dsl.setPage(page);

    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    List<Long> districtIds = DataScopeService.intersectDistrictIds(filters.getDistrictIds(), scope);

    PersonDtos.PersonSearchRequest req = new PersonDtos.PersonSearchRequest(
        filters.getNameLike(),
        filters.getIdNo(),
        filters.getDisabilityCardNo(),
        districtIds,
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

  private String generateExplanation(AssistantDsl.Filters filters, PageResponse<PersonDtos.PersonRow> result) {
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