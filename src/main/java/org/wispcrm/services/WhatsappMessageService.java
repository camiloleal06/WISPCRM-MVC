package org.wispcrm.services;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.google.api.client.http.HttpMethods.POST;

@Slf4j

@Service
public class WhatsappMessageService {

    public static final String TO = "to";
    public static final String BODY = "body";
    public static final String AREA_CODE = "+57";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ENVIO_DE_MENSAJE_DE_WHATS_APP = "Envio de mensaje de whatsApp {}";

   // @Value("${message.uriWhatsappApiSender}")
    private String tokenBearerWhatsappApiSender = "Bearer 53d2b099e4caa7a4aac7941bbe0b843918d68ea86ec53127bb676608d38b2a20";
   // @Value("${message.tokenBearerWhatsappApiSender}")
    private String uriWhatsappApiSender = "https://www.wasenderapi.com/api/send-message";

    public WhatsappMessageService() {
        // TODO document why this constructor is empty
    }

    public void sendDocumentAndMessageWasenderapi(String clientNumber, String msg,  String pathDocument,String fileName)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE+clientNumber);
        payload.put("text", msg);
        payload.put("documentUrl", pathDocument);
        payload.put("fileName", fileName);
        String jsonPayload = mapper.writeValueAsString(payload);

        HttpRequest request = getBuilder(jsonPayload)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logResponse(response.body());
    }


    @Async("threadPoolTaskExecutor")
    public void sendImageAndMessageWasenderapi(String clientNumber, String msg,  String imageUrl)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE+clientNumber);
        payload.put("text", msg);
        payload.put("imageUrl", imageUrl);
        String jsonPayload = mapper.writeValueAsString(payload);

        HttpRequest request = getBuilder(jsonPayload)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logResponse(response.body());
    }

    public void sendSimpleMessageWasenderapi(String clientNumber, String msg)
            throws IOException, InterruptedException {


        log.info("Datos"+" tOKEN :"+tokenBearerWhatsappApiSender+ " URI: "+uriWhatsappApiSender);

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE+clientNumber);
        payload.put("text", msg);
        String jsonPayload = mapper.writeValueAsString(payload);
        log.info("Datos"+jsonPayload);

        HttpRequest.Builder requestBuilder = getBuilder(jsonPayload);

        HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        logResponse(response.body());
    }

    public void sendSimpleMessageToGroupWasApiSender(String groupId, String msg)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to",groupId );
        payload.put("text", msg);
        String jsonPayload = mapper.writeValueAsString(payload);

        HttpRequest.Builder requestBuilder = getBuilder(jsonPayload);

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logResponse(response.body());
    }

    private HttpRequest.Builder getBuilder(String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(uriWhatsappApiSender))
                .header(AUTHORIZATION, tokenBearerWhatsappApiSender)
                .header(TYPE, APPLICATION_JSON)
                .method(POST , HttpRequest.BodyPublishers
                        .ofString(jsonPayload));
    }

    private void logResponse(String response) throws IOException {
        log.info(ENVIO_DE_MENSAJE_DE_WHATS_APP, response);
    }
}