package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum Periodicidad {
    ANUAL(1, 12),
    SEMESTRAL(2, 6),
    TRIMESTRAL(4, 3),
    BIMESTRAL(6, 2),
    MENSUAL(12, 1),
    QUINCENAL(24, 0.5);

    private final int periodosPorAno;
    private final double mesesEntrePagos;

    Periodicidad(int periodosPorAno, double mesesEntrePagos) {
        this.periodosPorAno = periodosPorAno;
        this.mesesEntrePagos = mesesEntrePagos;
    }

    public int getMesesEntrePagos() {
        return (int) Math.ceil(mesesEntrePagos);
    }
}