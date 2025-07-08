package com.upc.ven_con_plata_backend.estimating.application.internal.domainservices;

import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowPeriod;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.estimating.domain.services.FinancialCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FinancialCalculatorServiceImpl implements FinancialCalculatorService {

    public static final int DEFAULT_PRECISION = 15;
    private static final int FINANCIAL_PRECISION = 8;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    // ===================================================================
    // == 1. METODO PRINCIPAL DE LA INTERFAZ ==
    // ===================================================================

    @Override
    public CalculationResult calculate(BondParameters params) {
        int periodsPerYear = getPeriodsPerYear(params.frequency());
        BigDecimal couponRatePerPeriod = convertToPeriodicRate(params.couponRate(), periodsPerYear);
        BigDecimal marketRatePerPeriod = convertToPeriodicRate(params.marketRate(), periodsPerYear);

        List<CashFlowPeriod> cashFlow = generateFrenchMethodCashFlow(params, marketRatePerPeriod); // This will be modified later

        FinancialMetrics metrics = calculateMetrics(params, cashFlow, marketRatePerPeriod, periodsPerYear);

        return new CalculationResult(metrics, cashFlow);
    }

    // ===================================================================
    // == 2. MÉTODOS DE AYUDA PARA CÁLCULOS INTERNOS ==
    // ===================================================================

    // --- Conversión de Tasas y Frecuencias (Refactorizado para claridad) ---

    private int getPeriodsPerYear(Frequency frequency) {
        return switch (frequency) {
            case SEMESTER -> 2;
            case QUARTER -> 4;
            case MONTH -> 12;
            case BIMONTHLY -> 6;
            case FOUR_MONTHLY -> 3;
            case FORTNIGHT -> 24;
            case DAY -> 360; // Usar base 360 como es común en finanzas
            default -> 1; // Para YEAR
        };
    }

    private BigDecimal convertToPeriodicRate(InterestRate annualRate, int periodsPerYear) {
        BigDecimal effectiveAnnualRate = annualRate.toEffectiveAnnualRate();
        double base = 1.0 + effectiveAnnualRate.doubleValue();
        double exponent = 1.0 / periodsPerYear;
        return BigDecimal.valueOf(Math.pow(base, exponent) - 1)
                .setScale(DEFAULT_PRECISION, ROUNDING_MODE);
    }

    private BigDecimal getCouponRatePerPeriod(InterestRate annualCouponRate, int periodsPerYear) {
        // Correcto: Llama a toEffectiveAnnualRate, que ya devuelve un decimal.
        BigDecimal effectiveAnnualRate = annualCouponRate.toEffectiveAnnualRate();
        return BigDecimal.valueOf(Math.pow(1 + effectiveAnnualRate.doubleValue(), 1.0 / periodsPerYear) - 1)
                .setScale(DEFAULT_PRECISION, ROUNDING_MODE);
    }

    private BigDecimal getMarketRatePerPeriod(InterestRate annualMarketRate, int periodsPerYear) {
        // Correcto: Llama a toEffectiveAnnualRate, que ya devuelve un decimal.
        BigDecimal effectiveAnnualRate = annualMarketRate.toEffectiveAnnualRate();
        return BigDecimal.valueOf(Math.pow(1 + effectiveAnnualRate.doubleValue(), 1.0 / periodsPerYear) - 1)
                .setScale(DEFAULT_PRECISION, ROUNDING_MODE);
    }

    // --- Generación del Flujo de Caja - Metodo Francés ---
    private List<CashFlowPeriod> generateFrenchMethodCashFlow(BondParameters params, BigDecimal periodicMarketRate) {
        List<CashFlowPeriod> periods = new ArrayList<>();
        BigDecimal currentBalance = params.faceValue().amount();
        int totalPeriods = params.totalPeriods();

        // Calculate the constant periodic payment using the French method formula
        BigDecimal payment = calculateFrenchMethodPayment(params.faceValue().amount(), periodicMarketRate, totalPeriods);


        for (int i = 1; i <= totalPeriods; i++) {
            BigDecimal initialBalance = currentBalance;
            BigDecimal interest = currentBalance.multiply(periodicMarketRate);
            BigDecimal principalAmortization = payment.subtract(interest);
            BigDecimal cashFlowForHolder = payment;

            GracePeriodState graceState = determineGraceState(i, params.gracePeriod());

            if (graceState == GracePeriodState.TOTAL) {
                // During total grace, only interest accrues and is added to the balance
                interest = currentBalance.multiply(periodicMarketRate);
                principalAmortization = BigDecimal.ZERO;
                cashFlowForHolder = BigDecimal.ZERO; // No payment during total grace
                currentBalance = currentBalance.add(interest);
            } else if (graceState == GracePeriodState.PARTIAL) {
                // During partial grace, only interest is paid
                interest = currentBalance.multiply(periodicMarketRate);
                principalAmortization = BigDecimal.ZERO;
                cashFlowForHolder = interest; // Only interest is paid
                // Balance remains unchanged
            } else {
                // No grace period, standard French method amortization
                interest = currentBalance.multiply(periodicMarketRate);
                principalAmortization = payment.subtract(interest);
                cashFlowForHolder = payment;
                currentBalance = currentBalance.subtract(principalAmortization);
            }


            // Adjust the last period's amortization to account for potential rounding differences
            if (i == totalPeriods && graceState == GracePeriodState.NONE) {
                principalAmortization = initialBalance;
                cashFlowForHolder = initialBalance.add(interest); // Final payment includes remaining principal and interest
            } else if (i == totalPeriods && graceState == GracePeriodState.PARTIAL) {
                principalAmortization = initialBalance;
                cashFlowForHolder = initialBalance.add(interest);
                currentBalance = BigDecimal.ZERO;
            } else if (i == totalPeriods && graceState == GracePeriodState.TOTAL) {
                principalAmortization = initialBalance; // Amortize the full remaining balance
                cashFlowForHolder = interest.add(initialBalance); // Payment includes capitalized interest and principal
                currentBalance = BigDecimal.ZERO;
            }



            periods.add(new CashFlowPeriod(i, graceState, new Money(initialBalance),
                    new Money(interest.setScale(FINANCIAL_PRECISION, ROUNDING_MODE)),
                    new Money(cashFlowForHolder.setScale(FINANCIAL_PRECISION, ROUNDING_MODE)),
                    new Money(principalAmortization.setScale(FINANCIAL_PRECISION, ROUNDING_MODE)),
                    new Money(currentBalance.setScale(FINANCIAL_PRECISION, ROUNDING_MODE)),
                    new Money(cashFlowForHolder.setScale(FINANCIAL_PRECISION, ROUNDING_MODE))));
        }
        return periods;
    }

    private BigDecimal calculateFrenchMethodPayment(BigDecimal principal, BigDecimal periodicRate, int numberOfPeriods) {
        if (periodicRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(numberOfPeriods), DEFAULT_PRECISION, ROUNDING_MODE);
        }
        // French method payment formula: P = PV * [ i(1 + i)^n ] / [ (1 + i)^n – 1]
        BigDecimal ratePlusOneToPowerN = BigDecimal.ONE.add(periodicRate).pow(numberOfPeriods);

        return principal.multiply(periodicRate.multiply(ratePlusOneToPowerN).divide(ratePlusOneToPowerN.subtract(BigDecimal.ONE), DEFAULT_PRECISION, ROUNDING_MODE));
    }

    private GracePeriodState determineGraceState(int currentPeriod, GracePeriod gracePeriod) {
        if (gracePeriod.type() == GraceType.TOTAL && currentPeriod <= gracePeriod.capitalPeriods()) {
            return GracePeriodState.TOTAL;
        }
        // Asumiendo que \"gracia parcial\" aplica hasta el final del periodo de gracia de capital
        if (gracePeriod.type() == GraceType.PARTIAL && currentPeriod <= gracePeriod.capitalPeriods()) {
            return GracePeriodState.PARTIAL;
        }
        return GracePeriodState.NONE;
    }

    // --- Cálculo de Métricas Financieras ---

    private FinancialMetrics calculateMetrics(BondParameters params, List<CashFlowPeriod> cashFlow, BigDecimal marketRatePerPeriod, int periodsPerYear) {
        List<BigDecimal> holderFlowsList = cashFlow.stream().map(p -> p.cashFlow().amount()).collect(Collectors.toList());
        BigDecimal bondPrice = calculatePresentValue(holderFlowsList, marketRatePerPeriod);

        BigDecimal macaulayDurationInPeriods = calculateMacaulayDurationInPeriods(cashFlow, marketRatePerPeriod, bondPrice);
        BigDecimal macaulayDurationInYears = macaulayDurationInPeriods.divide(BigDecimal.valueOf(periodsPerYear), FINANCIAL_PRECISION, ROUNDING_MODE);

        BigDecimal modifiedDurationInPeriods = macaulayDurationInPeriods.divide(BigDecimal.ONE.add(marketRatePerPeriod), DEFAULT_PRECISION, ROUNDING_MODE);
        BigDecimal modifiedDurationInYears = modifiedDurationInPeriods.divide(BigDecimal.valueOf(periodsPerYear), FINANCIAL_PRECISION, ROUNDING_MODE);

        BigDecimal convexityInPeriods = calculateConvexityInPeriods(cashFlow, marketRatePerPeriod, bondPrice);
        BigDecimal convexityInYearsSq = convexityInPeriods.divide(BigDecimal.valueOf((long) periodsPerYear * periodsPerYear), FINANCIAL_PRECISION, ROUNDING_MODE);

        BigDecimal trea = calculateAnnualIRR(getHolderCashFlowWithCosts(params, cashFlow), periodsPerYear);
        BigDecimal tcea = calculateAnnualIRR(getIssuerCashFlowWithCosts(params, cashFlow), periodsPerYear);

        return new FinancialMetrics(tcea, trea, macaulayDurationInYears, modifiedDurationInYears, convexityInYearsSq,
                new Money(bondPrice), new Money(bondPrice));
    }

    private BigDecimal calculatePresentValue(List<BigDecimal> flows, BigDecimal discountRate) {
        return IntStream.range(0, flows.size())
                .mapToObj(i -> flows.get(i).divide(BigDecimal.ONE.add(discountRate).pow(i + 1), DEFAULT_PRECISION, ROUNDING_MODE))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMacaulayDurationInPeriods(List<CashFlowPeriod> cashFlow, BigDecimal discountRate, BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal weightedTimeSum = IntStream.range(0, cashFlow.size())
                .mapToObj(i -> {
                    BigDecimal periodTime = BigDecimal.valueOf(i + 1);
                    BigDecimal flow = cashFlow.get(i).cashFlow().amount();
                    BigDecimal pvFactor = BigDecimal.ONE.divide(BigDecimal.ONE.add(discountRate).pow(i + 1), DEFAULT_PRECISION, ROUNDING_MODE);
                    return flow.multiply(pvFactor).multiply(periodTime);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weightedTimeSum.divide(price, DEFAULT_PRECISION, ROUNDING_MODE);
    }

/*
    private BigDecimal calculateMacaulayDurationInPeriods(List<CashFlowPeriod> cashFlow, BigDecimal discountRate, BigDecimal price) {
        BigDecimal weightedTimeSum = IntStream.range(0, cashFlow.size())
                .mapToObj(i -> {
                    BigDecimal periodTime = BigDecimal.valueOf(i + 1);
                    BigDecimal flow = cashFlow.get(i).cashFlow().amount();
                    BigDecimal pvFactor = BigDecimal.ONE.divide(BigDecimal.ONE.add(discountRate).pow(i + 1), DEFAULT_PRECISION, ROUNDING_MODE);
                    return flow.multiply(pvFactor).multiply(periodTime);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return price.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : weightedTimeSum.divide(price, DEFAULT_PRECISION, ROUNDING_MODE);
    }
*/

    private BigDecimal calculateConvexityInPeriods(List<CashFlowPeriod> cashFlow, BigDecimal discountRate, BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal convexitySum = IntStream.range(0, cashFlow.size())
        .mapToObj(i -> {
            BigDecimal t = BigDecimal.valueOf(i + 1);
            BigDecimal flow = cashFlow.get(i).cashFlow().amount();
            BigDecimal numerator = flow.multiply(t).multiply(t.add(BigDecimal.ONE));
            BigDecimal denominator = BigDecimal.ONE.add(discountRate).pow(i + 3);
            return numerator.divide(denominator, DEFAULT_PRECISION, ROUNDING_MODE);
        })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return convexitySum.divide(price, DEFAULT_PRECISION, ROUNDING_MODE);
    }

    /*
    private BigDecimal calculateConvexityInPeriods(List<CashFlowPeriod> cashFlow, BigDecimal discountRate, BigDecimal price) {
        BigDecimal convexitySum = IntStream.range(0, cashFlow.size())
                .mapToObj(i -> {
                    BigDecimal t = BigDecimal.valueOf(i + 1);
                    BigDecimal flow = cashFlow.get(i).cashFlow().amount();
                    BigDecimal pvFactor = BigDecimal.ONE.divide(BigDecimal.ONE.add(discountRate).pow(i + 1), DEFAULT_PRECISION, ROUNDING_MODE);
                    return flow.multiply(pvFactor).multiply(t).multiply(t.add(BigDecimal.ONE));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (price.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return convexitySum.divide(price.multiply(BigDecimal.ONE.add(discountRate).pow(2)), DEFAULT_PRECISION, ROUNDING_MODE);
    }
    */

    // --- Lógica de Tasa Interna de Retorno (TIR / IRR) ---

    private List<BigDecimal> getHolderCashFlowWithCosts(BondParameters params, List<CashFlowPeriod> cashFlow) {
        List<BigDecimal> flows = new ArrayList<>();

        // 1. Tomar los costos (en %) desde los parámetros y convertirlos a decimal.
        BigDecimal sabPct = params.investorSabCost().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);
        BigDecimal cavaliPct = params.investorCavaliCost().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);

        // 2. Sumar los porcentajes de costo.
        BigDecimal totalCostPercentage = sabPct.add(cavaliPct);

        // 3. Calcular el desembolso total (Flujo 0) y añadirlo a la lista (negativo).
        BigDecimal totalInvestment = params.purchasePrice().amount().multiply(BigDecimal.ONE.add(totalCostPercentage));
        flows.add(totalInvestment.negate());

        // 4. Añadir los flujos de caja futuros.
        cashFlow.forEach(p -> flows.add(p.cashFlow().amount()));

        return flows;
    }

    private List<BigDecimal> getIssuerCashFlowWithCosts(BondParameters params, List<CashFlowPeriod> cashFlow) {
        List<BigDecimal> flows = new ArrayList<>();

        // 1. Tomar los costos (en %) desde los parámetros y convertirlos a decimal.
        BigDecimal structPct = params.issuerStructuringCost().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);
        BigDecimal placePct = params.issuerPlacementCost().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);
        BigDecimal cavaliPct = params.issuerCavaliCost().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE);

        // 2. Sumar los porcentajes de costo.
        BigDecimal totalCostPercentage = structPct.add(placePct).add(cavaliPct);

        // 3. Calcular el ingreso neto (Flujo 0) y añadirlo a la lista (positivo).
        BigDecimal totalCostAmount = params.faceValue().amount().multiply(totalCostPercentage);
        BigDecimal netProceeds = params.faceValue().amount().subtract(totalCostAmount);
        flows.add(netProceeds);

        // 4. Añadir los flujos de caja futuros (negativos para el emisor).
        cashFlow.forEach(p -> flows.add(p.cashFlow().amount().negate()));

        return flows;
    }

    private List<BigDecimal> getHolderCashFlow(BondParameters params, List<CashFlowPeriod> cashFlow) {
        List<BigDecimal> flows = new ArrayList<>();
        flows.add(params.purchasePrice().amount().negate());
        cashFlow.forEach(p -> flows.add(p.cashFlow().amount()));
        return flows;
    }

    private List<BigDecimal> getIssuerCashFlow(BondParameters params, List<CashFlowPeriod> cashFlow) {
        List<BigDecimal> flows = new ArrayList<>();
        BigDecimal commissionAmount = params.issuePrice().amount().multiply(params.commission().divide(BigDecimal.valueOf(100), DEFAULT_PRECISION, ROUNDING_MODE));
        flows.add(params.issuePrice().amount().subtract(commissionAmount));
        cashFlow.forEach(p -> flows.add(p.cashFlow().amount().negate()));
        return flows;
    }

    private BigDecimal calculateAnnualIRR(List<BigDecimal> flows, int periodsPerYear) {
        BigDecimal ratePerPeriod = calculateIRR(flows, 0.1, 100, 1e-10);
        // La fórmula para anualizar y multiplicar por 100 para mostrar como porcentaje es correcta.
        return BigDecimal.ONE.add(ratePerPeriod).pow(periodsPerYear).subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100)).setScale(FINANCIAL_PRECISION, ROUNDING_MODE);
    }

    public BigDecimal calculateIRR(final List<BigDecimal> cashFlows, double guess, int maxIterations, double tolerance) {
        BigDecimal rate = new BigDecimal(guess);
        for (int i = 0; i < maxIterations; i++) {
            BigDecimal npv = calculateNPV(cashFlows, rate);
            BigDecimal derivative = calculateNPVDerivative(cashFlows, rate);
            if (derivative.abs().compareTo(BigDecimal.valueOf(1e-15)) < 0) break;
            BigDecimal newRate = rate.subtract(npv.divide(derivative, DEFAULT_PRECISION, ROUNDING_MODE));
            if (newRate.subtract(rate).abs().compareTo(BigDecimal.valueOf(tolerance)) < 0) return newRate;
            rate = newRate;
        }
        return rate;
    }

    private BigDecimal calculateNPV(List<BigDecimal> cashFlows, BigDecimal rate) {
        BigDecimal npv = BigDecimal.ZERO;
        for (int t = 0; t < cashFlows.size(); t++) {
            npv = npv.add(cashFlows.get(t).divide(BigDecimal.ONE.add(rate).pow(t), DEFAULT_PRECISION, ROUNDING_MODE));
        }
        return npv;
    }

    private BigDecimal calculateNPVDerivative(List<BigDecimal> cashFlows, BigDecimal rate) {
        BigDecimal derivative = BigDecimal.ZERO;
        for (int t = 1; t < cashFlows.size(); t++) {
            BigDecimal term = cashFlows.get(t).multiply(BigDecimal.valueOf(-t));
            derivative = derivative.add(term.divide(BigDecimal.ONE.add(rate).pow(t + 1), DEFAULT_PRECISION, ROUNDING_MODE));
        }
        return derivative;
    }
}