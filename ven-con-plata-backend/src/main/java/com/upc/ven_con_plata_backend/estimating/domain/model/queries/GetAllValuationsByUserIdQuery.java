package com.upc.ven_con_plata_backend.estimating.domain.model.queries;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GetAllValuationsByUserIdQuery(@NotNull @Positive Long userId) {}
