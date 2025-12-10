package com.diariokanto.web.controller;

import com.diariokanto.web.dto.EquipoDTO;
import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.service.EquipoService;
import com.diariokanto.web.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/equipos")
public class EquipoController {

    @Autowired private EquipoService equipoService;
    @Autowired private PokemonService pokemonService; // Para el autocompletado de nombres

    // 1. Ver mis equipos (Lista)
    @GetMapping("/mis-equipos")
    public String verMisEquipos(Model model, Authentication auth) {
        UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
        model.addAttribute("equipos", equipoService.obtenerMisEquipos(usuario.getEmail()));
        return "mis-equipos";
    }

    // 2. Vista del Constructor (Formulario)
    @GetMapping("/crear")
    public String vistaCrearEquipo(Model model) {
        // Pasamos la lista de Pokémon para el autocompletado
        model.addAttribute("listaPokemon", pokemonService.getTodosLosNombres());
        return "crear-equipo";
    }

    // 3. Recibir el JSON del equipo y guardarlo (AJAX)
    @PostMapping("/guardar")
    @ResponseBody // Importante: devuelve JSON/Texto, no una vista HTML
    public String guardarEquipo(@RequestBody EquipoDTO equipo, Authentication auth) {
        UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
        try {
            equipoService.guardarEquipo(equipo, usuario.getEmail());
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // 4. Ver todos los equipos (Página principal de Equipos)
    @GetMapping("")
    public String verEquiposComunidad(Model model) {
        model.addAttribute("equipos", equipoService.obtenerTodos());
        return "equipos";
    }
}