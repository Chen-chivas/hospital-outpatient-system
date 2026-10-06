package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.outpatient.entity.Blacklist;
import com.hospital.outpatient.entity.MissedAppointment;
import com.hospital.outpatient.entity.RegistrationOrder;
import com.hospital.outpatient.mapper.BlacklistMapper;
import com.hospital.outpatient.mapper.MissedAppointmentMapper;
import com.hospital.outpatient.mapper.RegistrationOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class BlacklistService {

    @Autowired
    private MissedAppointmentMapper missedAppointmentMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private RegistrationOrderMapper orderMapper;

    // 配置参数
    private static final int MAX_MISSED_COUNT = 3; // 最大允许爽约次数
    private static final int BLOCK_DAYS = 30; // 拉黑时长(天)

    // 检查患者是否在黑名单中（核心拦截方法）
    public boolean isInBlacklist(Long patientId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getPatientId, patientId)
                .eq(Blacklist::getIsLimited, 1)
                .gt(Blacklist::getLimitEndTime, LocalDateTime.now());

        return blacklistMapper.selectCount(wrapper) > 0;
    }

    // 处理昨日爽约订单（定时任务调用）
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void processYesterdayMissedOrders() {
        log.info("开始处理昨日爽约订单");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 1. 查询昨日所有已支付且未取消的订单
        LambdaQueryWrapper<RegistrationOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(RegistrationOrder::getOrderStatus, "PAID")
                .eq(RegistrationOrder::getVisitDate, yesterday);

        List<RegistrationOrder> orders = orderMapper.selectList(orderWrapper);

        for (RegistrationOrder order : orders) {
            // 2. 标记订单为爽约
            order.setOrderStatus("MISSED");
            orderMapper.updateById(order);

            // 3. 记录爽约明细
            MissedAppointment missed = new MissedAppointment();
            missed.setPatientId(order.getPatientId());
            missed.setOrderId(order.getOrderId());
            missed.setMissedTime(LocalDateTime.now());
            missedAppointmentMapper.insert(missed);

            log.info("标记订单{}为爽约，患者ID：{}", order.getSerialNo(), order.getPatientId());

            // 4. 更新患者爽约次数并检查是否需要拉黑
            updateMissedCountAndCheckBlock(order.getPatientId());
        }

        // 5. 自动解除到期的黑名单
        autoUnblockExpired();
        log.info("昨日爽约订单处理完成，共处理{}个订单", orders.size());
    }

    // 更新爽约次数并自动拉黑
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void updateMissedCountAndCheckBlock(Long patientId) {
        // 1. 查询或创建患者黑名单记录
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getPatientId, patientId);
        Blacklist blacklist = blacklistMapper.selectOne(wrapper);

        if (blacklist == null) {
            // 第一次爽约，创建记录
            blacklist = new Blacklist();
            blacklist.setPatientId(patientId);
            blacklist.setMissedCount(1);
            blacklist.setIsLimited(0);
            blacklist.setCreateTime(LocalDateTime.now());
            blacklist.setUpdateTime(LocalDateTime.now());
            blacklistMapper.insert(blacklist);
            log.info("患者{}首次爽约，爽约次数：1", patientId);
        } else {
            // 已有记录，次数+1
            LambdaUpdateWrapper<Blacklist> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Blacklist::getPatientId, patientId)
                    .setSql("missed_count = missed_count + 1")
                    .set(Blacklist::getUpdateTime, LocalDateTime.now());
            blacklistMapper.update(null, updateWrapper);

            // 重新查询最新次数
            blacklist = blacklistMapper.selectOne(wrapper);
            log.info("患者{}爽约次数更新为：{}", patientId, blacklist.getMissedCount());
        }

        // 2. 达到阈值自动拉黑
        if (blacklist.getMissedCount() >= MAX_MISSED_COUNT && blacklist.getIsLimited() == 0) {
            addToBlacklist(patientId);
        }
    }

    // 将患者加入黑名单（支持手动拉黑无爽约记录的患者）
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void addToBlacklist(Long patientId) {
        // 先查询是否已有记录
        LambdaQueryWrapper<Blacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Blacklist::getPatientId, patientId);
        Blacklist blacklist = blacklistMapper.selectOne(queryWrapper);

        if (blacklist == null) {
            // 无记录：新建并拉黑
            blacklist = new Blacklist();
            blacklist.setPatientId(patientId);
            blacklist.setMissedCount(0);
            blacklist.setIsLimited(1);
            blacklist.setLimitStartTime(LocalDateTime.now());
            blacklist.setLimitEndTime(LocalDateTime.now().plusDays(BLOCK_DAYS));
            blacklist.setCreateTime(LocalDateTime.now());
            blacklist.setUpdateTime(LocalDateTime.now());
            blacklistMapper.insert(blacklist);
            log.info("患者{}已被手动加入黑名单（新记录），限制至：{}",
                    patientId, blacklist.getLimitEndTime());
        } else {
            // 已有记录：更新拉黑状态
            blacklist.setIsLimited(1);
            blacklist.setLimitStartTime(LocalDateTime.now());
            blacklist.setLimitEndTime(LocalDateTime.now().plusDays(BLOCK_DAYS));
            blacklist.setUpdateTime(LocalDateTime.now());
            blacklistMapper.updateById(blacklist);
            log.info("患者{}已被加入黑名单（更新记录），限制至：{}，爽约次数：{}",
                    patientId, blacklist.getLimitEndTime(), blacklist.getMissedCount());
        }
    }

    // 手动解除黑名单
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public boolean unblockPatient(Long patientId) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getPatientId, patientId)
                .eq(Blacklist::getIsLimited, 1);
        Blacklist blacklist = blacklistMapper.selectOne(wrapper);

        if (blacklist == null) {
            return false;
        }

        LambdaUpdateWrapper<Blacklist> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Blacklist::getPatientId, patientId)
                .set(Blacklist::getIsLimited, 0)
                .set(Blacklist::getLimitEndTime, LocalDateTime.now())
                .set(Blacklist::getUpdateTime, LocalDateTime.now());

        blacklistMapper.update(null, updateWrapper);
        log.info("患者{}的黑名单已被手动解除", patientId);
        return true;
    }

    // 自动解除到期的黑名单
    private void autoUnblockExpired() {
        LambdaUpdateWrapper<Blacklist> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Blacklist::getIsLimited, 1)
                .lt(Blacklist::getLimitEndTime, LocalDateTime.now())
                .set(Blacklist::getIsLimited, 0)
                .set(Blacklist::getUpdateTime, LocalDateTime.now());

        int count = blacklistMapper.update(null, wrapper);
        if (count > 0) {
            log.info("自动解除了{}个到期的黑名单", count);
        }
    }

    // 获取所有生效中的黑名单
    public List<Blacklist> getActiveBlacklist() {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Blacklist::getIsLimited, 1)
                .orderByDesc(Blacklist::getLimitStartTime);
        return blacklistMapper.selectList(wrapper);
    }

    // 手动重置爽约次数
    @Transactional(transactionManager = "hospitalTransactionManager", rollbackFor = Exception.class)
    public void resetMissedCount(Long patientId) {
        LambdaUpdateWrapper<Blacklist> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Blacklist::getPatientId, patientId)
                .set(Blacklist::getMissedCount, 0)
                .set(Blacklist::getUpdateTime, LocalDateTime.now());

        blacklistMapper.update(null, wrapper);
        log.info("患者{}的爽约次数已重置为0", patientId);
    }
}