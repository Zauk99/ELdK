package com.diariokanto.web.service;

import com.diariokanto.web.dto.EquipoDTO;
import com.diariokanto.web.dto.MiembroEquipoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PdfService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GRAPHQL_URL = "https://beta.pokeapi.co/graphql/v1beta";

    public byte[] generarPdfShowdown(EquipoDTO equipo) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // 1. TRADUCIR TODO EL EQUIPO AL INGLÉS
            Map<String, String> diccionario = crearDiccionarioTraduccion(equipo);

            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Team Report: " + equipo.getNombre(), fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);

            // Fuente Showdown
            Font fontShowdown = FontFactory.getFont(FontFactory.COURIER, 11);

            // --- CAMBIO CLAVE: Un solo StringBuilder para todo el equipo ---
            StringBuilder fullTeamText = new StringBuilder();

            for (MiembroEquipoDTO m : equipo.getMiembros()) {
                
                // NOMBRE (Nickname (Species) o Species)
                String speciesName = capitalize(m.getNombrePokemon());
                String line1 = (m.getMote() != null && !m.getMote().isEmpty() && !m.getMote().equalsIgnoreCase(m.getNombrePokemon()))
                        ? m.getMote() + " (" + speciesName + ")"
                        : speciesName;
                
                fullTeamText.append(line1);

                // OBJETO
                if (hasText(m.getObjeto())) {
                    String itemIngles = diccionario.getOrDefault(m.getObjeto(), m.getObjeto());
                    fullTeamText.append(" @ ").append(formatShowdown(itemIngles));
                }
                fullTeamText.append("\n");

                // HABILIDAD
                if (hasText(m.getHabilidad())) {
                    String habIngles = diccionario.getOrDefault(m.getHabilidad(), m.getHabilidad());
                    fullTeamText.append("Ability: ").append(formatShowdown(habIngles)).append("\n");
                }

                // EVS
                if (tieneEvs(m)) {
                    fullTeamText.append("EVs: ");
                    boolean first = true;
                    first = appendEv(fullTeamText, m.getHpEv(), "HP", first);
                    first = appendEv(fullTeamText, m.getAttackEv(), "Atk", first);
                    first = appendEv(fullTeamText, m.getDefenseEv(), "Def", first);
                    first = appendEv(fullTeamText, m.getSpAttackEv(), "SpA", first);
                    first = appendEv(fullTeamText, m.getSpDefenseEv(), "SpD", first);
                    first = appendEv(fullTeamText, m.getSpeedEv(), "Spe", first);
                    fullTeamText.append("\n");
                }

                // NATURALEZA
                if (hasText(m.getNaturaleza())) {
                    String natIngles = diccionario.getOrDefault(m.getNaturaleza(), m.getNaturaleza());
                    // Parche: Si la API no traduce "Neutra" o similar, forzamos "Serious" que es la neutra por defecto en Showdown
                    if (natIngles.equalsIgnoreCase("Neutra") || natIngles.equalsIgnoreCase("Neutral")) {
                        natIngles = "Serious";
                    }
                    fullTeamText.append(formatShowdown(natIngles)).append(" Nature\n");
                }

                // MOVIMIENTOS
                appendMove(fullTeamText, m.getMovimiento1(), diccionario);
                appendMove(fullTeamText, m.getMovimiento2(), diccionario);
                appendMove(fullTeamText, m.getMovimiento3(), diccionario);
                appendMove(fullTeamText, m.getMovimiento4(), diccionario);

                // IMPORTANTE: Doble salto de línea al final de cada Pokémon
                fullTeamText.append("\n"); 
            }

            // Añadimos TODO el texto como un único párrafo.
            // Esto garantiza que al copiar, los saltos de línea se respeten.
            document.add(new Paragraph(fullTeamText.toString(), fontShowdown));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- MÉTODOS DE TRADUCCIÓN ---

    private Map<String, String> crearDiccionarioTraduccion(EquipoDTO equipo) {
        Set<String> terminos = new HashSet<>();

        // Recolectar TODO en un solo Set para simplificar
        for (MiembroEquipoDTO m : equipo.getMiembros()) {
            if(hasText(m.getMovimiento1())) terminos.add(m.getMovimiento1());
            if(hasText(m.getMovimiento2())) terminos.add(m.getMovimiento2());
            if(hasText(m.getMovimiento3())) terminos.add(m.getMovimiento3());
            if(hasText(m.getMovimiento4())) terminos.add(m.getMovimiento4());
            if(hasText(m.getHabilidad())) terminos.add(m.getHabilidad());
            if(hasText(m.getObjeto())) terminos.add(m.getObjeto());
            if(hasText(m.getNaturaleza())) terminos.add(m.getNaturaleza());
        }

        // TreeMap insensible a mayúsculas para evitar problemas de "Rayo" vs "rayo"
        Map<String, String> diccionario = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        if (terminos.isEmpty()) return diccionario;

        // Llamadas a la API por lotes
        traducirLote(terminos, "pokemon_v2_move", "pokemon_v2_movenames", diccionario);
        traducirLote(terminos, "pokemon_v2_ability", "pokemon_v2_abilitynames", diccionario);
        traducirLote(terminos, "pokemon_v2_item", "pokemon_v2_itemnames", diccionario);
        traducirLote(terminos, "pokemon_v2_nature", "pokemon_v2_naturenames", diccionario);

        return diccionario;
    }

    private void traducirLote(Set<String> terminos, String tablaRaiz, String tablaNombres, Map<String, String> diccionario) {
        try {
            // Convertimos la lista de términos a formato JSON array: ["Rayo", "Trueno"]
            String jsonArray = terminos.stream()
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(", ", "[", "]"));

            // Query optimizada: Busca coincidencias en español y devuelve el nombre inglés (slug)
            String query = String.format(
                "query { %s(where: {%s: {name: {_in: %s}, language_id: {_eq: 7}}}) { name %s(where: {language_id: {_eq: 7}}) { name } } }",
                tablaRaiz, tablaNombres, jsonArray, tablaNombres
            );

            Map<String, String> body = new HashMap<>();
            body.put("query", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            String response = restTemplate.postForObject(GRAPHQL_URL, entity, String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode dataList = root.path("data").path(tablaRaiz);

            for (JsonNode item : dataList) {
                String englishName = item.get("name").asText();
                // El nombre español con el que buscaremos en el mapa
                JsonNode spanishNode = item.path(tablaNombres).get(0);
                if (spanishNode != null && !spanishNode.isMissingNode()) {
                    String spanishName = spanishNode.get("name").asText();
                    diccionario.put(spanishName, englishName);
                }
            }

        } catch (Exception e) {
            // Ignoramos errores parciales para no romper el PDF, simplemente saldrá en español si falla
            System.err.println("Warning traducción: " + e.getMessage());
        }
    }

    private String formatShowdown(String slug) {
        if (slug == null) return "";
        // Convierte "thunder-punch" en "Thunder Punch"
        return Arrays.stream(slug.split("-"))
                .map(this::capitalize)
                .collect(Collectors.joining(" "));
    }

    private void appendMove(StringBuilder sb, String move, Map<String, String> dic) {
        if (hasText(move)) {
            String translated = dic.getOrDefault(move, move);
            sb.append("- ").append(formatShowdown(translated)).append("\n");
        }
    }

    private boolean appendEv(StringBuilder sb, int valor, String statName, boolean isFirst) {
        if (valor > 0) {
            if (!isFirst) sb.append(" / ");
            sb.append(valor).append(" ").append(statName);
            return false;
        }
        return isFirst;
    }

    private boolean tieneEvs(MiembroEquipoDTO m) {
        return m.getHpEv() > 0 || m.getAttackEv() > 0 || m.getDefenseEv() > 0 || 
               m.getSpAttackEv() > 0 || m.getSpDefenseEv() > 0 || m.getSpeedEv() > 0;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}