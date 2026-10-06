package com.medical.controller;

import com.medical.entity.DrugInventory;
import com.medical.service.PharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/smart")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;

    @GetMapping("/pharmacy")
    public String pharmacyPage(Model model) {
        model.addAttribute("pendingPrescriptions", pharmacyService.getPendingPrescriptions());
        model.addAttribute("pendingDispense", pharmacyService.getPendingDispense());
        model.addAttribute("drugs", pharmacyService.getAllDrugs());
        model.addAttribute("warnings", pharmacyService.getStockWarnings());
        return "smart/pharmacy";
    }

    @PostMapping("/api/pharmacy/audit")
    @ResponseBody
    public Map<String, Object> auditPrescription(@RequestParam Long id,
                                                 @RequestParam boolean approved,
                                                 @RequestParam(required = false) String rejectReason) {
        return pharmacyService.auditPrescription(id, approved, rejectReason);
    }

    @PostMapping("/api/pharmacy/dispense")
    @ResponseBody
    public Map<String, Object> dispensePrescription(@RequestParam Long id, @RequestParam Long pharmacistId) {
        return pharmacyService.dispensePrescription(id, pharmacistId);
    }

    @PostMapping("/api/pharmacy/drug/add")
    @ResponseBody
    public Map<String, Object> addDrug(@RequestBody DrugInventory drug) {
        return pharmacyService.addDrug(drug);
    }

    @PostMapping("/api/pharmacy/stock/update")
    @ResponseBody
    public Map<String, Object> updateStock(@RequestParam Long id, @RequestParam int quantity) {
        return pharmacyService.updateStock(id, quantity);
    }

    @GetMapping("/api/pharmacy/drug/search")
    @ResponseBody
    public Map<String, Object> searchDrugs(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", pharmacyService.searchDrugs(keyword));
        return result;
    }

    @GetMapping("/api/pharmacy/drug/{id}")
    @ResponseBody
    public Map<String, Object> getDrugById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", pharmacyService.getDrugById(id));
        return result;
    }
}
