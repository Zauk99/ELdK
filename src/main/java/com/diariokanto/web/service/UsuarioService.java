package com.diariokanto.web.service;

import com.diariokanto.web.dto.UsuarioDTO;
import com.diariokanto.web.dto.UsuarioRegistroDTO;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    @Value("${api.url}") // Lee http://localhost:8080/api desde application.properties
    private String apiUrl;

    private final RestTemplate restTemplate;

    public UsuarioService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía una petición POST Multipart a la API para registrar un usuario con
     * foto.
     */
    // ... imports ...

    public void registrar(UsuarioRegistroDTO dto, MultipartFile foto) {
        String url = apiUrl + "/usuarios/registro";

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("nombreCompleto", dto.getNombreCompleto());
            body.add("username", dto.getUsername());
            body.add("email", dto.getEmail());
            body.add("password", dto.getPassword());
            body.add("movil", dto.getMovil());

            // --- CAMBIO CLAVE AQUÍ ---
            if (foto != null && !foto.isEmpty()) {
                // Caso A: Usuario subió foto real
                ByteArrayResource fileAsResource = new ByteArrayResource(foto.getBytes()) {
                    @Override public String getFilename() { return foto.getOriginalFilename(); }
                };
                body.add("foto", fileAsResource);
            } else {
                // Caso B: No hay foto -> Enviamos un archivo vacío para FORZAR Multipart
                ByteArrayResource emptyResource = new ByteArrayResource(new byte[0]) {
                    @Override public String getFilename() { return ""; } // Nombre vacío indica que no hay fichero
                };
                body.add("foto", emptyResource);
            }
            // -------------------------

            HttpHeaders headers = new HttpHeaders();
            // Recuerda: NO poner setContentType manualmente aquí, Spring lo pondrá solo al ver el recurso

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, requestEntity, Void.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en registro: " + e.getMessage());
        }
    }

    // Método para actualizar DATOS
    public UsuarioDTO actualizar(Long id, UsuarioDTO datos, MultipartFile foto) {
        String url = apiUrl + "/usuarios/actualizar/" + id;
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("username", datos.getUsername());
            body.add("nombreCompleto", datos.getNombreCompleto());
            body.add("pokemonFavorito", datos.getPokemonFavorito());

            if (foto != null && !foto.isEmpty()) {
                ByteArrayResource fileAsResource = new ByteArrayResource(foto.getBytes()) {
                    @Override public String getFilename() { return foto.getOriginalFilename(); }
                };
                body.add("foto", fileAsResource);
            } else {
                 ByteArrayResource empty = new ByteArrayResource(new byte[0]) { @Override public String getFilename() { return ""; }};
                 body.add("foto", empty);
            }

            HttpHeaders headers = new HttpHeaders();
            // Recuerda: NO setContentType
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            return restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, requestEntity, UsuarioDTO.class).getBody();
        } catch (Exception e) {
            // Importante: Lanzamos el mensaje exacto que devuelve la API (ej: "Usuario ya existe")
            throw new RuntimeException(e.getMessage());
        }
    }

    // Método para cambiar PASSWORD
    public void cambiarPassword(Long id, String nuevaPass) {
        String url = apiUrl + "/usuarios/password/" + id;
        restTemplate.put(url, nuevaPass);
    }

    // Obtener lista completa
    public List<UsuarioDTO> listarTodos() {
        try {
            String url = apiUrl + "/usuarios";
            UsuarioDTO[] response = restTemplate.getForObject(url, UsuarioDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            return List.of();
        }
    }

    // Enviar cambio de rol
    public void cambiarRol(Long id, String nuevoRol) {
        String url = apiUrl + "/usuarios/rol/" + id;
        try {
            restTemplate.put(url, nuevoRol);
        } catch (Exception e) {
            // Capturamos el mensaje de error de la API (ej: "No se puede degradar...")
            throw new RuntimeException(e.getMessage()); 
        }
    }
}