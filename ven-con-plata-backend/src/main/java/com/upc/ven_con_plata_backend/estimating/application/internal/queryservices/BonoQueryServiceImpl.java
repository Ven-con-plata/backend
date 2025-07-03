package com.upc.ven_con_plata_backend.estimating.application.internal.queryservices;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.*;
import com.upc.ven_con_plata_backend.estimating.domain.services.BonoQueryService;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories.BonoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BonoQueryServiceImpl implements BonoQueryService {

    private final BonoRepository bonoRepository;

    public BonoQueryServiceImpl(BonoRepository bonoRepository) {
        this.bonoRepository = bonoRepository;
    }

    @Override
    public List<Bono> handle(GetAllBonosQuery query) {
        return bonoRepository.findAll();
    }

    @Override
    public List<Bono> handle(GetBonosByEmisorIdQuery query) {
        return bonoRepository.findById(query.emisorId())
                .map(bono -> List.of(bono))
                .orElse(List.of());
    }

    @Override
    public List<Bono> handle(GetBonosByCompanyIdQuery query) {
        return bonoRepository.findById(query.companyId())
                .map(bono -> List.of(bono))
                .orElse(List.of());
    }

    @Override
    public List<Bono> handle(GetBonosByInvestorIdQuery query) {
        return bonoRepository.findById(query.investorId())
                .map(bono -> List.of(bono))
                .orElse(List.of());
    }

    @Override
    public Optional<Bono> handle(GetBonoByIdQuery query) {
        return bonoRepository.findById(query.bonoId());
    }
}
