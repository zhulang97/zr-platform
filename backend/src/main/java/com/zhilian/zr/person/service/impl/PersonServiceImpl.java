package com.zhilian.zr.person.service.impl;

import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.mapper.PersonSearchMapper;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PersonServiceImpl implements PersonService {
  private static final int EXPORT_MAX_ROWS = 50000;

  private final PersonSearchMapper personSearchMapper;
  private final DataScopeService dataScopeService;

  public PersonServiceImpl(PersonSearchMapper personSearchMapper, DataScopeService dataScopeService) {
    this.personSearchMapper = personSearchMapper;
    this.dataScopeService = dataScopeService;
  }

  @Override
  public PageResponse<PersonDtos.PersonRow> search(PersonDtos.PersonSearchRequest req) {
    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    List<Long> districtIds = DataScopeService.intersectDistrictIds(req.districtIds(), scope);

    if (scope != null && (districtIds == null || districtIds.isEmpty())) {
      return new PageResponse<>(0, req.pageNo(), req.pageSize(), List.of());
    }

    PersonDtos.PersonSearchRequest normalized = copyWithDistrictIds(req, districtIds);

    long total = personSearchMapper.count(normalized);
    int offset = (req.pageNo() - 1) * req.pageSize();
    List<PersonSearchMapper.PersonRowDb> items = personSearchMapper.list(normalized, req.pageSize(), offset);
    List<PersonDtos.PersonRow> rows = items.stream().map(this::toRow).toList();
    return new PageResponse<>(total, req.pageNo(), req.pageSize(), rows);
  }

  @Override
  public byte[] exportCsv(PersonDtos.PersonSearchRequest req) {
    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    List<Long> districtIds = DataScopeService.intersectDistrictIds(req.districtIds(), scope);
    if (scope != null && (districtIds == null || districtIds.isEmpty())) {
      return "personId,name,idNo,disabilityCategoryCode,disabilityCategoryName,disabilityLevelCode,disabilityLevelName,district,street,cardStatusCode,cardStatusName,hasCar,hasMedicalSubsidy,hasPensionSubsidy,riskFlag\n".getBytes(StandardCharsets.UTF_8);
    }
    PersonDtos.PersonSearchRequest normalized = copyWithDistrictIds(req, districtIds);
    List<PersonSearchMapper.PersonRowDb> items = personSearchMapper.listForExport(normalized, EXPORT_MAX_ROWS);

    StringBuilder sb = new StringBuilder();
    sb.append("personId,name,idNo,disabilityCategoryCode,disabilityCategoryName,disabilityLevelCode,disabilityLevelName,district,street,cardStatusCode,cardStatusName,hasCar,hasMedicalSubsidy,hasPensionSubsidy,riskFlag\n");
    for (PersonSearchMapper.PersonRowDb r : items) {
      PersonDtos.PersonRow row = toRow(r);
      sb.append(row.personId()).append(',')
          .append(csv(row.nameMasked())).append(',')
          .append(csv(row.idNoMasked())).append(',')
          .append(csv(row.disabilityCategoryCode())).append(',')
          .append(csv(row.disabilityCategoryName())).append(',')
          .append(csv(row.disabilityLevelCode())).append(',')
          .append(csv(row.disabilityLevelName())).append(',')
          .append(csv(row.district())).append(',')
          .append(csv(row.street())).append(',')
          .append(csv(row.cardStatusCode())).append(',')
          .append(csv(row.cardStatusName())).append(',')
          .append(row.hasCar()).append(',')
          .append(row.hasMedicalSubsidy()).append(',')
          .append(row.hasPensionSubsidy()).append(',')
          .append(row.riskFlag()).append('\n');
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static PersonDtos.PersonSearchRequest copyWithDistrictIds(PersonDtos.PersonSearchRequest req, List<Long> districtIds) {
    return new PersonDtos.PersonSearchRequest(
        req.nameLike(),
        req.idNo(),
        req.disabilityCardNo(),
        districtIds,
        req.streetIds(),
        req.disabilityCategories(),
        req.disabilityLevels(),
        req.hasCar(),
        req.hasMedicalSubsidy(),
        req.hasPensionSubsidy(),
        req.hasBlindCard(),
        req.cardStatus(),
        req.subsidyStatus(),
        req.pageNo(),
        req.pageSize()
    );
  }

  private PersonDtos.PersonRow toRow(PersonSearchMapper.PersonRowDb r) {
    return new PersonDtos.PersonRow(
        r.getPersonId() == null ? 0 : r.getPersonId(),
        maskName(r.getName()),
        maskIdNo(r.getIdNo()),
        r.getDisabilityCategoryCode(),
        r.getDisabilityCategoryName(),
        r.getDisabilityLevelCode(),
        r.getDisabilityLevelName(),
        r.getDistrict(),
        r.getStreet(),
        r.getCardStatusCode(),
        r.getCardStatusName(),
        isTrue(r.getHasCar()),
        isTrue(r.getHasMedicalSubsidy()),
        isTrue(r.getHasPensionSubsidy()),
        isTrue(r.getRiskFlag())
    );
  }

  private static String csv(String v) {
    if (v == null) {
      return "";
    }
    String s = v.replace("\"", "\"\"");
    if (s.contains(",") || s.contains("\n") || s.contains("\r")) {
      return '"' + s + '"';
    }
    return s;
  }

  private static boolean isTrue(Integer v) {
    return v != null && v == 1;
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
