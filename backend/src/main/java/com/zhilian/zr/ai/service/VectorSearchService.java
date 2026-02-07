package com.zhilian.zr.ai.service;

import com.zhilian.zr.ai.entity.VectorDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VectorSearchService {
    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final DashScopeEmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    public VectorSearchService(DashScopeEmbeddingService embeddingService, JdbcTemplate jdbcTemplate) {
        this.embeddingService = embeddingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void indexPerson(Long personId, String text) {
        try {
            List<Float> vector = embeddingService.embed(text);
            if (vector.isEmpty()) {
                log.warn("Failed to generate embedding for person: {}", personId);
                return;
            }

            String vectorJson = JSON.toJSONString(vector);
            jdbcTemplate.update(
                "merge into t_person_vector (person_id, vector, updated_at) key (person_id) values (?, ?, now())",
                personId, vectorJson
            );
            log.info("Indexed person: {} with vector dimension: {}", personId, vector.size());
        } catch (Exception e) {
            log.error("Error indexing person: {}", personId, e);
        }
    }

    public void indexAnomalyCase(Long caseId, String text) {
        try {
            List<Float> vector = embeddingService.embed(text);
            if (vector.isEmpty()) {
                log.warn("Failed to generate embedding for anomaly case: {}", caseId);
                return;
            }

            String vectorJson = JSON.toJSONString(vector);
            jdbcTemplate.update(
                "merge into t_anomaly_case_vector (case_id, vector, updated_at) key (case_id) values (?, ?, now())",
                caseId, vectorJson
            );
            log.info("Indexed anomaly case: {} with vector dimension: {}", caseId, vector.size());
        } catch (Exception e) {
            log.error("Error indexing anomaly case: {}", caseId, e);
        }
    }

    public List<VectorDoc> searchSimilar(String query, int limit) {
        try {
            List<Float> queryVector = embeddingService.embed(query);
            if (queryVector.isEmpty()) {
                log.warn("Failed to generate embedding for query: {}", query);
                return Collections.emptyList();
            }

            String vectorJson = JSON.toJSONString(queryVector);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "select id, type, content, score, vector " +
                "from (" +
                "  select person_id as id, 'person' as type, concat('person:', name) as content, " +
                "         cosine_similarity(?, vector) as score, vector " +
                "  from t_person_vector pv " +
                "  join t_person p on p.person_id = pv.person_id " +
                "  union all " +
                "  select case_id as id, 'anomaly_case' as type, concat('case:', title) as content, " +
                "         cosine_similarity(?, vector) as score, vector " +
                "  from t_anomaly_case_vector acv " +
                "  join t_anomaly_case ac on ac.case_id = acv.case_id " +
                ") combined " +
                "order by score desc " +
                "limit ?",
                vectorJson, vectorJson, limit
            );

            List<VectorDoc> docs = new ArrayList<>();
            for (Map<String, Object> row : results) {
                VectorDoc doc = new VectorDoc();
                doc.setId(((Number) row.get("id")).longValue());
                doc.setType((String) row.get("type"));
                doc.setContent((String) row.get("content"));
                doc.setScore(((Number) row.get("score")).doubleValue());
                docs.add(doc);
            }

            return docs;
        } catch (Exception e) {
            log.error("Error searching similar documents", e);
            return Collections.emptyList();
        }
    }

    public List<VectorDoc> searchByPerson(String query, Long personId, int limit) {
        try {
            List<Float> queryVector = embeddingService.embed(query);
            if (queryVector.isEmpty()) {
                return Collections.emptyList();
            }

            String vectorJson = JSON.toJSONString(queryVector);

            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "select case_id as id, 'anomaly_case' as type, concat('case:', title) as content, " +
                "       cosine_similarity(?, vector) as score " +
                "from t_anomaly_case_vector acv " +
                "join t_anomaly_case ac on ac.case_id = acv.case_id " +
                "where ac.person_id = ? " +
                "order by score desc " +
                "limit ?",
                vectorJson, personId, limit
            );

            List<VectorDoc> docs = new ArrayList<>();
            for (Map<String, Object> row : results) {
                VectorDoc doc = new VectorDoc();
                doc.setId(((Number) row.get("id")).longValue());
                doc.setType((String) row.get("type"));
                doc.setContent((String) row.get("content"));
                doc.setScore(((Number) row.get("score")).doubleValue());
                docs.add(doc);
            }

            return docs;
        } catch (Exception e) {
            log.error("Error searching by person", e);
            return Collections.emptyList();
        }
    }

    public void deletePersonVector(Long personId) {
        jdbcTemplate.update("delete from t_person_vector where person_id = ?", personId);
    }

    public void deleteAnomalyCaseVector(Long caseId) {
        jdbcTemplate.update("delete from t_anomaly_case_vector where case_id = ?", caseId);
    }
}
