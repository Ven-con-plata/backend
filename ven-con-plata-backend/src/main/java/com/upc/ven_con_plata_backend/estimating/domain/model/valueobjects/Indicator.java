package com.upc.ven_con_plata_backend.estimating.domain.model.valueobjects;

import com.upc.ven_con_plata_backend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
public class Indicator extends AuditableModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=50)
    private IndicatorName nombre;

    @Column(nullable=false, precision=18, scale=6)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=12)
    private Unidad unidad;

    protected Indicator(){}

    public Indicator(String nombre, BigDecimal valor, String unidad) {
        this.nombre = IndicatorName.valueOf(nombre);
        this.valor = valor;
        this.unidad = Unidad.valueOf(unidad);
    }
}
