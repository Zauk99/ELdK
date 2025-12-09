package com.diariokanto.web.dto;

import lombok.Data;

@Data
public class PokemonDTO {
    private Long id;
    private String nombre;
    private String imagenUrl;
    private String imagenShinyUrl; // <--- NUEVO CAMPO
    private String condicionEvolucion;

    // Actualizamos el constructor manual para incluir la shiny
    public PokemonDTO(Long id, String nombre, String imagenUrl, String imagenShinyUrl) {
        this.id = id;
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
        this.imagenShinyUrl = imagenShinyUrl;
    }
}