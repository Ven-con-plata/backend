package com.upc.ven_con_plata_backend.estimating.domain.model.commands;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeleteBondValuationCommand(
        @NotNull @Positive Long valuationId
) {}
