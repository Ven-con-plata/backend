package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

public record GracePeriod(GraceType type, int capitalPeriods, int interestPeriods) {
    public GracePeriod {
        if (capitalPeriods < 0 || interestPeriods < 0) {
            throw new IllegalArgumentException("Grace periods cannot be negative.");
        }
        if (type == GraceType.TOTAL && (capitalPeriods != interestPeriods)) {
            // En gracia total, ambos períodos deben ser iguales por simplicidad del modelo
            throw new IllegalArgumentException("For TOTAL grace, capital and interest periods must be equal.");
        }
        if (type == GraceType.PARTIAL && interestPeriods > 0) {
            throw new IllegalArgumentException("For PARTIAL grace, interest periods must be 0.");
        }
    }
}
