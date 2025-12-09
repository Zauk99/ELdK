package com.diariokanto.web.service;

import com.diariokanto.web.dto.CategoriaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Service
public class CategoriaService {

    @Value("${api.url}")
    private String apiUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<CategoriaDTO> listarTodas() {
        try {
            // Llamamos a tu CategoriaController de la API
            String url = apiUrl + "/categorias"; 
            CategoriaDTO[] response = restTemplate.getForObject(url, CategoriaDTO[].class);
            return Arrays.asList(response);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}