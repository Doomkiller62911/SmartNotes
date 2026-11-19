package com.smartnotes.controller;

import com.smartnotes.service.RecuperacionService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// Recuperación de contraseña por correo
@Controller
public class RecuperacionController {
    
    private final RecuperacionService recuperacionService;
    private final MessageSource messageSource;
    
    public RecuperacionController(
            RecuperacionService recuperacionService,
            MessageSource messageSource) {
        
        this.recuperacionService = recuperacionService;
        this.messageSource = messageSource;
    }
    
    //Se pide la recuperación.
     @GetMapping("/recuperar")
    public String formulario() {
        return "recuperacion/solicitar";
    }
    
    // Enlace de recuperación al correo registrado.
    @PostMapping("/recuperar")
    public String solicitar(@RequestParam String usuarioOCorreo,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String urlBase = ServletUriComponentsBuilder.fromContextPath(request)
                .build().toUriString();

        try {
            recuperacionService.solicitar(usuarioOCorreo.trim(), urlBase);
            redirectAttributes.addFlashAttribute("todoOk",
                    mensaje("recuperacion.enviado"));
        } catch (MessagingException | MailException e) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("recuperacion.error.envio"));
        }
        return "redirect:/recuperar";
    }

    
    @GetMapping("/restablecer/{token}")
    public String restablecer(@PathVariable String token, Model model,
            RedirectAttributes redirectAttributes) {

        if (!recuperacionService.tokenValido(token)) {
            redirectAttributes.addFlashAttribute("error",
                    mensaje("recuperacion.error.enlace"));
            return "redirect:/recuperar";
        }
        model.addAttribute("token", token);
        return "recuperacion/restablecer";
    }

    // Guarda la nueva contraseña.
    @PostMapping("/restablecer")
    public String guardar(@RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmacion,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            recuperacionService.restablecer(token, password, confirmacion);
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "recuperacion/restablecer";
        }

        return "redirect:/login?actualizada";
    }

    private String mensaje(String clave) {
        return messageSource.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
    
}
