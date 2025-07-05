package com.upc.ven_con_plata_backend.estimating.application.internal.commandservices;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import com.upc.ven_con_plata_backend.estimating.domain.services.BonoCommandService;
import com.upc.ven_con_plata_backend.estimating.domain.services.CalculadoraFinancieraDomainService;
import com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories.BonoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BonoCommandServiceImpl implements BonoCommandService {

    private final BonoRepository bonoRepository;
    private final CalculadoraFinancieraDomainService calculadoraService;

    public BonoCommandServiceImpl(BonoRepository bonoRepository, CalculadoraFinancieraDomainService calculadoraService) {
        this.bonoRepository = bonoRepository;
        this.calculadoraService = calculadoraService;
    }
    /*
    @Override
    public Optional<Bono> handle(CreateBonoCommand command) {
        Tasa tasaInteres = new Tasa(command.tasaInteres(), command.periodicidadInteres());
        Tasa cok = new Tasa(command.cok(), command.periodicidadCok());
        PeriodosGracia gracia = new PeriodosGracia(command.periodosGraciaTotal(), command.periodosGraciaParcial());
        CostesInversion costesInversion = new CostesInversion(command.costeFlotacion(), command.costeCavali());
        BeneficioInversion beneficioInversion = new BeneficioInversion(command.primaVencimiento());
        CostesInicialesDeudor costesIniciales = new CostesInicialesDeudor(
                command.costesNotariales(), command.costesRegistrales(), command.costesTasacion(),
                command.comisionEstudio(), command.comisionActivacion()
        );
        GastosPeriodicosDeudor gastosPeriodicosDeudor = new GastosPeriodicosDeudor(
                command.seguroDesgravamen(), command.seguroRiesgo(), command.comisionPeriodica(),
                command.portes(), command.gastosAdministrativos()
        );

        // Crear aggregate
        Bono bono = new Bono(
                command.currency(), command.valorNominal(), command.valorComercial(),
                command.fechaEmision(), command.fechaVencimiento(), command.plazoEnAnios(),
                command.frecuenciaPago(), tasaInteres, cok, gracia, costesInversion,
                beneficioInversion, costesIniciales, gastosPeriodicosDeudor
        );

        // Generar cronogramas
        bono.generarCronogramas();

        // Calcular indicadores
        IndicadoresEmisor indicadoresEmisor = calculadoraService.calcularIndicadoresEmisor(bono);
        IndicadoresInversor indicadoresInversor = calculadoraService.calcularIndicadoresInversor(bono, cok.getValor());

        // Establecer indicadores en los cronogramas
        bono.getCronogramaEmisor().establecerIndicadoresEmisor(indicadoresEmisor);
        bono.getCronogramaInversor().establecerIndicadoresInversor(indicadoresInversor);

        // Guardar
        Bono savedBono = bonoRepository.save(bono);

        return Optional.of(savedBono);
    }

    @Override
    public Optional<Bono> handle(UpdateBonoCommand command) {
        // 1. Obtener el bono que se va a actualizar
        Bono bono = bonoRepository.findById(command.bonoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Bono con ID %d no encontrado", command.bonoId())
                ));

        // 2. Validar que puede ser modificado
        if (!bono.puedeSerModificado()) {
            throw new IllegalStateException(
                    String.format("El bono %d no puede ser modificado en su estado actual: %s", bono.getId(), bono.getEstado().name())
            );
        }

        // 3. Aplicar actualizaciones paso a paso
        boolean requiereRecalculoCompleto = false;

        // Actualizar datos financieros básicos
        if (hayCambiosFinancieros(bono, command)) {
            bono.actualizarDatosFinancieros(
                    command.valorNominal(),
                    command.valorComercial(),
                    command.fechaVencimiento()
            );
            requiereRecalculoCompleto = true;
        }
        // Actualizar tasas
        if (hayCambiosTasas(bono, command)) {
            bono.actualizarTasas(
                    command.tasaInteres(),
                    command.periodicidadCok(),
                    command.cok(),
                    command.periodicidadCok()
            );
            requiereRecalculoCompleto = true;
        }

        // Actualizar períodos de gracia
        if (hayCambiosGracia(bono, command)) {
            bono.actualizarPeriodosGracia(
                    command.periodosGraciaTotal(),
                    command.periodosGraciaParcial()
            );
            requiereRecalculoCompleto = true;
        }

        // 4. Regenerar cronogramas si es necesario
        if (requiereRecalculoCompleto) {
            // Recalcular indicadores
            var indicadoresEmisor = calculadoraService.calcularIndicadoresEmisor(bono);
            var indicadoresInversor = calculadoraService.calcularIndicadoresInversor(bono, bono.getCok().getValor());

            // Establecer indicadores actualizados
            bono.getCronogramaEmisor().establecerIndicadoresEmisor(indicadoresEmisor);
            bono.getCronogramaInversor().establecerIndicadoresInversor(indicadoresInversor);
        }

        // 5. Persistir cambios
        Bono bonoActualizado = bonoRepository.save(bono);

        // 6. Retornar el bono actualizado
        return Optional.of(bonoActualizado);
    }

    // Métodos de validación de cambios
    private boolean hayCambiosFinancieros(Bono bono, UpdateBonoCommand command) {
        return !bono.getValorNominal().equals(command.valorNominal()) ||
                !bono.getValorComercial().equals(command.valorComercial()) ||
                !bono.getFechaVencimiento().equals(command.fechaVencimiento());
    }

    private boolean hayCambiosTasas(Bono bono, UpdateBonoCommand command) {
        return !bono.getTasaInteres().getValor().equals(command.tasaInteres()) ||
                !bono.getCok().getValor().equals(command.cok());
    }

    private boolean hayCambiosGracia(Bono bono, UpdateBonoCommand command) {
        return bono.getGracia().getTotal() != command.periodosGraciaTotal() ||
                bono.getGracia().getParcial() != command.periodosGraciaParcial();
    }
    */
}
