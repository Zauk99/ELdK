package com.diariokanto.web.controller;

import com.diariokanto.web.dto.UsuarioRegistroDTO;
import com.diariokanto.web.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/login")
    public String login(Model model) {
        // 1. Necesario para el autocompletado de Pokémon
        model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
        
        // 2. ¡IMPORTANTE! Necesario para el th:object="${usuario}" del formulario de registro
        // Si no añades esto, la página explota porque no encuentra la variable "usuario"
        model.addAttribute("usuario", new UsuarioRegistroDTO());
        
        return "login";
    }
}