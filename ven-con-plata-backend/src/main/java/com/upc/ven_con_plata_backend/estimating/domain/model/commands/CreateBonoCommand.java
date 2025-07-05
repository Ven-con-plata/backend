package com.upc.ven_con_plata_backend.estimating.domain.model.commands;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Currency;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.Periodicidad;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBonoCommand(
        String moneda,
        BigDecimal valorNominal,
        BigDecimal valorComercial,
        Integer plazoEnAnios,
        Periodicidad frecuenciaPago,
        BigDecimal tasaInteres,
        Periodicidad periodicidadInteres,
        BigDecimal cok,
        Periodicidad periodicidadCok,
        Integer periodosGraciaTotal,
        Integer periodosGraciaParcial,
        BigDecimal costeFlotacion,
        BigDecimal costeCavali,
        BigDecimal primaVencimiento,
        BigDecimal costesNotariales,
        BigDecimal costesRegistrales,
        BigDecimal costesTasacion,
        BigDecimal comisionEstudio,
        BigDecimal comisionActivacion,
        BigDecimal seguroDesgravamen,
        BigDecimal seguroRiesgo,
        BigDecimal comisionPeriodica,
        BigDecimal portes,
        BigDecimal gastosAdministrativos
) {
}
