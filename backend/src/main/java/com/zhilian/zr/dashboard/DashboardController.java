package com.zhilian.zr.dashboard;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.geo.service.GeoCodingService;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.person.service.PersonIndexService;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final PersonService personService;
    private final DataScopeService dataScopeService;
    private final GeoCodingService geoCodingService;
    private final PersonIndexService personIndexService;
    
    /**
     * 获取大屏统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Long userId = CurrentUser.userId();
        
        // 从PersonIndexService获取统计数据
        long totalPersons = personIndexService.getTotalCount();
        Map<String, Long> streetCountMap = personIndexService.getCountByStreet();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPersons", totalPersons);
        stats.put("todayNew", 0);
        stats.put("anomalies", 0);
        stats.put("streetCount", streetCountMap.size());
        
        return ApiResponse.ok(stats);
    }
    
    /**
     * 获取带坐标的残疾人分布数据（用于地图）
     */
    @GetMapping("/persons/locations")
    public ApiResponse<List<Map<String, Object>>> getPersonLocations(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String levelCode) {
        
        Long userId = CurrentUser.userId();
        
        // 获取数据范围
        List<Long> scope = dataScopeService.districtScopeOrNullForAdmin(
            userId, 
            CurrentUser.authorities()
        );
        
        // 从PersonIndexService获取有位置信息的人员数据
        List<PersonIndexEntity> persons = personIndexService.getPersonsWithLocation();
        
        List<Map<String, Object>> locations = new ArrayList<>();
        
        for (PersonIndexEntity person : persons) {
            Map<String, Object> location = new HashMap<>();
            location.put("idCard", person.getIdCard());
            location.put("name", person.getName());
            location.put("address", person.getContactAddress());
            location.put("longitude", person.getLongitude());
            location.put("latitude", person.getLatitude());
            location.put("categoryCode", person.getDisabilityCategory());
            location.put("levelCode", person.getDisabilityLevel());
            location.put("street", person.getStreet());
            location.put("district", person.getDistrict());
            
            // 根据参数筛选
            boolean match = true;
            if (categoryCode != null && !categoryCode.isEmpty()) {
                match = categoryCode.equals(person.getDisabilityCategory());
            }
            if (match && levelCode != null && !levelCode.isEmpty()) {
                match = levelCode.equals(person.getDisabilityLevel());
            }
            
            if (match) {
                locations.add(location);
            }
        }
        
        return ApiResponse.ok(locations);
    }
    
    /**
     * 地理编码 - 将地址转为经纬度
     */
    @PostMapping("/geocode")
    public ApiResponse<?> geocodeAddress(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        String city = request.getOrDefault("city", "上海市");
        
        if (address == null || address.trim().isEmpty()) {
            return ApiResponse.fail("地址不能为空");
        }
        
        var result = geoCodingService.geocode(address, city);
        
        if (result.isSuccess()) {
            Map<String, Object> data = new HashMap<>();
            data.put("longitude", result.getLongitude());
            data.put("latitude", result.getLatitude());
            data.put("formattedAddress", result.getFormattedAddress());
            data.put("level", result.getLevel());
            return ApiResponse.ok(data);
        } else {
            return ApiResponse.fail(result.getErrorMessage());
        }
    }
    
    /**
     * 更新人员坐标
     */
    @PostMapping("/persons/{personId}/location")
    public ApiResponse<Void> updatePersonLocation(
            @PathVariable Long personId,
            @RequestBody Map<String, Object> request) {
        
        BigDecimal longitude = new BigDecimal(request.get("longitude").toString());
        BigDecimal latitude = new BigDecimal(request.get("latitude").toString());
        String accuracy = (String) request.getOrDefault("accuracy", "MANUAL");
        
        // TODO: 更新数据库中的经纬度
        // personService.updateLocation(personId, longitude, latitude, accuracy);
        
        return ApiResponse.ok(null);
    }
    
    /**
     * 获取分布统计数据
     */
    @GetMapping("/distribution")
    public ApiResponse<Map<String, Object>> getDistribution() {
        Map<String, Object> data = new HashMap<>();
        
        // 从PersonIndexService获取残疾类别分布
        Map<String, Long> categoryDist = personIndexService.getCountByCategory();
        data.put("category", categoryDist);
        
        // 残疾等级分布 - 需要额外处理
        Map<String, Integer> levelDist = new HashMap<>();
        data.put("level", levelDist);
        
        // 年龄分布 - 暂时返回空
        Map<String, Integer> ageDist = new HashMap<>();
        data.put("age", ageDist);
        
        // 街道统计TOP5
        Map<String, Long> streetStatsMap = personIndexService.getCountByStreet();
        List<Map<String, Object>> streetStats = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Long> entry : streetStatsMap.entrySet()) {
            if (count >= 5) break;
            streetStats.add(Map.of("name", entry.getKey(), "count", entry.getValue()));
            count++;
        }
        data.put("streetStats", streetStats);
        
        return ApiResponse.ok(data);
    }
}
