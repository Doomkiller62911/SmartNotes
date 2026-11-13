package com.smartnotes.controller;

import com.smartnotes.domain.Usuario;
import com.smartnotes.service.RevisionService;
import com.smartnotes.service.UsuarioService;
import java.security.Principal;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Pantallas de revisión para el perfil Release (HU 11)
@Controller
@RequestMapping("/revision")
public class RevisionController {

    private final RevisionService revisionService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    public RevisionController(
            RevisionService revisionService,
            UsuarioService usuarioService,
            MessageSource messageSource) {

        this.revisionService = revisionService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    // Datos del usuario que aparecen en la cabecera.
    @ModelAttribute
    public void prepararUsuarioCabecera(Principal principal, Model model) {
        if (principal == null) {
            return;
        }
        usuarioService.getUsuarioPorUsername(principal.getName())
                .ifPresent(usuario -> {
                    model.addAttribute("nombreUsuarioSesion",
                            usuario.getNombre() + " " + usuario.getApellidoPaterno());
                    model.addAttribute("puestoUsuarioSesion", usuario.getPuesto());
                    model.addAttribute("rolUsuarioSesion", usuario.getRol().getNombre());
                });
    }

    // Lista de notas pendientes de revisión.
    @GetMapping({"", "/pendientes"})
    public String pendientes(Authentication autenticacion, Model model) {
        exigirRevisor(autenticacion);

        var notas = revisionService.listarPendientes();
        model.addAttribute("notas", notas);
        model.addAttribute("totalPendientes", notas.size());

        return "revision/listado";
    }

    // Detalle de una nota para aprobarla o rechazarla.
    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Integer id,
            Authentication autenticacion,
            Model model,
            RedirectAttributes redirectAttributes) {

        exigirRevisor(autenticacion);

        try {
            model.addAttribute("nota", revisionService.buscar(id));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.noEncontrada"));
            return "redirect:/revision/pendientes";
        }
        return "revision/detalle";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(
            @PathVariable Integer id,
            Authentication autenticacion,
            RedirectAttributes redirectAttributes) {

        exigirRevisor(autenticacion);

        try {
            revisionService.aprobar(id, obtenerUsuario(autenticacion));
            redirectAttributes.addFlashAttribute("todoOk", mensaje("revision.aprobada"));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.noPendiente"));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.noEncontrada"));
        }
        return "redirect:/revision/pendientes";
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(
            @PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            Authentication autenticacion,
            RedirectAttributes redirectAttributes) {

        exigirRevisor(autenticacion);

        // El motivo es obligatorio: si falta, se vuelve al detalle
        if (motivo == null || motivo.isBlank()) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.motivo"));
            return "redirect:/revision/" + id;
        }

        try {
            revisionService.rechazar(id, motivo, obtenerUsuario(autenticacion));
            redirectAttributes.addFlashAttribute("todoOk", mensaje("revision.rechazada"));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.noPendiente"));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", mensaje("revision.error.noEncontrada"));
        }
        return "redirect:/revision/pendientes";
    }

    private Usuario obtenerUsuario(Authentication autenticacion) {
        return usuarioService.getUsuarioPorUsername(autenticacion.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    // Solo Release y Administrador pueden revisar notas
    private void exigirRevisor(Authentication autenticacion) {
        boolean permitido = autenticacion != null
                && autenticacion.isAuthenticated()
                && autenticacion.getAuthorities().stream()
                        .anyMatch(rol -> "ROLE_RELEASE".equals(rol.getAuthority())
                        || "ROLE_ADMIN".equals(rol.getAuthority()));

        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private String mensaje(String clave) {
        return messageSource.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
}
