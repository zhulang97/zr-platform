package com.zhilian.zr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class AppConfigController {

    @Value("${amap.key:}")
    private String amapKey;

    @Value("${amap.securityConfig:}")
    private String amapSecurityConfig;

    /**
     * 获取高德地图配置（前端使用）
     */
    @GetMapping("/amap")
    public Map<String, String> getAmapConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("key", amapKey);
        config.put("securityCode", amapSecurityConfig);
        return config;
    }
}
