package com.smartnotes.service;

import com.smartnotes.domain.Rol;
import com.smartnotes.domain.Usuario;
import com.smartnotes.dto.UsuarioFormulario;
import com.smartnotes.repository.RolRepository;
import com.smartnotes.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActividadService actividadService;

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Usuario>getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }
    
    @Transactional(readOnly = true)
    public UsuarioFormulario obtenerFormulario(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        
       Usuario usuario = usuarioOpt.get();

        UsuarioFormulario formulario = new UsuarioFormulario();
        formulario.setIdUsuario(usuario.getIdUsuario());
        formulario.setUsername(usuario.getUsername());
        formulario.setNombre(usuario.getNombre());
        formulario.setApellidoPaterno(usuario.getApellidoPaterno());
        formulario.setApellidoMaterno(usuario.getApellidoMaterno());
        formulario.setPuesto(usuario.getPuesto());
        formulario.setCorreo(usuario.getCorreo());
        formulario.setIdRol(usuario.getRol().getIdRol());
        formulario.setActivo(usuario.isActivo());
        return formulario;
        
    }
    
        @Transactional
        public void guardar(UsuarioFormulario formulario) {
        Optional<Usuario> usuarioRepetido
                = usuarioRepository.findByUsername(formulario.getUsername());
        if (usuarioRepetido.isPresent()
                && !usuarioRepetido.get().getIdUsuario().equals(formulario.getIdUsuario())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        Optional<Usuario> correoRepetido
                = usuarioRepository.findByCorreo(formulario.getCorreo());
        if (correoRepetido.isPresent()
                && !correoRepetido.get().getIdUsuario().equals(formulario.getIdUsuario())) {
            throw new IllegalArgumentException("El correo ya existe.");
        }

        Rol rol = rolRepository.findById(formulario.getIdRol()).orElse(null);
        if (rol == null) {
            throw new IllegalArgumentException("El perfil no existe.");
        }

        Usuario usuario = new Usuario();
        if (formulario.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(formulario.getIdUsuario()).orElse(null);
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no existe.");
            }
        }
        
        boolean hayClave = formulario.getPassword() != null
                && !formulario.getPassword().isBlank();
        if (usuario.getIdUsuario() == null && !hayClave) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (hayClave) {
            usuario.setPassword(passwordEncoder.encode(formulario.getPassword()));
        }

        usuario.setUsername(formulario.getUsername());
        usuario.setNombre(formulario.getNombre());
        usuario.setApellidoPaterno(formulario.getApellidoPaterno());
        usuario.setApellidoMaterno(formulario.getApellidoMaterno());
        usuario.setPuesto(formulario.getPuesto());
        usuario.setCorreo(formulario.getCorreo());
        usuario.setRol(rol);
        usuario.setActivo(formulario.isActivo());
        
                String accion = "EDITAR";
        if (usuario.getIdUsuario() == null) {
            accion = "CREAR";
        }

        usuarioRepository.save(usuario);
        actividadService.registrarActividad(accion, "Usuario",
                "Usuario: " + usuario.getUsername());
}
        
        @Transactional
    public void cambiarEstado(Integer idUsuario, boolean activo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);

        String accion = "DESACTIVAR";
        if (activo) {
            accion = "ACTIVAR";
        }
        actividadService.registrarActividad(accion, "Usuario",
                "Usuario: " + usuario.getUsername());
    }
}  

