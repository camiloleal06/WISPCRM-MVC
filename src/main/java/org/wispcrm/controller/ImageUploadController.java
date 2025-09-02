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

@Controller
public class ImageUploadController {

    private static final String UPLOAD_DIR = "/var/www/html/"; // Ruta absoluta en tu servidor
    private static final String FIXED_NAME = "image";      // Nombre fijo de la imagen

    private final WhatsappMessageService whatsappMessageService;

    private String extension = "";

    public ImageUploadController(WhatsappMessageService whatsappMessageService) {
        this.whatsappMessageService = whatsappMessageService;
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload"; // nombre de la vista Thymeleaf (upload.html)
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model)
            throws InterruptedException {
        if (!file.isEmpty()) {
            try {
               String originalName = file.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                Path path = Paths.get(UPLOAD_DIR + FIXED_NAME + extension);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                model.addAttribute("message", "Imagen subida y enviada por WhatsApp con éxito");
               // model.addAttribute("imageUrl", publicUrl);

            } catch (IOException e) {
                model.addAttribute("message", "Error: " + e.getMessage());
            }
        }
        return "upload";
    }

    @PostMapping("/send-message")
    public String handleFileUpload(Model model)
            throws InterruptedException, IOException {
        String publicUrl = "http://sysredcartagena.duckdns.org/" + FIXED_NAME + extension;
        whatsappMessageService.sendImageAndMessageWasenderapi("3225996394", "📷 Holis",publicUrl);
        return "upload";
    }

}
