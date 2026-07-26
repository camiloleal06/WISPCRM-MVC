package org.wispcrm.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class FcmService {

    @Value("${fcm.credentials.path:/home/camiloleal/wispcrm/firebase-credentials.json}")
    private String credentialsPath;

    @Value("${fcm.tokens:}")
    private List<String> tokens;

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(credentialsPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[FCM] Firebase inicializado correctamente");
            }
        } catch (IOException e) {
            log.error("[FCM] Error inicializando Firebase: {}", e.getMessage());
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendPush(String title, String body) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("[FCM] No hay tokens configurados");
            return;
        }
        tokens.forEach(token -> {
            try {
                Message message = Message.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .setToken(token)
                        .build();
                FirebaseMessaging.getInstance().send(message);
                log.info("[FCM] Push enviado: {}", title);
            } catch (Exception e) {
                log.error("[FCM] Error enviando push a token {}: {}", token, e.getMessage());
            }
        });
    }
}
