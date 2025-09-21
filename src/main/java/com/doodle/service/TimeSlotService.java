package com.doodle.service;

import com.doodle.domain.TimeSlot;
import com.doodle.domain.enums.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TimeSlotService {
    
    TimeSlot createTimeSlot(TimeSlot timeSlot);
    
    TimeSlot getTimeSlotById(UUID id);
    
    Page<TimeSlot> getTimeSlotsByCalendarId(UUID calendarId, Pageable pageable);
    
    List<TimeSlot> getAvailableSlots(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime);
    
    Page<TimeSlot> getTimeSlotsByCalendarIdAndTimeRange(
        UUID calendarId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    TimeSlot updateTimeSlot(UUID id, TimeSlot timeSlot);
    
    void deleteTimeSlot(UUID id);
    
    boolean hasOverlappingSlots(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime, UUID excludeSlotId);
    
    List<TimeSlot> getBusySlotsByUsers(List<String> userIds, LocalDateTime startTime, LocalDateTime endTime);
    
    void markSlotAsBusy(UUID slotId);
    
    void markSlotAsAvailable(UUID slotId);
    
    List<Object[]> getSlotCountByDate(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime);
}