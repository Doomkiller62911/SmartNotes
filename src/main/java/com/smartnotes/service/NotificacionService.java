package com.smartnotes.service;

import com.smartnotes.domain.NotaCorreccion;
import com.smartnotes.domain.Notificacion;
import com.smartnotes.domain.Usuario;
import com.smartnotes.repository.NotificacionRepository;
import com.smartnotes.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Notificacion> getNotificaciones(String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return new ArrayList<>();
        }
        return notificacionRepository
                .findByUsuarioIdUsuarioOrderByFechaCreacionDesc(usuarioOpt.get().getIdUsuario());
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String username) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return 0;
        }
        return notificacionRepository
                .countByUsuarioIdUsuarioAndLeidaFalse(usuarioOpt.get().getIdUsuario());
    }

    @Transactional
    public void crear(Usuario usuario, NotaCorreccion nota, String tipo, String motivo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setNota(nota);
        notificacion.setTipo(tipo);
        notificacion.setMotivo(motivo);
        notificacion.setLeida(false);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarLeida(Integer idNotificacion, String username) {
        Optional<Notificacion> notificacionOpt = notificacionRepository.findById(idNotificacion);
        if (notificacionOpt.isEmpty()) {
            throw new IllegalArgumentException("Notificación no encontrada.");
        }

        Notificacion notificacion = notificacionOpt.get();
        if (!notificacion.getUsuario().getUsername().equals(username)) {
            throw new IllegalArgumentException("Notificación no encontrada.");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }
}