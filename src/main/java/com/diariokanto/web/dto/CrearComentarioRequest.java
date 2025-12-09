package com.diariokanto.web.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor // Genera constructor con todos los argumentos automáticamente
public class CrearComentarioRequest {
    private Long noticiaId;
    private String emailUsuario;
    private String texto;
}