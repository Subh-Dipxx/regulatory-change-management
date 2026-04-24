package com.internship.tool.scheduler;

import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import com.internship.tool.service.NotificationEmailService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueNotificationScheduler {

    private static final List<String> NON_OVERDUE_STATUSES = List.of("IMPLEMENTED", "CLOSED");

    private final RegulatoryChangeRepository regulatoryChangeRepository;
    private final NotificationEmailService notificationEmailService;

    public OverdueNotificationScheduler(
            RegulatoryChangeRepository regulatoryChangeRepository,
            NotificationEmailService notificationEmailService
    ) {
        this.regulatoryChangeRepository = regulatoryChangeRepository;
        this.notificationEmailService = notificationEmailService;
    }

    @Scheduled(cron = "${notification.overdue-cron}")
    public void notifyOverdueChanges() {
        List<RegulatoryChange> overdueChanges = regulatoryChangeRepository
                .findByEffectiveDateBeforeAndStatusNotIn(LocalDate.now(), NON_OVERDUE_STATUSES);

        notificationEmailService.sendOverdueNotification(overdueChanges);
    }
}
