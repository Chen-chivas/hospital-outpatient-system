package com.hospital.outpatient.controller;

import com.hospital.outpatient.entity.SysDepartment;
import com.hospital.outpatient.service.SysDepartmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private SysDepartmentService departmentService;

    // 系统首页（按角色跳转）
    @GetMapping("/outpatient")
    public String index(Model model, HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        if (role == null) {
            return "redirect:/outpatient/reg/login";
        }

        // 医生 → 工作台
        if ("DOCTOR".equals(role)) {
            return "redirect:/outpatient/doctor";
        }
        // 管理员 → 管理后台
        if ("ADMIN".equals(role)) {
            return "redirect:/outpatient/admin/schedule";
        }

        // 患者 → 挂号首页
        List<SysDepartment> departments = departmentService.getAllFirstLevel();
        model.addAttribute("departments", departments);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "outpatient/index";
    }
}
