package com.diariokanto.web.service;

import com.diariokanto.web.dto.PokemonDTO;
import com.diariokanto.web.dto.PokemonDetalleDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PokemonService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // CACHÉ MAESTRA
    private List<PokemonDTO> masterList = new ArrayList<>();

    // --- DICCIONARIOS DE TRADUCCIÓN ---
    private static final Map<String, String> TRADUCCION_TIPOS = Map.ofEntries(
        Map.entry("normal", "Normal"), Map.entry("fighting", "Lucha"), Map.entry("flying", "Volador"),
        Map.entry("poison", "Veneno"), Map.entry("ground", "Tierra"), Map.entry("rock", "Roca"),
        Map.entry("bug", "Bicho"), Map.entry("ghost", "Fantasma"), Map.entry("steel", "Acero"),
        Map.entry("fire", "Fuego"), Map.entry("water", "Agua"), Map.entry("grass", "Planta"),
        Map.entry("electric", "Eléctrico"), Map.entry("psychic", "Psíquico"), Map.entry("ice", "Hielo"),
        Map.entry("dragon", "Dragón"), Map.entry("dark", "Siniestro"), Map.entry("fairy", "Hada"),
        Map.entry("unknown", "Desconocido"), Map.entry("shadow", "Oscuro")
    );

    private static final Map<String, String> TRADUCCION_STATS = Map.of(
        "hp", "PS",
        "attack", "Ataque",
        "defense", "Defensa",
        "special-attack", "At. Esp.",
        "special-defense", "Def. Esp.",
        "speed", "Velocidad"
    );

    // ==========================================
    // 1. CARGA INICIAL (INIT)
    // ==========================================
    @PostConstruct
    public void init() {
        try {
            // Cargar TODOS (incluyendo formas especiales) para la lógica de evoluciones
            String url = "https://pokeapi.co/api/v2/pokemon?limit=2000";
            Map response = restTemplate.getForObject(url, Map.class);
            List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");

            for (Map<String, String> p : results) {
                String urlDetalle = p.get("url");
                String[] partes = urlDetalle.split("/");
                Long id = Long.parseLong(partes[partes.length - 1]);

                String name = p.get("name");
                String imagen = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
                String shiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/" + id + ".png";
                
                // Guardamos TODO en la caché, sin filtrar todavía
                masterList.add(new PokemonDTO(id, name, imagen, shiny));
            }
            System.out.println(">>> Pokédex cargada completa: " + masterList.size() + " registros.");
        } catch (Exception e) {
            System.err.println("Error cargando Pokédex inicial: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. MÉTODOS DE LISTADO Y BÚSQUEDA
    // ==========================================
    
    // Método para el dropdown del perfil
    public List<PokemonDTO> getTodosLosNombres() {
        return this.masterList;
    }
    // --- AÑADE ESTE MÉTODO ---
    // Método alias para mantener coherencia con el controlador de Equipos
    public List<PokemonDTO> obtenerTodos() {
        return this.masterList;
    }

    public List<PokemonDTO> obtenerListaPaginada(int page, int limit) {
        // Filtramos para mostrar solo los originales en la paginación
        List<PokemonDTO> originales = masterList.stream()
                .filter(p -> p.getId() < 10000)
                .collect(Collectors.toList());

        int start = page * limit;
        int end = Math.min(start + limit, originales.size());
        if (start >= originales.size()) return new ArrayList<>();
        return originales.subList(start, end);
    }

    public List<PokemonDTO> buscarPokemon(String query) {
        if (query == null || query.isEmpty()) return new ArrayList<>();
        // Filtramos aquí también para que el buscador no muestre basura técnica
        return masterList.stream()
                .filter(p -> p.getId() < 10000) 
                .filter(p -> p.getNombre().toLowerCase().contains(query.toLowerCase()))
                .limit(20)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 3. DETALLE DEL POKÉMON (EL NÚCLEO)
    // ==========================================
    public PokemonDetalleDTO obtenerDetalle(Long id) {
        try {
            String urlPokemon = "https://pokeapi.co/api/v2/pokemon/" + id;
            Map data = restTemplate.getForObject(urlPokemon, Map.class);

            // A. Detectar Sufijo (para evoluciones regionales)
            String nombreActual = (String) data.get("name");
            String sufijoForma = detectarSufijo(nombreActual);

            // B. Datos de Especie
            Map speciesObj = (Map) data.get("species");
            String urlSpecies = (String) speciesObj.get("url");
            Map speciesData = restTemplate.getForObject(urlSpecies, Map.class);

            PokemonDetalleDTO dto = new PokemonDetalleDTO();
            dto.setId(id);
            dto.setNombre(nombreActual);
            dto.setAltura(((Number) data.get("height")).doubleValue() / 10.0);
            dto.setPeso(((Number) data.get("weight")).doubleValue() / 10.0);

            // Imágenes
            Map sprites = (Map) data.get("sprites");
            Map other = (Map) sprites.get("other");
            Map official = (Map) other.get("official-artwork");
            String imgUrl = (official != null && official.get("front_default") != null) 
                            ? (String) official.get("front_default") 
                            : (String) sprites.get("front_default");
            
            dto.setImagenUrl(imgUrl);
            dto.setImagenShinyUrl(official != null ? (String) official.get("front_shiny") : null);

            // Descripción
            dto.setDescripcion(obtenerDescripcionEsp(speciesData));

            // C. Procesar Stats, Tipos (Traducidos) y Habilidades
            procesarStatsYTipos(dto, data);

            // D. Cadena Evolutiva Inteligente (pasando el sufijo)
            Map evoChainUrlMap = (Map) speciesData.get("evolution_chain");
            if (evoChainUrlMap != null) {
                String urlCadena = (String) evoChainUrlMap.get("url");
                Map evoData = restTemplate.getForObject(urlCadena, Map.class);
                Map chain = (Map) evoData.get("chain");
                
                List<PokemonDTO> evoluciones = new ArrayList<>();
                procesarCadenaRecursiva(chain, evoluciones, sufijoForma);
                dto.setLineaEvolutiva(evoluciones);
            }

            // E. Variaciones
            List<Map> varieties = (List<Map>) speciesData.get("varieties");
            List<PokemonDTO> listaVariaciones = new ArrayList<>();
            for (Map v : varieties) {
                Map pInfo = (Map) v.get("pokemon");
                String vName = (String) pInfo.get("name");
                String vUrl = (String) pInfo.get("url");
                String[] parts = vUrl.split("/");
                Long vId = Long.parseLong(parts[parts.length - 1]);

                if (!vId.equals(id)) {
                    String vImg = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + vId + ".png";
                    String vShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/" + vId + ".png";
                    listaVariaciones.add(new PokemonDTO(vId, vName, vImg, vShiny));
                }
            }
            dto.setVariaciones(listaVariaciones);

            return dto;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==========================================
    // 4. MÉTODOS AUXILIARES DE LÓGICA
    // ==========================================

    // Procesa Tipos (Mapa para CSS/Texto), Habilidades (Traducidas) y Stats (Traducidos)
    private void procesarStatsYTipos(PokemonDetalleDTO dto, Map data) {
        // 1. TIPOS
        List<Map> typesList = (List<Map>) data.get("types");
        Map<String, String> mapaTipos = new LinkedHashMap<>();
        for (Map t : typesList) {
            String nombreIngles = (String) ((Map)t.get("type")).get("name");
            String nombreEsp = TRADUCCION_TIPOS.getOrDefault(nombreIngles, nombreIngles);
            mapaTipos.put(nombreIngles, nombreEsp);
        }
        dto.setTipos(mapaTipos);

        // 2. HABILIDADES
        List<Map> abilitiesList = (List<Map>) data.get("abilities");
        List<String> habilidades = new ArrayList<>();
        for (Map a : abilitiesList) {
            Map abilityInfo = (Map) a.get("ability");
            String nombreIngles = (String) abilityInfo.get("name");
            String url = (String) abilityInfo.get("url");
            
            String nombreEsp = obtenerNombreHabilidadEnEspanol(url);
            if (nombreEsp != null) {
                habilidades.add(nombreEsp);
            } else {
                habilidades.add(nombreIngles.replace("-", " "));
            }
        }
        dto.setHabilidades(habilidades);

        // 3. STATS
        List<Map> statsList = (List<Map>) data.get("stats");
        Map<String, Integer> estadisticas = new LinkedHashMap<>();
        for (Map s : statsList) {
            String nombreIngles = (String)((Map)s.get("stat")).get("name");
            String nombreEsp = TRADUCCION_STATS.getOrDefault(nombreIngles, nombreIngles.toUpperCase());
            estadisticas.put(nombreEsp, (Integer)s.get("base_stat"));
        }
        dto.setEstadisticas(estadisticas);

        // Dentro de procesarStatsYTipos o donde proceses el detalle
    // Añade esto para capturar los movimientos:
    
    // 4. MOVIMIENTOS (Añadir esto en obtenerDetalle o procesarStatsYTipos)
    List<Map> movesList = (List<Map>) data.get("moves");
    List<String> movimientos = new ArrayList<>();
    if (movesList != null) {
        for (Map m : movesList) {
            Map moveInfo = (Map) m.get("move");
            String moveName = (String) moveInfo.get("name");
            // Formatear nombre (ej: "thunder-punch" -> "Thunder Punch")
            movimientos.add(moveName.replace("-", " ")); 
        }
    }
    dto.setMovimientos(movimientos);
    }

    // Recursividad para la cadena evolutiva con soporte regional
    private void procesarCadenaRecursiva(Map nodo, List<PokemonDTO> lista, String sufijo) {
        if (nodo == null) return;

        Map species = (Map) nodo.get("species");
        String nombreBase = (String) species.get("name");
        
        // Buscamos si existe la variante con el sufijo (ej: raichu-alola)
        PokemonDTO pokemonElegido = buscarVariante(nombreBase, sufijo);
        
        // Si no existe, usamos el base
        if (pokemonElegido == null) {
            String url = (String) species.get("url");
            String[] parts = url.split("/");
            Long id = Long.parseLong(parts[parts.length - 1]);
            String img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";
            String shiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/shiny/" + id + ".png";
            pokemonElegido = new PokemonDTO(id, nombreBase, img, shiny);
        }

        // Condición de evolución
        List<Map> detalles = (List<Map>) nodo.get("evolution_details");
        if (detalles != null && !detalles.isEmpty()) {
            pokemonElegido.setCondicionEvolucion(traducirCondicion(detalles.get(0)));
        }

        lista.add(pokemonElegido);

        List<Map> hijos = (List<Map>) nodo.get("evolves_to");
        for (Map hijo : hijos) {
            procesarCadenaRecursiva(hijo, lista, sufijo);
        }
    }

    private String obtenerNombreHabilidadEnEspanol(String urlHabilidad) {
        try {
            Map data = restTemplate.getForObject(urlHabilidad, Map.class);
            List<Map> names = (List<Map>) data.get("names");
            for (Map nameEntry : names) {
                Map language = (Map) nameEntry.get("language");
                if ("es".equals(language.get("name"))) return (String) nameEntry.get("name");
            }
        } catch (Exception e) {}
        return null;
    }

    private String obtenerDescripcionEsp(Map speciesData) {
        try {
            List<Map> flavorEntries = (List<Map>) speciesData.get("flavor_text_entries");
            for (Map entry : flavorEntries) {
                Map language = (Map) entry.get("language");
                if ("es".equals(language.get("name"))) {
                    return ((String) entry.get("flavor_text")).replace("\n", " ").replace("\f", " ");
                }
            }
        } catch (Exception e) {}
        return "Descripción no disponible.";
    }

    private String detectarSufijo(String nombre) {
        String[] sufijos = {"-alola", "-galar", "-hisui", "-paldea", "-gmax", "-mega"};
        for (String s : sufijos) {
            if (nombre.contains(s)) return s;
        }
        return "";
    }

    private PokemonDTO buscarVariante(String nombreBase, String sufijo) {
        if (sufijo == null || sufijo.isEmpty()) return null;
        String nombreBuscado = nombreBase + sufijo;
        return masterList.stream()
                .filter(p -> p.getNombre().equals(nombreBuscado))
                .findFirst()
                .orElse(null);
    }

    private String traducirCondicion(Map detalle) {
        if (detalle == null) return "";
        Map trigger = (Map) detalle.get("trigger");
        String triggerName = (String) trigger.get("name");

        if ("level-up".equals(triggerName)) {
            if (detalle.get("min_level") != null) return "Nv. " + detalle.get("min_level");
            if (detalle.get("min_happiness") != null) return "Felicidad";
            if (detalle.get("known_move") != null) return "Con Movimiento";
            if (detalle.get("location") != null) return "En Zona Especial";
            return "Nivel";
        }
        if ("use-item".equals(triggerName)) {
            Map item = (Map) detalle.get("item");
            String itemName = (String) item.get("name");
            if(itemName.contains("stone")) return "Piedra";
            return "Objeto";
        }
        if ("trade".equals(triggerName)) return "Intercambio";
        
        return "Especial";
    }

    // ==========================================
    // NUEVA LÓGICA DE FILTRADO COMBINADO
    // ==========================================

    public List<PokemonDTO> filtrarCombinado(String tipo, Integer gen) {
        // Empezamos con la lista completa (solo originales < 10000)
        List<PokemonDTO> resultado = masterList.stream()
                .filter(p -> p.getId() < 10000)
                .collect(Collectors.toList());

        // 1. FILTRO POR TIPO (Si se seleccionó uno)
        if (tipo != null && !tipo.isEmpty()) {
            // Obtenemos los nombres de los pokémon de ese tipo desde la API
            Set<String> nombresDeTipo = obtenerNombresPorTipo(tipo);
            
            // Filtramos nuestra lista manteniendo solo los que están en ese Set
            resultado = resultado.stream()
                    .filter(p -> nombresDeTipo.contains(p.getNombre()))
                    .collect(Collectors.toList());
        }

        // 2. FILTRO POR GENERACIÓN (Si se seleccionó una)
        if (gen != null && gen > 0) {
            resultado = resultado.stream()
                    .filter(p -> perteneceAGeneracion(p.getId(), gen))
                    .collect(Collectors.toList());
        }

        return resultado;
    }

    // Método auxiliar para consultar la API de tipos (caché simple recomendada aquí si fuera prod)
    private Set<String> obtenerNombresPorTipo(String tipo) {
        try {
            String url = "https://pokeapi.co/api/v2/type/" + tipo;
            Map data = restTemplate.getForObject(url, Map.class);
            List<Map> pokemonList = (List<Map>) data.get("pokemon");
            
            return pokemonList.stream()
                    .map(entry -> (String) ((Map) entry.get("pokemon")).get("name"))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    // Rangos de IDs oficiales por generación
    private boolean perteneceAGeneracion(Long id, int gen) {
        return switch (gen) {
            case 1 -> id >= 1 && id <= 151;
            case 2 -> id >= 152 && id <= 251;
            case 3 -> id >= 252 && id <= 386;
            case 4 -> id >= 387 && id <= 493;
            case 5 -> id >= 494 && id <= 649;
            case 6 -> id >= 650 && id <= 721;
            case 7 -> id >= 722 && id <= 809;
            case 8 -> id >= 810 && id <= 905;
            case 9 -> id >= 906 && id <= 1025;
            default -> false;
        };
    }
}