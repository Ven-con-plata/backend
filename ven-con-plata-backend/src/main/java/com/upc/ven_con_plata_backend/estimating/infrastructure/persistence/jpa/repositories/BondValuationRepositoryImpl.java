package com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories;

import lombok.RequiredArgsConstructor;
import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.BondValuation;
import com.upc.ven_con_plata_backend.estimating.domain.repositories.BondValuationRepository;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.mappers.BondValuationPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Implementación de la interfaz BondValuationRepository del dominio.
 * Esta clase actúa como un adaptador entre el modelo de dominio y la persistencia JPA.
 */
@Repository // Le decimos a Spring que esta es una implementación de un repositorio.
@RequiredArgsConstructor
public class BondValuationRepositoryImpl implements BondValuationRepository {

    // Inyectamos el repositorio de Spring Data JPA
    private final BondValuationJpaRepository jpaRepository;

    // Inyectamos el mapper que traduce entre dominio y JPA
    private final BondValuationPersistenceMapper mapper;

    @Override
    public BondValuation save(BondValuation valuation) {
        var jpaEntity = mapper.toJpaEntity(valuation);

        // El objeto 'savedEntity' es la entidad JPA que ahora tiene el ID.
        var savedEntity = jpaRepository.save(jpaEntity);

        // Reconstruimos el objeto de dominio a partir de la entidad guardada.
        BondValuation savedDomainObject = mapper.toDomain(savedEntity);

        // POR SEGURIDAD ABSOLUTA: Si el mapper fallara en asignar el ID, lo forzamos aquí.
        // Aunque no debería ser necesario con la configuración actual, esto no hace daño.
        if (savedDomainObject.getId() == null && savedEntity.getId() != null) {
            savedDomainObject.setId(savedEntity.getId());
        }

        return savedDomainObject;
    }

    @Override
    public Optional<BondValuation> findById(Long id) {
        // Busca la entidad JPA y, si la encuentra, la mapea al dominio
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}