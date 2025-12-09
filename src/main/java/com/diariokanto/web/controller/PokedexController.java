package com.diariokanto.web.controller;

import com.diariokanto.web.dto.PokemonDTO;
import com.diariokanto.web.service.PokemonService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PokedexController {

    @Autowired
    private PokemonService pokemonService;

    /* @GetMapping("/pokedex")
    public String pokedex(@RequestParam(defaultValue = "0") int page, Model model) {
        int limit = 20; // Pokemons por página

        // Cargar lista
        var lista = pokemonService.obtenerListaPaginada(page, limit);
        model.addAttribute("pokemons", lista);

        // Variables para la paginación (Anterior / Siguiente)
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", lista.size() == limit); // Asumimos que si hay 20, hay más

        return "pokedex";
    } */

    @GetMapping("/pokemon/{id}")
    public String verDetallePokemon(@PathVariable Long id, Model model) {
        var pokemon = pokemonService.obtenerDetalle(id);
        if (pokemon == null) {
            return "redirect:/pokedex";
        }
        model.addAttribute("pokemon", pokemon);
        return "pokemon-detail";
    }

    @GetMapping("/pokedex")
    public String pokedex(@RequestParam(defaultValue = "0") int page, 
                          @RequestParam(required = false) String query, 
                          Model model) {
        
        List<PokemonDTO> lista;
        
        if (query != null && !query.isEmpty()) {
            // Modo Búsqueda
            lista = pokemonService.buscarPokemon(query);
            model.addAttribute("isSearch", true); // Para ocultar botones de paginación
        } else {
            // Modo Normal
            lista = pokemonService.obtenerListaPaginada(page, 20);
            model.addAttribute("isSearch", false);
        }

        model.addAttribute("pokemons", lista);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", lista.size() == 20); // Aproximado
        model.addAttribute("queryActual", query);
        
        return "pokedex";
    }
}