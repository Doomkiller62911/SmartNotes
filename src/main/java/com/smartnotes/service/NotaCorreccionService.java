package com.smartnotes.service;

import com.smartnotes.domain.EstadoNota;
import com.smartnotes.domain.NotaCorreccion;
import com.smartnotes.domain.Plantilla;
import com.smartnotes.domain.PlantillaVariable;
import com.smartnotes.domain.TeamMember;
import com.smartnotes.domain.Variable;
import com.smartnotes.dto.NotaGeneradorForm;
import com.smartnotes.repository.NotaCorreccionRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartnotes.domain.Usuario;


@Service
public class NotaCorreccionService {

    private final NotaCorreccionRepository notaCorreccionRepository;
    private final PlantillaService plantillaService;
    private final TeamMemberService teamMemberService;
    private final NotaHistorialService notaHistorialService;

    public NotaCorreccionService(
            NotaCorreccionRepository notaCorreccionRepository,
            PlantillaService plantillaService,
            TeamMemberService teamMemberService,
            NotaHistorialService notaHistorialService) {
        this.notaCorreccionRepository = notaCorreccionRepository;
        this.plantillaService = plantillaService;
        this.teamMemberService = teamMemberService;
        this.notaHistorialService = notaHistorialService;
    }

    public List<NotaCorreccion> findAll() {
        return notaCorreccionRepository.findAllByOrderByFechaCreacionDesc();
    }

    public NotaCorreccion findById(Integer id) {
        return notaCorreccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nota no encontrada: " + id));
    }

    public String generarVistaPrevia(NotaGeneradorForm form) {
        Plantilla plantilla = plantillaService.findById(form.getPlantillaId());
        TeamMember teamMember = teamMemberService.findById(form.getTeamMemberId());
        return construirContenido(plantilla, teamMember, form);
    }

    public List<String> validarNota(NotaGeneradorForm form) {
        List<String> errores = new ArrayList<>();

        if (form.getFechaInicio() == null) {
            errores.add("La fecha de inicio es obligatoria.");
        }
        if (form.getFechaFin() == null) {
            errores.add("La fecha final es obligatoria.");
        }
        if (form.getFechaInicio() != null
                && form.getFechaFin() != null
                && form.getFechaFin().isBefore(form.getFechaInicio())) {
            errores.add("La fecha final no puede ser anterior a la fecha de inicio.");
        }
        if (form.getDescripcion() == null || form.getDescripcion().isBlank()) {
            errores.add("La descripción no puede estar vacía.");
        }
        if (form.getCorreccion() == null || form.getCorreccion().isBlank()) {
            errores.add("La corrección no puede estar vacía.");
        }
        if (form.getEvidencia() != null && form.getEvidencia().isBlank()) {
            errores.add("La evidencia no puede quedar en blanco.");
        }

        return errores;
    }

    @Transactional
    public NotaCorreccion guardar(
            NotaGeneradorForm form,
            Usuario usuario) {

        Plantilla plantilla = plantillaService.findById(form.getPlantillaId());
        TeamMember teamMember = teamMemberService.findById(form.getTeamMemberId());
        NotaCorreccion nota = new NotaCorreccion();
        nota.setUsuario(usuario);
        nota.setCategoria(plantilla.getCategoria());

        nota.setPlantilla(plantilla);
        nota.setTeamMember(teamMember);
        aplicarFormulario(nota, form);
        nota.setContenidoGenerado(construirContenido(plantilla, teamMember, form));

        return notaCorreccionRepository.save(nota);
    }

    @Transactional
    public NotaCorreccion actualizar(Integer id, NotaGeneradorForm form) {
        validarFormulario(form);

        NotaCorreccion nota = findById(id);
        Plantilla plantilla = plantillaService.findById(form.getPlantillaId());
        TeamMember teamMember = teamMemberService.findById(form.getTeamMemberId());

        nota.setPlantilla(plantilla);
        nota.setTeamMember(teamMember);
        aplicarFormulario(nota, form);
        nota.setContenidoGenerado(construirContenido(plantilla, teamMember, form));

        return notaCorreccionRepository.save(nota);
    }

    @Transactional
    public void eliminar(Integer id) {
        notaCorreccionRepository.deleteById(id);
    }

    private void validarFormulario(NotaGeneradorForm form) {
        List<String> errores = validarNota(form);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errores));
        }
    }

    private void aplicarFormulario(NotaCorreccion nota, NotaGeneradorForm form) {
        nota.setFechaError(form.getFechaInicio());
        nota.setFechaEmision(form.getFechaFin());
        nota.setDescripcionError(form.getDescripcion());
        nota.setCorreccionRealizada(form.getCorreccion());
        nota.setEvidenciaObjetiva(normalizarEvidencias(form.getEvidencia()));
        nota.setEstado(form.isGuardarComoBorrador()
                ? EstadoNota.BORRADOR
                : EstadoNota.PENDIENTE_REVISION); //Se cambia de acuerdo con los cambios realizados en los estados.
    }

    private String construirContenido(
            Plantilla plantilla,
            TeamMember teamMember,
            NotaGeneradorForm form) {

        Map<String, String> valores = new HashMap<>();
        valores.put("plantilla", plantilla.getNombre());
        valores.put("teamMember", teamMember.getNombre());
        valores.put("member", teamMember.getNombre());
        valores.put("cargo", teamMember.getPuesto());
        valores.put("fechaInicio", formatDate(form.getFechaInicio()));
        valores.put("fechaFin", formatDate(form.getFechaFin()));
        valores.put("descripcion", nullToEmpty(form.getDescripcion()));
        valores.put("correccion", nullToEmpty(form.getCorreccion()));
        valores.put("evidencias", normalizarEvidencias(form.getEvidencia()));
        // Nombres utilizados por las plantillas del catálogo.
        valores.put("TM", nullToEmpty(teamMember.getNombre()));
        valores.put("FECHA ERROR", formatDate(form.getFechaInicio()));
        valores.put("FECHA DE INGRESO", formatDate(form.getFechaFin()));
        valores.put("EQUIPO", nullToEmpty(form.getEquipo()));

        for (PlantillaVariable relacion : plantilla.getVariables()) {
            Variable variable = relacion.getVariable();
            if (variable != null && variable.getNombre() != null) {
                valores.putIfAbsent(variable.getNombre(),
                        valorVariableGenerado(variable, valores));
            }
        }

        String contenido = plantilla.getContenido();
        for (Map.Entry<String, String> entrada : valores.entrySet()) {
            String valor = entrada.getValue() == null ? "" : entrada.getValue();
            contenido = contenido.replace("{{" + entrada.getKey() + "}}", valor);
            contenido = contenido.replace("[" + entrada.getKey() + "]", valor);
        }
        return contenido;
    }

    private String valorVariableGenerado(
            Variable variable,
            Map<String, String> valores) {
        return valores.getOrDefault(variable.getNombre(), "");
    }

    private String normalizarEvidencias(String evidencia) {
        if (evidencia == null || evidencia.isBlank()) {
            return "Sin evidencias adjuntas";
        }

        String[] items = evidencia.split("[\\r\\n,;]+");
        List<String> evidencias = new ArrayList<>();
        for (String item : items) {
            String valor = item.trim();
            if (!valor.isEmpty()) {
                evidencias.add(valor);
            }
        }
        return evidencias.isEmpty()
                ? "Sin evidencias adjuntas"
                : String.join("; ", evidencias);
    }

    private String formatDate(LocalDate date) {
        return date == null
                ? ""
                : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
