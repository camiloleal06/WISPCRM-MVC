package org.wispcrm.services;

import java.io.File;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    JavaMailSender javaMailSender;

    public void sendMail(String from, String to, String subject, String body) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);

        javaMailSender.send(mail);
    }

    public void sendEmailAttachment(String subject, String message, String fromEmailAddress, String toEmailAddresses,
            boolean isHtmlMail, File attachment) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmailAddress);
            helper.setTo(toEmailAddresses);
            helper.setSubject(subject);

            if (isHtmlMail) {
                helper.setText("<html><body>" + message + "</html></body>", true);
            } else {
                helper.setText(message);
            }
            FileSystemResource file = new FileSystemResource(attachment);
            helper.addAttachment(attachment.getName(), file);
            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEMailWithAttach(String to, String attachment) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setFrom(new InternetAddress("sysredcartagena@gmail.com"));
        message.setSubject("subject");
        message.setText("body");
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.addAttachment("attachment-document-name.jpg", new ClassPathResource(attachment));
        javaMailSender.send(message);

    }
}