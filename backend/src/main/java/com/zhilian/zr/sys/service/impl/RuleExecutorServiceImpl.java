package com.zhilian.zr.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.anomaly.entity.AnomalySnapshotEntity;
import com.zhilian.zr.anomaly.mapper.AnomalySnapshotMapper;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.RuleEntity;
import com.zhilian.zr.sys.mapper.RuleMapper;
import com.zhilian.zr.sys.service.RuleExecutorService;
import com.zhilian.zr.sys.service.RuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class RuleExecutorServiceImpl implements RuleExecutorService {

    private static final Logger log = LoggerFactory.getLogger(RuleExecutorServiceImpl.class);

    private final RuleMapper ruleMapper;
    private final RuleService ruleService;
    private final JdbcTemplate jdbcTemplate;
    private final AnomalySnapshotMapper anomalySnapshotMapper;

    public RuleExecutorServiceImpl(RuleMapper ruleMapper, RuleService ruleService,
                                  JdbcTemplate jdbcTemplate, AnomalySnapshotMapper anomalySnapshotMapper) {
        this.ruleMapper = ruleMapper;
        this.ruleService = ruleService;
        this.jdbcTemplate = jdbcTemplate;
        this.anomalySnapshotMapper = anomalySnapshotMapper;
    }

    @Override
    @Transactional
    public void executeRule(RuleEntity rule) {
        if (rule.getEnabled() == null || rule.getEnabled() != 1) {
            log.info("Rule {} is disabled, skipping execution", rule.getRuleCode());
            return;
        }

        log.info("Executing rule: {} - {}", rule.getRuleCode(), rule.getName());

        try {
            List<Map<String, Object>> persons = jdbcTemplate.queryForList("select person_id, name, id_no, birth_date from t_person");

            for (Map<String, Object> person : persons) {
                if (evaluateRule(rule, person)) {
                    saveSnapshot(rule, person);
                }
            }

            ruleService.updateLastExecutedAt(rule.getRuleId());
            log.info("Rule {} executed successfully, processed {} persons", rule.getRuleCode(), persons.size());
        } catch (Exception e) {
            log.error("Error executing rule {}: {}", rule.getRuleCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to execute rule: " + rule.getRuleCode(), e);
        }
    }

    private boolean evaluateRule(RuleEntity rule, Map<String, Object> person) {
        String condition = rule.getRuleCondition();
        if (condition == null || condition.isBlank()) {
            return false;
        }

        String conditionLower = condition.toLowerCase();

        switch (conditionLower) {
            case "age_over_80":
                Object birthDate = person.get("birth_date");
                return birthDate != null && calculateAge(birthDate.toString()) > 80;
            case "high_disability_level":
                Long personId = ((Number) person.get("person_id")).longValue();
                return checkHighDisabilityLevel(personId);
            case "has_caregiver":
                return checkHasCaregiver(((Number) person.get("person_id")).longValue());
            case "lives_alone":
                return checkLivesAlone(((Number) person.get("person_id")).longValue());
            case "recent_hospitalization":
                return checkRecentHospitalization(((Number) person.get("person_id")).longValue());
            default:
                log.warn("Unknown rule condition: {}", condition);
                return false;
        }
    }

    private int calculateAge(String birthDate) {
        try {
            if (birthDate == null || birthDate.length() < 4) return 0;
            int birthYear = Integer.parseInt(birthDate.substring(0, 4));
            int currentYear = Instant.now().atZone(java.time.ZoneId.systemDefault()).getYear();
            return currentYear - birthYear;
        } catch (Exception e) {
            log.error("Error calculating age for birth date: {}", birthDate, e);
            return 0;
        }
    }

    private boolean checkHighDisabilityLevel(Long personId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "select count(*) from t_disability_card where person_id = ? and level_code in ('3', '4')",
                Integer.class,
                personId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking disability level for person: {}", personId, e);
            return false;
        }
    }

    private boolean checkHasCaregiver(Long personId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "select count(*) from t_caregiver where person_id = ? and status = 1",
                Integer.class,
                personId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking caregiver for person: {}", personId, e);
            return false;
        }
    }

    private boolean checkLivesAlone(Long personId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "select count(*) from t_household where person_id = ? and lives_alone = 1",
                Integer.class,
                personId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking living alone for person: {}", personId, e);
            return false;
        }
    }

    private boolean checkRecentHospitalization(Long personId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "select count(*) from t_hospitalization where person_id = ? and admission_date >= dateadd(month, -3, getdate())",
                Integer.class,
                personId
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error checking recent hospitalization for person: {}", personId, e);
            return false;
        }
    }

    private void saveSnapshot(RuleEntity rule, Map<String, Object> person) {
        AnomalySnapshotEntity snapshot = new AnomalySnapshotEntity();
        snapshot.setSnapshotId(IdGenerator.nextId());
        snapshot.setRuleId(rule.getRuleId());
        snapshot.setRuleCode(rule.getRuleCode());
        snapshot.setRuleName(rule.getName());
        snapshot.setPersonId(((Number) person.get("person_id")).longValue());
        snapshot.setPersonName(person.get("name") != null ? person.get("name").toString() : "");
        snapshot.setAnomalyType(rule.getRuleType());
        snapshot.setSeverity(rule.getSeverity());
        snapshot.setConditionData(rule.getRuleCondition());
        snapshot.setMatchedValues(person.toString());
        snapshot.setCreatedAt(Instant.now());
        anomalySnapshotMapper.insert(snapshot);
        log.debug("Saved snapshot for rule {} and person {}", rule.getRuleCode(), person.get("name"));
    }

    @Override
    public void executeAllRules() {
        log.info("Starting execution of all active rules");
        List<RuleEntity> activeRules = getActiveRules();
        for (RuleEntity rule : activeRules) {
            try {
                executeRule(rule);
            } catch (Exception e) {
                log.error("Failed to execute rule {}: {}", rule.getRuleCode(), e.getMessage(), e);
            }
        }
        log.info("Completed execution of {} rules", activeRules.size());
    }

    @Override
    public List<RuleEntity> getActiveRules() {
        return ruleMapper.selectList(new LambdaQueryWrapper<RuleEntity>()
            .eq(RuleEntity::getEnabled, 1)
            .orderByAsc(RuleEntity::getPriority)
            .orderByAsc(RuleEntity::getRuleCode));
    }
}
