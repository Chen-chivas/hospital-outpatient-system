package com.hospital.outpatient.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.outpatient.entity.SysDepartment;
import com.hospital.outpatient.mapper.SysDepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysDepartmentService {

    @Autowired
    private SysDepartmentMapper departmentMapper;

    // 查询所有一级科室
    public List<SysDepartment> getAllFirstLevel() {
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDepartment::getParentId, 0)
                .eq(SysDepartment::getStatus, 1)
                .orderByAsc(SysDepartment::getSortNum);
        return departmentMapper.selectList(wrapper);
    }
}