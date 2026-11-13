package com.smartnotes.service;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import com.smartnotes.domain.NotaHistorial;
import com.smartnotes.domain.Usuario;
import com.smartnotes.repository.NotaHistorialRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotaHistorialService {

    private static final int MAX_DETALLE = 500;

    private final NotaHistorialRepository notaHistorialRepository;

    // Guarda un cambio de estado. estadoAnterior es null si la nota es nueva.
    @Transactional
    public void registrar(NotaCorreccion nota, EstadoNota estadoAnterior,
            EstadoNota estadoNuevo, Usuario usuario, String detalle) {

        NotaHistorial registro = new NotaHistorial();
        registro.setNota(nota);
        registro.setEstadoAnterior(estadoAnterior);
        registro.setEstadoNuevo(estadoNuevo);
        registro.setUsuario(usuario);
        registro.setDetalle(recortar(detalle));
        notaHistorialRepository.save(registro);
    }

    @Transactional(readOnly = true)
    public List<NotaHistorial> listarPorNota(Integer idNota) {
        return notaHistorialRepository
                .findByNota_IdNotaOrderByFechaHoraAscIdHistorialAsc(idNota);
    }

    // La columna detalle admite 500 caracteres
    private String recortar(String texto) {
        if (texto == null || texto.length() <= MAX_DETALLE) {
            return texto;
        }
        return texto.substring(0, MAX_DETALLE - 3) + "...";
    }
}
