package com.fleet.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador para servir la interfaz web del dashboard.
 * 
 * Este controlador maneja el enrutamiento de la raíz del sitio web
 * hacia la aplicación web estática (HTML/CSS/JavaScript).
 * 
 * <p>Propósito:
 * Permite acceder al dashboard de monitoreo en tiempo real mediante
 * la URL raíz (http://localhost:8080/) en lugar de requerir
 * /index.html explícitamente.
 * 
 * @author Sistema de Monitoreo de Flota
 * @version 1.0
 */
@Controller
public class IndexController {
    
    /**
     * Sirve la página principal del dashboard.
     * 
     * Utiliza "forward:" en lugar de "redirect:" para que Spring sirva
     * el archivo estático sin realizar una redirección HTTP.
     * Esto mantiene la URL en "/" y evita problemas de CORS.
     * 
     * @return String que indica a Spring que reenvíe a /index.html
     */
    @GetMapping("/")
    public String index() {
        // forward: enruta internamente a /index.html (sin HTTP redirect)
        return "forward:/index.html";
    }
}
