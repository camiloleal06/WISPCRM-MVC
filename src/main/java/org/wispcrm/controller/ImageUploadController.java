package org.wispcrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.wispcrm.services.WhatsappMessageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class ImageUploadController {

    private final WhatsappMessageService whatsappMessageService;

    public ImageUploadController(WhatsappMessageService whatsappMessageService) {
        this.whatsappMessageService = whatsappMessageService;
    }
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    String publicUrl = "http://sysredcartagena.duckdns.org/image.jpg";

    @GetMapping("/send")
    public String showForm() {
        return "sendMessage";
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String phones,
            @RequestParam String message,
            Model model) throws IOException, InterruptedException {
        String[] phoneList = phones.split(",");
        sendBulkMessages(List.of(phoneList), message, publicUrl);
        model.addAttribute("status", "Mensaje enviado correctamente a " + phoneList.length + " números.");
        return "sendMessage";
    }



    public void sendBulkMessages(List<String> phoneList, String message, String publicUrl) {
        if (phoneList.isEmpty()) return;

        Iterator<String> iterator = phoneList.iterator();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (iterator.hasNext()) {
                    String phone = iterator.next().trim();
                    try {
                       whatsappMessageService.sendImageAndMessageWasenderapi(phone, message, publicUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                              if (iterator.hasNext()) {
                        scheduler.schedule(this, 10, TimeUnit.SECONDS);
                    }
                }
            }
        };
        // arranca la primera tarea inmediatamente
        scheduler.execute(task);
    }
}
