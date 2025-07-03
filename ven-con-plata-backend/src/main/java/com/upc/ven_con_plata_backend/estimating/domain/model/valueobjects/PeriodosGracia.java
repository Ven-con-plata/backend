package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class PeriodosGracia {

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int parcial;

    protected PeriodosGracia() {}

    public PeriodosGracia(Integer total, Integer parcial) {
        validarPeriodos(total, parcial);
        this.total = total != null ? total : 0;
        this.parcial = parcial != null ? parcial : 0;
    }

    private void validarPeriodos(Integer total, Integer parcial) {
        if (total != null && total < 0) {
            throw new IllegalArgumentException("Los períodos de gracia total no pueden ser negativos");
        }
        if (parcial != null && parcial < 0) {
            throw new IllegalArgumentException("Los períodos de gracia parcial no pueden ser negativos");
        }
        if (total != null && parcial != null && parcial > total) {
            throw new IllegalArgumentException("Los períodos de gracia parcial no pueden ser mayores a los totales");
        }
    }

    public boolean tieneGracia() {
        return total > 0;
    }

    public boolean tieneGraciaParcial() {
        return parcial > 0;
    }

    public boolean tieneGraciaTotal() {
        return total > parcial;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PeriodosGracia that = (PeriodosGracia) obj;
        return Objects.equals(total, that.total) && Objects.equals(parcial, that.parcial);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, parcial);
    }
}
