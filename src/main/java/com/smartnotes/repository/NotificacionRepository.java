package com.smartnotes.repository;

import com.smartnotes.domain.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    
    List<Notificacion> findByUsuarioIdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);

    long countByUsuarioIdUsuarioAndLeidaFalse(Integer idUsuario);
    
}
