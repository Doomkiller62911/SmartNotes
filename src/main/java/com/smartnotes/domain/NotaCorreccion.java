package com.smartnotes.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nota_correccion")
public class NotaCorreccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Integer idNota;

    // Usuario que registra la nota
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // Team Member que cometió el error
    @ManyToOne
    @JoinColumn(name = "id_team_member", nullable = false)
    private TeamMember teamMember;

    @ManyToOne
    @JoinColumn(name = "id_plantilla")
    private Plantilla plantilla;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(name = "fecha_error")
    private LocalDate fechaError;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "descripcion_error", columnDefinition = "TEXT")
    private String descripcionError;

    @Column(name = "correccion_realizada", columnDefinition = "TEXT")
    private String correccionRealizada;

    @Column(name = "evidencia_objetiva", columnDefinition = "TEXT")
    private String evidenciaObjetiva;

    @Column(name = "contenido_generado", columnDefinition = "TEXT")
    private String contenidoGenerado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoNota estado = EstadoNota.BORRADOR;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;
    
     // Datos de la revision de Release (HU 11)
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @ManyToOne
    @JoinColumn(name = "id_revisor")
    private Usuario revisor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;
}
