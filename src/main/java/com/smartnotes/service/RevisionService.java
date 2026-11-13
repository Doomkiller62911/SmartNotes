package com.smartnotes.service;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import com.smartnotes.domain.Usuario;
import com.smartnotes.repository.NotaCorreccionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lógica de la revisión que hace Release (HU 11)
@Service
@RequiredArgsConstructor
public class RevisionService {

    private final NotaCorreccionRepository notaCorreccionRepository;
    private final NotaHistorialService notaHistorialService;

    @Transactional(readOnly = true)
    public List<NotaCorreccion> listarPendientes() {
        return notaCorreccionRepository
                .findByEstadoOrderByFechaCreacionAsc(EstadoNota.PENDIENTE_REVISION);
    }

    // Lanza IllegalArgumentException si la nota no existe
    @Transactional(readOnly = true)
    public NotaCorreccion buscar(Integer idNota) {
        return notaCorreccionRepository.findById(idNota)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nota no encontrada: " + idNota));
    }

    // Lanza IllegalStateException si la nota ya no está pendiente
    @Transactional
    public void aprobar(Integer idNota, Usuario revisor) {
        NotaCorreccion nota = exigirPendiente(idNota);

        nota.setEstado(EstadoNota.APROBADA);
        nota.setRevisor(revisor);
        nota.setFechaRevision(LocalDateTime.now());
        nota.setMotivoRechazo(null);
        notaCorreccionRepository.save(nota);

        notaHistorialService.registrar(nota, EstadoNota.PENDIENTE_REVISION,
                EstadoNota.APROBADA, revisor, "Nota aprobada por Release");
    }

    // El motivo es obligatorio (HU 11). Lanza IllegalStateException si ya no está pendiente
    @Transactional
    public void rechazar(Integer idNota, String motivo, Usuario revisor) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del rechazo es obligatorio.");
        }
        NotaCorreccion nota = exigirPendiente(idNota);
        String motivoLimpio = motivo.trim();

        nota.setEstado(EstadoNota.RECHAZADA);
        nota.setRevisor(revisor);
        nota.setFechaRevision(LocalDateTime.now());
        nota.setMotivoRechazo(motivoLimpio);
        notaCorreccionRepository.save(nota);

        notaHistorialService.registrar(nota, EstadoNota.PENDIENTE_REVISION,
                EstadoNota.RECHAZADA, revisor, "Rechazada. Motivo: " + motivoLimpio);
    }

    private NotaCorreccion exigirPendiente(Integer idNota) {
        NotaCorreccion nota = buscar(idNota);
        if (nota.getEstado() != EstadoNota.PENDIENTE_REVISION) {
            throw new IllegalStateException("La nota ya no está pendiente de revisión.");
        }
        return nota;
    }
}
