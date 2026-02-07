package com.zhilian.zr.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.ai.entity.AnomalyCaseEntity;
import com.zhilian.zr.ai.mapper.AnomalyCaseMapper;
import com.zhilian.zr.common.ids.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AnomalyCaseService {
    private static final Logger log = LoggerFactory.getLogger(AnomalyCaseService.class);

    private final AnomalyCaseMapper anomalyCaseMapper;
    private final VectorSearchService vectorSearchService;

    public AnomalyCaseService(AnomalyCaseMapper anomalyCaseMapper, VectorSearchService vectorSearchService) {
        this.anomalyCaseMapper = anomalyCaseMapper;
        this.vectorSearchService = vectorSearchService;
    }

    @Transactional
    public AnomalyCaseEntity createCase(Long personId, String title, String description,
                                       String anomalyType, Integer severity) {
        AnomalyCaseEntity caseEntity = new AnomalyCaseEntity();
        caseEntity.setCaseId(IdGenerator.nextId());
        caseEntity.setPersonId(personId);
        caseEntity.setTitle(title);
        caseEntity.setDescription(description);
        caseEntity.setAnomalyType(anomalyType);
        caseEntity.setSeverity(severity);
        caseEntity.setCreatedAt(Instant.now());
        anomalyCaseMapper.insert(caseEntity);

        String text = String.format("%s %s %s %s", title, description, anomalyType, severity);
        vectorSearchService.indexAnomalyCase(caseEntity.getCaseId(), text);

        log.info("Created anomaly case: {} for person: {}", caseEntity.getCaseId(), personId);
        return caseEntity;
    }

    public AnomalyCaseEntity getCase(Long caseId) {
        return anomalyCaseMapper.selectById(caseId);
    }

    public List<AnomalyCaseEntity> listCasesByPerson(Long personId) {
        return anomalyCaseMapper.selectList(new LambdaQueryWrapper<AnomalyCaseEntity>()
            .eq(AnomalyCaseEntity::getPersonId, personId)
            .orderByDesc(AnomalyCaseEntity::getCreatedAt));
    }

    public List<AnomalyCaseEntity> listCasesByType(String anomalyType) {
        return anomalyCaseMapper.selectList(new LambdaQueryWrapper<AnomalyCaseEntity>()
            .eq(AnomalyCaseEntity::getAnomalyType, anomalyType)
            .orderByDesc(AnomalyCaseEntity::getCreatedAt));
    }

    @Transactional
    public AnomalyCaseEntity updateResolution(Long caseId, String resolution, Long handlerUserId) {
        AnomalyCaseEntity caseEntity = anomalyCaseMapper.selectById(caseId);
        if (caseEntity == null) {
            throw new IllegalArgumentException("Anomaly case not found");
        }
        caseEntity.setResolution(resolution);
        caseEntity.setHandlerUserId(handlerUserId);
        caseEntity.setResolvedAt(Instant.now());
        anomalyCaseMapper.updateById(caseEntity);
        return caseEntity;
    }

    @Transactional
    public void deleteCase(Long caseId) {
        vectorSearchService.deleteAnomalyCaseVector(caseId);
        anomalyCaseMapper.deleteById(caseId);
    }
}
