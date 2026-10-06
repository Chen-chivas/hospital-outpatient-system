package com.example.ocs.common.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class SerialNoGenerator {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

  private SerialNoGenerator() {}

  public static String next(String prefix) {
    String ts = TS.format(Instant.now());
    int r = 100000 + RANDOM.nextInt(900000);
    return prefix + "-" + ts + "-" + r;
  }
}

