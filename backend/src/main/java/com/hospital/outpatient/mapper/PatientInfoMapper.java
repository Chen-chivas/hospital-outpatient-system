package com.hospital.outpatient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.outpatient.entity.PatientInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientInfoMapper extends BaseMapper<PatientInfo> {
}