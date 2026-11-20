package com.smartnotes.domain;

import com.smartnotes.service.NotificacionService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notificaciones")
public class NotificacionController {
    
    private final NotificacionService notificacionService;
    
    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping({"", "/listado"})
    public String listado(Principal principal, Model model) {
        model.addAttribute("notificaciones",
                notificacionService.getNotificaciones(principal.getName()));
        model.addAttribute("noLeidas",
                notificacionService.contarNoLeidas(principal.getName()));
        return "notificaciones/listado";
    }

    @PostMapping("/{id}/leida")
    public String marcarLeida(@PathVariable Integer id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            notificacionService.marcarLeida(id, principal.getName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/notificaciones";
    }
}
