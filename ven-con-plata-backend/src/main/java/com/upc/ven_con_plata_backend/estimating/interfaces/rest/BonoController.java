package com.upc.ven_con_plata_backend.estimating.interfaces.rest;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.CreateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.commands.UpdateBonoCommand;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetAllBonosQuery;
import com.upc.ven_con_plata_backend.estimating.domain.model.queries.GetBonoByIdQuery;
import com.upc.ven_con_plata_backend.estimating.domain.services.BonoCommandService;
import com.upc.ven_con_plata_backend.estimating.domain.services.BonoQueryService;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.BonoResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.CreateBonoResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.resources.UpdateBonoResource;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform.BonoResourceFromEntityAssembler;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform.CreateBonoCommandFromResourceAssembler;
import com.upc.ven_con_plata_backend.estimating.interfaces.rest.transform.UpdateBonoCommandFromResourceAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/bonos", produces = MediaType.APPLICATION_JSON_VALUE)
public class BonoController {

    private final BonoCommandService bonoCommandService;
    private final BonoQueryService bonoQueryService;

    public BonoController(BonoCommandService bonoCommandService, BonoQueryService bonoQueryService) {
        this.bonoCommandService = bonoCommandService;
        this.bonoQueryService = bonoQueryService;
    }

    @PostMapping
    public ResponseEntity<BonoResource> createBono(@RequestBody CreateBonoResource resource) {
        var createBonoCommand = CreateBonoCommandFromResourceAssembler.toCommandFromResource(resource);
        var bonoOptional = bonoCommandService.handle(createBonoCommand);
        if(bonoOptional.isEmpty()) return ResponseEntity.badRequest().build();
        var bono = bonoOptional.get();
        var bonoResource = BonoResourceFromEntityAssembler.toResourceFromEntity(bono);
        return new ResponseEntity<>(bonoResource, HttpStatus.CREATED);
    }
    /*
    @PutMapping("/{bonoId}")
    public ResponseEntity<BonoResource> updateBono(@PathVariable Long bonoId, @RequestBody UpdateBonoResource resource) {
        var updateBonoCommand = UpdateBonoCommandFromResourceAssembler.toCommandFromResource(bonoId, resource);
        var bonoOptional = bonoCommandService.handle(updateBonoCommand);
        if(bonoOptional.isEmpty()) return ResponseEntity.badRequest().build();
        var bono = bonoOptional.get();
        var bonoResource = BonoResourceFromEntityAssembler.toResourceFromEntity(bono);
        return ResponseEntity.ok(bonoResource);
    }

    @GetMapping
    public ResponseEntity<List<BonoResource>> getAllBonos() {
        List<Bono> bonos = bonoQueryService.handle(new GetAllBonosQuery());
        List<BonoResource> bonoResources = bonos.stream().map(BonoResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(bonoResources);
    }

    @GetMapping("/{bonoId}")
    public ResponseEntity<BonoResource> getBonoById(@PathVariable Long bonoId) {
        var query = new GetBonoByIdQuery(bonoId);
        var bonoOptional = bonoQueryService.handle(query);
        if(bonoOptional.isEmpty()) return ResponseEntity.badRequest().build();
        var bono = bonoOptional.get();
        var bonoResource = BonoResourceFromEntityAssembler.toResourceFromEntity(bono);
        return ResponseEntity.ok(bonoResource);
    }*/
}
