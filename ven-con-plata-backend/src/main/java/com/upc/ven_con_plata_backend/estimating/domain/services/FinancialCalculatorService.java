package com.upc.ven_con_plata_backend.estimating.domain.services;

import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowPeriod;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.BondParameters;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.FinancialMetrics;

import java.util.List;

/**
 * Contrato para un servicio de dominio que realiza cálculos financieros.
 * Su implementación vivirá en la capa de aplicación o infraestructura.
 */
public interface FinancialCalculatorService {

    /**
         * DTO interno para devolver los resultados del cálculo.
         * Es estático para que pueda ser usado sin una instancia del servicio.
         */
        record CalculationResult(FinancialMetrics metrics, List<CashFlowPeriod> cashFlow) {
    }

    /**
     * Realiza todos los cálculos financieros basados en los parámetros de un bono.
     * @param params Los parámetros de entrada del bono.
     * @return Un objeto CalculationResult que contiene las métricas y el flujo de caja.
     */
    CalculationResult calculate(BondParameters params);
}