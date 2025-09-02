package org.wispcrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.wispcrm.services.WhatsappMessageService;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class ImageUploadController {

    public static final String STATUS = "status";
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
            @RequestParam String message, @RequestParam("image") MultipartFile image,
            Model model) {
        try {
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                imageUrl = getImageUrl(image, model);
            }
            else {
                imageUrl = publicUrl;
            }

        String[] phoneList = phones.split(",");
        sendBulkMessages(List.of(phoneList), message, imageUrl);
        model.addAttribute(STATUS, "Mensaje enviado correctamente a " + phoneList.length + " números.");

         } catch (Exception e) {
            model.addAttribute(STATUS, "Error al enviar el mensaje: " + e.getMessage());
        }
        return "sendMessage";
    }

    private String getImageUrl(MultipartFile image, Model model) {
        String imageUrl;
        try {
            File savedFile = new File("/var/www/html/uploads/" + image.getOriginalFilename());
            image.transferTo(savedFile);
            imageUrl = "http://sysredcartagena.duckdns.org/uploads/" + image.getOriginalFilename();
        }
        catch (IOException e) {
            imageUrl = publicUrl;
            model.addAttribute(STATUS, "Error al subir la imagen: se enviara imagen por defecto " + e.getMessage());
        }
        return imageUrl;
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
