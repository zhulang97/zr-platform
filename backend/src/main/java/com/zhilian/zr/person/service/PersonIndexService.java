package com.zhilian.zr.person.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zr.geo.service.GeoCodingService;
import com.zhilian.zr.importing.mapper.ImportModuleConfigMapper;
import com.zhilian.zr.importing.mapper.ImportModuleFieldMapper;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.person.mapper.PersonIndexMapper;
import com.zhilian.zr.policy.dto.PolicyConditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class PersonIndexService {
    
    private final PersonIndexMapper personIndexMapper;
    private final ImportModuleConfigMapper moduleConfigMapper;
    private final ImportModuleFieldMapper moduleFieldMapper;
    private final GeoCodingService geoCodingService;
    private final JdbcTemplate jdbcTemplate;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PersonIndexService.class);
    
    public PersonIndexService(
            PersonIndexMapper personIndexMapper,
            ImportModuleConfigMapper moduleConfigMapper,
            ImportModuleFieldMapper moduleFieldMapper,
            GeoCodingService geoCodingService,
            JdbcTemplate jdbcTemplate) {
        this.personIndexMapper = personIndexMapper;
        this.moduleConfigMapper = moduleConfigMapper;
        this.moduleFieldMapper = moduleFieldMapper;
        this.geoCodingService = geoCodingService;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Transactional
    public void upsertPersonIndex(String idCard, Map<String, Object> moduleData, String moduleCode) {
        if (idCard == null || idCard.isEmpty()) {
            return;
        }
        
        PersonIndexEntity existing = personIndexMapper.selectById(idCard);
        boolean isNew = existing == null;
        
        PersonIndexEntity entity = existing != null ? existing : new PersonIndexEntity();
        entity.setIdCard(idCard);
        entity.setLastSeenAt(LocalDateTime.now());
        
        if (isNew) {
            entity.setFirstSeenAt(LocalDateTime.now());
            entity.setLocationStatus("PENDING");
        }
        
        Map<String, String> fieldMapping = getFieldMapping(moduleCode);
        
        setIfPresent(entity::setName, moduleData, fieldMapping.getOrDefault("NAME", "NAME"));
        setIfPresent(entity::setGender, moduleData, fieldMapping.getOrDefault("GENDER", "GENDER"));
        setIfPresent(entity::setPhone, moduleData, fieldMapping.getOrDefault("PHONE", "CONTACT_PHONE"));
        setIfPresent(entity::setDistrict, moduleData, fieldMapping.getOrDefault("DISTRICT", "DISTRICT"));
        setIfPresent(entity::setStreet, moduleData, fieldMapping.getOrDefault("STREET", "STREET"));
        setIfPresent(entity::setCommittee, moduleData, fieldMapping.getOrDefault("COMMITTEE", "COMMITTEE"));
        setIfPresent(entity::setContactAddress, moduleData, fieldMapping.getOrDefault("CONTACT_ADDRESS", "CONTACT_ADDRESS"));
        setIfPresent(entity::setHouseholdAddress, moduleData, fieldMapping.getOrDefault("HOUSEHOLD_ADDRESS", "HOUSEHOLD_ADDRESS"));
        setIfPresent(entity::setDisabilityCategory, moduleData, fieldMapping.getOrDefault("DISABILITY_CATEGORY", "DISABILITY_CATEGORY"));
        setIfPresent(entity::setDisabilityLevel, moduleData, fieldMapping.getOrDefault("DISABILITY_LEVEL", "DISABILITY_LEVEL"));
        
        if (entity.getContactAddress() != null && entity.getLocationStatus() == null) {
            entity.setLocationStatus("PENDING");
        }
        
        if (isNew) {
            personIndexMapper.insert(entity);
        } else {
            personIndexMapper.updateById(entity);
        }
        
        // 在事务提交后再执行地理编码，避免影响主事务
        // 注意：从moduleData中获取地址，而不是从entity，因为entity可能是从数据库加载的旧数据
        String contactAddress = moduleData.containsKey("联系地址") ? (String) moduleData.get("联系地址") :
                               (moduleData.containsKey("CONTACT_ADDRESS") ? (String) moduleData.get("CONTACT_ADDRESS") : null);

        // 尝试多种可能的地址字段名
        if (contactAddress == null || contactAddress.isEmpty()) {
            for (String key : moduleData.keySet()) {
                String lowerKey = key.toLowerCase();
                if (lowerKey.contains("address") || lowerKey.contains("地址")) {
                    Object val = moduleData.get(key);
                    if (val != null && !val.toString().trim().isEmpty()) {
                        contactAddress = val.toString();
                        log.info("Found address from key: {} = {}", key, contactAddress);
                        break;
                    }
                }
            }
        }

        log.info(" upsertPersonIndex - idCard: {}, moduleData keys: {}, contactAddress: {}",
                idCard, moduleData.keySet(), contactAddress);

        if (contactAddress != null && !contactAddress.isEmpty()) {
            entity.setLocationStatus("PENDING");
            final String finalIdCard = idCard;
            final String finalAddress = contactAddress;
            
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        updateLocationAsync(finalIdCard, finalAddress);
                    }
                });
            } else {
                // 如果没有活动的事务，直接异步执行
                updateLocationAsync(finalIdCard, finalAddress);
            }
        }
    }
    
    private void setIfPresent(java.util.function.Consumer<String> setter, Map<String, Object> data, String key) {
        if (key != null && data.containsKey(key)) {
            Object value = data.get(key);
            if (value != null) {
                setter.accept(value.toString());
            }
        }
    }
    
    @Async
    public void updateLocationAsync(String idCard, String address) {
        try {
            log.info("Starting geocoding for idCard: {}, address: {}", idCard, address);
            GeoCodingService.GeoResult result = geoCodingService.geocode(address, "上海");
            log.info("Geocoding result for idCard {}: success={}, longitude={}, latitude={}", 
                    idCard, result.isSuccess(), result.getLongitude(), result.getLatitude());
            if (result != null && result.isSuccess()) {
                BigDecimal longitude = result.getLongitude();
                BigDecimal latitude = result.getLatitude();
                if (longitude != null && latitude != null) {
                    PersonIndexEntity entity = new PersonIndexEntity();
                    entity.setIdCard(idCard);
                    entity.setLongitude(longitude);
                    entity.setLatitude(latitude);
                    entity.setLocationStatus("SUCCESS");
                    entity.setLastSeenAt(LocalDateTime.now());
                    personIndexMapper.updateById(entity);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update location for {}: {}", idCard, e.getMessage());
            try {
                PersonIndexEntity entity = new PersonIndexEntity();
                entity.setIdCard(idCard);
                entity.setLocationStatus("FAILED");
                personIndexMapper.updateById(entity);
            } catch (Exception ex) {
                log.error("Failed to update location status: {}", ex.getMessage());
            }
        }
    }
    
    private Map<String, String> getFieldMapping(String moduleCode) {
        Map<String, String> mapping = new HashMap<>();
        try {
            String sql = "SELECT MODULE_FIELD_CODE, COMMON_FIELD_CODE FROM T_MODULE_FIELD_MAPPING WHERE MODULE_CODE = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, moduleCode);
            for (Map<String, Object> row : rows) {
                String moduleField = row.get("MODULE_FIELD_CODE").toString();
                String commonField = row.get("COMMON_FIELD_CODE").toString();
                mapping.put(commonField, moduleField);
            }
        } catch (Exception e) {
            log.warn("Failed to get field mapping for {}: {}", moduleCode, e.getMessage());
        }
        
        // Add default Chinese field name mappings if not present
        addDefaultChineseMappings(mapping);
        
        return mapping;
    }
    
    private void addDefaultChineseMappings(Map<String, String> mapping) {
        // Common field mappings for Chinese field names (production file format)
        Map<String, String> chineseMappings = new HashMap<>();
        chineseMappings.put("ID_CARD", "证件号码");
        chineseMappings.put("NAME", "姓名");
        chineseMappings.put("GENDER", "性别");
        chineseMappings.put("DISTRICT", "区县");
        chineseMappings.put("STREET", "街道");
        chineseMappings.put("COMMITTEE", "居委");
        chineseMappings.put("CONTACT_ADDRESS", "联系地址");
        chineseMappings.put("HOUSEHOLD_ADDRESS", "户口地址");
        chineseMappings.put("PHONE", "联系电话");
        chineseMappings.put("DISABILITY_CATEGORY", "残疾类别");
        chineseMappings.put("DISABILITY_LEVEL", "残疾等级");
        chineseMappings.put("DISABILITY_CERT_NO", "残疾证号");
        chineseMappings.put("DISABILITY_STATUS", "状态");
        
        // Only add if not already present in mapping
        chineseMappings.forEach((commonField, chineseField) -> {
            if (!mapping.containsKey(commonField) || mapping.get(commonField).equals(commonField)) {
                mapping.put(commonField, chineseField);
            }
        });
    }
    
    public List<PersonIndexEntity> getPersonsWithLocation() {
        LambdaQueryWrapper<PersonIndexEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(PersonIndexEntity::getLongitude)
               .isNotNull(PersonIndexEntity::getLatitude)
               .eq(PersonIndexEntity::getLocationStatus, "SUCCESS");
        return personIndexMapper.selectList(wrapper);
    }
    
    public List<PersonIndexEntity> getAllPersons() {
        return personIndexMapper.selectList(null);
    }
    
    public Map<String, Long> getCountByStreet() {
        String sql = "SELECT STREET, COUNT(*) as CNT FROM T_PERSON_INDEX WHERE STREET IS NOT NULL GROUP BY STREET ORDER BY CNT DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(row.get("STREET").toString(), ((Number) row.get("CNT")).longValue());
        }
        return result;
    }
    
    public Map<String, Long> getCountByCategory() {
        String sql = "SELECT DISABILITY_CATEGORY, COUNT(*) as CNT FROM T_PERSON_INDEX WHERE DISABILITY_CATEGORY IS NOT NULL GROUP BY DISABILITY_CATEGORY ORDER BY CNT DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(row.get("DISABILITY_CATEGORY").toString(), ((Number) row.get("CNT")).longValue());
        }
        return result;
    }
    
    public long getTotalCount() {
        return personIndexMapper.selectCount(null);
    }
    
    public PersonIndexEntity getByIdCard(String idCard) {
        return personIndexMapper.selectById(idCard);
    }

    public Page<PersonIndexEntity> search(PolicyConditions conditions, int pageNo, int pageSize) {
        LambdaQueryWrapper<PersonIndexEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (conditions != null) {
            List<String> categories = conditions.disabilityCategories;
            List<String> levels = conditions.disabilityLevels;
            List<Long> districts = conditions.districtIds;
            
            if (categories != null && !categories.isEmpty()) {
                wrapper.in(PersonIndexEntity::getDisabilityCategory, categories);
            }
            if (levels != null && !levels.isEmpty()) {
                wrapper.in(PersonIndexEntity::getDisabilityLevel, levels);
            }
            if (districts != null && !districts.isEmpty()) {
                wrapper.in(PersonIndexEntity::getDistrict, districts.stream()
                    .map(String::valueOf).toList());
            }
        }
        
        wrapper.orderByDesc(PersonIndexEntity::getLastSeenAt);
        
        Page<PersonIndexEntity> page = new Page<>(pageNo, pageSize);
        return personIndexMapper.selectPage(page, wrapper);
    }

    public Map<String, Object> getFullDetail(String idCard) {
        Map<String, Object> result = new HashMap<>();
        
        PersonIndexEntity basic = personIndexMapper.selectById(idCard);
        if (basic == null) {
            return result;
        }
        
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("name", basic.getName());
        basicInfo.put("gender", basic.getGender());
        basicInfo.put("age", basic.getAge());
        basicInfo.put("idCard", basic.getIdCard());
        basicInfo.put("phone", basic.getPhone());
        basicInfo.put("district", basic.getDistrict());
        basicInfo.put("street", basic.getStreet());
        basicInfo.put("committee", basic.getCommittee());
        basicInfo.put("contactAddress", basic.getContactAddress());
        basicInfo.put("householdAddress", basic.getHouseholdAddress());
        result.put("basic", basicInfo);
        
        Map<String, Object> disabilityInfo = new HashMap<>();
        disabilityInfo.put("category", basic.getDisabilityCategory());
        disabilityInfo.put("level", basic.getDisabilityLevel());
        result.put("disability", disabilityInfo);
        
        Map<String, List<Map<String, Object>>> modulesByCategory = new LinkedHashMap<>();
        
        List<String> categories = List.of("基本保障", "医疗康复", "证件管理", "家庭服务", 
            "补贴发放", "就业培训", "辅助服务", "其他服务", "调查统计", "帮扶服务");
        for (String cat : categories) {
            modulesByCategory.put(cat, new ArrayList<>());
        }
        
        String[] tableNames = {
            "T_IMP_DISABLED_CERT", "T_IMP_DISABLED_MGMT", "T_IMP_PENSION_SUBSIDY",
            "T_IMP_REHAB_SUBSIDY", "T_IMP_CHRONIC_DISEASE", "T_IMP_INSTITUTION_CARE",
            "T_IMP_COCHLEAR_IMPLANT", "T_IMP_REHAB_ORTHODONTICS", "T_IMP_CHILD_REHAB",
            "T_IMP_HEALTH_CHECK", "T_IMP_PERSONAL_CERT", "T_IMP_FAMILY_SITUATION",
            "T_IMP_TRAFFIC_SUBSIDY", "T_IMP_PRECISE_HELP", "T_IMP_TWO_SUBSIDIES",
            "T_IMP_FESTIVAL_CONDOLENCE", "T_IMP_TEMPORARY_AID", "T_IMP_EMPLOYMENT",
            "T_IMP_VOCATIONAL_TRAINING", "T_IMP_ASSISTIVE_DEVICE", "T_IMP_LOAN_CARD",
            "T_IMP_DISABLED_APPLICATION", "T_IMP_SUNSHINE_INSTITUTION", "T_IMP_MEILUN_CAR",
            "T_IMP_EDUCATION_AID", "T_IMP_BASIC_SURVEY", "T_IMP_PAIRING_HELP"
        };
        
        String[] moduleCodes = {
            "disabled_cert", "disabled_mgmt", "pension_subsidy",
            "rehab_subsidy", "chronic_disease", "institution_care",
            "cochlear_implant", "rehab_orthodontics", "child_rehab",
            "health_check", "personal_cert", "family_situation",
            "traffic_subsidy", "precise_help", "two_subsidies",
            "festival_condolence", "temporary_aid", "employment",
            "vocational_training", "assistive_device", "loan_card",
            "disabled_application", "sunshine_institution", "meilun_car",
            "education_aid", "basic_survey", "pairing_help"
        };
        
        String[] moduleNames = {
            "残疾人证", "残疾管理", "养老补助",
            "康复医疗/器材补助", "慢性病/康护", "机构养护",
            "人工耳蜗", "康复体矫", "儿童康复矫正",
            "体检", "个人证管理", "残疾家庭情况",
            "交通补贴", "精准帮扶", "两项补贴",
            "节日慰问金", "临时困难金", "就业",
            "职业技能培训", "辅助器具", "借信卡",
            "残疾人申请", "阳光机构", "美伦车",
            "助学", "残疾人基本状况调查", "结对帮扶"
        };
        
        String[] categoriesArr = {
            "基本保障", "基本保障", "基本保障",
            "医疗康复", "医疗康复", "医疗康复",
            "医疗康复", "医疗康复", "医疗康复",
            "医疗康复", "证件管理", "家庭服务",
            "补贴发放", "补贴发放", "补贴发放",
            "补贴发放", "补贴发放", "就业培训",
            "就业培训", "辅助服务", "其他服务",
            "其他服务", "其他服务", "其他服务",
            "其他服务", "调查统计", "帮扶服务"
        };
        
        for (int i = 0; i < tableNames.length; i++) {
            try {
                String tableName = tableNames[i];
                String sql = "SELECT * FROM " + tableName + " WHERE ID_CARD = ?";
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, idCard);
                
                if (!rows.isEmpty()) {
                    Map<String, Object> moduleData = new HashMap<>();
                    moduleData.put("moduleCode", moduleCodes[i]);
                    moduleData.put("moduleName", moduleNames[i]);
                    moduleData.put("hasData", true);
                    moduleData.put("data", rows);
                    
                    // 获取配置的展示字段及其值
                    List<Map<String, Object>> displayFields = getDisplayFields(moduleCodes[i]);
                    List<Map<String, Object>> fieldValues = new ArrayList<>();
                    Map<String, Object> firstRow = rows.get(0);
                    
                    for (Map<String, Object> field : displayFields) {
                        String fieldName = (String) field.get("FIELD_NAME");
                        String fieldCode = (String) field.get("FIELD_CODE");
                        Object value = firstRow.get(fieldName);
                        if (value == null) {
                            value = firstRow.get(fieldCode);
                        }
                        Map<String, Object> fv = new LinkedHashMap<>();
                        fv.put("name", fieldName);
                        fv.put("value", value != null ? value : "");
                        fieldValues.add(fv);
                    }
                    moduleData.put("fieldValues", fieldValues);
                    
                    String category = categoriesArr[i];
                    modulesByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(moduleData);
                }
            } catch (Exception e) {
                log.warn("Failed to query table {}: {}", tableNames[i], e.getMessage());
            }
        }
        
        Map<String, List<Map<String, Object>>> modules = new LinkedHashMap<>();
        for (String cat : categories) {
            List<Map<String, Object>> list = modulesByCategory.get(cat);
            if (list != null && !list.isEmpty()) {
                modules.put(cat, list);
            }
        }
        result.put("modules", modules);
        
        return result;
    }
    
    private String generateSummary(String moduleName, Map<String, Object> data) {
        if (data == null) return "";
        try {
            return switch (moduleName) {
                case "残疾人证" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    String date = (String) data.getOrDefault("初次领证日期", data.getOrDefault("FIRST_APPLY_DATE", ""));
                    yield "状态: " + status + (date.isEmpty() ? "" : ", 初次领证: " + date);
                }
                case "残疾管理" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    String plate = (String) data.getOrDefault("车牌号", data.getOrDefault("PLATE_NO", ""));
                    String annual = (String) data.getOrDefault("年检状态", data.getOrDefault("ANNUAL_INSPECTION", ""));
                    String result = "状态: " + status;
                    if (!plate.isEmpty()) result += ", 车牌: " + plate;
                    if (!annual.isEmpty()) result += ", 年检: " + annual;
                    yield result;
                }
                case "养老补助" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    yield "状态: " + status + (amount != null ? ", 金额: " + amount : "");
                }
                case "康复医疗/器材补助" -> {
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    String date = (String) data.getOrDefault("发放日期", data.getOrDefault("GRANT_DATE", ""));
                    yield (amount != null ? "金额: " + amount : "有数据") + (date.isEmpty() ? "" : ", 日期: " + date);
                }
                case "慢性病/康护" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "机构养护" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "人工耳蜗" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "康复体矫" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "儿童康复矫正" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "体检" -> {
                    String date = (String) data.getOrDefault("体检日期", data.getOrDefault("CHECK_DATE", ""));
                    String result = (String) data.getOrDefault("体检结果", data.getOrDefault("CHECK_RESULT", ""));
                    yield (date.isEmpty() ? "未体检" : date) + (result.isEmpty() ? "" : ", 结果: " + result);
                }
                case "个人证管理" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "残疾家庭情况" -> {
                    yield "有数据";
                }
                case "交通补贴" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "精准帮扶" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "两项补贴" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    yield "状态: " + status + (amount != null ? ", 金额: " + amount : "");
                }
                case "节日慰问金" -> {
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    String date = (String) data.getOrDefault("发放日期", data.getOrDefault("GRANT_DATE", ""));
                    yield (amount != null ? "金额: " + amount : "有数据") + (date.isEmpty() ? "" : ", " + date);
                }
                case "临时困难金" -> {
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    yield amount != null ? "金额: " + amount : "有数据";
                }
                case "就业" -> {
                    String employment = (String) data.getOrDefault("就业状态", data.getOrDefault("EMPLOYMENT_STATUS", ""));
                    String unit = (String) data.getOrDefault("单位名称", data.getOrDefault("UNIT_NAME", ""));
                    yield (employment.isEmpty() ? "无" : employment) + (unit.isEmpty() ? "" : ", 单位: " + unit);
                }
                case "职业技能培训" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "辅助器具" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "借信卡" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "残疾人申请" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "阳光机构" -> {
                    String name = (String) data.getOrDefault("机构名称", data.getOrDefault("INSTITUTION_NAME", ""));
                    yield name.isEmpty() ? "有数据" : name;
                }
                case "美伦车" -> {
                    String status = (String) data.getOrDefault("状态", data.getOrDefault("STATUS", ""));
                    yield "状态: " + status;
                }
                case "助学" -> {
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    yield amount != null ? "金额: " + amount : "有数据";
                }
                case "残疾人基本状况调查" -> {
                    String date = (String) data.getOrDefault("调查日期", data.getOrDefault("SURVEY_DATE", ""));
                    yield date.isEmpty() ? "有数据" : "调查日期: " + date;
                }
                case "结对帮扶" -> {
                    String name = (String) data.getOrDefault("帮扶人", data.getOrDefault("HELPER_NAME", ""));
                    yield name.isEmpty() ? "有数据" : "帮扶人: " + name;
                }
                default -> {
                    Object amount = data.get("补助金额") != null ? data.get("补助金额") : data.get("SUBSIDY_AMOUNT");
                    if (amount != null) yield "金额: " + amount;
                    yield "有数据";
                }
            };
        } catch (Exception e) {
            return "有数据";
        }
    }
    
    private List<Map<String, Object>> getDisplayFields(String moduleCode) {
        String sql = "SELECT FIELD_CODE, FIELD_NAME FROM T_IMPORT_MODULE_FIELD WHERE MODULE_CODE = ? AND IS_DISPLAY = 1 ORDER BY SORT_ORDER";
        return jdbcTemplate.queryForList(sql, moduleCode);
    }
}
