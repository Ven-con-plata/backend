package com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories;

import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.model.BondValuationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BondValuationJpaRepository extends JpaRepository<BondValuationJpaEntity, Long> {
    List<BondValuationJpaEntity> findByUserId(Long userId);
}
