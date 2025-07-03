package com.upc.ven_con_plata_backend.estimating.domain.services;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface BonoQueryService {
    List<Bono> handle(GetAllBonosQuery query);
    List<Bono> handle(GetBonosByEmisorIdQuery query);
    List<Bono> handle(GetBonosByCompanyIdQuery query);
    List<Bono> handle(GetBonosByInvestorIdQuery query);
    Optional<Bono> handle(GetBonoByIdQuery query);
}
