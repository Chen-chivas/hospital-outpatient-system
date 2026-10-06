package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.outpatient.entity.NumberSource;
import com.hospital.outpatient.mapper.NumberSourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NumberSourceService {

    @Autowired
    private NumberSourceMapper numberSourceMapper;

    // 根据排班ID查询所有可用号源（status=1，排除已停用的）
    public List<NumberSource> getByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<NumberSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NumberSource::getScheduleId, scheduleId)
                .eq(NumberSource::getStatus, 1) // 只查可预约的
                .orderByAsc(NumberSource::getSlotNumber);
        return numberSourceMapper.selectList(wrapper);
    }

    // 锁定号源（原子操作，防止超挂）
    @Transactional(rollbackFor = Exception.class)
    public boolean lockNumberSource(Long sourceId) {
        // 通用写法：直接写SQL表达式，所有版本都兼容
        LambdaUpdateWrapper<NumberSource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NumberSource::getSourceId, sourceId)
                .gt(NumberSource::getRemainingCount, 0)
                .setSql("remaining_count = remaining_count - 1") // 剩余号源-1
                .setSql("locked_count = locked_count + 1"); // 锁定号源+1

        int affected = numberSourceMapper.update(null, wrapper);
        if (affected == 0) {
            return false; // 号源不足
        }

        // 如果剩余号源为0，更新状态为已约满
        NumberSource source = numberSourceMapper.selectById(sourceId);
        if (source.getRemainingCount() == 0) {
            source.setStatus(2);
            numberSourceMapper.updateById(source);
        }

        return true;
    }

    // 释放号源（原子操作，退号/取消订单时调用）
    @Transactional(rollbackFor = Exception.class)
    public void releaseNumberSource(Long sourceId) {
        // 通用写法：直接写SQL表达式，所有版本都兼容
        LambdaUpdateWrapper<NumberSource> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NumberSource::getSourceId, sourceId)
                .gt(NumberSource::getLockedCount, 0) // 确保有锁定的号源
                .setSql("remaining_count = remaining_count + 1") // 剩余号源+1
                .setSql("locked_count = locked_count - 1") // 锁定号源-1
                .set(NumberSource::getStatus, 1); // 恢复为可预约状态

        numberSourceMapper.update(null, wrapper);
    }
}