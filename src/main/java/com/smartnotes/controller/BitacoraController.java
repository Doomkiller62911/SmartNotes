package com.smartnotes.controller;

import com.smartnotes.repository.CategoriaRepository;
import com.smartnotes.service.BitacoraService;
import com.smartnotes.service.UsuarioService;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BitacoraController {

    private final BitacoraService bitacoraService;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public BitacoraController(BitacoraService bitacoraService,
            CategoriaRepository categoriaRepository,
            UsuarioService usuarioService) {
        this.bitacoraService = bitacoraService;
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/bitacora")
    public String listado(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer id,
            Principal principal,
            Model model) {

        var notas = bitacoraService.buscar(nombre, idCategoria, fecha);
        model.addAttribute("notas", notas);

        var seleccionada = bitacoraService.buscarPorId(id);
        model.addAttribute("seleccionada", seleccionada.orElse(null));

        var categorias = categoriaRepository.findByActivoTrue();
        model.addAttribute("categorias", categorias);

        var usuario = usuarioService.getUsuarioPorUsername(principal.getName());
        model.addAttribute("usuario", usuario.orElse(null));

        model.addAttribute("nombre", nombre);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("fecha", fecha);

        return "bitacora";
    }
}