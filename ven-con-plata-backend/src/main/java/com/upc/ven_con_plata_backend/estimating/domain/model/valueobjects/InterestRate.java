package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Optional;

import static com.upc.ven_con_plata_backend.estimating.application.internal.domainservices.FinancialCalculatorServiceImpl.DEFAULT_PRECISION;
import static com.upc.ven_con_plata_backend.estimating.application.internal.domainservices.FinancialCalculatorServiceImpl.ROUNDING_MODE;
public record InterestRate(BigDecimal value, RateType type, Optional<Capitalization> capitalization) {
    public InterestRate {
        // Reglas de negocio encapsuladas aquí
        if (type == RateType.NOMINAL && capitalization.isEmpty()) {
            throw new IllegalArgumentException("Capitalization is required for a nominal rate.");
        }
        if (type == RateType.EFFECTIVE && capitalization.isPresent()) {
            throw new IllegalArgumentException("Capitalization is not applicable for an effective rate.");
        }
    }

    public BigDecimal toEffectiveAnnualRate() {
        // La conversión de porcentaje a decimal ocurre aquí y solo aquí.
        // El 'value' es el número que viene de la API (ej. 6.0)
        BigDecimal rateAsDecimal = value.divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);

        if (type == RateType.EFFECTIVE) {
            return rateAsDecimal;
        }

        // Lógica para Tasa Nominal
       int m = capitalization.orElseThrow(() -> new IllegalStateException("Capitalization is required for nominal rate.")).getPeriodsPerYear();
        BigDecimal ratePerPeriod = rateAsDecimal.divide(BigDecimal.valueOf(m), DEFAULT_PRECISION, ROUNDING_MODE);
        return BigDecimal.ONE.add(ratePerPeriod).pow(m).subtract(BigDecimal.ONE);
    }
}