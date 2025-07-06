package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.CashFlowEntry;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowEntryResource(
        int periodo,
        LocalDate fechaPago,
        Double amortizacion,
        Double interes,
        Double cuotaTotal,
        Double saldoRestante
) {
    public static CashFlowEntryResource fromEntity(CashFlowEntry e) {
        return new CashFlowEntryResource(
                e.getPeriodo(),
                e.getFechaPago(),
                e.getAmortizacion(),
                e.getInteres(),
                e.getCuotaTotal(),
                e.getSaldoRestante()
        );
    }
}
