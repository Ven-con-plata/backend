package com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.mappers;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.BondValuation;
import com.upc.ven_con_plata_backend.estimating.domain.model.entities.CashFlowPeriod;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.model.BondValuationJpaEntity;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.model.CashFlowPeriodJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BondValuationPersistenceMapper {

    // ===================================================================
    // == Mapeo de Dominio a Entidad de Persistencia (JPA) ==
    // ===================================================================
    // Para esta dirección, las anotaciones @Mapping funcionan bien porque la navegación
    // es simple (desde un objeto anidado a campos planos).
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "createdAt", target = "createdAt"),
            @Mapping(source = "updatedAt", target = "updatedAt"),
            @Mapping(source = "valuationName", target = "valuationName"),
            @Mapping(source = "userId.id", target = "userId"),
            @Mapping(source = "parameters.faceValue.amount", target = "faceValueAmount"),
            @Mapping(source = "parameters.faceValue.currency", target = "faceValueCurrency"),
            @Mapping(source = "parameters.issuePrice.amount", target = "issuePriceAmount"),
            @Mapping(source = "parameters.issuePrice.currency", target = "issuePriceCurrency"),
            @Mapping(source = "parameters.purchasePrice.amount", target = "purchasePriceAmount"),
            @Mapping(source = "parameters.purchasePrice.currency", target = "purchasePriceCurrency"),
            @Mapping(source = "parameters.issueDate", target = "issueDate"),
            @Mapping(source = "parameters.maturityDate", target = "maturityDate"),
            @Mapping(source = "parameters.totalPeriods", target = "totalPeriods"),
            @Mapping(source = "parameters.couponRate.value", target = "couponRateValue"),
            @Mapping(source = "parameters.couponRate.type", target = "couponRateType"),
            @Mapping(source = "parameters.couponRate.capitalization", target = "couponRateCapitalization"),
            @Mapping(source = "parameters.frequency", target = "frequency"),
            @Mapping(source = "parameters.gracePeriod.type", target = "graceType"),
            @Mapping(source = "parameters.gracePeriod.capitalPeriods", target = "graceCapitalPeriods"),
            @Mapping(source = "parameters.gracePeriod.interestPeriods", target = "graceInterestPeriods"),
            @Mapping(source = "parameters.commission", target = "commission"),
            @Mapping(source = "parameters.marketRate.value", target = "marketRateValue"),
            @Mapping(source = "metrics.tcea", target = "tcea"),
            @Mapping(source = "metrics.trea", target = "trea"),
            @Mapping(source = "metrics.macaulayDuration", target = "macaulayDuration"),
            @Mapping(source = "metrics.modifiedDuration", target = "modifiedDuration"),
            @Mapping(source = "metrics.convexity", target = "convexity"),
            @Mapping(source = "metrics.dirtyPrice.amount", target = "dirtyPriceAmount"),
            @Mapping(source = "metrics.dirtyPrice.currency", target = "dirtyPriceCurrency"),
            @Mapping(source = "metrics.cleanPrice.amount", target = "cleanPriceAmount"),
            @Mapping(source = "metrics.cleanPrice.currency", target = "cleanPriceCurrency"),
            @Mapping(target = "cashFlow", source = "cashFlow")
    })
    BondValuationJpaEntity toJpaEntity(BondValuation domain);

    // Metodo de ayuda para la lista
    List<CashFlowPeriodJpaEntity> cashFlowPeriodListToJpaEntityList(List<CashFlowPeriod> list);


    // ===================================================================
    // == Mapeo de Entidad de Persistencia (JPA) a Dominio (Implementación Manual) ==
    // ===================================================================
    // ABANDONAMOS LAS ANOTACIONES Y PROVEEMOS LA IMPLEMENTACIÓN COMPLETA
    default BondValuation toDomain(BondValuationJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        BondParameters parameters = new BondParameters(
                new Money(jpaEntity.getFaceValueAmount(), jpaEntity.getFaceValueCurrency()),
                new Money(jpaEntity.getIssuePriceAmount(), jpaEntity.getIssuePriceCurrency()),
                new Money(jpaEntity.getPurchasePriceAmount(), jpaEntity.getPurchasePriceCurrency()),
                jpaEntity.getIssueDate(),
                jpaEntity.getMaturityDate(),
                jpaEntity.getTotalPeriods(),
                new InterestRate(jpaEntity.getCouponRateValue(), jpaEntity.getCouponRateType(), Optional.ofNullable(jpaEntity.getCouponRateCapitalization())),
                jpaEntity.getFrequency(),
                new GracePeriod(jpaEntity.getGraceType(), jpaEntity.getGraceCapitalPeriods(), jpaEntity.getGraceInterestPeriods()),
                jpaEntity.getCommission(),
                new InterestRate(jpaEntity.getMarketRateValue(), RateType.EFFECTIVE, Optional.empty()),
                jpaEntity.getIssuerStructuringCost(),
                jpaEntity.getIssuerPlacementCost(),
                jpaEntity.getIssuerCavaliCost(),
                jpaEntity.getInvestorSabCost(),
                jpaEntity.getInvestorCavaliCost()
        );

        FinancialMetrics metrics = null;
        if (jpaEntity.getTcea() != null) {
            metrics = new FinancialMetrics(
                    jpaEntity.getTcea(), jpaEntity.getTrea(), jpaEntity.getMacaulayDuration(),
                    jpaEntity.getModifiedDuration(), jpaEntity.getConvexity(),
                    new Money(jpaEntity.getDirtyPriceAmount(), jpaEntity.getDirtyPriceCurrency()),
                    new Money(jpaEntity.getCleanPriceAmount(), jpaEntity.getCleanPriceCurrency())
            );
        }

        BondValuation valuation = new BondValuation(
                jpaEntity.getValuationName(), new UserId(jpaEntity.getUserId()), parameters
        );

        valuation.setId(jpaEntity.getId());
        valuation.setCreatedAt(jpaEntity.getCreatedAt());
        valuation.setUpdatedAt(jpaEntity.getUpdatedAt());

        if (metrics != null && jpaEntity.getCashFlow() != null) {
            List<CashFlowPeriod> cashFlowDomainList = jpaEntity.getCashFlow().stream()
                    .map(this::cashFlowPeriodFromJpaEntity)
                    .collect(Collectors.toList());
            valuation.completeValuation(metrics, cashFlowDomainList);
        }

        return valuation;
    }

    /**
     * Implementación MANUAL y EXPLÍCITA para mapear un CashFlowPeriod de dominio a su entidad JPA.
     * Esto reemplaza la versión generada automáticamente que estaba incompleta.
     */
    default CashFlowPeriodJpaEntity cashFlowPeriodToJpaEntity(CashFlowPeriod domain) {
        if (domain == null) {
            return null;
        }

        CashFlowPeriodJpaEntity jpaEntity = new CashFlowPeriodJpaEntity();

        // Mapeo explícito campo por campo
        jpaEntity.setPeriodNumber(domain.number());
        jpaEntity.setGracePeriodState(domain.gracePeriodState());

        jpaEntity.setInitialBalanceAmount(domain.initialBalance().amount());
        jpaEntity.setInitialBalanceCurrency(domain.initialBalance().currency());

        jpaEntity.setInterestAmount(domain.interest().amount());
        jpaEntity.setInterestCurrency(domain.interest().currency());

        jpaEntity.setCouponAmount(domain.coupon().amount());
        jpaEntity.setCouponCurrency(domain.coupon().currency());

        jpaEntity.setAmortizationAmount(domain.amortization().amount());
        jpaEntity.setAmortizationCurrency(domain.amortization().currency());

        jpaEntity.setFinalBalanceAmount(domain.finalBalance().amount());
        jpaEntity.setFinalBalanceCurrency(domain.finalBalance().currency());

        jpaEntity.setCashFlowAmount(domain.cashFlow().amount());
        jpaEntity.setCashFlowCurrency(domain.cashFlow().currency());

        return jpaEntity;
    }

    /**
     * Implementación MANUAL y EXPLÍCITA para mapear una entidad JPA a un CashFlowPeriod de dominio.
     */
    default CashFlowPeriod cashFlowPeriodFromJpaEntity(CashFlowPeriodJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return new CashFlowPeriod(
                jpaEntity.getPeriodNumber(),
                jpaEntity.getGracePeriodState(),
                new Money(jpaEntity.getInitialBalanceAmount(), jpaEntity.getInitialBalanceCurrency()),
                new Money(jpaEntity.getInterestAmount(), jpaEntity.getInterestCurrency()),
                new Money(jpaEntity.getCouponAmount(), jpaEntity.getCouponCurrency()),
                new Money(jpaEntity.getAmortizationAmount(), jpaEntity.getAmortizationCurrency()),
                new Money(jpaEntity.getFinalBalanceAmount(), jpaEntity.getFinalBalanceCurrency()),
                new Money(jpaEntity.getCashFlowAmount(), jpaEntity.getCashFlowCurrency())
        );
    }

    // Metodo de ayuda genérico para desenvolver Optional, usado en el @Mapping de 'toJpaEntity'.
    default <T> T fromOptional(Optional<T> optional) {
        return optional.orElse(null);
    }
}