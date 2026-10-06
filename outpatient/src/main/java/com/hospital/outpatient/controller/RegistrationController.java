package com.hospital.outpatient.controller;

import com.hospital.outpatient.entity.NumberSource;
import com.hospital.outpatient.entity.RegistrationOrder;
import com.hospital.outpatient.entity.Schedule;
import com.hospital.outpatient.service.BlacklistService;
import com.hospital.outpatient.service.NumberSourceService;
import com.hospital.outpatient.service.PatientInfoService;
import com.hospital.outpatient.service.RegistrationService;
import com.hospital.outpatient.service.ScheduleService;
import com.hospital.outpatient.dto.RegistrationOrderDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reg")
public class RegistrationController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private NumberSourceService numberSourceService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private PatientInfoService patientInfoService;

    // ==================== 患者选择 ====================

    // 患者选择页面
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("patients", patientInfoService.getAllPatients());
        return "reg/login";
    }

    // 处理患者选择
    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> doLogin(@RequestParam("patientId") Long patientId,
                                        HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String patientName = patientInfoService.getPatientName(patientId);
        if (patientName != null && !"未知患者".equals(patientName)) {
            session.setAttribute("patientId", patientId);
            session.setAttribute("patientName", patientName);
            result.put("success", true);
            result.put("patientName", patientName);
        } else {
            result.put("success", false);
            result.put("message", "患者不存在");
        }
        return result;
    }

    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("patientId");
        session.removeAttribute("patientName");
        return "redirect:/reg/login";
    }

    // ==================== 挂号流程 ====================

    // 选择科室后跳转到排班列表
    @GetMapping("/schedule")
    public String scheduleList(@RequestParam("deptId") Integer deptId,
                               @RequestParam("visitDate") String visitDateStr,
                               Model model,
                               HttpSession session) {
        // 检查登录
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/reg/login";
        }

        LocalDate visitDate = LocalDate.parse(visitDateStr);
        List<Schedule> schedules = scheduleService.getByDeptIdAndDate(deptId, visitDate);

        // 预处理展示数据，避免Thymeleaf复杂表达式
        Map<Long, String> doctorNameMap = scheduleService.getDoctorNameMap();
        Map<Long, String> doctorTitleMap = scheduleService.getDoctorTitleMap();
        String[] avatarColors = {"av-blue", "av-green", "av-purple", "av-orange", "av-rose", "av-cyan"};

        List<Map<String, Object>> scheduleList = new ArrayList<>();
        int idx = 0;
        for (Schedule s : schedules) {
            Map<String, Object> item = new HashMap<>();
            item.put("scheduleId", s.getScheduleId());
            item.put("doctorId", s.getDoctorId());
            item.put("deptId", s.getDeptId());
            item.put("startTime", s.getStartTime());
            item.put("endTime", s.getEndTime());
            item.put("registrationFee", s.getRegistrationFee());

            String doctorName = doctorNameMap.getOrDefault(s.getDoctorId(), "未知");
            item.put("doctorName", doctorName);
            item.put("doctorTitle", doctorTitleMap.getOrDefault(s.getDoctorId(), ""));

            // 头像字符（取名字最后两个字）
            String avatar = doctorName.length() >= 2 ? doctorName.substring(doctorName.length() - 2) : doctorName;
            item.put("avatarChar", avatar);
            item.put("avatarClass", avatarColors[idx % avatarColors.length]);
            idx++;
            scheduleList.add(item);
        }

        model.addAttribute("scheduleList", scheduleList);
        model.addAttribute("deptId", deptId);
        model.addAttribute("visitDate", visitDate);
        model.addAttribute("patientName", session.getAttribute("patientName"));

        return "reg/schedule";
    }

    // 选择排班后跳转到号源选择页面
    @GetMapping("/number")
    public String numberList(@RequestParam("scheduleId") Long scheduleId,
                             @RequestParam("visitDate") String visitDateStr,
                             Model model,
                             HttpSession session) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/reg/login";
        }

        Schedule schedule = scheduleService.getById(scheduleId);
        List<NumberSource> numberSources = numberSourceService.getByScheduleId(scheduleId);
        LocalDate visitDate = LocalDate.parse(visitDateStr);
        String doctorName = scheduleService.getDoctorName(schedule.getDoctorId());

        model.addAttribute("schedule", schedule);
        model.addAttribute("numberSources", numberSources);
        model.addAttribute("visitDate", visitDate);
        model.addAttribute("doctorName", doctorName);
        model.addAttribute("patientName", session.getAttribute("patientName"));

        return "reg/number";
    }

    // 提交挂号申请
    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitRegistration(@RequestParam("sourceId") Long sourceId,
                                                   @RequestParam("scheduleId") Long scheduleId,
                                                   @RequestParam("doctorId") Long doctorId,
                                                   @RequestParam("deptId") Integer deptId,
                                                   @RequestParam("payAmount") BigDecimal payAmount,
                                                   HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Long patientId = (Long) session.getAttribute("patientId");

        if (patientId == null) {
            result.put("success", false);
            result.put("message", "请先选择就诊人");
            return result;
        }

        // 检查是否在黑名单中
        if (blacklistService.isInBlacklist(patientId)) {
            result.put("success", false);
            result.put("message", "挂号失败：您因累计爽约次数过多，已被加入黑名单，30天内无法挂号");
            return result;
        }

        RegistrationOrder order = new RegistrationOrder();
        order.setSourceId(sourceId);
        order.setScheduleId(scheduleId);
        order.setDoctorId(doctorId);
        order.setDeptId(deptId);
        order.setPatientId(patientId);
        order.setPayAmount(payAmount);

        String msg = registrationService.createOrder(order);
        if ("success".equals(msg)) {
            result.put("success", true);
            result.put("message", "挂号成功，请在30分钟内完成支付");
        } else {
            result.put("success", false);
            result.put("message", msg);
        }
        return result;
    }

    // 我的挂号订单
    @GetMapping("/my")
    public String myOrders(Model model,
                           HttpSession session,
                           @RequestParam(required = false) String successMsg,
                           @RequestParam(required = false) String errorMsg,
                           @RequestParam(required = false, defaultValue = "all") String filter) {
        Long patientId = (Long) session.getAttribute("patientId");
        if (patientId == null) {
            return "redirect:/reg/login";
        }

        List<RegistrationOrderDTO> orders = registrationService.getByPatientIdWithDetails(patientId);

        // 统计
        long pendingCount = orders.stream().filter(o -> "PENDING".equals(o.getOrderStatus())).count();
        long paidCount = orders.stream().filter(o -> "PAID".equals(o.getOrderStatus())).count();
        // 合计金额
        String totalAmount = orders.stream()
                .filter(o -> o.getPayAmount() != null)
                .map(o -> o.getPayAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .toPlainString();

        model.addAttribute("orders", orders);
        model.addAttribute("successMsg", successMsg);
        model.addAttribute("errorMsg", errorMsg);
        model.addAttribute("patientName", session.getAttribute("patientName"));
        model.addAttribute("patientId", patientId);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("totalCount", orders.size());
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("filter", filter);

        return "reg/my";
    }

    // 立即支付（模拟支付成功）
    @GetMapping("/pay")
    public String payOrder(@RequestParam("orderId") Long orderId,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        if (session.getAttribute("patientId") == null) {
            return "redirect:/reg/login";
        }
        boolean success = registrationService.paySuccess(orderId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "支付成功！您已成功预约号源");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "支付失败，订单已超时或状态异常");
        }
        return "redirect:/reg/my";
    }

    // 取消待支付订单
    @GetMapping("/cancelPending")
    public String cancelPendingOrder(@RequestParam("orderId") Long orderId,
                                      RedirectAttributes redirectAttributes,
                                      HttpSession session) {
        if (session.getAttribute("patientId") == null) {
            return "redirect:/reg/login";
        }
        boolean success = registrationService.cancelPendingOrder(orderId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "订单已取消，号源已自动释放");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "取消失败，订单状态异常");
        }
        return "redirect:/reg/my";
    }

    // 退号
    @GetMapping("/cancel")
    public String cancelOrder(@RequestParam("orderId") Long orderId,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (session.getAttribute("patientId") == null) {
            return "redirect:/reg/login";
        }
        boolean success = registrationService.cancelOrder(orderId, "患者主动退号");
        if (success) {
            redirectAttributes.addFlashAttribute("successMsg", "退号成功！号源已自动释放");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "退号失败，订单状态异常");
        }
        return "redirect:/reg/my";
    }

    // AJAX获取订单详情
    @GetMapping("/detail/{orderId}")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable Long orderId, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        if (session.getAttribute("patientId") == null) {
            result.put("success", false);
            return result;
        }
        RegistrationOrderDTO dto = registrationService.getOrderDetail(orderId);
        result.put("success", dto != null);
        result.put("data", dto);
        return result;
    }
}
