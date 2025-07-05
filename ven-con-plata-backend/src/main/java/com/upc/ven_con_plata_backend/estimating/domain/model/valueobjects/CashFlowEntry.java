package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashFlowEntry {

    @Column(nullable = false)
    private int periodo;

    @Column(nullable = false)
    private LocalDate fecha_pago;

    @Column(nullable = false)
    private Double amortizacion;

    @Column(nullable = false)
    private Double interes;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoEntry tipo;

    @Column (nullable = false)
    private Double cuota_total;


    public CashFlowEntry(int periodo, LocalDate fecha_pago, Double amortizacion, Double interes, String tipo, Double cuota_total) {
        //validarDatos(fecha, monto, tipo);
        this.periodo = periodo;
        this.amortizacion = amortizacion;
        this.interes = interes;
        this.fecha_pago = fecha_pago;
        this.cuota_total = cuota_total;
        this.tipo = TipoEntry.valueOf(tipo.toUpperCase());
    }

    private void validarDatos(LocalDate fecha, BigDecimal monto, TipoEntry tipo) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (monto == null) {
            throw new IllegalArgumentException("El monto no puede ser nulo");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de entry no puede ser nulo");
        }
    }
/*
    public boolean esIngreso() {
        return monto.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esEgreso() {
        return monto.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean esCupon() {
        return tipo == TipoEntry.CUPON;
    }

    public boolean esAmortizacion() {
        return tipo == TipoEntry.AMORTIZACION;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CashFlowEntry that = (CashFlowEntry) obj;
        return Objects.equals(fecha, that.fecha) &&
                Objects.equals(monto, that.monto) &&
                tipo == that.tipo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fecha, monto, tipo);
    }

    @Override
    public String toString() {
        return String.format("CashFlowEntry{fecha=%s, monto=%s, tipo=%s}",
                fecha, monto, tipo);
    }*/
}
