package com.zhilian.zr.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class VectorIndexingService {
    private static final Logger log = LoggerFactory.getLogger(VectorIndexingService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DashScopeEmbeddingService embeddingService;

    public VectorIndexingService(JdbcTemplate jdbcTemplate, DashScopeEmbeddingService embeddingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void indexPendingPersons() {
        log.info("Starting indexing of pending persons");

        List<Map<String, Object>> pendingPersons = jdbcTemplate.queryForList(
            "select p.person_id, p.name, p.id_no, pd.category_name, pd.level_name, p.district_id, d.name as district_name, s.name as street_name, p.address " +
            "from t_person p " +
            "left join t_person_vector pv on pv.person_id = p.person_id " +
            "left join t_disability_card dc on dc.person_id = p.person_id " +
            "left join t_dict pd on pd.dict_type = 'disability_category' and pd.dict_code = dc.category_code " +
            "left join t_dict pl on pl.dict_type = 'disability_level' and pl.dict_code = dc.level_code " +
            "left join t_district d on d.id = p.district_id " +
            "left join t_street s on s.id = p.street_id " +
            "where pv.person_id is null " +
            "limit 100"
        );

        for (Map<String, Object> person : pendingPersons) {
            Long personId = ((Number) person.get("person_id")).longValue();
            String name = (String) person.get("name");
            String idNo = (String) person.get("id_no");
            String category = (String) person.get("category_name");
            String level = (String) person.get("level_name");
            String district = (String) person.get("district_name");
            String street = (String) person.get("street_name");
            String address = (String) person.get("address");

            String text = String.format("姓名: %s, 身份证号: %s, 残疾类别: %s, 残疾等级: %s, 地区: %s, 街道: %s, 地址: %s",
                name, idNo, category, level, district, street, address);

            try {
                embeddingService.embed(text);
                jdbcTemplate.update(
                    "merge into t_person_vector (person_id, vector, updated_at) key (person_id) values (?, ?, now())",
                    personId, text
                );
                log.info("Indexed person: {} - {}", personId, name);
            } catch (Exception e) {
                log.error("Error indexing person: {}", personId, e);
            }
        }

        log.info("Completed indexing of {} pending persons", pendingPersons.size());
    }

    @Scheduled(fixedRate = 7200000)
    @Transactional
    public void rebuildIndex() {
        log.info("Starting full vector index rebuild");

        List<Map<String, Object>> persons = jdbcTemplate.queryForList(
            "select p.person_id, p.name, p.id_no, pd.category_name, pd.level_name, p.district_id, d.name as district_name, s.name as street_name, p.address " +
            "from t_person p " +
            "left join t_disability_card dc on dc.person_id = p.person_id " +
            "left join t_dict pd on pd.dict_type = 'disability_category' and pd.dict_code = dc.category_code " +
            "left join t_dict pl on pl.dict_type = 'disability_level' and pl.dict_code = dc.level_code " +
            "left join t_district d on d.id = p.district_id " +
            "left join t_street s on s.id = p.street_id " +
            "limit 1000"
        );

        for (Map<String, Object> person : persons) {
            Long personId = ((Number) person.get("person_id")).longValue();
            String name = (String) person.get("name");
            String idNo = (String) person.get("id_no");
            String category = (String) person.get("category_name");
            String level = (String) person.get("level_name");
            String district = (String) person.get("district_name");
            String street = (String) person.get("street_name");
            String address = (String) person.get("address");

            String text = String.format("姓名: %s, 身份证号: %s, 残疾类别: %s, 残疾等级: %s, 地区: %s, 街道: %s, 地址: %s",
                name, idNo, category, level, district, street, address);

            try {
                embeddingService.embed(text);
                jdbcTemplate.update(
                    "merge into t_person_vector (person_id, vector, updated_at) key (person_id) values (?, ?, now())",
                    personId, text
                );
            } catch (Exception e) {
                log.error("Error indexing person: {}", personId, e);
            }
        }

        log.info("Completed full vector index rebuild for {} persons", persons.size());
    }

    public void indexPerson(Long personId) {
        List<Map<String, Object>> persons = jdbcTemplate.queryForList(
            "select p.person_id, p.name, p.id_no, pd.category_name, pd.level_name, p.district_id, d.name as district_name, s.name as street_name, p.address " +
            "from t_person p " +
            "left join t_disability_card dc on dc.person_id = p.person_id " +
            "left join t_dict pd on pd.dict_type = 'disability_category' and pd.dict_code = dc.category_code " +
            "left join t_dict pl on pl.dict_type = 'disability_level' and pl.dict_code = dc.level_code " +
            "left join t_district d on d.id = p.district_id " +
            "left join t_street s on s.id = p.street_id " +
            "where p.person_id = ?",
            personId
        );

        if (!persons.isEmpty()) {
            log.warn("Person not found: {}", personId);
            return;
        }

        Map<String, Object> person = persons.get(0);
        String name = (String) person.get("name");
        String idNo = (String) person.get("id_no");
        String category = (String) person.get("category_name");
        String level = (String) person.get("level_name");
        String district = (String) person.get("district_name");
        String street = (String) person.get("street_name");
        String address = (String) person.get("address");

        String text = String.format("姓名: %s, 身份证号: %s, 残疾类别: %s, 残疾等级: %s, 地区: %s, 街道: %s, 地址: %s",
            name, idNo, category, level, district, street, address);

        try {
            embeddingService.embed(text);
            jdbcTemplate.update(
                "merge into t_person_vector (person_id, vector, updated_at) key (person_id) values (?, ?, now())",
                personId, text
            );
            log.info("Indexed person: {} - {}", personId, name);
        } catch (Exception e) {
            log.error("Error indexing person: {}", personId, e);
        }
    }
}
