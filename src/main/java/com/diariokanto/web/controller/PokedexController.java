package com.diariokanto.web.controller;

import com.diariokanto.web.dto.PokemonDTO;
import com.diariokanto.web.dto.PokemonDetalleDTO;
import com.diariokanto.web.service.PokemonService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

    // En PokedexController.java
@GetMapping("/pokemon/{id}")
public String verDetallePokemon(@PathVariable Long id, 
                                @RequestParam(required = false) List<String> tipo,
                                @RequestParam(required = false) Integer gen,
                                @RequestParam(required = false) String query,
                                Model model) {
    var pokemon = pokemonService.obtenerDetalle(id);
    if (pokemon == null) {
        return "redirect:/pokedex";
    }
    model.addAttribute("pokemon", pokemon);
    
    // Pasamos los filtros de vuelta para el botón "Volver"
    model.addAttribute("tipoFiltro", tipo);
    model.addAttribute("genFiltro", gen);
    model.addAttribute("queryFiltro", query);
    
    return "pokemon-detail";
}

    @GetMapping("/pokedex")
public String pokedex(@RequestParam(defaultValue = "0") int page, 
                      @RequestParam(required = false) String query,
                      @RequestParam(required = false) List<String> tipo, // CAMBIO: Ahora es List
                      @RequestParam(required = false) Integer gen,
                      Model model) {
    
    List<PokemonDTO> listaFiltrada;
    boolean hayFiltrosActivos = (query != null && !query.isEmpty()) || 
                                (tipo != null && !tipo.isEmpty()) || 
                                (gen != null && gen > 0);

    if (hayFiltrosActivos) {
        if (query != null && !query.isEmpty()) {
            listaFiltrada = pokemonService.buscarPokemon(query);
        } else {
            // Llamamos al nuevo método que maneja la lista de tipos
            listaFiltrada = pokemonService.filtrarPorVariosTipos(tipo, gen);
        }
        model.addAttribute("isSearch", true);
        model.addAttribute("pokemons", listaFiltrada);
    } else {
        listaFiltrada = pokemonService.obtenerListaPaginada(page, 20);
        model.addAttribute("isSearch", false);
        model.addAttribute("pokemons", listaFiltrada);
    }

    model.addAttribute("currentPage", page);
    model.addAttribute("hasPrevious", page > 0);
    model.addAttribute("hasNext", !hayFiltrosActivos && listaFiltrada.size() == 20); 
    
    model.addAttribute("queryActual", query);
    model.addAttribute("tiposActuales", tipo); // Fundamental para la persistencia en el HTML
    model.addAttribute("genActual", gen);
    
    return "pokedex";
}

    // En PokedexController.java
    @GetMapping("/api/internal/pokemon-options/{id}")
    @ResponseBody
    public PokemonDetalleDTO obtenerOpcionesCombate(@PathVariable Long id) {
        // Reutilizamos tu servicio existente
        return pokemonService.obtenerDetalle(id); 
    }
}