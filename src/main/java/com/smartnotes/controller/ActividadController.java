package com.smartnotes.controller;

import com.smartnotes.service.ActividadService;
import com.smartnotes.service.UsuarioService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/actividad")
public class ActividadController {
    
    private final ActividadService actividadService;
    private final UsuarioService usuarioService;

    public ActividadController(ActividadService actividadService,
            UsuarioService usuarioService) {
        this.actividadService = actividadService;
        this.usuarioService = usuarioService;
    }

    @GetMapping({"", "/listado"})
    public String listado(
            @RequestParam(required = false) Integer idUsuario,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        model.addAttribute("actividades",
                actividadService.getActividades(idUsuario, desde, hasta));
        model.addAttribute("usuarios", usuarioService.getUsuarios());
        model.addAttribute("idUsuario", idUsuario);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "actividad/listado";
    }
    
}
