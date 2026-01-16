package com.diariokanto.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            model.addAttribute("status", statusCode);
            
            if(statusCode == 404) {
                model.addAttribute("error", "Parece que te has perdido en el Bosque Verde. Esta página no existe.");
            } else if(statusCode == 500) {
                model.addAttribute("error", "Error interno del servidor.");
            } else if(statusCode == 403) {
                model.addAttribute("error", "Acceso denegado. ¡Necesitas una medalla de gimnasio para pasar!");
            }
        }
        return "error";
    }
}