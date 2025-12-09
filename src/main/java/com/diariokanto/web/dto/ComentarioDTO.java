package com.diariokanto.web.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ComentarioDTO {
    private Long id;
    private String texto;
    private String autorNombre;
    private LocalDateTime fecha;
}