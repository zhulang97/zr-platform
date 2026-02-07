package com.zhilian.zr.importing.service;

import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {
  String generateTemplate(String type);

  long upload(String type, MultipartFile file, String note);

  Map<String, Object> validate(long batchId);

  Map<String, Object> commit(long batchId, String strategy);
}