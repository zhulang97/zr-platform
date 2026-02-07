package com.zhilian.zr.anomaly.service;

import com.zhilian.zr.anomaly.dto.AnomalyDtos;
import com.zhilian.zr.common.api.PageResponse;

public interface AnomalyService {
  PageResponse<AnomalyDtos.AnomalyRow> search(AnomalyDtos.SearchRequest req);

  AnomalyDtos.AnomalyDetail detail(long anomalyId);

  void updateStatus(long anomalyId, AnomalyDtos.StatusRequest req);

  byte[] exportCsv(AnomalyDtos.SearchRequest req);
}
