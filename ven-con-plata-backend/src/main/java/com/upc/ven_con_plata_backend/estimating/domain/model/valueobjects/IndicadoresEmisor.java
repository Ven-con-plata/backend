package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndicadoresEmisor {
    @Column(precision = 15, scale = 2)
    private BigDecimal precioBono;

    @Column(precision = 15, scale = 2)
    private BigDecimal van;

    @Column(precision = 8, scale = 4)
    private BigDecimal tir;

    @Column(precision = 8, scale = 4)
    private BigDecimal tcea;

    public IndicadoresEmisor(BigDecimal precioBono, BigDecimal van, BigDecimal tir, BigDecimal tcea) {
        validarIndicadores(van, tir, tcea);
        this.precioBono = precioBono;
        this.van = van;
        this.tir = tir;
        this.tcea = tcea;
    }

    private void validarIndicadores(BigDecimal van, BigDecimal tir, BigDecimal tcea) {
        // Validaciones básicas - pueden ser más específicas según reglas de negocio
        if (tir != null && tir.compareTo(BigDecimal.valueOf(-1)) < 0) {
            throw new IllegalArgumentException("La TIR no puede ser menor a -100%");
        }
        if (tcea != null && tcea.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La TCEA no puede ser negativa");
        }
    }

    public boolean esRentableParaEmisor() {
        return van != null && van.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean tieneTirPositiva() {
        return tir != null && tir.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IndicadoresEmisor that = (IndicadoresEmisor) obj;
        return Objects.equals(van, that.van) &&
                Objects.equals(tir, that.tir) &&
                Objects.equals(tcea, that.tcea);
    }

    @Override
    public int hashCode() {
        return Objects.hash(van, tir, tcea);
    }
}
