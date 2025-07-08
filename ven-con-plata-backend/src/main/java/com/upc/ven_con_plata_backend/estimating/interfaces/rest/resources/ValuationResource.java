package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resource que representa la respuesta completa de una valoración para la API.
 * Contiene todos los datos de entrada y de salida.
 */
public record ValuationResource(
        Long id,
        String valuationName,
        Long userId,
        // --- Parámetros de Entrada ---
        BigDecimal faceValue,
        LocalDate issueDate,
        BigDecimal tcea,
        BigDecimal trea,
        BigDecimal macaulayDurationInYears,
        BigDecimal modifiedDurationInYears,
        BigDecimal convexity,
        BigDecimal dirtyPrice,
        BigDecimal cleanPrice,

        // --- Flujo de Caja ---
        List<CashFlowPeriodResource> cashFlow
) {}