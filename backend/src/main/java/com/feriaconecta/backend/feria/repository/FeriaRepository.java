package com.feriaconecta.backend.feria.repository;

import com.feriaconecta.backend.feria.model.Feria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FeriaRepository extends JpaRepository<Feria, Integer> {

    /**
     * Obtiene las ferias de un organizador,
     * ordenadas por fecha de forma descendente.
     *
     * @param organizadorId identificador del organizador
     * @return ferias pertenecientes al organizador
     */
    List<Feria> findByOrganizador_IdOrderByFechaDesc(Integer organizadorId);

    /**
     * Comprueba si el organizador tiene una feria
     * registrada con el mismo nombre y fecha.
     *
     * @param organizadorId identificador del organizador
     * @param nombre        nombre de la feria
     * @param fecha         fecha de realización
     * @return true si existe una feria duplicada
     */
    boolean existsByOrganizador_IdAndNombreAndFecha(
            Integer organizadorId,
            String nombre,
            LocalDate fecha
    );

    /**
     * Comprueba si existe otra feria con el mismo nombre
     * y fecha, excluyendo la feria que se está editando.
     *
     * @param organizadorId identificador del organizador
     * @param nombre        nombre de la feria
     * @param fecha         fecha de realización
     * @param feriaId       identificador de la feria actual
     * @return true si existe otra feria con esos datos
     */
    boolean existsByOrganizador_IdAndNombreAndFechaAndIdNot(
            Integer organizadorId,
            String nombre,
            LocalDate fecha,
            Integer feriaId
    );
}
