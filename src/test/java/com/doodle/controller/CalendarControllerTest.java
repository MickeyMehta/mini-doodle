package com.doodle.controller;

import com.doodle.domain.Calendar;
import com.doodle.service.CalendarService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
class CalendarControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CalendarService calendarService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void createCalendar_Success() throws Exception {
        // Given
        UUID calendarId = UUID.randomUUID();
        Calendar calendar = new Calendar("Test Calendar", "user123", ZoneId.of("UTC"));
        calendar.setId(calendarId);
        
        when(calendarService.createCalendar(any(Calendar.class))).thenReturn(calendar);
        
        CalendarController.CreateCalendarRequest request = new CalendarController.CreateCalendarRequest();
        request.setName("Test Calendar");
        request.setUserId("user123");
        request.setTimezone("UTC");
        
        // When & Then
        mockMvc.perform(post("/api/v1/calendars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Calendar"))
                .andExpect(jsonPath("$.userId").value("user123"));
    }
    
    @Test
    void getCalendar_Success() throws Exception {
        // Given
        UUID calendarId = UUID.randomUUID();
        Calendar calendar = new Calendar("Test Calendar", "user123", ZoneId.of("UTC"));
        calendar.setId(calendarId);
        
        when(calendarService.getCalendarById(calendarId)).thenReturn(calendar);
        
        // When & Then
        mockMvc.perform(get("/api/v1/calendars/{id}", calendarId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(calendarId.toString()))
                .andExpect(jsonPath("$.name").value("Test Calendar"));
    }
    
    @Test
    void getCalendarsByUser_Success() throws Exception {
        // Given
        String userId = "user123";
        Calendar calendar = new Calendar("Test Calendar", userId, ZoneId.of("UTC"));
        Page<Calendar> calendarPage = new PageImpl<>(List.of(calendar));
        
        when(calendarService.getCalendarsByUserId(eq(userId), any())).thenReturn(calendarPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/calendars")
                .param("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(userId))
                .andExpect(jsonPath("$.content[0].name").value("Test Calendar"));
    }
    
    @Test
    void updateCalendar_Success() throws Exception {
        // Given
        UUID calendarId = UUID.randomUUID();
        Calendar updatedCalendar = new Calendar("Updated Calendar", "user123", ZoneId.of("UTC"));
        updatedCalendar.setId(calendarId);
        
        when(calendarService.updateCalendar(eq(calendarId), any(Calendar.class))).thenReturn(updatedCalendar);
        
        CalendarController.UpdateCalendarRequest request = new CalendarController.UpdateCalendarRequest();
        request.setName("Updated Calendar");
        request.setTimezone("UTC");
        
        // When & Then
        mockMvc.perform(put("/api/v1/calendars/{id}", calendarId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Calendar"));
    }
    
    @Test
    void deleteCalendar_Success() throws Exception {
        // Given
        UUID calendarId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(delete("/api/v1/calendars/{id}", calendarId))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void getCalendarCount_Success() throws Exception {
        // Given
        String userId = "user123";
        long count = 5L;
        
        when(calendarService.countCalendarsByUserId(userId)).thenReturn(count);
        
        // When & Then
        mockMvc.perform(get("/api/v1/calendars/user/{userId}/count", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }
}