package com.smartnotes.repository;

import com.smartnotes.domain.Actividad;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    
    @Query("SELECT a FROM Actividad a "
            + "WHERE (:idUsuario IS NULL OR a.usuario.idUsuario = :idUsuario) "
            + "AND (:desde IS NULL OR a.fechaHora >= :desde) "
            + "AND (:hasta IS NULL OR a.fechaHora < :hasta) "
            + "ORDER BY a.fechaHora DESC")
    List<Actividad> consultaActividad(
            @Param("idUsuario") Integer idUsuario,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
    
}
