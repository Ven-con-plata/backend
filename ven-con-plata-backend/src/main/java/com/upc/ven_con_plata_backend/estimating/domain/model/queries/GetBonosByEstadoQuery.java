package com.upc.ven_con_plata_backend.estimating.domain.model.queries;

import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.EstadoBono;

public record GetBonosByEstadoQuery(EstadoBono estado) {}