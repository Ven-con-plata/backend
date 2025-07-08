package com.upc.ven_con_plata_backend.estimating.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import com.upc.ven_con_plata_backend.estimating.application.internal.dtos.ValuationResponseDto;
import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.BondValuation;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetAllValuationsByUserIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetValuationByIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.services.queryservices.BondValuationQueryService;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.mappers.BondValuationPersistenceMapper;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.model.BondValuationJpaEntity;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories.BondValuationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de la interfaz BondValuationQueryService.
 * Se encarga de manejar todas las operaciones de consulta relacionadas con BondValuation.
 */
@Service
@RequiredArgsConstructor
public class BondValuationQueryServiceImpl implements BondValuationQueryService {

    // Dependencia directa al repositorio de Spring Data JPA para optimizar las lecturas.
    private final BondValuationJpaRepository jpaRepository;

    // Dependencia al mapper de persistencia para reconstruir los objetos de dominio o DTOs.
    private final BondValuationPersistenceMapper persistenceMapper;

    /**
     * Maneja la consulta para obtener una valoración por su ID.
     * @param query El objeto de consulta que contiene el ID.
     * @return Un Optional que contiene el DTO de respuesta si se encuentra la valoración.
     */
    @Override
    @Transactional(readOnly = true) // Anotación para optimizar transacciones de solo lectura
    public Optional<ValuationResponseDto> handle(GetValuationByIdQuery query) {
        // Busca la entidad JPA por ID.
        // Si la encuentra, la mapea al DTO de respuesta.
        return jpaRepository.findById(query.valuationId())
                .map(this::mapToDto);
    }

    /**
     * Maneja la consulta para obtener todas las valoraciones de un usuario específico.
     * @param query El objeto de consulta que contiene el ID del usuario.
     * @return Una lista de DTOs de respuesta.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ValuationResponseDto> handle(GetAllValuationsByUserIdQuery query) {
        // Busca todas las entidades JPA por el ID del usuario.
        // Convierte el stream de entidades a una lista de DTOs de respuesta.
        return jpaRepository.findByUserId(query.userId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Metodo de ayuda privado para convertir una BondValuationJpaEntity a un ValuationResponseDto.
     * Este metodo centraliza la lógica de mapeo para evitar duplicación de código.
     * @param entity La entidad de persistencia a convertir.
     * @return El DTO de respuesta poblado.
     */
    private ValuationResponseDto mapToDto(BondValuationJpaEntity entity) {
        // 1. Reutilizamos el mapper de persistencia para reconstruir el Aggregate de Dominio completo.
        // Esto es útil porque el mapper ya tiene toda la lógica para ensamblar los VOs complejos.
        BondValuation domainObject = persistenceMapper.toDomain(entity);

        // 2. Construimos el DTO de respuesta a partir del objeto de dominio.
        // Esto asegura que los datos en el DTO son consistentes con las reglas de negocio.
        return new ValuationResponseDto(
                domainObject.getId(),
                domainObject.getValuationName(),
                domainObject.getUserId().id(),
                domainObject.getParameters(),
                domainObject.getMetrics(),
                domainObject.getCashFlow()
        );
    }
}