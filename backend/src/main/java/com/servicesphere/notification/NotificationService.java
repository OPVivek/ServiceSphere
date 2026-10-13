package com.servicesphere.notification;

import com.servicesphere.common.PagedResponse;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.notification.dto.NotificationDto;
import com.servicesphere.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(User recipient, String title, String message, NotificationType type, Long referenceId, String referenceType) {
        if (recipient == null) return null;
        Notification notif = new Notification(recipient, title, message, type, referenceId, referenceType);
        log.info("Notification queued for user {}: {}", recipient.getEmail(), title);
        return notificationRepository.save(notif);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto> getUserNotifications(Long userId, int page, int size) {
        Page<Notification> notifPage = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return PagedResponse.of(notifPage.map(NotificationDto::fromEntity));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (notif.getRecipient().getId().equals(userId)) {
            notif.setRead(true);
            notificationRepository.save(notif);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
