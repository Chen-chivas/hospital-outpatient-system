package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.entity.DoctorInfo;
import com.hospital.outpatient.entity.PatientInfo;
import com.hospital.outpatient.entity.SysUser;
import com.hospital.outpatient.mapper.DoctorInfoMapper;
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

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    // 获取所有用户列表（患者 + 医生 + 管理员）
    public List<Map<String, Object>> getAllUsers() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. 患者（沿用已验证的查询方式）
        List<PatientInfo> patients = patientInfoMapper.selectList(null);
        for (PatientInfo p : patients) {
            SysUser user = sysUserMapper.selectById(p.getPatientId());
            if (user != null && "PATIENT".equals(user.getRole()) && (user.getStatus() == null || user.getStatus() == 1)) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", p.getPatientId());
                map.put("username", user.getUsername());
                map.put("realName", user.getRealName());
                map.put("phone", user.getPhone());
                map.put("role", user.getRole());
                map.put("gender", p.getGender());
                map.put("age", p.getAge());
                result.add(map);
            }
        }

        // 2. 医生
        List<DoctorInfo> doctors = doctorInfoMapper.selectList(null);
        for (DoctorInfo d : doctors) {
            SysUser user = sysUserMapper.selectById(d.getDoctorId());
            if (user != null && "DOCTOR".equals(user.getRole()) && (user.getStatus() == null || user.getStatus() == 1)) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", d.getDoctorId());
                map.put("username", user.getUsername());
                map.put("realName", user.getRealName());
                map.put("phone", user.getPhone());
                map.put("role", user.getRole());
                map.put("title", d.getTitle());
                result.add(map);
            }
        }

        // 3. 管理员（直接查 sys_user role='ADMIN'）
        LambdaQueryWrapper<SysUser> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(SysUser::getRole, "ADMIN");
        List<SysUser> admins = sysUserMapper.selectList(adminQuery);
        for (SysUser user : admins) {
            if (user.getStatus() == null || user.getStatus() == 1) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getUserId());
                map.put("username", user.getUsername());
                map.put("realName", user.getRealName());
                map.put("phone", user.getPhone());
                map.put("role", user.getRole());
                result.add(map);
            }
        }

        return result;
    }

    // 根据ID获取患者详情
    public PatientInfo getById(Long patientId) {
        return patientInfoMapper.selectById(patientId);
    }

    // 获取用户姓名（所有角色通用）
    public String getUserName(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRealName() : "未知用户";
    }

    // 获取用户角色
    public String getUserRole(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getRole() : null;
    }
}
