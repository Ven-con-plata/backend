package com.upc.ven_con_plata_backend.estimating.domain.model.entities;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.estimating.domain.services.CalculadoraFinancieraDomainService;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class CashFlowSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolSchedule rol;

    @ElementCollection
    @CollectionTable(
            name = "entries",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    private List<CashFlowEntry> entries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name= "indicadores",
            joinColumns = @JoinColumn(name = "indicator_id")
    )
    private List<Indicator> indicadores = new ArrayList<>();

    protected CashFlowSchedule() { /* JPA */ }
    public CashFlowSchedule(Bono bono, RolSchedule rol) {
        this.rol  = rol;
        crearPara(bono);
    }

    private static final MathContext MATH_CTX = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Fábrica: crea el schedule, sus entries y los indicadores
     */
    public void crearPara(Bono bono) {
        switch (this.rol){
            case EMISOR: {
                this.generarEntriesParaEmisor(bono);
                this.generarIndicadoresEmisor(bono);
            }
            case INVERSOR:{
                this.generarEntriesParaInversor(bono);
                this.generarIndicadoresInversor(bono);
            }
        }
    }

    private void generarEntriesParaEmisor(Bono bono) {
        entries.clear();

        // ─── 1) Flujo t=0 ───
        BigDecimal V = bono.getValorNominal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal netoInicial = bono.getCostesInicialesDeudor()
                .calcularTotal(V)
                .setScale(2, RoundingMode.HALF_UP);

        entries.add(new CashFlowEntry(
                0,
                bono.getCreatedAt(),       // uso fechaEmision en vez de createdAt
                BigDecimal.ZERO.doubleValue(),
                BigDecimal.ZERO.doubleValue(),
                netoInicial.doubleValue(),
                V.doubleValue()
        ));

        // ─── 2) Parámetros francés ───
        int mesesPago     = bono.getFrecuenciaPago().getMeses();
        int totalPeriodos = bono.getPlazoEnAnios() * 12 / mesesPago;

        BigDecimal rBase = bono.getTasaInteres()
                .getValor()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        int mesesBase = bono.getTasaInteres()
                .getUnidad()
                .getMeses();

        double exponent = (double) mesesPago / mesesBase;
        double i = Math.pow(1 + rBase.doubleValue(), exponent) - 1;
        double A = V.doubleValue() *
                (i / (1 - Math.pow(1 + i, -totalPeriodos)));

        // ─── 3) Cuotas ───
        BigDecimal saldo = V;

        // --- 4) Periodos de Gracia ---
        int graciaTotal    = bono.getGracia().getTotal();
        int graciaParcial  = bono.getGracia().getParcial();

        for (int p = 1; p <= totalPeriodos; p++) {
            LocalDate fecha = bono.getCreatedAt()
                    .plusMonths((long)p * mesesPago);

            BigDecimal interesBD    = BigDecimal.ZERO;
            BigDecimal amortBD      = BigDecimal.ZERO;
            BigDecimal pagoBaseBD   = BigDecimal.ZERO;

            if (p <= graciaTotal) {
                // periodo de gracia total: ni interés ni amortización
            }
            else if (p <= graciaTotal + graciaParcial) {
                // periodo de gracia parcial: paga solo interés
                interesBD  = saldo
                        .multiply(BigDecimal.valueOf(i), MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);
                pagoBaseBD = interesBD;
            }
            else {
                // periodo normal: método francés completo
                // 1) interés
                interesBD  = saldo
                        .multiply(BigDecimal.valueOf(i), MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);

                // 2) amortización = cuota francesa A – interés
                amortBD    = BigDecimal.valueOf(A)
                        .subtract(interesBD)
                        .setScale(2, RoundingMode.HALF_UP);

                // 3) actualizo saldo pendiente
                saldo      = saldo
                        .subtract(amortBD, MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);

                // 4) la cuota base (sin gastos periódicos) es A
                pagoBaseBD = BigDecimal.valueOf(A)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // 5) gastos periódicos dinámicos (si p>0)
            BigDecimal gastosBD = bono.getGastosPeriodicosDeudor()
                    .calcularTotal(saldo, bono.getValorComercial())
                    .setScale(2, RoundingMode.HALF_UP);

            // 6) cuota total = base + gastos
            BigDecimal cuotaTotalBD = pagoBaseBD
                    .add(gastosBD)
                    .setScale(2, RoundingMode.HALF_UP);

            // 7) saldoRestante: en gracia total/parcial permanece igual al saldo previo
            BigDecimal saldoRestanteBD = saldo;

            // 8) agregar el entry
            entries.add(new CashFlowEntry(
                    p,
                    fecha,
                    amortBD.doubleValue(),
                    interesBD.doubleValue(),
                    cuotaTotalBD.doubleValue(),
                    saldoRestanteBD.doubleValue()
            ));
        }
    }

    private void generarIndicadoresEmisor(Bono bono){
        BigDecimal cok = cambiarCok(bono);
        CalculadoraFinancieraDomainService calculadoraDomainService = new CalculadoraFinancieraDomainService();
        IndicadoresEmisor indicadoresEmisor = calculadoraDomainService.calcularIndicadoresEmisor(this.entries, cok, bono.getFrecuenciaPago());
        this.indicadores.add(new Indicator("TOTAL_BOND_PRICE",indicadoresEmisor.getPrecioBono(),"VALOR"));
        this.indicadores.add(new Indicator("VAN",indicadoresEmisor.getVan(),"VALOR"));
        this.indicadores.add(new Indicator("TIR",indicadoresEmisor.getTir(),"PORCENTAJE"));
        this.indicadores.add(new Indicator("TCEA",indicadoresEmisor.getTcea(),"PORCENTAJE"));
    }

    private void generarEntriesParaInversor(Bono bono) {
        entries.clear();

        // 1) Flujo t=0: pago del inversor (costes de inversión)
        BigDecimal V = bono.getValorNominal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal costesInv = bono.getCostesInversion()
                .calcularTotal(V)
                .setScale(2, RoundingMode.HALF_UP);
        entries.add(new CashFlowEntry(
                0,
                bono.getCreatedAt(),
                0.0,
                0.0,
                costesInv.negate().doubleValue(),
                V.doubleValue()
        ));

        // 2) Parámetros del método francés usando tasa de cupón
        int mesesPago     = bono.getFrecuenciaPago().getMeses();
        int totalPeriodos = bono.getPlazoEnAnios() * 12 / mesesPago;

        BigDecimal rBaseInteres = bono.getTasaInteres()
                .getValor()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        int mesesBaseInteres = bono.getTasaInteres()
                .getUnidad()
                .getMeses();

        double exponent = (double) mesesPago / mesesBaseInteres;
        double i        = Math.pow(1 + rBaseInteres.doubleValue(), exponent) - 1;
        double A        = V.doubleValue() *
                (i / (1 - Math.pow(1 + i, -totalPeriodos)));

        // 3) Generación de cuotas (sin gastos periódicos para el inversor)
        BigDecimal saldo = V;


        int graciaTotal   = bono.getGracia().getTotal();
        int graciaParcial = bono.getGracia().getParcial();

        for (int p = 1; p <= totalPeriodos; p++) {
            LocalDate fecha = bono.getCreatedAt()
                    .plusMonths((long)p * mesesPago);

            BigDecimal interesBD  = BigDecimal.ZERO;
            BigDecimal amortBD    = BigDecimal.ZERO;
            BigDecimal cuotaBaseBD= BigDecimal.ZERO;

            if (p <= graciaTotal) {
                // Gracia total: ni interés, ni amortización, cuota base = 0
            }
            else if (p <= graciaTotal + graciaParcial) {
                // Gracia parcial: solo interés
                interesBD   = saldo
                        .multiply(BigDecimal.valueOf(i), MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);
                cuotaBaseBD = interesBD;
                // saldo NO cambia
            }
            else {
                // Periodo normal: método francés
                interesBD   = saldo
                        .multiply(BigDecimal.valueOf(i), MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);

                amortBD     = BigDecimal.valueOf(A)
                        .subtract(interesBD)
                        .setScale(2, RoundingMode.HALF_UP);

                // actualiza saldo
                saldo       = saldo
                        .subtract(amortBD, MATH_CTX)
                        .setScale(2, RoundingMode.HALF_UP);

                cuotaBaseBD = BigDecimal.valueOf(A)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Prima de vencimiento en el último periodo
            BigDecimal cuotaBD = cuotaBaseBD;
            if (p == totalPeriodos) {
                BigDecimal primaPct = bono.getBeneficioInversion()
                        .getPrimaVencimiento()
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                BigDecimal prima = primaPct.multiply(bono.getValorNominal())
                        .setScale(2, RoundingMode.HALF_UP);
                cuotaBD = cuotaBD.add(prima);
            }

            entries.add(new CashFlowEntry(
                    p,
                    fecha,
                    amortBD.doubleValue(),
                    interesBD.doubleValue(),
                    cuotaBD.doubleValue(),
                    saldo.doubleValue()
            ));
        }

    }

    private void generarIndicadoresInversor(Bono bono){
        BigDecimal cok = cambiarCok(bono);
        CalculadoraFinancieraDomainService calculadoraDomainService = new CalculadoraFinancieraDomainService();
        IndicadoresEmisor indicadoresInversor = calculadoraDomainService.calcularIndicadoresInversor(this.entries, cok, bono.getFrecuenciaPago());
        this.indicadores.add(new Indicator("TOTAL_BOND_PRICE",indicadoresInversor.getPrecioBono(),"VALOR"));
        this.indicadores.add(new Indicator("VAN",indicadoresInversor.getVan(),"VALOR"));
        this.indicadores.add(new Indicator("TIR",indicadoresInversor.getTir(),"PORCENTAJE"));
        this.indicadores.add(new Indicator("TCEA",indicadoresInversor.getTrea(),"PORCENTAJE"));
    }

    // helper
    private BigDecimal cambiarCok(Bono bono){
        // 1) Toma el valor % en decimal
        BigDecimal rBase = bono.getCok()
                .getValor()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        // 2) Meses que representa la unidad del COK
        int mesesBase = bono.getCok()
                .getUnidad()
                .getMeses();
        // 3) Meses entre cada pago
        int mesesPago = bono.getFrecuenciaPago().getMeses();
        // 4) Exponente para ajustar la capitalización
        double exp = (double) mesesPago / mesesBase;
        // 5) Fórmula: i = (1 + rBase)^exp - 1
        double i = Math.pow(1 + rBase.doubleValue(), exp) - 1;
        return BigDecimal.valueOf(i)
                .setScale(10, RoundingMode.HALF_UP);
    }
}
