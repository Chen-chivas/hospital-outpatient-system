package com.example.ocs.module.billing.api;

import com.example.ocs.module.billing.domain.PaymentMethod;

public record PayBillRequest(PaymentMethod paymentMethod) {}

