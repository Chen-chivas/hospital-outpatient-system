package com.hospital.outpatient.controller;

import com.hospital.outpatient.entity.Blacklist;
import com.hospital.outpatient.entity.DoctorInfo;
import com.hospital.outpatient.entity.Schedule;
import com.hospital.outpatient.entity.SuspensionApplication; // 新增这行
import com.hospital.outpatient.service.DoctorInfoService;
import com.hospital.outpatient.service.ScheduleService;
import com.hospital.outpatient.service.SuspensionService; // 新增这行
import com.hospital.outpatient.service.BlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/outpatient/admin/schedule")
public class AdminScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DoctorInfoService doctorInfoService;

    @Autowired
    private SuspensionService suspensionService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private com.hospital.outpatient.service.PatientInfoService patientInfoService;

    // 排班列表页
    @GetMapping
    public String scheduleList(@RequestParam(required = false) LocalDate date,
                               @RequestParam(required = false) Integer deptId,
                               Model model) {
        // 默认显示今天及以后的排班
        if (date == null) {
            date = LocalDate.now();
        }

        List<Schedule> schedules = scheduleService.getByDateAndDept(date, deptId);
        Map<Long, String> doctorNameMap = scheduleService.getDoctorNameMap();
        List<DoctorInfo> doctors = doctorInfoService.list();

        model.addAttribute("schedules", schedules);
        model.addAttribute("doctorNameMap", doctorNameMap);
        model.addAttribute("doctors", doctors);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedDeptId", deptId);

        return "outpatient/admin/schedule-list";
    }

    // 跳转到新增排班页面
    @GetMapping("/add")
    public String addSchedule(Model model) {
        List<DoctorInfo> doctors = doctorInfoService.list();
        Map<Long, String> doctorNameMap = scheduleService.getDoctorNameMap();
        model.addAttribute("doctors", doctors);
        model.addAttribute("doctorNameMap", doctorNameMap);
        model.addAttribute("schedule", new Schedule());
        return "outpatient/admin/schedule-form";
    }

    // 跳转到编辑排班页面
    @GetMapping("/edit/{id}")
    public String editSchedule(@PathVariable Long id, Model model) {
        Schedule schedule = scheduleService.getById(id);
        List<DoctorInfo> doctors = doctorInfoService.list();
        Map<Long, String> doctorNameMap = scheduleService.getDoctorNameMap();
        model.addAttribute("doctors", doctors);
        model.addAttribute("doctorNameMap", doctorNameMap);
        model.addAttribute("schedule", schedule);
        return "outpatient/admin/schedule-form";
    }

    // 保存排班（新增和编辑通用）
    @PostMapping("/save")
    public String saveSchedule(@ModelAttribute Schedule schedule, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.saveOrUpdateWithNumberSources(schedule);
            redirectAttributes.addFlashAttribute("successMsg", "排班保存成功，号源已自动生成");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "排班保存失败：" + e.getMessage());
        }
        return "redirect:/outpatient/admin/schedule";
    }

    // 删除排班
    @GetMapping("/delete/{id}")
    public String deleteSchedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            scheduleService.removeWithNumberSources(id);
            redirectAttributes.addFlashAttribute("successMsg", "排班删除成功，对应号源已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "排班删除失败：" + e.getMessage());
        }
        return "redirect:/outpatient/admin/schedule";
    }

    // 停诊审核列表
    @GetMapping("/suspension")
    public String suspensionList(Model model) {
        List<SuspensionApplication> pendingApplications = suspensionService.getPendingApplications();
        Map<Long, String> doctorNameMap = scheduleService.getDoctorNameMap();
        model.addAttribute("applications", pendingApplications);
        model.addAttribute("doctorNameMap", doctorNameMap);
        // 也查已审核的申请
        List<SuspensionApplication> allApplications = suspensionService.getAllApplications();
        model.addAttribute("allApplications", allApplications);
        return "outpatient/admin/suspension-audit";
    }

    // 审核停诊申请
    @PostMapping("/suspension/audit")
    public String auditApplication(@RequestParam("applicationId") Long applicationId,
                                   @RequestParam("status") String status,
                                   @RequestParam(value = "auditComment", required = false) String auditComment,
                                   RedirectAttributes redirectAttributes) {
        // 测试用，默认使用管理员ID(1)
        Long auditorId = 1L;
        boolean success = suspensionService.auditApplication(applicationId, status, auditComment, auditorId);

        if (success) {
            if ("APPROVED".equals(status)) {
                redirectAttributes.addFlashAttribute("successMsg", "审核通过，系统已自动取消所有排班和订单");
            } else {
                redirectAttributes.addFlashAttribute("successMsg", "审核已驳回");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "审核失败，申请状态异常");
        }

        return "redirect:/outpatient/admin/schedule/suspension";
    }

    // 黑名单管理页面
    @GetMapping("/blacklist")
    public String blacklistPage(Model model) {
        List<Blacklist> blacklist = blacklistService.getActiveBlacklist();
        // 查询患者姓名
        Map<Long, String> patientNameMap = new java.util.HashMap<>();
        for (Blacklist b : blacklist) {
            String name = patientInfoService.getUserName(b.getPatientId());
            patientNameMap.put(b.getPatientId(), name);
        }
        // 获取所有患者列表（供手动拉黑下拉选择）
        model.addAttribute("allPatients", patientInfoService.getAllUsers());
        model.addAttribute("blacklist", blacklist);
        model.addAttribute("patientNameMap", patientNameMap);
        return "outpatient/admin/blacklist";
    }

    // 手动拉黑患者（删除多余reason参数，调用只传patientId）
    @PostMapping("/blacklist/add")
    public String addToBlacklist(@RequestParam("patientId") Long patientId,
                                 RedirectAttributes redirectAttributes) {
        try {
            // 只传患者ID，不再传reason、操作人ID
            blacklistService.addToBlacklist(patientId);
            redirectAttributes.addFlashAttribute("successMsg", "患者已成功加入黑名单");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "拉黑失败：" + e.getMessage());
        }
        return "redirect:/outpatient/admin/schedule/blacklist";
    }

    // 手动解除黑名单（删除多余operatorId参数）
    @GetMapping("/blacklist/unblock/{patientId}")
    public String unblockPatient(@PathVariable Long patientId, RedirectAttributes redirectAttributes) {
        // 只传患者ID
        boolean success = blacklistService.unblockPatient(patientId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "黑名单已成功解除");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "解除失败，该患者不在黑名单中");
        }
        return "redirect:/outpatient/admin/schedule/blacklist";
    }

    // 新增：重置爽约次数
    @GetMapping("/blacklist/reset/{patientId}")
    public String resetMissedCount(@PathVariable Long patientId, RedirectAttributes redirectAttributes) {
        blacklistService.resetMissedCount(patientId);
        redirectAttributes.addFlashAttribute("successMsg", "患者爽约次数已重置为0");
        return "redirect:/outpatient/admin/schedule/blacklist";
    }
}
