package com.doodle.domain;

import com.doodle.domain.enums.SlotStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_slots", indexes = {
    @Index(name = "idx_time_slot_calendar_id", columnList = "calendar_id"),
    @Index(name = "idx_time_slot_start_time", columnList = "start_time"),
    @Index(name = "idx_time_slot_end_time", columnList = "end_time"),
    @Index(name = "idx_time_slot_status", columnList = "status"),
    @Index(name = "idx_time_slot_calendar_time", columnList = "calendar_id, start_time, end_time")
})
public class TimeSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private Calendar calendar;
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;
    
    @OneToOne(mappedBy = "timeSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Meeting meeting;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public TimeSlot() {}
    
    public TimeSlot(Calendar calendar, LocalDateTime startTime, LocalDateTime endTime, SlotStatus status) {
        this.calendar = calendar;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    // Business methods
    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }
    
    public boolean overlaps(LocalDateTime start, LocalDateTime end) {
        return startTime.isBefore(end) && endTime.isAfter(start);
    }
    
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Calendar getCalendar() { return calendar; }
    public void setCalendar(Calendar calendar) { this.calendar = calendar; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
    
    public Meeting getMeeting() { return meeting; }
    public void setMeeting(Meeting meeting) { this.meeting = meeting; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}