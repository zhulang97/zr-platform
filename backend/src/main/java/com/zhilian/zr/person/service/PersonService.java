package com.zhilian.zr.person.service;

import com.zhilian.zr.common.api.PageResponse;
import com.zhilian.zr.person.dto.PersonDtos;

public interface PersonService {
  PageResponse<PersonDtos.PersonRow> search(PersonDtos.PersonSearchRequest req);

  byte[] exportCsv(PersonDtos.PersonSearchRequest req);
}
