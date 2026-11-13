package com.smartnotes.repository;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaCorreccionRepository extends JpaRepository<NotaCorreccion, Integer> {

    List<NotaCorreccion> findAllByOrderByFechaCreacionDesc();

    // Notas por estado, de la mas antigua a la mas reciente (la usa  solo el perfil de Release)
    List<NotaCorreccion> findByEstadoOrderByFechaCreacionAsc(EstadoNota estado);
}