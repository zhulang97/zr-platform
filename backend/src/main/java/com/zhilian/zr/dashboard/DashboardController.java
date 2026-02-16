package com.zhilian.zr.dashboard;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.geo.service.GeoCodingService;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.security.CurrentUser;
import com.zhilian.zr.security.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final PersonService personService;
    private final DataScopeService dataScopeService;
    private final GeoCodingService geoCodingService;
    
    /**
     * 获取大屏统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Long userId = CurrentUser.userId();
        
        // TODO: 从实际服务获取统计数据
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPersons", 12568);
        stats.put("todayNew", 8);
        stats.put("anomalies", 23);
        stats.put("streetCount", 185);
        
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
        
        List<Long> districtIds = districtId != null 
            ? List.of(districtId) 
            : scope;
        
        // TODO: 从数据库查询带坐标的残疾人数据
        // 这里先返回模拟数据
        List<Map<String, Object>> locations = new ArrayList<>();
        
        // 模拟数据 - 长宁区各地点
        addMockLocation(locations, 1L, "张**", "长宁路100号", 
            new BigDecimal("121.4035"), new BigDecimal("31.2198"),
            "LIMB", "二级", "新华街道", List.of("残车", "医疗"));
        
        addMockLocation(locations, 2L, "李**", "定西路200号",
            new BigDecimal("121.4142"), new BigDecimal("31.2185"),
            "VISION", "一级", "新华街道", List.of("盲人证", "养老"));
        
        addMockLocation(locations, 3L, "王**", "愚园路300号",
            new BigDecimal("121.4221"), new BigDecimal("31.2215"),
            "HEARING", "三级", "江苏路街道", List.of("医疗"));
        
        addMockLocation(locations, 4L, "刘**", "武夷路400号",
            new BigDecimal("121.4089"), new BigDecimal("31.2156"),
            "LIMB", "一级", "华阳路街道", List.of("残车", "医疗", "养老"));
        
        addMockLocation(locations, 5L, "陈**", "虹桥路500号",
            new BigDecimal("121.3978"), new BigDecimal("31.2034"),
            "MENTAL", "二级", "周家桥街道", List.of("医疗"));
        
        addMockLocation(locations, 6L, "杨**", "天山路600号",
            new BigDecimal("121.3956"), new BigDecimal("31.2123"),
            "SPEECH", "四级", "天山路街道", List.of());
        
        addMockLocation(locations, 7L, "赵**", "仙霞路700号",
            new BigDecimal("121.3856"), new BigDecimal("31.2089"),
            "INTELLECTUAL", "二级", "仙霞新村街道", List.of("医疗", "养老"));
        
        addMockLocation(locations, 8L, "黄**", "虹桥路800号",
            new BigDecimal("121.4012"), new BigDecimal("31.2056"),
            "LIMB", "三级", "程家桥街道", List.of("残车"));
        
        // 根据参数筛选
        if (categoryCode != null) {
            locations = locations.stream()
                .filter(l -> categoryCode.equals(l.get("categoryCode")))
                .collect(Collectors.toList());
        }
        
        if (levelCode != null) {
            locations = locations.stream()
                .filter(l -> levelCode.equals(l.get("levelCode")))
                .collect(Collectors.toList());
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
        
        // 残疾类别分布
        Map<String, Integer> categoryDist = new HashMap<>();
        categoryDist.put("LIMB", 4568);
        categoryDist.put("VISION", 2345);
        categoryDist.put("HEARING", 1987);
        categoryDist.put("SPEECH", 1234);
        categoryDist.put("INTELLECTUAL", 1456);
        categoryDist.put("MENTAL", 978);
        data.put("category", categoryDist);
        
        // 残疾等级分布
        Map<String, Integer> levelDist = new HashMap<>();
        levelDist.put("一级", 3567);
        levelDist.put("二级", 4234);
        levelDist.put("三级", 2789);
        levelDist.put("四级", 1978);
        data.put("level", levelDist);
        
        // 年龄分布
        Map<String, Integer> ageDist = new HashMap<>();
        ageDist.put("0-18", 456);
        ageDist.put("19-35", 1234);
        ageDist.put("36-50", 3456);
        ageDist.put("51-65", 4567);
        ageDist.put("65+", 2855);
        data.put("age", ageDist);
        
        // 街道统计TOP5
        List<Map<String, Object>> streetStats = new ArrayList<>();
        streetStats.add(Map.of("name", "新华街道", "count", 1256));
        streetStats.add(Map.of("name", "江苏路街道", "count", 1145));
        streetStats.add(Map.of("name", "华阳路街道", "count", 1089));
        streetStats.add(Map.of("name", "周家桥街道", "count", 956));
        streetStats.add(Map.of("name", "天山路街道", "count", 892));
        data.put("streetStats", streetStats);
        
        return ApiResponse.ok(data);
    }
    
    private void addMockLocation(List<Map<String, Object>> list, Long id, String name, 
            String address, BigDecimal longitude, BigDecimal latitude,
            String categoryCode, String levelCode, String street, List<String> benefits) {
        Map<String, Object> location = new HashMap<>();
        location.put("personId", id);
        location.put("name", name);
        location.put("address", address);
        location.put("longitude", longitude);
        location.put("latitude", latitude);
        location.put("categoryCode", categoryCode);
        location.put("levelCode", levelCode);
        location.put("street", street);
        location.put("benefits", benefits);
        list.add(location);
    }
}
