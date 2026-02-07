package com.zhilian.zr.ai.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("t_anomaly_case")
public class AnomalyCaseEntity {
    @TableId
    private Long caseId;
    private Long personId;
    private String title;
    private String description;
    private String anomalyType;
    private Integer severity;
    private String resolution;
    private Long handlerUserId;
    private Instant resolvedAt;
    private Instant createdAt;

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Long getHandlerUserId() {
        return handlerUserId;
    }

    public void setHandlerUserId(Long handlerUserId) {
        this.handlerUserId = handlerUserId;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
