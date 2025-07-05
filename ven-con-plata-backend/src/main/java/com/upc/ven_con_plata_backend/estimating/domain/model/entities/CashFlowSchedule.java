package com.upc.ven_con_plata_backend.estimating.domain.model.entities;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashFlowSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolSchedule rol;

    @ElementCollection
    @CollectionTable(
            name = "entries",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    private List<CashFlowEntry> entries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name= "indicadores",
            joinColumns = @JoinColumn(name = "indicator_id")
    )
    private List<Indicator> indicadores = new ArrayList<>();

    /*
    public CashFlowSchedule(Bono bono, RolSchedule rol) {
        this.bono = bono;
        this.rol = rol;
        this.entries = new ArrayList<>();
    }

    // Métodos de negocio
    public void agregarEntry(CashFlowEntry entry) {
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    public void establecerIndicadoresEmisor(IndicadoresEmisor indicadores) {
        this.indicadoresEmisor = indicadores;
    }

    public void establecerIndicadoresInversor(IndicadoresInversor indicadores) {
        this.indicadoresInversor = indicadores;
    }

    public List<CashFlowEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public boolean esEmisor() {
        return rol == RolSchedule.EMISOR;
    }

    public boolean esInversor() {
        return rol == RolSchedule.INVERSOR;
    }
     */
}
