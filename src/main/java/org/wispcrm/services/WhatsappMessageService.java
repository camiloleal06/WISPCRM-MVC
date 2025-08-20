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
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.google.api.client.http.HttpMethods.POST;

@Slf4j

@Service
public class WhatsappMessageService {

    public static final String TOKEN = "token";
    public static final String TO = "to";
    public static final String BODY = "body";
    public static final String AREA_CODE = "+57";
    public static final String CONTENT_TYPE = "content-type";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    @Value("${message.urlChat}")
    private String urlChat;
    @Value("${message.urlDocument}")
    public String urlDocument;
    @Value("${message.msgToken}")
    private String msgToken;
    @Value("${message.uriWhatsappApiSender}")
    private String uriWhatsappApiSender;
    @Value("${message.tokenBearerWhatsappApiSender}")
    private String tokenBearerWhatsappApiSender;

    public WhatsappMessageService() {
        // TODO document why this constructor is empty
    }

      public void sendSimpleMessage(String clientNumber, String msg) throws IOException {
        //Enviando WhatsApp Message
        OkHttpClient client = new OkHttpClient();
         RequestBody body = new FormBody.Builder()
                .add(TOKEN, msgToken)
                .add(TO, AREA_CODE +clientNumber)
                .add(BODY, msg)
                .build();

        Request request = new Request.Builder()
                .url(urlChat)
                .post(body)
                .addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .build();
        Response response = client.newCall(request).execute();
    logResponse(response);
    }

    public void sendDocumentAndMessage(String clientNumber, String msg, String fileName, String pathDocument) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(TOKEN, msgToken)
                .add(TO, AREA_CODE +clientNumber)
                .add("filename", fileName)
                .add("document", pathDocument)
                .add("caption", msg)
                .build();
        Request request = new Request.Builder()
                .url(urlDocument)
                .post(body)
                .addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .build();

        Response response = client.newCall(request).execute();
       logResponse(response);
    }

    public void sendSimpleMessageToGroup(String groupId, String msg) throws IOException {
        //Enviando WhatsApp Message
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add(TOKEN, msgToken)
                .add(TO, groupId)
                .add(BODY, msg)
                .build();

        Request request = new Request.Builder()
                .url(urlChat)
                .post(body)
                .addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .build();
        Response response = client.newCall(request).execute();
        logResponse(response);
    }

    public void sendDocumentAndMessageWasenderapi(String clientNumber, String msg,  String pathDocument)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE+clientNumber);
        payload.put("text", msg);
        payload.put("documentUrl", pathDocument);
        String jsonPayload = mapper.writeValueAsString(payload);

        HttpRequest request = getBuilder(jsonPayload)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        logResponse(response.body());
    }

    public void sendSimpleMessageWasenderapi(String clientNumber, String msg)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> payload = new HashMap<>();
        payload.put("to", AREA_CODE+clientNumber);
        payload.put("text", msg);
        String jsonPayload = mapper.writeValueAsString(payload);

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

    private void logResponse(Response response) throws IOException {
       log.info("Envio de mensaje de whatsApp {}", response.body().string());
    }

    private void logResponse(String response) throws IOException {
        log.info("Envio de mensaje de whatsApp {}", response);
    }
}