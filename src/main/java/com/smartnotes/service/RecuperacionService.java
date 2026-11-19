package com.smartnotes.service;

import com.smartnotes.domain.TokenRecuperacion;
import com.smartnotes.domain.Usuario;
import com.smartnotes.repository.TokenRecuperacionRepository;
import com.smartnotes.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Recuperación de contraseña
@Service
@RequiredArgsConstructor
public class RecuperacionService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    // Genera un enlace temporal 
    @Transactional
    public void solicitar(String usuarioOCorreo, String urlBase) throws MessagingException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(usuarioOCorreo);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByCorreo(usuarioOCorreo);
        }

        // Si no existe o está inactivo no se muestra
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().isActivo()) {
            return;
        }
        Usuario usuario = usuarioOpt.get();

        
        TokenRecuperacion token = new TokenRecuperacion();
        token.setUsuario(usuario);
        token.setToken(UUID.randomUUID().toString());
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(token);

        String enlace = urlBase + "/restablecer/" + token.getToken();
        String contenido = "<p>Hola " + usuario.getNombre() + ",</p>"
                + "<p>Recibimos una solicitud para restablecer tu contraseña de SmartNotes.</p>"
                + "<p><a href=\"" + enlace + "\">Restablecer mi contraseña</a></p>"
                + "<p>El enlace es válido por 30 minutos. Si no fuiste tú, ignora este correo.</p>";

        correoService.enviarCorreoHtml(usuario.getCorreo(),
                "Recuperación de contraseña - SmartNotes", contenido);
    }
    
    
    // Revisa que el enlace exista, no se haya usado y no haya vencido
    @Transactional(readOnly = true)
    public boolean tokenValido(String codigo) {
        Optional<TokenRecuperacion> tokenOpt = tokenRepository.findByToken(codigo);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        TokenRecuperacion token = tokenOpt.get();
        return !token.isUsado()
                && token.getFechaExpiracion().isAfter(LocalDateTime.now());
    }

    // Se cambia la contraseña del usuario con el enlace recibido
    @Transactional
    public void restablecer(String codigo, String password, String confirmacion) {
        if (!tokenValido(codigo)) {
            throw new IllegalArgumentException("El enlace no es válido o ya venció.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (!password.equals(confirmacion)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        TokenRecuperacion token = tokenRepository.findByToken(codigo).get();
        Usuario usuario = token.getUsuario();
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario);

        // El enlace queda usado y no se puede usar de nuevo
        token.setUsado(true);
        tokenRepository.save(token);
    }
}