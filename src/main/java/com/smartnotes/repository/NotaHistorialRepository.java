package com.smartnotes.repository;

import com.smartnotes.domain.NotaHistorial;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotaHistorialRepository extends JpaRepository<NotaHistorial, Integer> {

    // Historial de una nota, del más antiguo al más reciente
    List<NotaHistorial> findByNota_IdNotaOrderByFechaHoraAscIdHistorialAsc(Integer idNota);
}
