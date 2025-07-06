package com.upc.ven_con_plata_backend.estimating.domain.services;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowSchedule;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class CalculadoraFinancieraDomainService {
    private static final int PRECISION = 10;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public IndicadoresEmisor calcularIndicadoresEmisor(List<CashFlowEntry> cashFlowEntries, BigDecimal cok, Periodicidad frecuenciaPago) {
        // Calcular VAN, TIR y TCEA desde perspectiva del emisor
        if (cashFlowEntries == null) {
            throw new IllegalStateException("El bono debe tener cronograma de emisor generado");
        }
        BigDecimal precioBono = calcularPrecioBono(cashFlowEntries, cok);
        BigDecimal van = calcularVAN(cashFlowEntries, precioBono);
        // 1) TIR periódica en %
        BigDecimal tir = calcularTIR(cashFlowEntries);
        BigDecimal tcea = calcularTCEA(tir, frecuenciaPago);

        return new IndicadoresEmisor(precioBono, van, tir, tcea);
    }

    public IndicadoresInversor calcularIndicadoresInversor(List<CashFlowEntry> cashFlowEntries, BigDecimal cok, Periodicidad frecuenciaPago) {
        // Calcular VAN, TIR y TCEA desde perspectiva del emisor
        if (cashFlowEntries == null) {
            throw new IllegalStateException("El bono debe tener cronograma de emisor generado");
        }
        BigDecimal precioBono = calcularPrecioBono(cashFlowEntries, cok);
        BigDecimal van = calcularVAN(cashFlowEntries, precioBono);
        // 1) TIR periódica en %
        BigDecimal tir = calcularTIR(cashFlowEntries);
        BigDecimal trea = calcularTREA(tir, frecuenciaPago);

        return new IndicadoresInversor(precioBono, van, tir, trea);
    }

    // Métodos privados de cálculo
    private BigDecimal calcularPrecioBono(List<CashFlowEntry> entries,
                                          BigDecimal tasaPeriodica) {
        BigDecimal precio = BigDecimal.ZERO;
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

        for (CashFlowEntry e : entries) {
            if (e.getPeriodo() == 0) continue;    // omitimos el flujo inicial
            BigDecimal flujo = BigDecimal.valueOf(e.getCuotaTotal());
            BigDecimal factor = BigDecimal.ONE
                    .add(tasaPeriodica)
                    .pow(e.getPeriodo(), mc);
            precio = precio.add(
                    flujo.divide(factor, 10, RoundingMode.HALF_UP)
            );
        }
        return precio.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularVAN(List<CashFlowEntry> entries, BigDecimal precio) {
        // Flujo inicial en t=0 (inversión neta al comprar el bono)
        BigDecimal flujo0 = BigDecimal.valueOf(entries.get(0).getCuotaTotal());
        return precio.subtract(flujo0)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTIR(List<CashFlowEntry> entries) {
        // Construye el array de flujos (t=0 negativo, t>0 positivos)
        double[] flujos = entries.stream()
                .mapToDouble(e -> e.getPeriodo() == 0
                        ? -e.getCuotaTotal()
                        :  e.getCuotaTotal())
                .toArray();

        // Llama al método IRR que devuelve la tasa periódica en decimal
        double irrDecimal = FinancialCalculator.irr(flujos);

        // Lo convierte a porcentaje con 4 decimales
        return BigDecimal.valueOf(irrDecimal * 100)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTCEA(BigDecimal tirPercentual,
                                    Periodicidad frecuenciaPago) {
        // 1) De porcentaje a decimal: 12.3456% → 0.123456
        BigDecimal tirDecimal = tirPercentual
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        // 2) Número de periodos anuales (p.ej. semestral → 2)
        int periodosPorAnio = 12 / frecuenciaPago.getMeses();

        // 3) Anualiza: (1 + tirDecimal)^(periodosPorAnio) – 1
        double tceaDecimal = Math.pow(
                1 + tirDecimal.doubleValue(),
                periodosPorAnio
        ) - 1;

        // 4) A porcentaje y escala
        return BigDecimal.valueOf(tceaDecimal * 100)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTREA(BigDecimal tirPercentual,
                                    Periodicidad frecuenciaPago) {
        // Convertir la TIR de porcentaje a decimal
        BigDecimal tirDecimal = tirPercentual
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        // Número de períodos de pago por año (p.ej. semestral → 2)
        int periodosPorAnio = 12 / frecuenciaPago.getMeses();

        // Anualizar: (1 + tirDecimal)^(periodosPorAnio) – 1
        double treaDecimal = Math.pow(
                1 + tirDecimal.doubleValue(),
                periodosPorAnio
        ) - 1;

        // A porcentaje con 4 decimales
        return BigDecimal.valueOf(treaDecimal * 100)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDuracion() {
        // Implementación del cálculo de duración de Macaulay
        return BigDecimal.ZERO; // Placeholder
    }

    private BigDecimal calcularDuracionModificada() {
        return BigDecimal.ZERO; // Placeholder
    }

    private BigDecimal calcularConvexidad() {
        // Implementación del cálculo de convexidad
        return BigDecimal.ZERO; // Placeholder
    }
}
