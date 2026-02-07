package com.zhilian.zr.stats.service.impl;

import com.zhilian.zr.stats.mapper.StatsMapper;
import com.zhilian.zr.stats.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    private final StatsMapper statsMapper;

    public StatsServiceImpl(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    @Override
    public Map<String, Object> overview(Map<String, Object> req) {
        log.info("Generating statistics overview");
        
        List<Long> districtIds = (List<Long>) req.get("districtIds");
        
        long totalPersons = statsMapper.countPersons(districtIds);
        StatsMapper.CoverageRow coverage = statsMapper.coverage(districtIds);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalPersons", totalPersons);
        result.put("coverage", Map.of(
            "car", coverage.getCarCount(),
            "medical", coverage.getMedicalCount(),
            "pension", coverage.getPensionCount(),
            "blind", coverage.getBlindCount()
        ));
        result.put("carCoverage", totalPersons > 0 ? coverage.getCarCount() * 100.0 / totalPersons : 0);
        result.put("medicalCoverage", totalPersons > 0 ? coverage.getMedicalCount() * 100.0 / totalPersons : 0);
        result.put("pensionCoverage", totalPersons > 0 ? coverage.getPensionCount() * 100.0 / totalPersons : 0);
        result.put("blindCoverage", totalPersons > 0 ? coverage.getBlindCount() * 100.0 / totalPersons : 0);
        
        return result;
    }

    @Override
    public Map<String, Object> disabilityDistribution(Map<String, Object> req) {
        log.info("Generating disability distribution");
        
        List<Long> districtIds = (List<Long>) req.get("districtIds");
        
        List<StatsMapper.KvRow> categories = statsMapper.disabilityCategoryDist(districtIds);
        List<StatsMapper.KvRow> levels = statsMapper.disabilityLevelDist(districtIds);
        List<StatsMapper.KvRow> districts = statsMapper.personsByDistrict(districtIds);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("categories", categories);
        result.put("levels", levels);
        result.put("districts", districts);
        result.put("summary", Map.of(
            "totalCategories", categories.stream().mapToLong(StatsMapper.KvRow::getV),
            "totalLevels", levels.stream().mapToLong(StatsMapper.KvRow::getV),
            "totalDistricts", districts.stream().mapToLong(StatsMapper.KvRow::getV)
        ));
        
        return result;
    }

    @Override
    public Map<String, Object> subsidyCoverage(Map<String, Object> req) {
        log.info("Generating subsidy coverage");
        
        List<Long> districtIds = (List<Long>) req.get("districtIds");
        
        StatsMapper.CoverageRow coverage = statsMapper.coverage(districtIds);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("coverage", Map.of(
            "car", coverage.getCarCount(),
            "medical", coverage.getMedicalCount(),
            "pension", coverage.getPensionCount(),
            "blind", coverage.getBlindCount()
        ));
        
        long totalPersons = statsMapper.countPersons(districtIds);
        List<StatsMapper.KvRow> districts = statsMapper.personsByDistrict(districtIds);
        Map<String, Object> byDistrict = new java.util.LinkedHashMap<>();
        for (StatsMapper.KvRow district : districts) {
            double carPct = totalPersons > 0 ? district.getV() * 100.0 / totalPersons : 0;
            double medicalPct = totalPersons > 0 ? coverage.getMedicalCount() * 100.0 / totalPersons : 0;
            double pensionPct = totalPersons > 0 ? coverage.getPensionCount() * 100.0 / totalPersons : 0;
            double blindPct = totalPersons > 0 ? coverage.getBlindCount() * 100.0 / totalPersons : 0;
            
            byDistrict.put(district.getK(), Map.of(
                "carCount", district.getV(),
                "carPct", String.format("%.2f%%", carPct),
                "medicalCount", coverage.getMedicalCount(),
                "medicalPct", String.format("%.2f%%", medicalPct),
                "pensionCount", coverage.getPensionCount(),
                "pensionPct", String.format("%.2f%%", pensionPct),
                "blindCount", coverage.getBlindCount(),
                "blindPct", String.format("%.2f%%", blindPct)
            ));
        }
        result.put("byDistrict", byDistrict);
        
        return result;
    }

    @Override
    public Map<String, Object> byDistrict(Map<String, Object> req) {
        log.info("Generating statistics by district");
        
        List<Long> districtIds = (List<Long>) req.get("districtIds");
        
        long totalPersons = statsMapper.countPersons(districtIds);
        List<StatsMapper.KvRow> categories = statsMapper.disabilityCategoryDist(districtIds);
        List<StatsMapper.KvRow> levels = statsMapper.disabilityLevelDist(districtIds);
        List<StatsMapper.KvRow> districts = statsMapper.personsByDistrict(districtIds);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalPersons", totalPersons);
        result.put("categories", categories);
        result.put("levels", levels);
        result.put("districts", districts);
        
        return result;
    }

    @Override
    public byte[] exportCsv(Map<String, Object> req) {
        log.info("Exporting statistics to CSV");
        
        List<Long> districtIds = (List<Long>) req.get("districtIds");
        
        Map<String, Object> overview = overview(req);
        Map<String, Object> distribution = disabilityDistribution(req);
        long totalPersons = ((Number) overview.get("totalPersons")).longValue();
        
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
            writer.println("统计概览");
            writer.println("持证残疾人总数," + overview.get("totalPersons"));
            writer.println("残车覆盖人数," + overview.get("coverage"));
            writer.println("残车覆盖率(%)");
            writer.println();
            
            writer.println("各街道残疾人分布");
            @SuppressWarnings("unchecked")
            Map<String, Object> cov = (Map<String, Object>) overview.get("coverage");
            writer.println("街道,持证人数,覆盖率(%)");
            for (Map.Entry<String, Object> entry : cov.entrySet()) {
                writer.printf("%s,%d,%.2f%%\n", entry.getKey(), entry.getValue(), 
                    totalPersons > 0 ? ((Number) entry.getValue()).doubleValue() * 100.0 / totalPersons : 0);
            }
            writer.println();
            
            writer.println("补贴覆盖率统计");
            writer.println("类型,覆盖人数,覆盖率(%)");
            writer.printf("残车,%d,%.2f%%\n", "持证", ((Number) cov.get("car")).doubleValue(),
                    totalPersons > 0 ? ((Number) cov.get("car")).doubleValue() * 100.0 / totalPersons : 0);
            writer.printf("医疗,%d,%.2f%%\n", "持证", ((Number) cov.get("medical")).doubleValue(),
                    totalPersons > 0 ? ((Number) cov.get("medical")).doubleValue() * 100.0 / totalPersons : 0);
            writer.printf("养老,%d,%.2f%%\n", "持证", ((Number) cov.get("pension")).doubleValue(),
                    totalPersons > 0 ? ((Number) cov.get("pension")).doubleValue() * 100.0 / totalPersons : 0);
            writer.printf("盲人,%d,%.2f%%\n", "持证", ((Number) cov.get("blind")).doubleValue(),
                    totalPersons > 0 ? ((Number) cov.get("blind")).doubleValue() * 100.0 / totalPersons : 0);
        }
        
        String csv = stringWriter.toString();
        log.info("Exported CSV with {} bytes", csv.getBytes().length);
        return csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
