package com.upc.ven_con_plata_backend.estimating.domain.model.entities;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.CashFlowEntry;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.IndicadoresEmisor;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.IndicadoresInversor;
import com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects.RolSchedule;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bono_id", nullable = false)
    private Bono bono;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private RolSchedule rol;

    @ElementCollection
    @CollectionTable(
            name = "cf_entries",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    private List<CashFlowEntry> entries = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "van", column = @Column(name = "emisor_van")),
            @AttributeOverride(name = "tir", column = @Column(name = "emisor_tir")),
            @AttributeOverride(name = "tcea", column = @Column(name = "emisor_tcea"))
    })
    private IndicadoresEmisor indicadoresEmisor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "van", column = @Column(name = "invesor_van")),
            @AttributeOverride(name = "tir", column = @Column(name = "invesor_tir")),
            @AttributeOverride(name = "precioBono", column = @Column(name = "invesor_precio")),
            @AttributeOverride(name = "trea", column = @Column(name = "invesor_trea")),
            @AttributeOverride(name = "duracion", column = @Column(name = "invesor_duracion")),
            @AttributeOverride(name = "duracionModificada", column = @Column(name = "invesor_duracion_mod")),
            @AttributeOverride(name = "convexidad", column = @Column(name = "invesor_convexidad"))
    })
    private IndicadoresInversor indicadoresInversor;

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
}
