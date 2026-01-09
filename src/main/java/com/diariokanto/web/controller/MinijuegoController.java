package com.diariokanto.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/minijuegos")
public class MinijuegoController {

    // Página principal con el menú de selección
    @GetMapping
    public String index() {
        return "minijuegos-index";
    }

    // Ruta para Voltorb Flip
    @GetMapping("/voltorb-flip")
    public String voltorbFlip() {
        return "voltorb-flip";
    }

    // Ruta para Adivina el Pokémon
    @GetMapping("/adivina-pokemon")
    public String adivinaPokemon() {
        return "adivina-pokemon";
    }
} 
