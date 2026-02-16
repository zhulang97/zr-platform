package com.zhilian.zr.importing.handler;

import com.zhilian.zr.importing.enums.ImportModule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ImportHandlerConfig {

    private final ImportHandlerRegistry registry;
    private final JdbcTemplate jdbcTemplate;

    public ImportHandlerConfig(ImportHandlerRegistry registry, JdbcTemplate jdbcTemplate) {
        this.registry = registry;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void registerHandlers() {
        for (ImportModule module : ImportModule.values()) {
            registry.register(new JdbcImportHandler(module, jdbcTemplate));
        }
    }
}
