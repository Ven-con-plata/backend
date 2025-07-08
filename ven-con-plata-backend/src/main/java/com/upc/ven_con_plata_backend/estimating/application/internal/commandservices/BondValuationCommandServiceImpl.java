package com.upc.ven_con_plata_backend.estimating.application.internal.commandservices;

import jakarta.transaction.Transactional;
import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.BondValuation;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.DeleteBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBondValuationCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.BondParameters;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.UserId;
import com.upc.ven_con_plata_backend.estimating.domain.repositories.BondValuationRepository;
import com.upc.ven_con_plata_backend.estimating.domain.services.FinancialCalculatorService;
import com.upc.ven_con_plata_backend.estimating.domain.services.commandservices.BondValuationCommandService;
import com.upc.ven_con_plata_backend.estimating.interfaces.acl.services.IamContextFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BondValuationCommandServiceImpl implements BondValuationCommandService { // Implementaría una interfaz CommandService<BondValuation>

    private final BondValuationRepository bondValuationRepository;
    private final FinancialCalculatorService financialCalculatorService;
    private final IamContextFacade iamContextFacade;

    @Autowired
    BondValuationCommandServiceImpl(
            BondValuationRepository bondValuationRepository, // Could not autowire. No beans of 'IamContextFacade' type found.
            FinancialCalculatorService financialCalculatorService,
            IamContextFacade iamContextFacade // Could not autowire. No beans of 'IamContextFacade' type found.
    ) {
        this.bondValuationRepository = bondValuationRepository;
        this.financialCalculatorService = financialCalculatorService;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Optional<Long> handle(CreateBondValuationCommand command) {
        // 1. Validar que el usuario existe usando la ACL
        iamContextFacade.fetchUserById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + command.userId() + " not found"));

        // 2. Crear los Value Objects a partir del comando
        var userId = new UserId(command.userId());
        var parameters = new BondParameters(command);

        // 3. Crear la instancia del Aggregate con su constructor público
        var valuation = new BondValuation(command.valuationName(), userId, parameters);

        // 4. Usar el servicio de cálculo para obtener las métricas y el flujo
        var calculationResult = financialCalculatorService.calculate(parameters);

        // 5. Completar el aggregate con los resultados del cálculo
        iamContextFacade.fetchUserById(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + command.userId() + " not found"));

        valuation.completeValuation(calculationResult.metrics(), calculationResult.cashFlow());

        // 6. Persistir el Aggregate y CAPTURAR el resultado
        BondValuation savedValuation = bondValuationRepository.save(valuation);

        // 7. Devolver el ID del objeto PERSISTIDO, que ya no es nulo.
        // Usamos Optional.ofNullable por seguridad, aunque en este punto no debería ser nulo.
        return Optional.ofNullable(savedValuation.getId());
    }

    @Override
    @Transactional
    public Optional<Long> handle(UpdateBondValuationCommand command) {
        // 1. Cargar el Aggregate existente desde el repositorio.
        var valuation = bondValuationRepository.findById(command.valuationId())
                .orElseThrow(() -> new IllegalArgumentException("Valuation with ID " + command.valuationId() + " not found"));

        // 2. Ejecutar el metodo de actualización en el Aggregate.
        valuation.update(command, financialCalculatorService); // Cannot resolve method 'update' in 'BondValuation'

        // 3. Persistir los cambios.
        bondValuationRepository.save(valuation);

        // 4. Devolver el ID.
        return Optional.of(valuation.getId());
    }

    @Override
    @Transactional
    public void handle(DeleteBondValuationCommand command) {
        if (!bondValuationRepository.existsById(command.valuationId())) {
            throw new IllegalArgumentException("Valuation with ID " + command.valuationId() + " not found");
        }
        bondValuationRepository.deleteById(command.valuationId());
    }
}