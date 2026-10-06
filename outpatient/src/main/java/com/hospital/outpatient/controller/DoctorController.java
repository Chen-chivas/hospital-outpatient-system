package com.hospital.outpatient.controller;

import com.hospital.outpatient.entity.SuspensionApplication;
import com.hospital.outpatient.service.SuspensionService;
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
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private SuspensionService suspensionService;

    @GetMapping
    public String doctorIndex() {
        return "doctor/index";
    }

    @GetMapping("/suspension/apply")
    public String applyPage() {
        return "doctor/suspension-apply";
    }

    @PostMapping("/suspension/submit")
    public String submitApplication(
            @RequestParam("suspensionDate") String suspensionDateStr,
            @RequestParam("reason") String reason,
            RedirectAttributes redirectAttributes
    ) {
        Long doctorId = 2L;
        LocalDate date = LocalDate.parse(suspensionDateStr);
        // 参数顺序、类型和service方法完全对齐
        boolean flag = suspensionService.submitApplication(doctorId, date, reason);

        if (flag) {
            redirectAttributes.addFlashAttribute("successMsg", "申请提交成功，等待管理员审核");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "该日期已有有效申请，请勿重复提交");
        }
        return "redirect:/doctor/suspension/list";
    }

    @GetMapping("/suspension/list")
    public String myAppList(Model model) {
        List<SuspensionApplication> list = suspensionService.getByDoctorId(2L);
        model.addAttribute("applications", list);
        return "doctor/suspension-list";
    }
}