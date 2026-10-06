package com.hospital.outpatient.task;

import com.hospital.outpatient.service.RegistrationService;
import com.hospital.outpatient.service.ScheduleGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.hospital.outpatient.service.BlacklistService;

import java.time.LocalDate;

@Component
@Slf4j
public class OrderTimeoutTask {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ScheduleGenerateService scheduleGenerateService;

    @Autowired
    private BlacklistService blacklistService;

    // 每分钟执行一次：检查超时未支付订单
    @Scheduled(cron = "0 * * * * ?")
    public void orderTimeoutCheck() {
        log.debug("⏰ 定时任务启动：开始检查超时未支付订单");
        registrationService.processTimeoutOrders();
    }

    // 每天凌晨00:00执行：生成未来第7天的排班和号源
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateTomorrowSchedule() {
        log.info("🌅 定时任务启动：开始生成未来第7天的排班和号源");
        LocalDate targetDate = LocalDate.now().plusDays(7);
        scheduleGenerateService.generateScheduleForDate(targetDate);
        log.info("✅ 未来第7天的排班和号源生成完成");
    }

    // 项目启动时执行一次：生成未来7天的所有排班和号源
    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 5000)
    public void generateInitialSchedule() {
        log.info("🚀 项目启动：开始生成未来7天的排班和号源");

        // 生成明天到第7天的排班
        for (int i = 1; i <= 7; i++) {
            LocalDate targetDate = LocalDate.now().plusDays(i);
            scheduleGenerateService.generateScheduleForDate(targetDate);
        }

        log.info("✅ 未来7天的排班和号源全部生成完成");
    }

    // 每天凌晨00:10执行：处理昨日爽约订单
    @Scheduled(cron = "0 10 0 * * ?")
    public void processMissedOrders() {
        log.info("⏰ 定时任务启动：开始处理昨日爽约订单");
        blacklistService.processYesterdayMissedOrders();
        log.info("✅ 昨日爽约订单处理完成");
    }
}