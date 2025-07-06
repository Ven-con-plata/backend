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
public class IndicadoresInversor {

    @Column(precision = 15, scale = 2)
    private BigDecimal van;

    @Column(precision = 8, scale = 4)
    private BigDecimal tir;

    @Column(precision = 15, scale = 2)
    private BigDecimal precioBono;

    @Column(precision = 8, scale = 4)
    private BigDecimal trea;

    @Column(precision = 10, scale = 6)
    private BigDecimal duracion;

    @Column(precision = 10, scale = 6)
    private BigDecimal duracionModificada;

    @Column(precision = 10, scale = 6)
    private BigDecimal convexidad;

    /*TODO habilitar todos*/
    public IndicadoresInversor(BigDecimal van, BigDecimal tir, BigDecimal precioBono,
                               BigDecimal trea, BigDecimal duracion, BigDecimal duracionModificada,
                               BigDecimal convexidad) {
        validarIndicadores(van, tir, precioBono, trea, duracion, duracionModificada, convexidad);
        this.van = van;
        this.tir = tir;
        this.precioBono = precioBono;
        this.trea = trea;
        this.duracion = duracion;
        this.duracionModificada = duracionModificada;
        this.convexidad = convexidad;
    }
    public IndicadoresInversor(BigDecimal precioBono, BigDecimal van, BigDecimal tir,
                               BigDecimal trea) {
        this.van = van;
        this.tir = tir;
        this.precioBono = precioBono;
        this.trea = trea;
    }

    private void validarIndicadores(BigDecimal van, BigDecimal tir, BigDecimal precioBono,
                                    BigDecimal trea, BigDecimal duracion, BigDecimal duracionModificada,
                                    BigDecimal convexidad) {
        if (precioBono != null && precioBono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio del bono debe ser mayor a cero");
        }
        if (trea != null && trea.compareTo(BigDecimal.valueOf(-1)) < 0) {
            throw new IllegalArgumentException("La TREA no puede ser menor a -100%");
        }
        if (duracion != null && duracion.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La duración no puede ser negativa");
        }
        if (duracionModificada != null && duracionModificada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La duración modificada no puede ser negativa");
        }
        if (convexidad != null && convexidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La convexidad no puede ser negativa");
        }
    }

    public boolean esRentableParaInversor() {
        return van != null && van.compareTo(BigDecimal.ZERO) >= 0;
    }

    public boolean tieneTirPositiva() {
        return tir != null && tir.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esBonoBajoRiesgo() {
        return duracion != null && duracion.compareTo(BigDecimal.valueOf(5)) <= 0;
    }

    public boolean esBonoAltoRiesgo() {
        return duracion != null && duracion.compareTo(BigDecimal.valueOf(10)) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IndicadoresInversor that = (IndicadoresInversor) obj;
        return Objects.equals(van, that.van) &&
                Objects.equals(tir, that.tir) &&
                Objects.equals(precioBono, that.precioBono) &&
                Objects.equals(trea, that.trea) &&
                Objects.equals(duracion, that.duracion) &&
                Objects.equals(duracionModificada, that.duracionModificada) &&
                Objects.equals(convexidad, that.convexidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(van, tir, precioBono, trea, duracion, duracionModificada, convexidad);
    }
}
