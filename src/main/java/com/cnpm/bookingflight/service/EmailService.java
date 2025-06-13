package com.cnpm.bookingflight.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailService {

    final JavaMailSender mailSender;

    public void send(String to, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirm your email");
        message.setText(content);
        mailSender.send(message);
    }

    public void sendWithAttachment(String to, String subject, String content, String attachmentName, byte[] attachment) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content);

        helper.addAttachment(attachmentName, new jakarta.activation.DataSource() {
            @Override
            public java.io.InputStream getInputStream() throws java.io.IOException {
                return new java.io.ByteArrayInputStream(attachment);
            }

            @Override
            public java.io.OutputStream getOutputStream() {
                throw new UnsupportedOperationException("Read-only");
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public String getName() {
                return attachmentName;
            }
        });

        mailSender.send(message);
    }
}