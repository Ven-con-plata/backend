package com.upc.ven_con_plata_backend.estimating.domain.model.entities;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
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
        for (int p = 1; p <= totalPeriodos; p++) {
            LocalDate fecha = bono.getCreatedAt()
                    .plusMonths((long)p * mesesPago);

            // interés
            BigDecimal interesBD = saldo
                    .multiply(BigDecimal.valueOf(i), MATH_CTX)
                    .setScale(2, RoundingMode.HALF_UP);

            // amortización
            BigDecimal amortBD = BigDecimal.valueOf(A)
                    .subtract(interesBD)
                    .setScale(2, RoundingMode.HALF_UP);

            // nuevo saldo
            saldo = saldo
                    .subtract(amortBD, MATH_CTX)
                    .setScale(2, RoundingMode.HALF_UP);

            // gastos periódicos dinámicos
            BigDecimal gastosBD = bono.getGastosPeriodicosDeudor()
                    .calcularTotal(saldo, bono.getValorComercial())
                    .setScale(2, RoundingMode.HALF_UP);

            // cuota total = A + gastos
            BigDecimal cuotaTotalBD = BigDecimal.valueOf(A)
                    .add(gastosBD)
                    .setScale(2, RoundingMode.HALF_UP);

            entries.add(new CashFlowEntry(
                    p,
                    fecha,
                    amortBD.doubleValue(),
                    interesBD.doubleValue(),
                    cuotaTotalBD.doubleValue(),
                    saldo.doubleValue()
            ));
        }
    }


    private void generarIndicadoresEmisor(){

    }

    private void generarEntriesParaInversor() {
        // Flujo inicial: egreso por compra (negativo para inversor)
    }
    private void generarIndicadoresInversor(Bono bono){

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
