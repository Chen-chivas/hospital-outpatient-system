package com.example.ocs.module.billing.api;

import com.example.ocs.common.exception.BusinessException;
import com.example.ocs.common.web.ApiResponse;
import com.example.ocs.common.web.ErrorCode;
import com.example.ocs.module.audit.application.AuditService;
import com.example.ocs.module.billing.application.BillingService;
import com.example.ocs.module.billing.domain.BillStatus;
import com.example.ocs.module.billing.domain.PaymentMethod;
import com.example.ocs.security.OcsPrincipal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {
  private final BillingService billingService;
  private final AuditService auditService;

  public BillingController(BillingService billingService, AuditService auditService) {
    this.billingService = billingService;
    this.auditService = auditService;
  }

  @GetMapping("/api/bills/my")
  @PreAuthorize("hasRole('PATIENT')")
  public ApiResponse<List<BillResponse>> my(Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    return ApiResponse.success(billingService.listByPatient(principal.userId()).stream().map(BillResponse::from).toList());
  }

  @GetMapping("/api/bills")
  @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
  public ApiResponse<List<BillResponse>> list(@RequestParam(value = "status", required = false) BillStatus status) {
    BillStatus finalStatus = status == null ? BillStatus.UNPAID : status;
    return ApiResponse.success(billingService.listByStatus(finalStatus).stream().map(BillResponse::from).toList());
  }

  @GetMapping("/api/bills/{billId}/items")
  @PreAuthorize("hasAnyRole('PATIENT','CASHIER','ADMIN')")
  public ApiResponse<List<BillItemResponse>> items(@PathVariable("billId") long billId, Authentication authentication) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var bill = billingService.getById(billId);
    if (bill.getPatient().getId() != principal.userId() && !principal.roles().contains("ADMIN") && !principal.roles().contains("CASHIER")) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "no permission");
    }
    return ApiResponse.success(billingService.listItems(billId).stream().map(BillItemResponse::from).toList());
  }

  @PostMapping("/api/bills/{billId}/pay")
  @PreAuthorize("hasAnyRole('PATIENT','CASHIER','ADMIN')")
  public ApiResponse<BillResponse> pay(
      @PathVariable("billId") long billId,
      @RequestBody(required = false) PayBillRequest request,
      Authentication authentication
  ) {
    OcsPrincipal principal = (OcsPrincipal) authentication.getPrincipal();
    var bill = billingService.getById(billId);
    if (bill.getPatient().getId() != principal.userId() && !principal.roles().contains("ADMIN") && !principal.roles().contains("CASHIER")) {
      throw new BusinessException(ErrorCode.BAD_REQUEST, "no permission");
    }
    PaymentMethod method = request == null ? null : request.paymentMethod();
    if (method == null) {
      method = principal.roles().contains("CASHIER") ? PaymentMethod.CASH : PaymentMethod.ALIPAY;
    }
    var paid = billingService.pay(billId, method);
    auditService.record(principal.userId(), "PAY", "billing", "Bill", paid.getId(), "{\"paymentMethod\":\"" + method.name() + "\"}");
    return ApiResponse.success(BillResponse.from(paid));
  }
}
