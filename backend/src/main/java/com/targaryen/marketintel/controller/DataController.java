package com.targaryen.marketintel.controller;

import com.targaryen.marketintel.model.Notification;
import com.targaryen.marketintel.model.Snapshot;
import com.targaryen.marketintel.repository.NotificationRepository;
import com.targaryen.marketintel.repository.SnapshotRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataController {
    
    private final NotificationRepository notificationRepository;
    private final SnapshotRepository snapshotRepository;

    public DataController(NotificationRepository notificationRepository, SnapshotRepository snapshotRepository) {
        this.notificationRepository = notificationRepository;
        this.snapshotRepository = snapshotRepository;
    }

    @GetMapping("/notifications")
    public List<Notification> getNotifications() {
        return notificationRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    @GetMapping("/snapshots")
    public List<Snapshot> getSnapshots() {
        return snapshotRepository.findAll().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
}
