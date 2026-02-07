package com.zhilian.zr.common.ids;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {
  private static final AtomicInteger SEQ = new AtomicInteger(0);

  private IdGenerator() {
  }

  public static long nextId() {
    long now = System.currentTimeMillis();
    int seq = SEQ.getAndIncrement() & 0x3FF;
    return (now << 10) | seq;
  }
}
