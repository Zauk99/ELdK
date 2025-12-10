package com.diariokanto.web.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class EquipoDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<MiembroEquipoDTO> miembros = new ArrayList<>();
}