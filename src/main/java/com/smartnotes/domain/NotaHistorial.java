package com.smartnotes.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

// Cada fila es un cambio de estado de una nota (HU 20: trazabilidad)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "nota_historial")
public class NotaHistorial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @ManyToOne
    @JoinColumn(name = "id_nota", nullable = false)
    private NotaCorreccion nota;

    // Es null cuando la nota se crea por primera vez
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 20)
    private EstadoNota estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoNota estadoNuevo;

    // Usuario que hizo el cambio
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // La fecha la genera la base de datos (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(length = 500)
    private String detalle;
}
