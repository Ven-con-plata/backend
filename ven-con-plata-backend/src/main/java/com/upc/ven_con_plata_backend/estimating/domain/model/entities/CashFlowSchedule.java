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

    private void generarEntriesParaInversor() {
        // Flujo inicial: egreso por compra (negativo para inversor)

    }
    private void generarIndicadoresInversor(Bono bono){

    }


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
    /*
    public CashFlowSchedule(Bono bono, RolSchedule rol) {
        this.bono = bono;
        this.rol = rol;
        this.entries = new ArrayList<>();
    }

    // Métodos de negocio
    public void agregarEntry(CashFlowEntry entry) {
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    public void establecerIndicadoresEmisor(IndicadoresEmisor indicadores) {
        this.indicadoresEmisor = indicadores;
    }

    public void establecerIndicadoresInversor(IndicadoresInversor indicadores) {
        this.indicadoresInversor = indicadores;
    }

    public List<CashFlowEntry> getEntries() {
        return new ArrayList<>(entries);
    }
    public boolean esEmisor() {
        return rol == RolSchedule.EMISOR;
    }

    public boolean esInversor() {
        return rol == RolSchedule.INVERSOR;
    }
     */
}
