package com.doodle.controller;

import com.doodle.domain.Calendar;
import com.doodle.service.CalendarService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendars")
@Tag(name = "Calendar Management", description = "APIs for managing user calendars")
public class CalendarController {
    
    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);
    
    private final CalendarService calendarService;
    
    @Autowired
    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }
    
    @PostMapping
    @Operation(summary = "Create a new calendar", description = "Creates a new calendar for a user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Calendar created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Calendar with name already exists")
    })
    public ResponseEntity<Calendar> createCalendar(@Valid @RequestBody CreateCalendarRequest request) {
        logger.info("Creating calendar for user: {}", request.getUserId());
        
        Calendar calendar = new Calendar(request.getName(), request.getUserId(), 
            ZoneId.of(request.getTimezone()));
        
        Calendar createdCalendar = calendarService.createCalendar(calendar);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCalendar);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get calendar by ID", description = "Retrieves a calendar by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calendar found"),
        @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    public ResponseEntity<Calendar> getCalendar(
            @Parameter(description = "Calendar ID") @PathVariable UUID id) {
        
        logger.debug("Fetching calendar with ID: {}", id);
        Calendar calendar = calendarService.getCalendarById(id);
        
        return ResponseEntity.ok(calendar);
    }
    
    @GetMapping
    @Operation(summary = "Get calendars by user ID", description = "Retrieves all calendars for a user")
    public ResponseEntity<Page<Calendar>> getCalendarsByUser(
            @Parameter(description = "User ID") @RequestParam String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        logger.debug("Fetching calendars for user: {}", userId);
        Page<Calendar> calendars = calendarService.getCalendarsByUserId(userId, pageable);
        
        return ResponseEntity.ok(calendars);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update calendar", description = "Updates an existing calendar")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calendar updated successfully"),
        @ApiResponse(responseCode = "404", description = "Calendar not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Calendar> updateCalendar(
            @Parameter(description = "Calendar ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateCalendarRequest request) {
        
        logger.info("Updating calendar with ID: {}", id);
        
        Calendar calendar = new Calendar();
        calendar.setName(request.getName());
        calendar.setTimezone(ZoneId.of(request.getTimezone()));
        
        Calendar updatedCalendar = calendarService.updateCalendar(id, calendar);
        
        return ResponseEntity.ok(updatedCalendar);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete calendar", description = "Deletes a calendar and all its time slots")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Calendar deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    public ResponseEntity<Void> deleteCalendar(
            @Parameter(description = "Calendar ID") @PathVariable UUID id) {
        
        logger.info("Deleting calendar with ID: {}", id);
        calendarService.deleteCalendar(id);
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get calendar count for user", description = "Returns the number of calendars for a user")
    public ResponseEntity<Long> getCalendarCount(
            @Parameter(description = "User ID") @PathVariable String userId) {
        
        long count = calendarService.countCalendarsByUserId(userId);
        return ResponseEntity.ok(count);
    }
    
    // DTOs
    public static class CreateCalendarRequest {
        @jakarta.validation.constraints.NotBlank
        private String name;
        
        @jakarta.validation.constraints.NotBlank
        private String userId;
        
        @jakarta.validation.constraints.NotBlank
        private String timezone = "UTC";
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }
    
    public static class UpdateCalendarRequest {
        @jakarta.validation.constraints.NotBlank
        private String name;
        
        @jakarta.validation.constraints.NotBlank
        private String timezone;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
    }
}