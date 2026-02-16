package com.zhilian.zr.geo.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@Slf4j
@Service
public class GeoCodingService {

    @Value("${amap.key:}")
    private String amapKey;

    @Value("${amap.securityConfig:}")
    private String securityConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo";

    /**
     * 地址转经纬度（地理编码）
     *
     * @param address 地址文本
     * @param city    城市（可选，提高精度）
     * @return GeoResult 包含经纬度的结果
     */
    public GeoResult geocode(String address, String city) {
        if (address == null || address.trim().isEmpty()) {
            return GeoResult.fail("地址不能为空");
        }

        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            StringBuilder urlBuilder = new StringBuilder(GEOCODE_URL)
                    .append("?key=").append(amapKey)
                    .append("&address=").append(encodedAddress);

            if (city != null && !city.isEmpty()) {
                urlBuilder.append("&city=").append(URLEncoder.encode(city, StandardCharsets.UTF_8));
            }

            // 如果配置了安全密钥，添加签名
            if (securityConfig != null && !securityConfig.isEmpty()) {
                String sign = generateSign(urlBuilder.toString());
                urlBuilder.append("&sig=").append(sign);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(urlBuilder.toString(), String.class);
            String body = response.getBody();

            JSONObject json = JSON.parseObject(body);
            String status = json.getString("status");

            if (!"1".equals(status)) {
                String info = json.getString("info");
                log.warn("地理编码失败: {}, 地址: {}", info, address);
                return GeoResult.fail("地理编码失败: " + info);
            }

            Integer count = json.getInteger("count");
            if (count == null || count == 0) {
                log.warn("未找到地址对应的坐标: {}", address);
                return GeoResult.fail("未找到该地址的坐标信息");
            }

            JSONObject firstGeoCode = json.getJSONArray("geocodes").getJSONObject(0);
            String location = firstGeoCode.getString("location");
            String[] coords = location.split(",");

            BigDecimal longitude = new BigDecimal(coords[0]);
            BigDecimal latitude = new BigDecimal(coords[1]);
            String formattedAddress = firstGeoCode.getString("formatted_address");
            String level = firstGeoCode.getString("level"); // 精度级别

            return GeoResult.success(longitude, latitude, formattedAddress, level);

        } catch (Exception e) {
            log.error("地理编码异常, 地址: {}", address, e);
            return GeoResult.fail("地理编码异常: " + e.getMessage());
        }
    }

    /**
     * 简化的地理编码（默认上海市）
     */
    public GeoResult geocode(String address) {
        return geocode(address, "上海市");
    }

    /**
     * 批量地理编码
     */
    public java.util.Map<String, GeoResult> batchGeocode(java.util.List<String> addresses) {
        java.util.Map<String, GeoResult> results = new java.util.HashMap<>();
        for (String address : addresses) {
            results.put(address, geocode(address));
            // 避免触发限流，适当延时
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return results;
    }

    /**
     * 经纬度转地址（逆地理编码）
     */
    public ReGeoResult reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        try {
            String location = longitude + "," + latitude;
            StringBuilder urlBuilder = new StringBuilder(REGEO_URL)
                    .append("?key=").append(amapKey)
                    .append("&location=").append(location)
                    .append("&extensions=all");

            if (securityConfig != null && !securityConfig.isEmpty()) {
                String sign = generateSign(urlBuilder.toString());
                urlBuilder.append("&sig=").append(sign);
            }

            ResponseEntity<String> response = restTemplate.getForEntity(urlBuilder.toString(), String.class);
            String body = response.getBody();

            JSONObject json = JSON.parseObject(body);
            String status = json.getString("status");

            if (!"1".equals(status)) {
                return ReGeoResult.fail("逆地理编码失败: " + json.getString("info"));
            }

            JSONObject regeocode = json.getJSONObject("regeocode");
            String formattedAddress = regeocode.getString("formatted_address");
            JSONObject addressComponent = regeocode.getJSONObject("addressComponent");

            ReGeoResult result = new ReGeoResult();
            result.setSuccess(true);
            result.setFormattedAddress(formattedAddress);
            result.setProvince(addressComponent.getString("province"));
            result.setCity(addressComponent.getString("city"));
            result.setDistrict(addressComponent.getString("district"));
            result.setStreet(addressComponent.getString("street"));
            result.setAddress(addressComponent.getString("address"));

            return result;

        } catch (Exception e) {
            log.error("逆地理编码异常", e);
            return ReGeoResult.fail("逆地理编码异常: " + e.getMessage());
        }
    }

    /**
     * 生成签名（如果启用了安全密钥）
     */
    private String generateSign(String url) {
        try {
            String pathAndParams = url.substring(url.indexOf("?") + 1);
            String signContent = pathAndParams + securityConfig;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signContent.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成签名失败", e);
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    // 结果类
    public static class GeoResult {
        private boolean success;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String formattedAddress;
        private String level; // 精度级别：省、市、区、街道、门牌号等
        private String errorMessage;

        public static GeoResult success(BigDecimal longitude, BigDecimal latitude, String formattedAddress, String level) {
            GeoResult result = new GeoResult();
            result.success = true;
            result.longitude = longitude;
            result.latitude = latitude;
            result.formattedAddress = formattedAddress;
            result.level = level;
            return result;
        }

        public static GeoResult fail(String message) {
            GeoResult result = new GeoResult();
            result.success = false;
            result.errorMessage = message;
            return result;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public BigDecimal getLongitude() { return longitude; }
        public BigDecimal getLatitude() { return latitude; }
        public String getFormattedAddress() { return formattedAddress; }
        public String getLevel() { return level; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class ReGeoResult {
        private boolean success;
        private String formattedAddress;
        private String province;
        private String city;
        private String district;
        private String street;
        private String address;
        private String errorMessage;

        public static ReGeoResult fail(String message) {
            ReGeoResult result = new ReGeoResult();
            result.success = false;
            result.errorMessage = message;
            return result;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getFormattedAddress() { return formattedAddress; }
        public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getErrorMessage() { return errorMessage; }
    }
}
