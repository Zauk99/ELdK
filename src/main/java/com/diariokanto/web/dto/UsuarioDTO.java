package com.diariokanto.web.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UsuarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fotoPerfilUrl;
    private String nombreCompleto;
    private String username;
    private String pokemonFavorito;
    private String email;
    private String rol;
    private String movil;
    private Boolean superAdmin;
    private boolean twoFactorEnabled;
    // Nota: No incluimos password aquí porque el backend no la devuelve
}