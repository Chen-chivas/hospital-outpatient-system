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

    // 系统首页
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        // 检查是否已选择患者
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/reg/login";
        }

        List<SysDepartment> departments = departmentService.getAllFirstLevel();
        model.addAttribute("departments", departments);
        model.addAttribute("patientName", session.getAttribute("patientName"));
        return "index";
    }
}
