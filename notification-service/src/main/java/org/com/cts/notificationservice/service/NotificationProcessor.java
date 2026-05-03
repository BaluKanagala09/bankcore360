package org.com.cts.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.com.cts.notificationservice.client.CustomerFeignClient;
import org.com.cts.notificationservice.client.MockCustomerFeignClient;
import org.com.cts.notificationservice.dto.CustomerNotificationProfile;
import org.com.cts.notificationservice.dto.NotificationEvent;
import org.com.cts.notificationservice.entity.Notification;
import org.com.cts.notificationservice.enums.NotificationChannel;
import org.com.cts.notificationservice.enums.NotificationType;
import org.com.cts.notificationservice.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationProcessor {

    private final MockCustomerFeignClient mockClient;
    private final CustomerFeignClient customerClient;
    private final EmailService emailService;
    private final PushService pushService;
    private final NotificationRepository repository;

    @Transactional
    public void process(NotificationEvent event) {
//changing to mocking client
        CustomerNotificationProfile profile =
                mockClient.getProfile(event.getCustomerId());

        boolean isTransaction = event.isTransactionEvent();

        String title = buildTitle(event);
        String message = buildMessage(event);
        // ✅ Push-only events (no DB, no email)
        if (isTransaction) {
            pushService.send(profile.getDeviceTokens(), title, message);
            return;
        }

        // ✅ Store notification
        Notification notification = saveNotification(event,title,message);

        // ✅ Email
        emailService.send(profile.getEmail(), notification);

        // ✅ Push
        pushService.send(profile.getDeviceTokens(), title, message);
    }

    private Notification saveNotification(NotificationEvent event,String title,String message) {

        Notification notification = Notification.builder()
                .customerId(event.getCustomerId())
                .type(NotificationType.valueOf(event.getEventType()))
                .title(title)
                .message(message)
                .channel(NotificationChannel.BOTH)
                .createdAt(LocalDateTime.now())
                .readFlag(false)
                .build();

        return repository.save(notification);
    }

    private String buildTitle(NotificationEvent event) {
        return switch (event.getEventType()) {
            case "CUSTOMER_REGISTERED" -> "Welcome to Our Bank";
            case "KYC_APPROVED" -> "KYC Approved";
            case "KYC_REJECTED" -> "KYC Rejected";
            case "ACCOUNT_CREATED" -> "Account Created";
            case "LOAN_APPROVED" -> "Loan Approved";
            case "LOAN_REJECTED" -> "Loan Rejected";
            case "LOAN_DUE" -> "Loan EMI Due";
            case "PAYMENT_FAILED" -> "Payment Failed";
            case "TRANSACTION_SUCCESS" -> "Transaction Successful";
            case "TRANSACTION_FAILED" -> "Transaction Failed";
            default -> "Notification";
        };
    }

    private String buildMessage(NotificationEvent event) {

        Map<String, Object> data = event.getData();

        return switch (event.getEventType()) {
            case "CUSTOMER_REGISTERED" ->
                    "Your registration was successful. Welcome aboard!";

            case "KYC_APPROVED" ->
                    "Your KYC has been approved successfully.";

            case "KYC_REJECTED" ->
                    "Your KYC was rejected. Please re-upload valid documents.";

            case "ACCOUNT_CREATED" ->
                    "Your bank account has been created successfully.";

            case "LOAN_APPROVED" ->
                    "Comgratulations!! Your loan of ₹" + data.get("loanAmount") + " has been approved. Hope you have great experience with pur bank .";

            case "LOAN_REJECTED" ->
                    "Sorry,we tried to process your request but unfortunately , Your loan application has been rejected.";

            case "LOAN_DUE" ->
                    "EMI of ₹" + data.get("emi") +
                            " is due on " + data.get("dueDate") + ".";

            case "PAYMENT_FAILED" ->
                    "Loan EMI payment failed due to insufficient balance.";

            case "TRANSACTION_SUCCESS" -> "Transaction Successful of amount "+data.get("amount");

            case "TRANSACTION_FAILED" -> "Transaction Failed";

            default ->
                    "You have a new notification.";
        };
    }
}