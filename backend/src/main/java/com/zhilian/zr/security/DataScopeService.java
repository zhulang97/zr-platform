package com.zhilian.zr.security;

import com.zhilian.zr.sys.mapper.UserDataScopeMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DataScopeService {

  private final UserDataScopeMapper userDataScopeMapper;

  public DataScopeService(UserDataScopeMapper userDataScopeMapper) {
    this.userDataScopeMapper = userDataScopeMapper;
  }

  /**
   * Returns null when user is treated as "admin" (no district restriction).
   */
  public List<Long> districtScopeOrNullForAdmin(long userId, Set<String> authorities) {
    // Sys admin has system management capabilities; treat as full data scope.
    if (authorities.contains("sys:user:search") || authorities.contains("sys:datascope:update")) {
      return null;
    }
    return userDataScopeMapper.listDistrictIds(userId);
  }

  public static List<Long> intersectDistrictIds(List<Long> requested, List<Long> scope) {
    if (scope == null) {
      return requested;
    }
    if (requested == null || requested.isEmpty()) {
      return scope;
    }
    Set<Long> s = new HashSet<>(scope);
    return requested.stream().filter(s::contains).distinct().toList();
  }
}
