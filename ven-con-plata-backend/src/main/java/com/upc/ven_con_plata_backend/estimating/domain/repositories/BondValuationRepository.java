package com.upc.ven_con_plata_backend.estimating.domain.repositories;


import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.BondValuation;

import java.util.Optional;

public interface BondValuationRepository {
    BondValuation save(BondValuation valuation);
    Optional<BondValuation> findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}