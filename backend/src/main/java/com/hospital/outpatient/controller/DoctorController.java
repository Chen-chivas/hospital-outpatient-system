package com.hospital.outpatient.controller;

import com.hospital.outpatient.entity.SuspensionApplication;
import com.hospital.outpatient.service.SuspensionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/outpatient/doctor")
public class DoctorController {

    @Autowired
    private SuspensionService suspensionService;

    @GetMapping
    public String doctorIndex(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/outpatient/reg/login";
        }
        return "outpatient/doctor/index";
    }

    @GetMapping("/suspension/apply")
    public String applyPage(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/outpatient/reg/login";
        }
        return "outpatient/doctor/suspension-apply";
    }

    @PostMapping("/suspension/submit")
    public String submitApplication(
            @RequestParam("suspensionDate") String suspensionDateStr,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        Long doctorId = (Long) session.getAttribute("userId");
        if (doctorId == null) {
            return "redirect:/outpatient/reg/login";
        }
        LocalDate date = LocalDate.parse(suspensionDateStr);
        boolean flag = suspensionService.submitApplication(doctorId, date, reason);

        if (flag) {
            redirectAttributes.addFlashAttribute("successMsg", "申请提交成功，等待管理员审核");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "该日期已有有效申请，请勿重复提交");
        }
        return "redirect:/outpatient/doctor/suspension/list";
    }

    @GetMapping("/suspension/list")
    public String myAppList(Model model, HttpSession session) {
        Long doctorId = (Long) session.getAttribute("userId");
        if (doctorId == null) {
            return "redirect:/outpatient/reg/login";
        }
        List<SuspensionApplication> list = suspensionService.getByDoctorId(doctorId);
        model.addAttribute("applications", list);
        return "outpatient/doctor/suspension-list";
    }
}
