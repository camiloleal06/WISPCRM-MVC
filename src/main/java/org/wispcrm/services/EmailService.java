package org.wispcrm.services;

import javax.mail.MessagingException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.wispcrm.modelo.facturas.Factura;

@Service
public class EmailService {

    private final TemplateEngine templateEngine;

    private final JavaMailSender javaMailSender;

    public EmailService(TemplateEngine templateEngine, JavaMailSender javaMailSender) {
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
    }

    @Async("threadPoolTaskExecutor")
    public void sendMail(Factura factura) {
        Context context = new Context();
        context.setVariable("factura", factura);
        String process = templateEngine.process("email", context);
        javax.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject("Llegó tu factura " + factura.getCliente().getNombres());
            helper.setText(process, true);
            helper.setTo(factura.getCliente().getEmail());
            javaMailSender.send(mimeMessage);
            System.out.println("Enviando email ...");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendMailNotification(Factura factura) {
        Context context = new Context();
        context.setVariable("factura", factura);
        String process = templateEngine.process("pago", context);
        javax.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject("Notificación pago cliente " + factura.getCliente().getNombres());
            helper.setText(process, true);
            helper.setTo("camilojesus1@gmail.com");
            javaMailSender.send(mimeMessage);
            System.out.println("Enviando email ...");
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        // return "email";
    }
}