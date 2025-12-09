package com.diariokanto.web.service;

import com.diariokanto.web.dto.ComentarioDTO;
import com.diariokanto.web.dto.CrearComentarioRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
public class ComentarioService {

    @Value("${api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ComentarioDTO> obtenerPorNoticia(Long noticiaId) {
        try {
            String url = apiUrl + "/comentarios/noticia/" + noticiaId;
            ComentarioDTO[] response = restTemplate.getForObject(url, ComentarioDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) { return List.of(); }
    }

    // ... imports (Añade CrearComentarioRequest)

    public void enviarComentario(Long noticiaId, String email, String texto) {
        String url = apiUrl + "/comentarios/crear";
        
        // Creamos el objeto limpio
        CrearComentarioRequest request = new CrearComentarioRequest(noticiaId, email, texto);
        
        // Lo enviamos como JSON (automático al usar postForObject con un objeto)
        restTemplate.postForObject(url, request, ComentarioDTO.class);
    }

    public void eliminarComentario(Long id) {
        String url = apiUrl + "/comentarios/" + id;
        restTemplate.delete(url);
    }
}