package com.zhilian.zr.person.controller;

import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.person.dto.PersonDtos;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.person.service.PersonDetailService;
import com.zhilian.zr.person.service.PersonIndexService;
import com.zhilian.zr.person.service.PersonService;
import com.zhilian.zr.audit.service.AuditService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

  private final PersonService personService;
  private final PersonDetailService personDetailService;
  private final PersonIndexService personIndexService;
  private final AuditService auditService;

  public PersonController(PersonService personService, PersonDetailService personDetailService, 
                         PersonIndexService personIndexService, AuditService auditService) {
    this.personService = personService;
    this.personDetailService = personDetailService;
    this.personIndexService = personIndexService;
    this.auditService = auditService;
  }

  @PostMapping("/search")
  @PreAuthorize("hasAuthority('person:search')")
  public ApiResponse<PageResponse<PersonDtos.PersonRow>> search(@Valid @RequestBody PersonDtos.PersonSearchRequest req) {
    return ApiResponse.ok(personService.search(req));
  }

  @PostMapping("/export")
  @PreAuthorize("hasAuthority('person:export')")
  public ResponseEntity<byte[]> export(@RequestBody PersonDtos.PersonSearchRequest req, HttpServletRequest request) {
    long start = System.currentTimeMillis();
    byte[] bytes = personService.exportCsv(req);
    auditService.log(request, "EXPORT", "persons", req, "OK", System.currentTimeMillis() - start);
    String filename = "persons-" + LocalDate.now() + ".csv";
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .contentType(new MediaType("text", "csv"))
        .body(bytes);
  }

  @GetMapping("/{personId}")
  @PreAuthorize("hasAuthority('person:read')")
  public ApiResponse<PersonDtos.PersonDetail> detail(@PathVariable long personId) {
    return ApiResponse.ok(personDetailService.detail(personId));
  }

  @GetMapping("/{personId}/biz")
  @PreAuthorize("hasAuthority('person:read')")
  public ApiResponse<Map<String, Object>> biz(@PathVariable long personId) {
    return ApiResponse.ok(personDetailService.biz(personId));
  }

  @GetMapping("/{personId}/risks")
  @PreAuthorize("hasAuthority('person:read')")
  public ApiResponse<List<Map<String, Object>>> risks(@PathVariable long personId) {
    return ApiResponse.ok(personDetailService.risks(personId));
  }

  @GetMapping("/by-idcard/{idCard}")
  @PreAuthorize("hasAuthority('person:read')")
  public ApiResponse<PersonIndexEntity> getByIdCard(@PathVariable String idCard) {
    PersonIndexEntity entity = personIndexService.getByIdCard(idCard);
    return ApiResponse.ok(entity);
  }

  @GetMapping("/by-idcard/{idCard}/full-detail")
  @PreAuthorize("hasAuthority('person:read')")
  public ApiResponse<Map<String, Object>> getFullDetail(@PathVariable String idCard) {
    Map<String, Object> detail = personIndexService.getFullDetail(idCard);
    return ApiResponse.ok(detail);
  }
}
