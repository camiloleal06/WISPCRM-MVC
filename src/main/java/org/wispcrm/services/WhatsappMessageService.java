package org.wispcrm.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static com.google.api.client.http.HttpMethods.POST;

@Slf4j
@Service
public class WhatsappMessageService {

    private static final String AREA_CODE = "+57";
    private static final String AUTHORIZATION = "Authorization";
    private static final String TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    @Value("${message.uriWhatsappApiSender}")
    private String uriWhatsappApiSender;

    @Value("${message.tokenBearerWhatsappApiSender}")
    private String tokenBearerWhatsappApiSender;

    private HttpClient client = HttpClient.newHttpClient();
    private ObjectMapper mapper = new ObjectMapper();

    // -------------------- MENSAJE SIMPLE --------------------
    public void sendSimpleMessageWasenderapi(String clientNumber, String msg) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE + clientNumber);
        payload.put("text", msg);

        sendHttpRequest(payload, "mensaje simple");
    }

    // -------------------- MENSAJE A GRUPO --------------------
    public void sendSimpleMessageToGroupWasApiSender(String groupId, String msg) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", groupId);
        payload.put("text", msg);

        sendHttpRequest(payload, "mensaje a grupo");
    }

    // -------------------- DOCUMENTO + MENSAJE --------------------
    public void sendDocumentAndMessageWasenderapi(String clientNumber, String msg, String pathDocument, String fileName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE + clientNumber);
        payload.put("text", msg);
        payload.put("documentUrl", pathDocument);
        payload.put("fileName", fileName);

        sendHttpRequest(payload, "documento + mensaje");
    }

    // -------------------- IMAGEN + MENSAJE ASÍNCRONO --------------------
    @Async("threadPoolTaskExecutor")
    public void sendImageAndMessageWasenderapi(String clientNumber, String msg, String imageUrl) {
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE + clientNumber);
        payload.put("text", msg);
        payload.put("imageUrl", imageUrl);

        sendHttpRequest(payload, "imagen + mensaje");
    }

    // -------------------- MÉTODO GENÉRICO DE ENVÍO HTTP --------------------
    private void sendHttpRequest(Map<String, String> payload, String tipoMensaje) {
        try {
            String jsonPayload = mapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriWhatsappApiSender))
                    .header(AUTHORIZATION, tokenBearerWhatsappApiSender)
                    .header(TYPE, APPLICATION_JSON)
                    .method(POST, HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Envio de {} exitoso: {}", tipoMensaje, response.body());

        } catch (UnknownHostException e) {
            log.error("No se pudo resolver el host {}. Verificar DNS: {}", uriWhatsappApiSender, e.getMessage());
        } catch (IOException e) {
            log.error("Error de conexión al enviar {}: {}", tipoMensaje, e.getMessage());
        } catch (InterruptedException e) {
            log.error("Envío de {} interrumpido: {}", tipoMensaje, e.getMessage());
            Thread.currentThread().interrupt(); // restaurar estado de interrupción
        } catch (Exception e) {
            log.error("Error inesperado al enviar {}: {}", tipoMensaje, e.getMessage());
        }
    }
}
