package com.zhilian.zr.anomaly.service.impl;

import com.zhilian.zr.anomaly.dto.AnomalyDtos;
import com.zhilian.zr.anomaly.mapper.AnomalySearchMapper;
import com.zhilian.zr.anomaly.mapper.AnomalyUpdateMapper;
import com.zhilian.zr.anomaly.service.AnomalyService;
import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnomalyServiceImpl implements AnomalyService {
  private static final int EXPORT_MAX_ROWS = 50000;

  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  private final AnomalySearchMapper anomalySearchMapper;
  private final AnomalyUpdateMapper anomalyUpdateMapper;
  private final DataScopeService dataScopeService;

  public AnomalyServiceImpl(AnomalySearchMapper anomalySearchMapper, AnomalyUpdateMapper anomalyUpdateMapper,
      DataScopeService dataScopeService) {
    this.anomalySearchMapper = anomalySearchMapper;
    this.anomalyUpdateMapper = anomalyUpdateMapper;
    this.dataScopeService = dataScopeService;
  }

  @Override
  public PageResponse<AnomalyDtos.AnomalyRow> search(AnomalyDtos.SearchRequest req) {
    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    List<Long> districtIds = DataScopeService.intersectDistrictIds(req.districtIds(), scope);

    if (scope != null && (districtIds == null || districtIds.isEmpty())) {
      return new PageResponse<>(0, req.pageNo(), req.pageSize(), List.of());
    }

    AnomalyDtos.SearchRequest normalized = new AnomalyDtos.SearchRequest(
        req.anomalyType(),
        districtIds,
        req.status(),
        req.pageNo(),
        req.pageSize());

    long total = anomalySearchMapper.count(normalized);
    int offset = (req.pageNo() - 1) * req.pageSize();
    List<AnomalySearchMapper.AnomalyRowDb> items = anomalySearchMapper.list(normalized, req.pageSize(), offset);
    List<AnomalyDtos.AnomalyRow> rows = items.stream().map(this::toRow).toList();
    return new PageResponse<>(total, req.pageNo(), req.pageSize(), rows);
  }

  @Override
  public AnomalyDtos.AnomalyDetail detail(long anomalyId) {
    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    AnomalySearchMapper.AnomalyDetailDb r = anomalySearchMapper.detail(anomalyId, scope);
    if (r == null) {
      throw new IllegalArgumentException("Anomaly not found or out of scope");
    }
    return new AnomalyDtos.AnomalyDetail(
        r.getAnomalyId() == null ? 0 : r.getAnomalyId(),
        r.getAnomalyType(),
        r.getAnomalyTypeName(),
        r.getRuleCode(),
        r.getRuleName(),
        r.getSeverity(),
        r.getStatus(),
        r.getHitTime() == null ? null : TS.format(r.getHitTime()),
        r.getHandleNote(),
        r.getHandledAt() == null ? null : TS.format(r.getHandledAt()),
        r.getSnapshotJson(),
        maskName(r.getPersonName()),
        maskIdNo(r.getPersonIdNo()),
        r.getDistrict(),
        r.getStreet()
    );
  }

  @Override
  public void updateStatus(long anomalyId, AnomalyDtos.StatusRequest req) {
    long userId = CurrentUser.userId();
    List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(userId, CurrentUser.authorities());
    if (scope != null) {
      Long ok = anomalySearchMapper.existsInDistrictScope(anomalyId, scope);
      if (ok == null) {
        throw new IllegalArgumentException("Anomaly not found or out of scope");
      }
    }
    int updated = anomalyUpdateMapper.updateStatus(anomalyId, req.status(), req.note(), userId, Instant.now());
    if (updated == 0) {
      throw new IllegalArgumentException("Anomaly not found");
    }
  }

  @Override
  public byte[] exportCsv(AnomalyDtos.SearchRequest req) {
    // reuse search normalization + scope
    PageResponse<AnomalyDtos.AnomalyRow> page = search(new AnomalyDtos.SearchRequest(
        req.anomalyType(), req.districtIds(), req.status(), 1, EXPORT_MAX_ROWS));

    StringBuilder sb = new StringBuilder();
    sb.append("anomalyId,anomalyType,anomalyTypeName,status,hitTime,personName,personIdNo,district,street\n");
    for (AnomalyDtos.AnomalyRow r : page.items()) {
      sb.append(r.anomalyId()).append(',')
          .append(csv(r.anomalyType())).append(',')
          .append(csv(r.anomalyTypeName())).append(',')
          .append(csv(r.status())).append(',')
          .append(csv(r.hitTime())).append(',')
          .append(csv(r.personNameMasked())).append(',')
          .append(csv(r.personIdNoMasked())).append(',')
          .append(csv(r.district())).append(',')
          .append(csv(r.street())).append('\n');
    }
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  private AnomalyDtos.AnomalyRow toRow(AnomalySearchMapper.AnomalyRowDb r) {
    return new AnomalyDtos.AnomalyRow(
        r.getAnomalyId() == null ? 0 : r.getAnomalyId(),
        r.getAnomalyType(),
        r.getAnomalyTypeName(),
        r.getStatus(),
        r.getHitTime() == null ? null : TS.format(r.getHitTime()),
        maskName(r.getPersonName()),
        maskIdNo(r.getPersonIdNo()),
        r.getDistrict(),
        r.getStreet()
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
