package com.smartnotes.controller;

import com.smartnotes.domain.Usuario;
import com.smartnotes.dto.UsuarioFormulario;
import com.smartnotes.repository.RolRepository;
import com.smartnotes.service.UsuarioService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final MessageSource messageSource;
    
    public UsuarioController(
    UsuarioService usuarioService,
    RolRepository rolRepository,
    MessageSource messageSource) {
        
    this.usuarioService = usuarioService;
    this.rolRepository = rolRepository;
    this.messageSource = messageSource;
    
    }

    @ModelAttribute
    public void prepararUsuarioCabecera(Principal principal, Model model) {
        if (principal == null) {
            return;
        }
        Optional<Usuario> usuarioOpt
                = usuarioService.getUsuarioPorUsername(principal.getName());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            model.addAttribute("nombreUsuarioSesion",
                    usuario.getNombre() + " " + usuario.getApellidoPaterno());
            model.addAttribute("puestoUsuarioSesion", usuario.getPuesto());
            model.addAttribute("rolUsuarioSesion", usuario.getRol().getNombre());
        }
    }

    @GetMapping({"", "/listado"})
    public String listado(Model model) {
        var usuarios = usuarioService.getUsuarios();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        return "usuarios/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new UsuarioFormulario());
        cargarRoles(model);
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("usuario", usuarioService.obtenerFormulario(id));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("usuario.error.noEncontrado"));
            return "redirect:/usuarios/listado";
        }
        cargarRoles(model);
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid @ModelAttribute("usuario") UsuarioFormulario formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        cargarRoles(model);

        if (bindingResult.hasErrors()) {
            return "usuarios/formulario";
        }

        try {
            usuarioService.guardar(formulario);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "usuarios/formulario";
        }

        redirectAttributes.addFlashAttribute("todoOk", mensaje("usuario.guardado"));
        return "redirect:/usuarios/listado";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
            @RequestParam boolean activa,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Usuario> usuarioOpt = usuarioService.getUsuario(id);
        if (usuarioOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("usuario.error.noEncontrado"));
            return "redirect:/usuarios/listado";
        }

        if (!activa && usuarioOpt.get().getUsername().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("usuario.error.propio"));
            return "redirect:/usuarios/listado";
        }

        usuarioService.cambiarEstado(id, activa);
        redirectAttributes.addFlashAttribute("todoOk",
                mensaje(activa ? "usuario.activado" : "usuario.desactivado"));
        return "redirect:/usuarios/listado";
    }
    
    private void cargarRoles(Model model) {
        model.addAttribute("roles", rolRepository.findAll());
    }

    private String mensaje(String clave) {
        return messageSource.getMessage(clave, null, LocaleContextHolder.getLocale());
    }

}


