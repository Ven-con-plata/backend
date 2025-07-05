package com.upc.ven_con_plata_backend.estimating.domain.services;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.CashFlowEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class FinancialCalculator {
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    /** NPV de un array de flujos y una tasa periódica (decimal). */
    public static BigDecimal npv(List<CashFlowEntry> entries, BigDecimal rate) {
        BigDecimal sum = BigDecimal.ZERO;
        for (CashFlowEntry e : entries) {
            BigDecimal cf = BigDecimal.valueOf(e.getCuotaTotal());
            BigDecimal discount = BigDecimal.ONE
                    .add(rate)
                    .pow(e.getPeriodo(), MC);
            sum = sum.add(cf.divide(discount, MC));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    /** IRR con Newton–Raphson sobre un array de doubles. */
    public static double irr(double[] cashFlows) {
        double x0 = 0.1;
        for (int i = 0; i < 100; i++) {
            double f   = npv(cashFlows, x0);
            double fʹ  = npvDerivative(cashFlows, x0);
            double x1  = x0 - f/fʹ;
            if (Math.abs(x1 - x0) < 1e-8) break;
            x0 = x1;
        }
        return x0;
    }

    private static double npv(double[] cf, double rate) {
        double sum = 0;
        for (int t = 0; t < cf.length; t++) {
            sum += cf[t] / Math.pow(1 + rate, t);
        }
        return sum;
    }

    private static double npvDerivative(double[] cf, double rate) {
        double sum = 0;
        for (int t = 1; t < cf.length; t++) {
            sum -= t * cf[t] / Math.pow(1 + rate, t + 1);
        }
        return sum;
    }
}
