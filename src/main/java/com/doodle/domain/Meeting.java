package com.doodle.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings", indexes = {
    @Index(name = "idx_meeting_time_slot_id", columnList = "time_slot_id"),
    @Index(name = "idx_meeting_created_at", columnList = "created_at")
})
public class Meeting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "meeting_participants", joinColumns = @JoinColumn(name = "meeting_id"))
    @Column(name = "participant_id")
    private List<String> participants = new ArrayList<>();
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Meeting() {}
    
    public Meeting(String title, String description, TimeSlot timeSlot) {
        this.title = title;
        this.description = description;
        this.timeSlot = timeSlot;
    }
    
    // Business methods
    public void addParticipant(String participantId) {
        if (!participants.contains(participantId)) {
            participants.add(participantId);
        }
    }
    
    public void removeParticipant(String participantId) {
        participants.remove(participantId);
    }
    
    public boolean hasParticipant(String participantId) {
        return participants.contains(participantId);
    }
    
    public LocalDateTime getStartTime() {
        return timeSlot != null ? timeSlot.getStartTime() : null;
    }
    
    public LocalDateTime getEndTime() {
        return timeSlot != null ? timeSlot.getEndTime() : null;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    
    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}