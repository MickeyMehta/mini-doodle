package com.doodle.service.impl;

import com.doodle.domain.Meeting;
import com.doodle.domain.TimeSlot;
import com.doodle.domain.enums.SlotStatus;
import com.doodle.exception.custom.ResourceNotFoundException;
import com.doodle.exception.custom.SlotNotAvailableException;
import com.doodle.repository.MeetingRepository;
import com.doodle.service.MeetingService;
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
public class MeetingServiceImpl implements MeetingService {
    
    private static final Logger logger = LoggerFactory.getLogger(MeetingServiceImpl.class);
    
    private final MeetingRepository meetingRepository;
    private final TimeSlotService timeSlotService;
    
    @Autowired
    public MeetingServiceImpl(MeetingRepository meetingRepository, TimeSlotService timeSlotService) {
        this.meetingRepository = meetingRepository;
        this.timeSlotService = timeSlotService;
    }
    
    @Override
    public Meeting scheduleMeeting(Meeting meeting) {
        logger.debug("Scheduling meeting: {} for slot: {}", meeting.getTitle(), meeting.getTimeSlot().getId());
        
        UUID slotId = meeting.getTimeSlot().getId();
        TimeSlot timeSlot = timeSlotService.getTimeSlotById(slotId);
        
        // Verify slot is available
        if (!timeSlot.isAvailable()) {
            throw new SlotNotAvailableException("Time slot is not available for booking");
        }
        
        // Check if slot already has a meeting
        if (timeSlot.getMeeting() != null) {
            throw new SlotNotAvailableException("Time slot already has a scheduled meeting");
        }
        
        // Set the complete time slot object
        meeting.setTimeSlot(timeSlot);
        
        // Save the meeting
        Meeting savedMeeting = meetingRepository.save(meeting);
        
        // Mark slot as busy
        timeSlotService.markSlotAsBusy(slotId);
        
        logger.info("Scheduled meeting with ID: {} for slot: {}", savedMeeting.getId(), slotId);
        
        return savedMeeting;
    }
    
    @Override
    @Cacheable(value = "meetings", key = "#id")
    @Transactional(readOnly = true)
    public Meeting getMeetingById(UUID id) {
        logger.debug("Fetching meeting with ID: {}", id);
        return meetingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Meeting> getMeetingsByParticipant(String participantId, Pageable pageable) {
        logger.debug("Fetching meetings for participant: {}", participantId);
        return meetingRepository.findByParticipant(participantId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        logger.debug("Fetching meetings between {} and {}", startTime, endTime);
        return meetingRepository.findByTimeRange(startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByParticipantAndTimeRange(
            String participantId, LocalDateTime startTime, LocalDateTime endTime) {
        
        logger.debug("Fetching meetings for participant: {} between {} and {}", 
            participantId, startTime, endTime);
        
        return meetingRepository.findByParticipantAndTimeRange(participantId, startTime, endTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Meeting> getMeetingsByCalendarUserId(String userId, Pageable pageable) {
        logger.debug("Fetching meetings for calendar user: {}", userId);
        return meetingRepository.findByCalendarUserId(userId, pageable);
    }
    
    @Override
    @CacheEvict(value = "meetings", key = "#id")
    public Meeting updateMeeting(UUID id, Meeting meeting) {
        logger.debug("Updating meeting with ID: {}", id);
        
        Meeting existingMeeting = getMeetingById(id);
        
        // Update meeting details (not time slot)
        existingMeeting.setTitle(meeting.getTitle());
        existingMeeting.setDescription(meeting.getDescription());
        existingMeeting.setParticipants(meeting.getParticipants());
        
        Meeting updatedMeeting = meetingRepository.save(existingMeeting);
        logger.info("Updated meeting with ID: {}", updatedMeeting.getId());
        
        return updatedMeeting;
    }
    
    @Override
    @CacheEvict(value = "meetings", key = "#id")
    public void deleteMeeting(UUID id) {
        logger.debug("Deleting meeting with ID: {}", id);
        
        Meeting meeting = getMeetingById(id);
        UUID slotId = meeting.getTimeSlot().getId();
        
        // Delete the meeting
        meetingRepository.delete(meeting);
        
        // Mark slot as available
        timeSlotService.markSlotAsAvailable(slotId);
        
        logger.info("Deleted meeting with ID: {} and freed slot: {}", id, slotId);
    }
    
    @Override
    @CacheEvict(value = "meetings", key = "#meetingId")
    public void addParticipantToMeeting(UUID meetingId, String participantId) {
        logger.debug("Adding participant {} to meeting {}", participantId, meetingId);
        
        Meeting meeting = getMeetingById(meetingId);
        meeting.addParticipant(participantId);
        meetingRepository.save(meeting);
        
        logger.info("Added participant {} to meeting {}", participantId, meetingId);
    }
    
    @Override
    @CacheEvict(value = "meetings", key = "#meetingId")
    public void removeParticipantFromMeeting(UUID meetingId, String participantId) {
        logger.debug("Removing participant {} from meeting {}", participantId, meetingId);
        
        Meeting meeting = getMeetingById(meetingId);
        meeting.removeParticipant(participantId);
        meetingRepository.save(meeting);
        
        logger.info("Removed participant {} from meeting {}", participantId, meetingId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countMeetingsByParticipant(String participantId) {
        return meetingRepository.countByParticipant(participantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Meeting> findMeetingsByTitle(String title) {
        logger.debug("Searching meetings with title containing: {}", title);
        return meetingRepository.findByTitleContaining(title);
    }
}