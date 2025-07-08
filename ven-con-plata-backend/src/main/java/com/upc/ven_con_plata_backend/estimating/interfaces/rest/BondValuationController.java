package com.upc.ven_con_plata_backend.estimating.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetAllValuationsByUserIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetValuationByIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.services.commandservices.BondValuationCommandService;
import com.upc.ven_con_plata_backend.estimating.domain.services.queryservices.BondValuationQueryService;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CreateValuationResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.ValuationResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform.ValuationCommandFromResourceAssembler;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform.ValuationResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/valuations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bond Valuations", description = "Endpoints for creating and managing bond valuations")
@RequiredArgsConstructor
public class BondValuationController {

    private final BondValuationCommandService commandService;
    private final BondValuationQueryService queryService;
    private final ValuationCommandFromResourceAssembler assemblerResource;
    private final ValuationResourceFromEntityAssembler assemblerDto;

    @PostMapping
    public ResponseEntity<ValuationResource> createValuation(@Valid @RequestBody CreateValuationResource resource) {

        // 1. Convertir Resource a Command
        var command = assemblerResource.toCommandFromResource(resource);

        // 2. Enviar el comando al servicio de aplicación
        var valuationId = commandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error while creating valuation"));

        // 3. Si se crea correctamente, obtener los datos para la respuesta
        var valuationDto = queryService.handle(new GetValuationByIdQuery(valuationId))
                .orElseThrow(() -> new RuntimeException("Could not retrieve created valuation"));

        // 4. Convertir el DTO de aplicación al Resource de API

        var valuationResource = assemblerDto.toResourceFromDto(valuationDto);

        // 5. Devolver 201 Created con el objeto creado
        return new ResponseEntity<>(valuationResource, HttpStatus.CREATED);
    }

    @GetMapping("/{valuationId}")
    public ResponseEntity<ValuationResource> getValuationById(@PathVariable Long valuationId) {
        var valuationDto = queryService.handle(new GetValuationByIdQuery(valuationId))
                .orElseThrow(() -> new RuntimeException("Valuation not found")); // Idealmente, una excepción personalizada

        var resource = assemblerDto.toResourceFromDto(valuationDto);
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ValuationResource>> getValuationsByUserId(@PathVariable Long userId) {
        var dtoList = queryService.handle(new GetAllValuationsByUserIdQuery(userId));

        var resourceList = dtoList.stream()
                .map(assemblerDto::toResourceFromDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resourceList);
    }
}