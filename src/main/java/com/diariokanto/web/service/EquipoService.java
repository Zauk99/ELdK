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
            String url = apiUrl + "/equipos/usuario?email=" + emailUsuario;
            EquipoDTO[] response = restTemplate.getForObject(url, EquipoDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
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
}