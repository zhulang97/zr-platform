package com.zhilian.zr.importing.handler;

import com.zhilian.zr.importing.enums.ImportModule;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImportHandlerRegistry {

    private final Map<ImportModule, AbstractImportHandler> handlers = new HashMap<>();

    public void register(AbstractImportHandler handler) {
        handlers.put(handler.getModule(), handler);
    }

    public AbstractImportHandler getHandler(ImportModule module) {
        return handlers.get(module);
    }

    public AbstractImportHandler getHandler(String moduleCode) {
        ImportModule module = ImportModule.fromCode(moduleCode);
        return module != null ? handlers.get(module) : null;
    }

    public boolean hasHandler(ImportModule module) {
        return handlers.containsKey(module);
    }

    public List<ImportModule> getRegisteredModules() {
        return List.copyOf(handlers.keySet());
    }
}
