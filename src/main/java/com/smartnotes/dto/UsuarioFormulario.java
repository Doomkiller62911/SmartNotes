package com.smartnotes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFormulario {
    
    private Integer idUsuario;
    
    @NotBlank
    @Size(max = 30)
    private String username;

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    private String apellidoPaterno;

    @Size(max = 50)
    private String apellidoMaterno;

    @Size(max = 60)
    private String puesto;

    @NotBlank
    @Email
    @Size(max = 100)
    private String correo;
    
    @Size(max = 25)
    private String password;

    @NotNull
    private Integer idRol;

    private boolean activo = true;
}
