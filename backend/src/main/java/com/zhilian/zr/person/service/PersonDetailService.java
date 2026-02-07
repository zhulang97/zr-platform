package com.zhilian.zr.person.service;

import com.zhilian.zr.person.dto.PersonDtos;
import java.util.List;
import java.util.Map;

public interface PersonDetailService {
  PersonDtos.PersonDetail detail(long personId);

  Map<String, Object> biz(long personId);

  List<Map<String, Object>> risks(long personId);
}
