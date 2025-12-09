package com.diariokanto.web.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticiaDTO {
    private Long id;
    private String titulo;
    private String resumen;
    private String imagenUrl;
    private String tagText;
    private String tagClass;
    private LocalDateTime fechaPublicacion;
    private String contenidoHtml;
    private String nombreCategoria;
}