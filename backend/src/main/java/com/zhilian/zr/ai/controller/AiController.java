package com.zhilian.zr.ai.controller;

import com.zhilian.zr.ai.entity.VectorDoc;
import com.zhilian.zr.ai.service.AiChatService;
import com.zhilian.zr.ai.service.AnomalyCaseService;
import com.zhilian.zr.ai.service.VectorSearchService;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.security.CurrentUser;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiChatService aiChatService;
    private final VectorSearchService vectorSearchService;
    private final AnomalyCaseService anomalyCaseService;

    public AiController(AiChatService aiChatService, VectorSearchService vectorSearchService,
                      AnomalyCaseService anomalyCaseService) {
        this.aiChatService = aiChatService;
        this.vectorSearchService = vectorSearchService;
        this.anomalyCaseService = anomalyCaseService;
    }

    public record ChatRequest(
        @NotBlank String query,
        Long personId
    ) {
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAuthority('ai:chat')")
    public ApiResponse<String> chat(@RequestBody ChatRequest req) {
        String response = aiChatService.chat(req.query(), req.personId());
        return ApiResponse.ok(response);
    }

    public record SearchRequest(@NotBlank String query, Integer limit) {
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('ai:search')")
    public ApiResponse<List<VectorDoc>> search(@RequestBody SearchRequest req) {
        int limit = req.limit() != null && req.limit() > 0 ? req.limit() : 10;
        List<VectorDoc> results = vectorSearchService.searchSimilar(req.query(), limit);
        return ApiResponse.ok(results);
    }

    public record IndexPersonRequest(Long personId, @NotBlank String text) {
    }

    @PostMapping("/index/person")
    @PreAuthorize("hasAuthority('ai:index')")
    public ApiResponse<Void> indexPerson(@RequestBody IndexPersonRequest req) {
        vectorSearchService.indexPerson(req.personId(), req.text());
        return ApiResponse.ok(null);
    }

    public record SearchPersonsRequest(@NotBlank String keyword, Integer limit) {
    }

    @PostMapping("/search/persons")
    @PreAuthorize("hasAuthority('ai:search')")
    public ApiResponse<List<PersonIndexEntity>> searchPersons(@RequestBody SearchPersonsRequest req) {
        int limit = req.limit() != null && req.limit() > 0 ? req.limit() : 10;
        List<PersonIndexEntity> results = vectorSearchService.searchPersons(req.keyword(), limit);
        return ApiResponse.ok(results);
    }

    public record CreateCaseRequest(
        Long personId,
        @NotBlank String title,
        String description,
        String anomalyType,
        Integer severity
    ) {
    }

    @PostMapping("/cases")
    @PreAuthorize("hasAuthority('ai:case:create')")
    public ApiResponse<Long> createCase(@RequestBody CreateCaseRequest req) {
        var caseEntity = anomalyCaseService.createCase(
            req.personId(),
            req.title(),
            req.description(),
            req.anomalyType(),
            req.severity()
        );
        return ApiResponse.ok(caseEntity.getCaseId());
    }

    @GetMapping("/cases/{caseId}")
    @PreAuthorize("hasAuthority('ai:case:view')")
    public ApiResponse<?> getCase(@PathVariable Long caseId) {
        var caseEntity = anomalyCaseService.getCase(caseId);
        return ApiResponse.ok(caseEntity);
    }

    @GetMapping("/cases/person/{personId}")
    @PreAuthorize("hasAuthority('ai:case:view')")
    public ApiResponse<List<?>> getCasesByPerson(@PathVariable Long personId) {
        var cases = anomalyCaseService.listCasesByPerson(personId);
        return ApiResponse.ok(cases);
    }

    public record ResolveCaseRequest(@NotBlank String resolution) {
    }

    @PutMapping("/cases/{caseId}/resolve")
    @PreAuthorize("hasAuthority('ai:case:resolve')")
    public ApiResponse<Void> resolveCase(@PathVariable Long caseId, @RequestBody ResolveCaseRequest req) {
        Long currentUserId = CurrentUser.userId();
        anomalyCaseService.updateResolution(caseId, req.resolution(), currentUserId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/cases/{caseId}")
    @PreAuthorize("hasAuthority('ai:case:delete')")
    public ApiResponse<Void> deleteCase(@PathVariable Long caseId) {
        anomalyCaseService.deleteCase(caseId);
        return ApiResponse.ok(null);
    }
}
