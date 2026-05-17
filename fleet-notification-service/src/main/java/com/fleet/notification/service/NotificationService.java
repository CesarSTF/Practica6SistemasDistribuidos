package com.fleet.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la lógica de envío de notificaciones.
 * Simula la integración con proveedores externos (Email, SMS, Push).
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Procesa y envía la notificación basándose en el contenido del mensaje.
     * @param alertMessage Mensaje de alerta recibido de la cola.
     */
    public void sendNotification(String alertMessage) {
        log.info("==================================================");
        log.info("           🔔 NUEVA NOTIFICACIÓN 🔔               ");
        log.info("==================================================");
        
        // Simulación de envío por diferentes canales
        log.info("📧 [EMAIL] Enviando correo al administrador de flota...");
        log.info("📱 [SMS] Enviando mensaje de texto al conductor...");
        
        log.info("📝 DETALLE DE LA ALERTA:");
        log.info("   {}", alertMessage);
        
        log.info("✅ Estado: Notificación enviada exitosamente.");
        log.info("==================================================");
    }
}
