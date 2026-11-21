package org.wispcrm.services;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.wispcrm.modelo.facturas.Factura;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async("threadPoolTaskExecutor")
    public void sendMail(Factura factura) {
        String html = buildHtml(factura);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
        try {
            helper.setSubject("Llegó tu factura " + factura.getCliente().getNombres());
            helper.setText(html, true);
            helper.setTo(factura.getCliente().getEmail());
            javaMailSender.send(mimeMessage);
            log.info("Email enviado a {}", factura.getCliente().getEmail());
        } catch (MessagingException e) {
            log.error("Error enviando email a {}: {}", factura.getCliente().getEmail(), e.getMessage());
        }
    }

    private String buildHtml(Factura factura) {
        return String.format("""
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px">
              <h2 style="color:#4a90e2">Factura #%d</h2>
              <p>Estimado(a) <strong>%s</strong>,</p>
              <p>Se ha generado su factura por el servicio de internet.</p>
              <table style="width:100%%;border-collapse:collapse;margin:20px 0">
                <tr style="background:#f5f5f5">
                  <td style="padding:10px;border:1px solid #ddd"><strong>Valor</strong></td>
                  <td style="padding:10px;border:1px solid #ddd">$%,.0f</td>
                </tr>
                <tr>
                  <td style="padding:10px;border:1px solid #ddd"><strong>Fecha de pago</strong></td>
                  <td style="padding:10px;border:1px solid #ddd">%s</td>
                </tr>
              </table>
              <p style="color:#888;font-size:12px">SYSRED &mdash; Servicio de Internet</p>
            </body></html>
            """,
            factura.getId(),
            factura.getCliente().getNombres() + " " + factura.getCliente().getApellidos(),
            factura.getValor(),
            factura.getFechapago() != null
                ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(factura.getFechapago())
                : "N/A"
        );
    }
}
