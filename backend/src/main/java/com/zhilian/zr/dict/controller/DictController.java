package com.zhilian.zr.dict.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.dict.entity.DictEntryEntity;
import com.zhilian.zr.dict.entity.DistrictEntity;
import com.zhilian.zr.dict.entity.StreetEntity;
import com.zhilian.zr.dict.mapper.DictEntryMapper;
import com.zhilian.zr.dict.mapper.DistrictMapper;
import com.zhilian.zr.dict.mapper.StreetMapper;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dicts")
public class DictController {

    private final DistrictMapper districtMapper;
    private final StreetMapper streetMapper;
    private final DictEntryMapper dictEntryMapper;

    public DictController(DistrictMapper districtMapper, StreetMapper streetMapper, DictEntryMapper dictEntryMapper) {
        this.districtMapper = districtMapper;
        this.streetMapper = streetMapper;
        this.dictEntryMapper = dictEntryMapper;
    }

    @GetMapping("/districts")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<Map<String, Object>>> districts() {
        List<DistrictEntity> list = districtMapper.selectList(new LambdaQueryWrapper<DistrictEntity>().orderByAsc(DistrictEntity::getId));
        return ApiResponse.ok(list.stream().map(d -> Map.<String, Object>of(
            "id", d.getId(),
            "name", d.getName(),
            "code", d.getCode()
        )).toList());
    }

    @GetMapping("/streets")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<Map<String, Object>>> streets(@RequestParam(required = false) List<Long> districtIds) {
        LambdaQueryWrapper<StreetEntity> q = new LambdaQueryWrapper<StreetEntity>()
            .in(districtIds != null && !districtIds.isEmpty(), StreetEntity::getDistrictId, districtIds)
            .orderByAsc(StreetEntity::getId);
        List<StreetEntity> list = streetMapper.selectList(q);
        return ApiResponse.ok(list.stream().map(s -> Map.<String, Object>of(
            "id", s.getId(),
            "districtId", s.getDistrictId(),
            "name", s.getName(),
            "code", s.getCode()
        )).toList());
    }

    @GetMapping("/disability-categories")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<Map<String, Object>>> disabilityCategories() {
        List<DictEntryEntity> list = dictEntryMapper.listByType("disability_category");
        return ApiResponse.ok(list.stream()
            .map(e -> Map.<String, Object>of("code", e.getDictCode(), "name", e.getDictName()))
            .toList());
    }

    @GetMapping("/disability-levels")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<Map<String, Object>>> disabilityLevels() {
        List<DictEntryEntity> list = dictEntryMapper.listByType("disability_level");
        return ApiResponse.ok(list.stream()
            .map(e -> Map.<String, Object>of("code", e.getDictCode(), "name", e.getDictName()))
            .toList());
    }

    @GetMapping("/types")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<DictEntryEntity>> listTypes() {
        return ApiResponse.ok(dictEntryMapper.listTypes());
    }

    @GetMapping("/{type}/items")
    @PreAuthorize("hasAuthority('dict:read')")
    public ApiResponse<List<DictEntryEntity>> listItems(@PathVariable String type, @RequestParam(required = false) Integer status) {
        List<DictEntryEntity> items = dictEntryMapper.selectList(new LambdaQueryWrapper<DictEntryEntity>()
            .eq(DictEntryEntity::getDictType, type)
            .eq(status != null, DictEntryEntity::getStatus, status)
            .orderByAsc(DictEntryEntity::getSort));
        return ApiResponse.ok(items);
    }

    public record CreateItemTypeRequest(
        @NotBlank String type,
        @NotBlank String code,
        @NotBlank String name,
        Integer sort,
        Integer status
    ) {
    }

    @PostMapping("/{type}/items")
    @PreAuthorize("hasAuthority('dict:create')")
    public ApiResponse<DictEntryEntity> createItem(@PathVariable String type, @RequestBody CreateItemTypeRequest req) {
        String id = type + ":" + req.code();
        DictEntryEntity existing = dictEntryMapper.selectById(id);
        if (existing != null) {
            throw new IllegalArgumentException("Dictionary item already exists");
        }

        DictEntryEntity entry = new DictEntryEntity();
        entry.setId(id);
        entry.setDictType(type);
        entry.setDictCode(req.code());
        entry.setDictName(req.name());
        entry.setSort(req.sort() != null ? req.sort() : 0);
        entry.setStatus(req.status() != null ? req.status() : 1);
        dictEntryMapper.insert(entry);
        return ApiResponse.ok(entry);
    }

    public record UpdateItemRequest(String name, Integer sort, Integer status) {
    }

    @PutMapping("/{type}/items/{code}")
    @PreAuthorize("hasAuthority('dict:update')")
    public ApiResponse<DictEntryEntity> updateItem(@PathVariable String type, @PathVariable String code, @RequestBody UpdateItemRequest req) {
        String id = type + ":" + code;
        DictEntryEntity entry = dictEntryMapper.selectById(id);
        if (entry == null) {
            throw new IllegalArgumentException("Dictionary item not found");
        }
        if (req.name() != null) {
            entry.setDictName(req.name());
        }
        if (req.sort() != null) {
            entry.setSort(req.sort());
        }
        if (req.status() != null) {
            entry.setStatus(req.status());
        }
        dictEntryMapper.updateById(entry);
        return ApiResponse.ok(entry);
    }

    @DeleteMapping("/{type}/items/{code}")
    @PreAuthorize("hasAuthority('dict:delete')")
    public ApiResponse<Void> deleteItem(@PathVariable String type, @PathVariable String code) {
        String id = type + ":" + code;
        dictEntryMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{type}/items/{code}/enable")
    @PreAuthorize("hasAuthority('dict:update')")
    public ApiResponse<Void> enableItem(@PathVariable String type, @PathVariable String code) {
        String id = type + ":" + code;
        DictEntryEntity entry = dictEntryMapper.selectById(id);
        if (entry == null) {
            throw new IllegalArgumentException("Dictionary item not found");
        }
        entry.setStatus(1);
        dictEntryMapper.updateById(entry);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{type}/items/{code}/disable")
    @PreAuthorize("hasAuthority('dict:update')")
    public ApiResponse<Void> disableItem(@PathVariable String type, @PathVariable String code) {
        String id = type + ":" + code;
        DictEntryEntity entry = dictEntryMapper.selectById(id);
        if (entry == null) {
            throw new IllegalArgumentException("Dictionary item not found");
        }
        entry.setStatus(0);
        dictEntryMapper.updateById(entry);
        return ApiResponse.ok(null);
    }
}
