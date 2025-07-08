package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import lombok.Getter;

public enum Capitalization {
    DAY(365), FORTNIGHT(24), MONTH(12), BIMONTHLY(6), QUARTER(4), FOUR_MONTHLY(3), SEMESTER(2), YEAR(1);
    @Getter
    private final int periodsPerYear;
    Capitalization(int periodsPerYear) { this.periodsPerYear = periodsPerYear; }
}
