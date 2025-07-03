package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
@Getter
public class Tasa {

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false, precision = 8, scale = 4)
    private Periodicidad unidad;

    protected Tasa() {}

    public Tasa(BigDecimal valor, Periodicidad unidad) {
        validarTasa(valor, unidad);
        this.valor = valor;
        this.unidad = unidad;
    }

    private void validarTasa(BigDecimal valor, Periodicidad unidad) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor de la tasa no puede ser nulo");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La tasa no puede ser negativa");
        }
        if (valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("La tasa no puede ser mayor al 100%");
        }
        if (unidad == null) {
            throw new IllegalArgumentException("La unidad de la tasa no puede ser nula");
        }
    }

    public BigDecimal convertirA(Periodicidad nuevaUnidad) {
        if (this.unidad == nuevaUnidad) {
            return this.valor;
        }

        // Lógica de conversión entre periodicidades
        // Simplificada - en producción sería más compleja
        double factorConversion = (double) this.unidad.getPeriodosPorAno() / nuevaUnidad.getPeriodosPorAno();
        return this.valor.multiply(BigDecimal.valueOf(factorConversion));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tasa tasa = (Tasa) obj;
        return Objects.equals(valor, tasa.valor) && unidad == tasa.unidad;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, unidad);
    }

    @Override
    public String toString() {
        return valor + "% " + unidad.name();
    }
}
