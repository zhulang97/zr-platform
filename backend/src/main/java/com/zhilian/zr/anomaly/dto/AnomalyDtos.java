package com.zhilian.zr.anomaly.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public final class AnomalyDtos {
  private AnomalyDtos() {
  }

  public record SearchRequest(
      String anomalyType,
      List<Long> districtIds,
      String status,
      @Min(1) int pageNo,
      @Min(1) @Max(200) int pageSize
  ) {
  }

  public record AnomalyRow(
      long anomalyId,
      String anomalyType,
      String anomalyTypeName,
      String status,
      String hitTime,
      String personNameMasked,
      String personIdNoMasked,
      String district,
      String street
  ) {
  }

  public record AnomalyDetail(
      long anomalyId,
      String anomalyType,
      String anomalyTypeName,
      String ruleCode,
      String ruleName,
      String severity,
      String status,
      String hitTime,
      String handleNote,
      String handledAt,
      String snapshotJson,
      String personNameMasked,
      String personIdNoMasked,
      String district,
      String street
  ) {
  }

  public record StatusRequest(String status, String note) {
  }
}
