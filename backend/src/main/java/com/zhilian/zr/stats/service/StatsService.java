package com.zhilian.zr.stats.service;

import java.util.Map;

public interface StatsService {
  Map<String, Object> overview(Map<String, Object> req);

  Map<String, Object> disabilityDistribution(Map<String, Object> req);

  Map<String, Object> subsidyCoverage(Map<String, Object> req);

  Map<String, Object> byDistrict(Map<String, Object> req);

  byte[] exportCsv(Map<String, Object> req);
}
