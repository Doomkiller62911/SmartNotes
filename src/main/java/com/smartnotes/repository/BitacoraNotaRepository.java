
package com.smartnotes.repository;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;      
import org.springframework.stereotype.Repository;


@Repository
public interface BitacoraNotaRepository extends JpaRepository<NotaCorreccion, Integer> {
    @Query("SELECT n FROM NotaCorreccion n "
            + "JOIN n.teamMember t "
            + "LEFT JOIN n.categoria c "
            + "WHERE n.estado = :estado "
            + "AND LOWER(t.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) "
            + "AND (:idCategoria IS NULL OR c.idCategoria = :idCategoria) "
            + "AND (:fecha IS NULL OR n.fechaError = :fecha) "
            + "ORDER BY n.idNota DESC")
    List<NotaCorreccion> consultaBitacora(
            @Param("estado") EstadoNota estado,
            @Param("nombre") String nombre,
            @Param("idCategoria") Integer idCategoria,
            @Param("fecha") LocalDate fecha);
    
}
