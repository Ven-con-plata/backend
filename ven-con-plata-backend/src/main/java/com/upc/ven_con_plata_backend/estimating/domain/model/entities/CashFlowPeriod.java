package com.upc.ven_con_plata_backend.estimating.domain.model.entities;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.GracePeriodState;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Money;

/**
 * @param cashFlow Flujo para el bonista (cupón + amortización)
 */
public record CashFlowPeriod(int number, GracePeriodState gracePeriodState, Money initialBalance, Money interest,
                             Money coupon, Money amortization, Money finalBalance, Money cashFlow) {
}