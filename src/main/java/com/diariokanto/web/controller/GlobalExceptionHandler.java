package com.diariokanto.web.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Errores de la API (4xx y 5xx)
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public String handleApiErrors(Exception ex, Model model) {
        String mensaje = ex.getMessage();
        
        // Limpiamos el mensaje técnico para el usuario
        if (mensaje.contains("400")) mensaje = "Petición incorrecta o datos inválidos.";
        else if (mensaje.contains("401")) mensaje = "No tienes permiso para realizar esta acción.";
        else if (mensaje.contains("404")) mensaje = "El recurso que buscas no existe.";
        else if (mensaje.contains("500")) mensaje = "Error interno del servidor (API).";

        model.addAttribute("error", mensaje);
        model.addAttribute("status", "API Error");
        return "error"; // Nombre de la plantilla error.html
    }

    // 2. Errores de Conexión (API apagada)
    @ExceptionHandler(ResourceAccessException.class)
    public String handleConnectionError(Model model) {
        model.addAttribute("error", "No se puede conectar con el servidor de datos. ¿Está encendida la API?");
        model.addAttribute("status", "Conexión");
        return "error";
    }

    // 3. NUEVO: Error 404 (URL no encontrada / No static resource)
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("error", "Parece que te has perdido. Esta página no existe en nuestra región.");
        model.addAttribute("status", "404"); // Código para mostrar (opcional)
        return "error";
    }

    // 4. Cualquier otro error no controlado (General)
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception ex, Model model) {
        model.addAttribute("error", "Ha ocurrido un error inesperado: " + ex.getMessage());
        model.addAttribute("status", "Error");
        return "error";
    }
}