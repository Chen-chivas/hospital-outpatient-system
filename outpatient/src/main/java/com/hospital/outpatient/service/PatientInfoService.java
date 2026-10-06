package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.entity.PatientInfo;
import com.hospital.outpatient.entity.SysUser;
import com.hospital.outpatient.mapper.PatientInfoMapper;
import com.hospital.outpatient.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientInfoService {

    @Autowired
    private PatientInfoMapper patientInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    // 获取所有患者列表（含用户基本信息）
    public List<Map<String, Object>> getAllPatients() {
        List<PatientInfo> patients = patientInfoMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PatientInfo p : patients) {
            SysUser user = sysUserMapper.selectById(p.getPatientId());
            if (user != null && "PATIENT".equals(user.getRole())) {
                Map<String, Object> map = new HashMap<>();
                map.put("patientId", p.getPatientId());
                map.put("realName", user.getRealName());
                map.put("phone", user.getPhone());
                map.put("gender", p.getGender());
                map.put("age", p.getAge());
                result.add(map);
            }
        }
        return result;
    }

    // 根据ID获取患者详情
    public PatientInfo getById(Long patientId) {
        return patientInfoMapper.selectById(patientId);
    }

    // 获取患者姓名
    public String getPatientName(Long patientId) {
        SysUser user = sysUserMapper.selectById(patientId);
        return user != null ? user.getRealName() : "未知患者";
    }
}
