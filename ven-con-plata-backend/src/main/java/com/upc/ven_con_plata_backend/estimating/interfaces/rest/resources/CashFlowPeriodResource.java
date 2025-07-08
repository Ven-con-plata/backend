package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;

public record CashFlowPeriodResource(
        int number,
        String gracePeriodState,
        BigDecimal initialBalance,
        BigDecimal interest,
        BigDecimal coupon,
        BigDecimal amortization,
        BigDecimal finalBalance,
        BigDecimal cashflow
) {}
