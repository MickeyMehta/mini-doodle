package com.doodle.service;

import com.doodle.domain.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MeetingService {
    
    Meeting scheduleMeeting(Meeting meeting);
    
    Meeting getMeetingById(UUID id);
    
    Page<Meeting> getMeetingsByParticipant(String participantId, Pageable pageable);
    
    List<Meeting> getMeetingsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    List<Meeting> getMeetingsByParticipantAndTimeRange(
        String participantId, LocalDateTime startTime, LocalDateTime endTime);
    
    Page<Meeting> getMeetingsByCalendarUserId(String userId, Pageable pageable);
    
    Meeting updateMeeting(UUID id, Meeting meeting);
    
    void deleteMeeting(UUID id);
    
    void addParticipantToMeeting(UUID meetingId, String participantId);
    
    void removeParticipantFromMeeting(UUID meetingId, String participantId);
    
    long countMeetingsByParticipant(String participantId);
    
    List<Meeting> findMeetingsByTitle(String title);
}