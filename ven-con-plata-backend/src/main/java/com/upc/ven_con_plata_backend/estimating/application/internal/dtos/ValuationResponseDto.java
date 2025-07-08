package com.upc.ven_con_plata_backend.estimating.application.internal.dtos;

import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowPeriod;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.BondParameters;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.FinancialMetrics;

import java.util.List;

/**
 * Data Transfer Object (DTO) para transportar los datos completos de una valoración
 * desde la capa de aplicación hacia las capas superiores (como la de interfaz).
 * Es un objeto inmutable de solo lectura.
 */
public record ValuationResponseDto(
        Long id,
        String valuationName,
        Long userId,
        BondParameters parameters,
        FinancialMetrics metrics,
        List<CashFlowPeriod> cashFlow
) {}