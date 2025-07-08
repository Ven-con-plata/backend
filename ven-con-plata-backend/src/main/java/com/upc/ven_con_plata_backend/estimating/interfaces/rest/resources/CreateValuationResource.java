package com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateValuationResource(
        @NotBlank @Size(min = 3, max = 50) String valuationName,

        @NotNull @Positive Long userId,

        @NotNull @DecimalMin(value = "0.01", message = "Face value must be positive")
        BigDecimal faceValue,

        @NotNull @DecimalMin(value = "0.01", message = "Issue price must be positive")
        BigDecimal issuePrice,

        @NotNull @DecimalMin(value = "0.01", message = "Purchase price must be positive")
        BigDecimal purchasePrice,

        @NotNull LocalDate issueDate,

        @NotNull @Future(message = "Maturity date must be in the future")
        LocalDate maturityDate,

        @NotNull @Min(value = 1, message = "Total periods must be at least 1")
        int totalPeriods,

        @NotBlank String rateType,

        // CORRECCIÓN CLAVE: La tasa cupón puede ser 0 para bonos cero cupón.
        @NotNull @DecimalMin(value = "0.0", message = "Rate value cannot be negative")
        BigDecimal rateValue,

        String capitalization, // Nullable

        @NotBlank String frequency,

        @NotBlank String graceType,

        @NotNull @Min(value = 0, message = "Grace capital periods cannot be negative")
        int graceCapital,

        @NotNull @Min(value = 0, message = "Grace interest periods cannot be negative")
        int graceInterest,

        // La comisión puede ser 0, pero no negativa.
        @NotNull @DecimalMin(value = "0.0", message = "Commission cannot be negative")
        @DecimalMax(value = "100.0", message = "Commission cannot be over 100%")
        BigDecimal commission,

        // La tasa de mercado debe ser positiva.
        @NotNull @DecimalMin(value = "0.0001", message = "Market rate must be positive")
        BigDecimal marketRate,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal issuerStructuringCost,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal issuerPlacementCost,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal issuerCavaliCost,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal investorSabCost,

        @NotNull @DecimalMin(value = "0.0", message = "Cost cannot be negative")
        BigDecimal investorCavaliCost
) {}