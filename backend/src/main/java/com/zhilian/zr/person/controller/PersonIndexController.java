package com.zhilian.zr.person.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zr.common.api.ApiResponse;
import com.zhilian.zr.person.entity.PersonIndexEntity;
import com.zhilian.zr.person.mapper.PersonIndexMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/person-index")
public class PersonIndexController {

    private final PersonIndexMapper personIndexMapper;

    public PersonIndexController(PersonIndexMapper personIndexMapper) {
        this.personIndexMapper = personIndexMapper;
    }

    @GetMapping("/{idCard}")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<PersonIndexEntity> getByIdCard(@PathVariable String idCard) {
        PersonIndexEntity entity = personIndexMapper.selectById(idCard);
        return ApiResponse.ok(entity);
    }

    public record SearchRequest(
        String name,
        String idCard,
        String street,
        String committee,
        String disabilityCategory,
        String disabilityLevel,
        Integer pageNo,
        Integer pageSize
    ) {}

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('person:search')")
    public ApiResponse<Page<PersonIndexEntity>> search(@RequestBody SearchRequest req) {
        int pageNo = req.pageNo() != null && req.pageNo() > 0 ? req.pageNo() : 1;
        int pageSize = req.pageSize() != null && req.pageSize() > 0 ? req.pageSize() : 20;

        LambdaQueryWrapper<PersonIndexEntity> wrapper = new LambdaQueryWrapper<>();
        
        if (req.name() != null && !req.name().isEmpty()) {
            wrapper.like(PersonIndexEntity::getName, req.name());
        }
        if (req.idCard() != null && !req.idCard().isEmpty()) {
            wrapper.eq(PersonIndexEntity::getIdCard, req.idCard());
        }
        if (req.street() != null && !req.street().isEmpty()) {
            wrapper.eq(PersonIndexEntity::getStreet, req.street());
        }
        if (req.committee() != null && !req.committee().isEmpty()) {
            wrapper.like(PersonIndexEntity::getCommittee, req.committee());
        }
        if (req.disabilityCategory() != null && !req.disabilityCategory().isEmpty()) {
            wrapper.eq(PersonIndexEntity::getDisabilityCategory, req.disabilityCategory());
        }
        if (req.disabilityLevel() != null && !req.disabilityLevel().isEmpty()) {
            wrapper.eq(PersonIndexEntity::getDisabilityLevel, req.disabilityLevel());
        }

        wrapper.orderByDesc(PersonIndexEntity::getLastSeenAt);

        Page<PersonIndexEntity> page = new Page<>(pageNo, pageSize);
        Page<PersonIndexEntity> result = personIndexMapper.selectPage(page, wrapper);
        
        return ApiResponse.ok(result);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<List<PersonIndexEntity>> getAll() {
        List<PersonIndexEntity> list = personIndexMapper.selectList(null);
        return ApiResponse.ok(list);
    }

    @GetMapping("/streets")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<List<String>> getStreets() {
        List<String> streets = personIndexMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PersonIndexEntity>()
                .select(PersonIndexEntity::getStreet)
                .isNotNull(PersonIndexEntity::getStreet)
                .groupBy(PersonIndexEntity::getStreet)
        ).stream().map(PersonIndexEntity::getStreet).filter(s -> s != null && !s.isEmpty()).sorted().toList();
        return ApiResponse.ok(streets);
    }

    @GetMapping("/committees")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<List<String>> getCommittees(
            @RequestParam(required = false) String street) {
        LambdaQueryWrapper<PersonIndexEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PersonIndexEntity::getCommittee)
              .isNotNull(PersonIndexEntity::getCommittee);
        if (street != null && !street.isEmpty()) {
            wrapper.eq(PersonIndexEntity::getStreet, street);
        }
        wrapper.groupBy(PersonIndexEntity::getCommittee);
        List<String> committees = personIndexMapper.selectList(wrapper)
            .stream().map(PersonIndexEntity::getCommittee)
            .filter(s -> s != null && !s.isEmpty()).sorted().toList();
        return ApiResponse.ok(committees);
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<List<String>> getCategories() {
        List<String> categories = personIndexMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PersonIndexEntity>()
                .select(PersonIndexEntity::getDisabilityCategory)
                .isNotNull(PersonIndexEntity::getDisabilityCategory)
                .groupBy(PersonIndexEntity::getDisabilityCategory)
        ).stream().map(PersonIndexEntity::getDisabilityCategory).filter(s -> s != null && !s.isEmpty()).sorted().toList();
        return ApiResponse.ok(categories);
    }

    @GetMapping("/levels")
    @PreAuthorize("hasAuthority('person:read')")
    public ApiResponse<List<String>> getLevels() {
        List<String> levels = personIndexMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PersonIndexEntity>()
                .select(PersonIndexEntity::getDisabilityLevel)
                .isNotNull(PersonIndexEntity::getDisabilityLevel)
                .groupBy(PersonIndexEntity::getDisabilityLevel)
        ).stream().map(PersonIndexEntity::getDisabilityLevel).filter(s -> s != null && !s.isEmpty()).sorted().toList();
        return ApiResponse.ok(levels);
    }
}
