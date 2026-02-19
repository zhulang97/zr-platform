package com.zhilian.zr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.zhilian.zr.**.mapper")
@EnableAsync
public class ZrPlatformApplication {
  public static void main(String[] args) {
    SpringApplication.run(ZrPlatformApplication.class, args);
  }
}
