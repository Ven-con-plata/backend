package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BonoResource(
        Long id,
        LocalDate createdAt,
        LocalDate updatedAt,
        BigDecimal primaVencimiento,
        int cokUnidad,
        BigDecimal cokValor,
        BigDecimal comisionActivacion,
        BigDecimal comisionEstudio,
        BigDecimal flotacion,
        BigDecimal cavali,
        BigDecimal notariales,
        BigDecimal registrales,
        BigDecimal tasacion,
        String estado,
        LocalDate fechaVencimiento,
        String frecuenciaPago,
        BigDecimal comisionPeriodica,
        BigDecimal gastosAdministrativos,
        BigDecimal portes,
        BigDecimal seguroDesgravamen,
        BigDecimal seguroRiesgo,
        int parcial,
        int total,
        String moneda,
        int plazoEnAnios,
        int interesUnidad,
        BigDecimal interesValor,
        BigDecimal valorComercial,
        BigDecimal valorNominal,
        List<CashFlowScheduleResource> cronogramas
) {
}
