package com.diariokanto.web.service;

import com.diariokanto.web.dto.EquipoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class EquipoService {

    @Value("${api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public void guardarEquipo(EquipoDTO equipo, String emailUsuario) {
        // Asumimos que la API tiene un endpoint para esto
        // Enviamos también el email para saber de quién es el equipo
        String url = apiUrl + "/equipos/crear?email=" + emailUsuario;
        restTemplate.postForObject(url, equipo, Void.class);
    }

    public List<EquipoDTO> obtenerMisEquipos(String emailUsuario) {
        try {
            // CORRECCIÓN: Cambiamos "/equipos/usuario" por "/equipos/mis-equipos"
            // para que coincida con tu API Backend.
            String url = apiUrl + "/equipos/mis-equipos?email=" + emailUsuario;

            EquipoDTO[] response = restTemplate.getForObject(url, EquipoDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            e.printStackTrace(); // Añade esto para ver errores en la consola si fallara algo más
        return List.of();
    }
    }

    public List<Map> obtenerTodos() {
        try {
            String url = apiUrl + "/equipos";
            Map[] response = restTemplate.getForObject(url, Map[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            return List.of();
        }
    }

    public EquipoDTO obtenerPorId(Long id) {
        try {
            String url = apiUrl + "/equipos/" + id;
            return restTemplate.getForObject(url, EquipoDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void eliminarEquipo(Long id, String emailUsuario) {
        try {
            // Llamamos a la API pasando el email para validar la propiedad
            restTemplate.delete(apiUrl + "/equipos/" + id + "?email=" + emailUsuario);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}