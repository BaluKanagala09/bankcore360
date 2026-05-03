package org.com.cts.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.com.cts.notificationservice.entity.Notification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void send(String to, Notification notification) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(notification.getTitle());
        message.setText(notification.getMessage());

        mailSender.send(message);
    }
}
