package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowEntryResource(
        LocalDate fecha,
        BigDecimal monto
) {
}
