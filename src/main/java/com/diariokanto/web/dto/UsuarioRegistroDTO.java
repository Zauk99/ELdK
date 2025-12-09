package com.diariokanto.web.dto;

import lombok.Data;

@Data
public class UsuarioRegistroDTO {
    private String nombreCompleto;
    private String email;
    private String username; // <--- EL NUEVO CAMPO
    private String password;
    private String movil;
}