package com.upc.ven_con_plata_backend.estimating.domain.services.queryservices;

import com.upc.ven_con_plata_backend.estimating.application.internal.dtos.ValuationResponseDto;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetAllValuationsByUserIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetValuationByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Contrato para los casos de uso de lectura de BondValuation.
 */
public interface BondValuationQueryService {
    Optional<ValuationResponseDto> handle(GetValuationByIdQuery query);
    List<ValuationResponseDto> handle(GetAllValuationsByUserIdQuery query);
}