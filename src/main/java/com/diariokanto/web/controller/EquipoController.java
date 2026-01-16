package com.diariokanto.web.controller;

import com.diariokanto.web.dto.EquipoDTO;
import com.diariokanto.web.dto.PokemonDTO;
import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.service.EquipoService;
import com.diariokanto.web.service.PdfService;
import com.diariokanto.web.service.PokemonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.diariokanto.web.dto.PokemonDetalleDTO;
import com.diariokanto.web.dto.MiembroEquipoDTO;

@Controller
@RequestMapping("/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;
    @Autowired
    private PokemonService pokemonService; // Para el autocompletado de nombres
    @Autowired
    private PdfService pdfService;

    // Instancia de ObjectMapper para convertir objetos a JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/crear")
    public String vistaCrearEquipo(Model model) {
        // 1. Obtenemos todos los Pokémon
        List<PokemonDTO> listaPokemon = pokemonService.obtenerTodos();

        // 2. Creamos el mapa: "pikachu" -> 25
        java.util.Map<String, Long> pokemonMap = new java.util.HashMap<>();
        for (PokemonDTO p : listaPokemon) {
            pokemonMap.put(p.getNombre().toLowerCase(), p.getId());
        }

        // 3. Convertimos a JSON: {"bulbasaur":1, "ivysaur":2...}
        String pokemonDbJson = "{}";
        try {
            pokemonDbJson = objectMapper.writeValueAsString(pokemonMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("listaPokemon", listaPokemon); // Para el buscador (datalist)
        model.addAttribute("pokemonDbJson", pokemonDbJson); // Para el script de imágenes

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
        // AÑADIMOS ESTO:
        model.addAttribute("pokemonIds", generarMapaPokemon());
        return "equipos";
    }

    @GetMapping("/mis-equipos")
    public String verMisEquipos(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/login";

        UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
        List<EquipoDTO> misEquipos = equipoService.obtenerMisEquipos(usuario.getEmail());

        model.addAttribute("equipos", misEquipos);
        // AÑADIMOS ESTO:
        model.addAttribute("pokemonIds", generarMapaPokemon());

        return "mis-equipos";
    }

    private Map<String, Long> generarMapaPokemon() {
        return pokemonService.obtenerTodos().stream()
                .collect(Collectors.toMap(
                        p -> p.getNombre().toLowerCase(),
                        p -> p.getId(),
                        (existing, replacement) -> existing // En caso de duplicados, mantener el primero
                ));
    }

    @GetMapping("/{id}")
    public String verDetalleEquipo(@PathVariable Long id, Model model) {
        EquipoDTO equipo = equipoService.obtenerPorId(id);
        
        if (equipo == null) {
            return "redirect:/equipos";
        }
        
        // 1. Generamos el mapa de IDs para buscar las fotos y datos
        Map<String, Long> mapaIds = generarMapaPokemon();
        
        // 2. Preparamos un mapa para guardar los detalles COMPLETOS (incluidos tipos) de cada miembro
        // Clave: Nombre del pokemon (en minúsculas), Valor: Su DTO con tipos
        Map<String, PokemonDetalleDTO> detallesPokemon = new HashMap<>();

        if (equipo.getMiembros() != null) {
            for (MiembroEquipoDTO miembro : equipo.getMiembros()) {
                String nombreKey = miembro.getNombrePokemon().toLowerCase();
                
                // Si aún no hemos buscado este pokémon, lo buscamos (evita llamadas repetidas si tienes 2 Pikachus)
                if (!detallesPokemon.containsKey(nombreKey)) {
                    Long pokeId = mapaIds.get(nombreKey);
                    if (pokeId != null) {
                        // Usamos el servicio existente que ya trae tipos, stats, etc.
                        PokemonDetalleDTO detalle = pokemonService.obtenerDetalle(pokeId);
                        detallesPokemon.put(nombreKey, detalle);
                    }
                }
            }
        }
        
        model.addAttribute("equipo", equipo);
        model.addAttribute("pokemonIds", mapaIds); 
        model.addAttribute("detallesPokemon", detallesPokemon); // <--- NUEVO ATTRIBUTO
        
        return "equipo-detalle"; 
    }

    @GetMapping("/editar/{id}")
    public String vistaEditarEquipo(@PathVariable Long id, Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        
        // 1. Obtener datos necesarios (igual que en crear)
        List<PokemonDTO> listaPokemon = pokemonService.obtenerTodos();
        Map<String, Long> pokemonMap = new java.util.HashMap<>();
        for (PokemonDTO p : listaPokemon) {
            pokemonMap.put(p.getNombre().toLowerCase(), p.getId());
        }
        
        String pokemonDbJson = "{}";
        String equipoJson = "null"; // Por defecto no hay equipo
        
        // 2. Obtener el equipo a editar
        EquipoDTO equipoAEditar = equipoService.obtenerPorId(id);
        
        // 3. Convertir todo a JSON para el JS
        try {
            pokemonDbJson = objectMapper.writeValueAsString(pokemonMap);
            if (equipoAEditar != null) {
                equipoJson = objectMapper.writeValueAsString(equipoAEditar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("listaPokemon", listaPokemon);
        model.addAttribute("pokemonDbJson", pokemonDbJson);
        // Pasamos el equipo JSON a la vista
        model.addAttribute("equipoJson", equipoJson);
        model.addAttribute("equipo", equipoAEditar != null ? equipoAEditar : new EquipoDTO());
        
        return "crear-equipo"; // Reutilizamos la misma plantilla
    }

    // POST para borrar desde un formulario HTML
    @PostMapping("/eliminar/{id}")
    public String eliminarEquipo(@PathVariable Long id, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            UsuarioDTO usuario = (UsuarioDTO) auth.getPrincipal();
            equipoService.eliminarEquipo(id, usuario.getEmail());
        }
        return "redirect:/equipos/mis-equipos";
    }

    @GetMapping("/exportar/{id}")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        // 1. Recuperamos el equipo con TODOS sus datos (incluidos EVs)
        EquipoDTO equipo = equipoService.obtenerPorId(id);
        
        if (equipo == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. Generamos el PDF
        byte[] pdfBytes = pdfService.generarPdfShowdown(equipo);

        // 3. Devolvemos el archivo para descarga
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=team_" + id + "_showdown.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}