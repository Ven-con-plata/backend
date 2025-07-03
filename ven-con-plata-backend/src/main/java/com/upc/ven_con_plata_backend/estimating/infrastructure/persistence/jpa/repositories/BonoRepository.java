package com.upc.ven_con_plata_backend.estimating.infrastructure.persistence.jpa.repositories;

import com.upc.ven_con_plata_backend.estimating.domain.model.aggregates.Bono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BonoRepository extends JpaRepository<Bono, Long> {

    Bono save(Bono bono);

    Optional<Bono> findById(Long id);

    List<Bono> findAll();

    void delete(Bono bono);
    
    boolean existsById(Long id);

    List<Bono> findByFechaVencimiento(LocalDate fecha);

    List<Bono> findByFechaVencimientoBetween(LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT b FROM Bono b WHERE b.moneda = :moneda")
    List<Bono> findByMoneda(@Param("moneda") String moneda);

    @Query("SELECT b FROM Bono b LEFT JOIN FETCH b.cronogramas WHERE b.id = :id")
    Bono findByIdWithCronogramas(@Param("id") Long id);
}
