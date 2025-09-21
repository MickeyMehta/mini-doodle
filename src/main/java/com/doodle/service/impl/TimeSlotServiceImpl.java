package com.doodle.service.impl;

import com.doodle.domain.TimeSlot;
import com.doodle.domain.enums.SlotStatus;
import com.doodle.exception.custom.ResourceNotFoundException;
import com.doodle.exception.custom.TimeConflictException;
import com.doodle.repository.TimeSlotRepository;
import com.doodle.service.CalendarService;
import com.doodle.service.TimeSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeSlotServiceImpl.class);
    
    private final TimeSlotRepository timeSlotRepository;
    private final CalendarService calendarService;
    
    @Autowired
    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepository, CalendarService calendarService) {
        this.timeSlotRepository = timeSlotRepository;
        this.calendarService = calendarService;
    }
    
    @Override
    public TimeSlot createTimeSlot(TimeSlot timeSlot) {
        logger.debug("Creating time slot for calendar: {}", timeSlot.getCalendar().getId());
        
        validateTimeSlot(timeSlot);
        
        // Check for overlapping slots
        if (hasOverlappingSlots(timeSlot.getCalendar().getId(), 
                timeSlot.getStartTime(), timeSlot.getEndTime(), null)) {
            throw new TimeConflictException("Time slot overlaps with existing slot");
        }
        
        TimeSlot savedSlot = timeSlotRepository.save(timeSlot);
        logger.info("Created time slot with ID: {}", savedSlot.getId());
        
        return savedSlot;
    }
    
    @Override
    @Cacheable(value = "timeSlots", key = "#id")
    @Transactional(readOnly = true)
    public TimeSlot getTimeSlotById(UUID id) {
        logger.debug("Fetching time slot with ID: {}", id);
        return timeSlotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Time slot not found with ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TimeSlot> getTimeSlotsByCalendarId(UUID calendarId, Pageable pageable) {
        logger.debug("Fetching time slots for calendar: {}", calendarId);
        // Verify calendar exists
        calendarService.getCalendarById(calendarId);
        return timeSlotRepository.findAll(pageable); // This should be filtered by calendar
    }
    
    @Override
    @Cacheable(value = "availableSlots", key = "#calendarId + '_' + #startTime + '_' + #endTime")
    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableSlots(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("Fetching available slots for calendar: {} between {} and {}", 
            calendarId, startTime, endTime);
        
        return timeSlotRepository.findAvailableSlots(calendarId, SlotStatus.AVAILABLE, startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TimeSlot> getTimeSlotsByCalendarIdAndTimeRange(
            UUID calendarId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        
        logger.debug("Fetching time slots for calendar: {} in time range", calendarId);
        return timeSlotRepository.findByCalendarIdAndTimeRange(calendarId, startTime, endTime, pageable);
    }
    
    @Override
    @CacheEvict(value = {"timeSlots", "availableSlots"}, key = "#id")
    public TimeSlot updateTimeSlot(UUID id, TimeSlot timeSlot) {
        logger.debug("Updating time slot with ID: {}", id);
        
        TimeSlot existingSlot = getTimeSlotById(id);
        validateTimeSlot(timeSlot);
        
        // Check for overlapping slots (excluding current slot)
        if (hasOverlappingSlots(existingSlot.getCalendar().getId(), 
                timeSlot.getStartTime(), timeSlot.getEndTime(), id)) {
            throw new TimeConflictException("Updated time slot would overlap with existing slot");
        }
        
        existingSlot.setStartTime(timeSlot.getStartTime());
        existingSlot.setEndTime(timeSlot.getEndTime());
        existingSlot.setStatus(timeSlot.getStatus());
        
        TimeSlot updatedSlot = timeSlotRepository.save(existingSlot);
        logger.info("Updated time slot with ID: {}", updatedSlot.getId());
        
        return updatedSlot;
    }
    
    @Override
    @CacheEvict(value = {"timeSlots", "availableSlots"}, key = "#id")
    public void deleteTimeSlot(UUID id) {
        logger.debug("Deleting time slot with ID: {}", id);
        
        TimeSlot timeSlot = getTimeSlotById(id);
        
        // Check if slot has a meeting
        if (timeSlot.getMeeting() != null) {
            throw new IllegalStateException("Cannot delete time slot with scheduled meeting");
        }
        
        timeSlotRepository.delete(timeSlot);
        logger.info("Deleted time slot with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingSlots(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime, UUID excludeSlotId) {
        return timeSlotRepository.existsOverlappingSlot(calendarId, excludeSlotId, startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getBusySlotsByUsers(List<String> userIds, LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("Fetching busy slots for users: {} between {} and {}", userIds, startTime, endTime);
        return timeSlotRepository.findBusySlotsByUsers(userIds, startTime, endTime);
    }
    
    @Override
    @CacheEvict(value = {"timeSlots", "availableSlots"}, key = "#slotId")
    public void markSlotAsBusy(UUID slotId) {
        logger.debug("Marking slot as busy: {}", slotId);
        TimeSlot slot = getTimeSlotById(slotId);
        slot.setStatus(SlotStatus.BUSY);
        timeSlotRepository.save(slot);
    }
    
    @Override
    @CacheEvict(value = {"timeSlots", "availableSlots"}, key = "#slotId")
    public void markSlotAsAvailable(UUID slotId) {
        logger.debug("Marking slot as available: {}", slotId);
        TimeSlot slot = getTimeSlotById(slotId);
        slot.setStatus(SlotStatus.AVAILABLE);
        timeSlotRepository.save(slot);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getSlotCountByDate(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime) {
        return timeSlotRepository.getSlotCountByDate(calendarId, startTime, endTime);
    }
    
    private void validateTimeSlot(TimeSlot timeSlot) {
        if (timeSlot.getStartTime().isAfter(timeSlot.getEndTime()) || 
            timeSlot.getStartTime().isEqual(timeSlot.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (timeSlot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot create time slot in the past");
        }
        
        long durationMinutes = timeSlot.getDurationMinutes();
        if (durationMinutes < 15 || durationMinutes > 480) {
            throw new IllegalArgumentException("Time slot duration must be between 15 minutes and 8 hours");
        }
    }
}