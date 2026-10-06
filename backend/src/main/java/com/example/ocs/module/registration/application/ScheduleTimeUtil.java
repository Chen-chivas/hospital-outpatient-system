package com.example.ocs.module.registration.application;

import com.example.ocs.module.registration.domain.Schedule;
import com.example.ocs.module.registration.domain.TimePeriod;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ScheduleTimeUtil {
  private static final LocalTime AM_START = LocalTime.of(8, 0);
  private static final LocalTime AM_END = LocalTime.of(12, 0);
  private static final LocalTime PM_START = LocalTime.of(13, 30);
  private static final LocalTime PM_END = LocalTime.of(17, 30);

  private ScheduleTimeUtil() {}

  public static Instant startAt(Schedule schedule, ZoneId zoneId) {
    LocalTime time = schedule.getTimePeriod() == TimePeriod.AM ? AM_START : PM_START;
    return schedule.getScheduleDate().atTime(time).atZone(zoneId).toInstant();
  }

  public static Instant endAt(Schedule schedule, ZoneId zoneId) {
    LocalTime time = schedule.getTimePeriod() == TimePeriod.AM ? AM_END : PM_END;
    return schedule.getScheduleDate().atTime(time).atZone(zoneId).toInstant();
  }
}

