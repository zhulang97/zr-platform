package com.zhilian.zr.person.service.impl;

import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.mapper.PersonDetailMapper;
import com.zhilian.zr.person.service.PersonDetailService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PersonDetailServiceImpl implements PersonDetailService {
  private final PersonDetailMapper personDetailMapper;
  private final DataScopeService dataScopeService;

  public PersonDetailServiceImpl(PersonDetailMapper personDetailMapper, DataScopeService dataScopeService) {
    this.personDetailMapper = personDetailMapper;
    this.dataScopeService = dataScopeService;
  }

  @Override
  public PersonDtos.PersonDetail detail(long personId) {
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(CurrentUser.userId(), CurrentUser.authorities());
    Map<String, Object> row = personDetailMapper.detail(personId, scope);
    if (row == null || row.isEmpty()) {
      throw new IllegalArgumentException("Person not found or out of scope");
    }
    String name = (String) row.get("name");
    String idNo = (String) row.get("idNo");
    Object issueDate = row.get("issueDate");
    String issue = issueDate == null ? null : issueDate.toString();
    return new PersonDtos.PersonDetail(
        maskName(name),
        maskIdNo(idNo),
        (String) row.get("disabilityCardNo"),
        (String) row.get("disabilityCategoryCode"),
        (String) row.get("disabilityCategoryName"),
        (String) row.get("disabilityLevelCode"),
        (String) row.get("disabilityLevelName"),
        issue,
        (String) row.get("cardStatusCode"),
        (String) row.get("cardStatusName")
    );
  }

  @Override
  public Map<String, Object> biz(long personId) {
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(CurrentUser.userId(), CurrentUser.authorities());
    Map<String, Object> row = personDetailMapper.biz(personId, scope);
    if (row == null || row.isEmpty()) {
      throw new IllegalArgumentException("Person not found or out of scope");
    }
    // Normalize flags for frontend convenience.
    Object sep = row.get("personCarSeparated");
    if (sep instanceof Number n) {
      row.put("personCarSeparated", n.intValue() == 1);
    }
    return row;
  }

  @Override
  public List<Map<String, Object>> risks(long personId) {
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(CurrentUser.userId(), CurrentUser.authorities());
    // If person out of scope, this should be empty; but make it explicit.
    Map<String, Object> person = personDetailMapper.detail(personId, scope);
    if (person == null || person.isEmpty()) {
      throw new IllegalArgumentException("Person not found or out of scope");
    }
    return personDetailMapper.risks(personId, scope);
  }

  private static String maskName(String name) {
    if (name == null || name.isBlank()) {
      return "";
    }
    if (name.length() == 1) {
      return "*";
    }
    return name.charAt(0) + "*";
  }

  private static String maskIdNo(String id) {
    if (id == null || id.length() < 8) {
      return "";
    }
    return id.substring(0, 3) + "********" + id.substring(id.length() - 4);
  }
}
