package com.doodle.controller;

import com.doodle.domain.TimeSlot;
import com.doodle.domain.enums.SlotStatus;
import com.doodle.service.CalendarService;
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
@RequestMapping("/api/v1/calendars/{calendarId}/slots")
@Tag(name = "Time Slot Management", description = "APIs for managing time slots in calendars")
public class TimeSlotController {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeSlotController.class);
    
    private final TimeSlotService timeSlotService;
    private final CalendarService calendarService;
    
    @Autowired
    public TimeSlotController(TimeSlotService timeSlotService, CalendarService calendarService) {
        this.timeSlotService = timeSlotService;
        this.calendarService = calendarService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new time slot", description = "Creates a new time slot in the specified calendar")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Time slot created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Time slot conflicts with existing slot")
    })
    public ResponseEntity<TimeSlot> createTimeSlot(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Valid @RequestBody CreateTimeSlotRequest request) {
        
        logger.info("Creating time slot for calendar: {}", calendarId);
        
        // Verify calendar exists and get it
        var calendar = calendarService.getCalendarById(calendarId);
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setCalendar(calendar);
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setStatus(request.getStatus() != null ? request.getStatus() : SlotStatus.AVAILABLE);
        
        TimeSlot createdSlot = timeSlotService.createTimeSlot(timeSlot);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSlot);
    }
    
    @GetMapping("/{slotId}")
    @Operation(summary = "Get time slot by ID", description = "Retrieves a specific time slot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Time slot found"),
        @ApiResponse(responseCode = "404", description = "Time slot not found")
    })
    public ResponseEntity<TimeSlot> getTimeSlot(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        
        logger.debug("Fetching time slot: {} from calendar: {}", slotId, calendarId);
        TimeSlot timeSlot = timeSlotService.getTimeSlotById(slotId);
        
        return ResponseEntity.ok(timeSlot);
    }
    
    @GetMapping
    @Operation(summary = "Get time slots", description = "Retrieves time slots for the calendar with optional filtering")
    public ResponseEntity<Page<TimeSlot>> getTimeSlots(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Start date for filtering") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Slot status filter") 
            @RequestParam(required = false) SlotStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Fetching time slots for calendar: {}", calendarId);
        
        Page<TimeSlot> slots;
        
        if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            slots = timeSlotService.getTimeSlotsByCalendarIdAndTimeRange(
                calendarId, startDateTime, endDateTime, pageable);
        } else {
            slots = timeSlotService.getTimeSlotsByCalendarId(calendarId, pageable);
        }
        
        return ResponseEntity.ok(slots);
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available time slots", description = "Retrieves all available time slots in a date range")
    public ResponseEntity<List<TimeSlot>> getAvailableSlots(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("Fetching available slots for calendar: {} between {} and {}", 
            calendarId, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<TimeSlot> availableSlots = timeSlotService.getAvailableSlots(
            calendarId, startDateTime, endDateTime);
        
        return ResponseEntity.ok(availableSlots);
    }
    
    @PutMapping("/{slotId}")
    @Operation(summary = "Update time slot", description = "Updates an existing time slot")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Time slot updated successfully"),
        @ApiResponse(responseCode = "404", description = "Time slot not found"),
        @ApiResponse(responseCode = "409", description = "Updated slot conflicts with existing slot")
    })
    public ResponseEntity<TimeSlot> updateTimeSlot(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Valid @RequestBody UpdateTimeSlotRequest request) {
        
        logger.info("Updating time slot: {} in calendar: {}", slotId, calendarId);
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(request.getStartTime());
        timeSlot.setEndTime(request.getEndTime());
        timeSlot.setStatus(request.getStatus());
        
        TimeSlot updatedSlot = timeSlotService.updateTimeSlot(slotId, timeSlot);
        
        return ResponseEntity.ok(updatedSlot);
    }
    
    @DeleteMapping("/{slotId}")
    @Operation(summary = "Delete time slot", description = "Deletes a time slot (only if no meeting is scheduled)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Time slot deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Time slot not found"),
        @ApiResponse(responseCode = "409", description = "Time slot has scheduled meeting")
    })
    public ResponseEntity<Void> deleteTimeSlot(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Slot ID") @PathVariable UUID slotId) {
        
        logger.info("Deleting time slot: {} from calendar: {}", slotId, calendarId);
        timeSlotService.deleteTimeSlot(slotId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{slotId}/status")
    @Operation(summary = "Update slot status", description = "Updates the status of a time slot")
    public ResponseEntity<Void> updateSlotStatus(
            @Parameter(description = "Calendar ID") @PathVariable UUID calendarId,
            @Parameter(description = "Slot ID") @PathVariable UUID slotId,
            @Parameter(description = "New status") @RequestParam SlotStatus status) {
        
        logger.info("Updating status of slot: {} to: {}", slotId, status);
        
        if (status == SlotStatus.AVAILABLE) {
            timeSlotService.markSlotAsAvailable(slotId);
        } else if (status == SlotStatus.BUSY) {
            timeSlotService.markSlotAsBusy(slotId);
        }
        
        return ResponseEntity.ok().build();
    }
    
    // DTOs
    public static class CreateTimeSlotRequest {
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Future
        private LocalDateTime startTime;
        
        @jakarta.validation.constraints.NotNull
        private LocalDateTime endTime;
        
        private SlotStatus status;
        
        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public SlotStatus getStatus() { return status; }
        public void setStatus(SlotStatus status) { this.status = status; }
    }
    
    public static class UpdateTimeSlotRequest {
        @jakarta.validation.constraints.NotNull
        private LocalDateTime startTime;
        
        @jakarta.validation.constraints.NotNull
        private LocalDateTime endTime;
        
        @jakarta.validation.constraints.NotNull
        private SlotStatus status;
        
        // Getters and setters
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public SlotStatus getStatus() { return status; }
        public void setStatus(SlotStatus status) { this.status = status; }
    }
}