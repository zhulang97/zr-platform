package com.zhilian.zr.sys.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhilian.zr.common.ids.IdGenerator;
import com.zhilian.zr.sys.entity.UserEntity;
import com.zhilian.zr.sys.mapper.UserMapper;
import com.zhilian.zr.sys.mapper.UserRoleMapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
  private final UserMapper userMapper;
  private final UserRoleMapper userRoleMapper;
  private final PasswordEncoder passwordEncoder;
  private final Environment environment;

  public AdminBootstrap(UserMapper userMapper, UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder,
      Environment environment) {
    this.userMapper = userMapper;
    this.userRoleMapper = userRoleMapper;
    this.passwordEncoder = passwordEncoder;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, "admin"));
    if (existing != null) {
      return;
    }

    boolean isDev = Arrays.stream(environment.getActiveProfiles())
        .map(p -> p.toLowerCase(Locale.ROOT))
        .anyMatch(p -> p.equals("dev"));

    String password = isDev ? "Admin@12345" : ("Init@" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));

    UserEntity admin = new UserEntity();
    admin.setUserId(IdGenerator.nextId());
    admin.setUsername("admin");
    admin.setDisplayName("Administrator");
    admin.setStatus(1);
    admin.setPasswordHash(passwordEncoder.encode(password));
    admin.setFailedCount(0);
    admin.setCreatedAt(Instant.now());
    admin.setUpdatedAt(Instant.now());
    userMapper.insert(admin);

    // role_id=1 is sys_admin in seed data
    userRoleMapper.insert(admin.getUserId(), 1L);

    System.out.println("[BOOTSTRAP] Created initial admin user. username=admin password=" + password);
  }
}
