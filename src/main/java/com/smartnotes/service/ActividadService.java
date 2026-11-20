package com.smartnotes.service;

import com.smartnotes.domain.Actividad;
import com.smartnotes.domain.Usuario;
import com.smartnotes.repository.ActividadRepository;
import com.smartnotes.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActividadService {
    
    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;

    public ActividadService(ActividadRepository actividadRepository,
            UsuarioRepository usuarioRepository) {
        this.actividadRepository = actividadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrarActividad(String accion, String entidadAfectada, String detalle) {
        Authentication sesion = SecurityContextHolder.getContext().getAuthentication();
        if (sesion == null) {
            return;
        }

        Optional<Usuario> resultado = usuarioRepository.findByUsername(sesion.getName());
        if (resultado.isEmpty()) {
            return;
        }
        Usuario usuario = resultado.get();

        Actividad actividad = new Actividad();
        actividad.setUsuario(usuario);
        actividad.setAccion(accion);
        actividad.setEntidadAfectada(entidadAfectada);
        actividad.setDetalle(detalle);
        actividadRepository.save(actividad);
    }

    @Transactional(readOnly = true)
    public List<Actividad> getActividades(Integer idUsuario, LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = null;
        LocalDateTime fin = null;

        if (desde != null) {
            inicio = desde.atStartOfDay();
        }
        if (hasta != null) {
            fin = hasta.plusDays(1).atStartOfDay();
        }

        return actividadRepository.consultaActividad(idUsuario, inicio, fin);
    }
    
}
