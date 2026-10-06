package com.hospital.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.outpatient.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {
}