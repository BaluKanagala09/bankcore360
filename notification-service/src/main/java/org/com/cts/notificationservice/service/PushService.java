package org.com.cts.notificationservice.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PushService {

    public void send(
            List<String> tokens,
            String title,
            String message
    ) {
        if (tokens == null || tokens.isEmpty()) return;

        // Mock implementation (replace with FCM later)
        tokens.forEach(token ->
                System.out.println(
                        "Push to " + token + " | " + title + " : " + message
                )
        );
    }
}

