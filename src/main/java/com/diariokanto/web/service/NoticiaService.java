package com.diariokanto.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import com.diariokanto.web.dto.NoticiaDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class NoticiaService {

    @Value("${api.url}") // Lee 'http://localhost:8080/api' del properties
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /* public List<NoticiaDTO> obtenerNoticias() {
        try {
            // Hacemos el GET a la API (igual que hizo Postman)
            String url = apiUrl + "/noticias";
            NoticiaDTO[] response = restTemplate.getForObject(url, NoticiaDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            System.err.println("Error conectando con la API: " + e.getMessage());
            return List.of(); // Devuelve lista vacía si falla
        }
    } */

    public NoticiaDTO obtenerPorId(Long id) {
        try {
            String url = apiUrl + "/noticias/" + id;
            return restTemplate.getForObject(url, NoticiaDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void guardarNoticia(NoticiaDTO noticia) {
        String url = apiUrl + "/noticias/crear";
        restTemplate.postForObject(url, noticia, Void.class);
    }

    public void guardarNoticiaMultipart(NoticiaDTO noticia, MultipartFile fichero) {
        String url = apiUrl + "/noticias/crear";

        try {
            // Preparamos el cuerpo de la petición (simulando un formulario HTML)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("titulo", noticia.getTitulo());
            body.add("contenido", noticia.getContenidoHtml());
            body.add("categoria", noticia.getNombreCategoria());

            if (fichero != null && !fichero.isEmpty()) {
                // Truco técnico: Convertir MultipartFile a Resource para RestTemplate
                ByteArrayResource fileAsResource = new ByteArrayResource(fichero.getBytes()) {
                    @Override
                    public String getFilename() {
                        return fichero.getOriginalFilename();
                    }
                };
                body.add("fichero", fileAsResource);
            }

            // Cabeceras correctas
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            restTemplate.postForObject(url, requestEntity, Void.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fallo al enviar noticia a la API");
        }
    }

    // ... imports ...

    // UPDATE
    public void actualizarNoticiaMultipart(Long id, NoticiaDTO noticia, MultipartFile fichero) {
        String url = apiUrl + "/noticias/" + id; // PUT /api/noticias/{id}

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("titulo", noticia.getTitulo());
            body.add("contenido", noticia.getContenidoHtml());
            body.add("categoria", noticia.getNombreCategoria());

            if (fichero != null && !fichero.isEmpty()) {
                ByteArrayResource fileAsResource = new ByteArrayResource(fichero.getBytes()) {
                    @Override public String getFilename() { return fichero.getOriginalFilename(); }
                };
                body.add("fichero", fileAsResource);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Usamos .put() no devuelve nada, o exchange si queremos respuesta
            restTemplate.put(url, requestEntity);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar noticia");
        }
    }

    // DELETE
    public void eliminarNoticia(Long id) {
        restTemplate.delete(apiUrl + "/noticias/" + id);
    }

    // Método sobrecargado: sin parámetros (todas)
    public List<NoticiaDTO> obtenerNoticias() {
        return obtenerNoticias(null);
    }

    // Método con búsqueda
    public List<NoticiaDTO> obtenerNoticias(String busqueda) {
        try {
            String url = apiUrl + "/noticias";
            if (busqueda != null && !busqueda.isEmpty()) {
                url += "?busqueda=" + busqueda;
            }
            NoticiaDTO[] response = restTemplate.getForObject(url, NoticiaDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            return List.of();
        }
    }

    // En com.diariokanto.web.service.NoticiaService
    public List<NoticiaDTO> buscarPorCategoria(String categoria) {
        try {
            String url = apiUrl + "/noticias?categoria=" + categoria;
            NoticiaDTO[] response = restTemplate.getForObject(url, NoticiaDTO[].class);
            return response != null ? Arrays.asList(response) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}