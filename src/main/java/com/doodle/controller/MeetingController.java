package com.doodle.controller;

import com.doodle.domain.Meeting;
import com.doodle.domain.TimeSlot;
import com.doodle.service.MeetingService;
import com.doodle.service.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/meetings")
@Tag(name = "Meeting Management", description = "APIs for scheduling and managing meetings")
public class MeetingController {
    
    private static final Logger logger = LoggerFactory.getLogger(MeetingController.class);
    
    private final MeetingService meetingService;
    private final TimeSlotService timeSlotService;
    
    @Autowired
    public MeetingController(MeetingService meetingService, TimeSlotService timeSlotService) {
        this.meetingService = meetingService;
        this.timeSlotService = timeSlotService;
    }
    
    @PostMapping
    @Operation(summary = "Schedule a new meeting", description = "Schedules a meeting in an available time slot")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Meeting scheduled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Time slot not available")
    })
    public ResponseEntity<Meeting> scheduleMeeting(@Valid @RequestBody ScheduleMeetingRequest request) {
        logger.info("Scheduling meeting: {} for slot: {}", request.getTitle(), request.getSlotId());
        
        TimeSlot timeSlot = timeSlotService.getTimeSlotById(request.getSlotId());
        
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setTimeSlot(timeSlot);
        meeting.setParticipants(request.getParticipants());
        
        Meeting scheduledMeeting = meetingService.scheduleMeeting(meeting);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduledMeeting);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get meeting by ID", description = "Retrieves a meeting by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meeting found"),
        @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<Meeting> getMeeting(
            @Parameter(description = "Meeting ID") @PathVariable UUID id) {
        
        logger.debug("Fetching meeting with ID: {}", id);
        Meeting meeting = meetingService.getMeetingById(id);
        
        return ResponseEntity.ok(meeting);
    }
    
    @GetMapping
    @Operation(summary = "Get meetings", description = "Retrieves meetings with optional filtering")
    public ResponseEntity<List<Meeting>> getMeetings(
            @Parameter(description = "Participant ID filter") 
            @RequestParam(required = false) String participantId,
            @Parameter(description = "Start date for filtering") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Title search") 
            @RequestParam(required = false) String title) {
        
        logger.debug("Fetching meetings with filters - participant: {}, dates: {} to {}", 
            participantId, startDate, endDate);
        
        if (title != null && !title.trim().isEmpty()) {
            return ResponseEntity.ok(meetingService.findMeetingsByTitle(title.trim()));
        }
        
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            if (participantId != null) {
                return ResponseEntity.ok(meetingService.getMeetingsByParticipantAndTimeRange(
                    participantId, startDateTime, endDateTime));
            } else {
                return ResponseEntity.ok(meetingService.getMeetingsByTimeRange(startDateTime, endDateTime));
            }
        }
        
        // If no time range specified, return empty list or throw error
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/participant/{participantId}")
    @Operation(summary = "Get meetings by participant", description = "Retrieves all meetings for a participant")
    public ResponseEntity<Page<Meeting>> getMeetingsByParticipant(
            @Parameter(description = "Participant ID") @PathVariable String participantId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Fetching meetings for participant: {}", participantId);
        Page<Meeting> meetings = meetingService.getMeetingsByParticipant(participantId, pageable);
        
        return ResponseEntity.ok(meetings);
    }
    
    @GetMapping("/calendar-user/{userId}")
    @Operation(summary = "Get meetings by calendar user", description = "Retrieves meetings in calendars owned by user")
    public ResponseEntity<Page<Meeting>> getMeetingsByCalendarUser(
            @Parameter(description = "Calendar owner user ID") @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Fetching meetings for calendar user: {}", userId);
        Page<Meeting> meetings = meetingService.getMeetingsByCalendarUserId(userId, pageable);
        
        return ResponseEntity.ok(meetings);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update meeting", description = "Updates meeting details (not time slot)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Meeting updated successfully"),
        @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<Meeting> updateMeeting(
            @Parameter(description = "Meeting ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateMeetingRequest request) {
        
        logger.info("Updating meeting with ID: {}", id);
        
        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle());
        meeting.setDescription(request.getDescription());
        meeting.setParticipants(request.getParticipants());
        
        Meeting updatedMeeting = meetingService.updateMeeting(id, meeting);
        
        return ResponseEntity.ok(updatedMeeting);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel meeting", description = "Cancels a meeting and frees the time slot")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Meeting cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    public ResponseEntity<Void> cancelMeeting(
            @Parameter(description = "Meeting ID") @PathVariable UUID id) {
        
        logger.info("Cancelling meeting with ID: {}", id);
        meetingService.deleteMeeting(id);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/participants")
    @Operation(summary = "Add participant", description = "Adds a participant to the meeting")
    public ResponseEntity<Void> addParticipant(
            @Parameter(description = "Meeting ID") @PathVariable UUID id,
            @RequestBody AddParticipantRequest request) {
        
        logger.info("Adding participant {} to meeting {}", request.getParticipantId(), id);
        meetingService.addParticipantToMeeting(id, request.getParticipantId());
        
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/participants/{participantId}")
    @Operation(summary = "Remove participant", description = "Removes a participant from the meeting")
    public ResponseEntity<Void> removeParticipant(
            @Parameter(description = "Meeting ID") @PathVariable UUID id,
            @Parameter(description = "Participant ID") @PathVariable String participantId) {
        
        logger.info("Removing participant {} from meeting {}", participantId, id);
        meetingService.removeParticipantFromMeeting(id, participantId);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/participant/{participantId}/count")
    @Operation(summary = "Get meeting count", description = "Returns the number of meetings for a participant")
    public ResponseEntity<Long> getMeetingCount(
            @Parameter(description = "Participant ID") @PathVariable String participantId) {
        
        long count = meetingService.countMeetingsByParticipant(participantId);
        return ResponseEntity.ok(count);
    }
    
    // DTOs
    public static class ScheduleMeetingRequest {
        @jakarta.validation.constraints.NotNull
        private UUID slotId;
        
        @jakarta.validation.constraints.NotBlank
        private String title;
        
        private String description;
        
        @jakarta.validation.constraints.NotEmpty
        private List<String> participants;
        
        // Getters and setters
        public UUID getSlotId() { return slotId; }
        public void setSlotId(UUID slotId) { this.slotId = slotId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getParticipants() { return participants; }
        public void setParticipants(List<String> participants) { this.participants = participants; }
    }
    
    public static class UpdateMeetingRequest {
        @jakarta.validation.constraints.NotBlank
        private String title;
        
        private String description;
        
        @jakarta.validation.constraints.NotEmpty
        private List<String> participants;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getParticipants() { return participants; }
        public void setParticipants(List<String> participants) { this.participants = participants; }
    }
    
    public static class AddParticipantRequest {
        @jakarta.validation.constraints.NotBlank
        private String participantId;
        
        // Getters and setters
        public String getParticipantId() { return participantId; }
        public void setParticipantId(String participantId) { this.participantId = participantId; }
    }
}