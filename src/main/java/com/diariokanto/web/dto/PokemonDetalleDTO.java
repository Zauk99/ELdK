package com.diariokanto.web.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Importante
import java.util.LinkedHashMap;

@Data
public class PokemonDetalleDTO {
    // ... (campos anteriores: id, nombre, imagenUrl, etc.) ...
    private Long id;
    private String nombre;
    private String imagenUrl;
    private String imagenShinyUrl;
    private Double altura;
    private Double peso;
    private String descripcion;
    private Map<String, String> tipos = new LinkedHashMap<>();
    private List<String> habilidades;
    private Map<String, Integer> estadisticas;

    // 👇👇 NUEVOS CAMPOS 👇👇
    private List<PokemonDTO> lineaEvolutiva = new ArrayList<>();
    private List<PokemonDTO> variaciones = new ArrayList<>();
}