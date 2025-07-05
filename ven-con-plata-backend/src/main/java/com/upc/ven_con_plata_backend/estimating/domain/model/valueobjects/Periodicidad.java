package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum Periodicidad {
    ANUAL(12),
    SEMESTRAL(6),
    CUATRIMESTRAL(4),
    TRIMESTRAL(3),
    MENSUAL(1);

    private final int meses;

    Periodicidad(int meses) {
        this.meses = meses;
    }

    /** Devuelve el número de meses que dura la periodicidad */
    public int getMeses() {
        return meses;
    }
}