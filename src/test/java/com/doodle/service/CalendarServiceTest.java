package com.doodle.service;

import com.doodle.domain.Calendar;
import com.doodle.exception.custom.ResourceNotFoundException;
import com.doodle.repository.CalendarRepository;
import com.doodle.service.impl.CalendarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {
    
    @Mock
    private CalendarRepository calendarRepository;
    
    @InjectMocks
    private CalendarServiceImpl calendarService;
    
    private Calendar testCalendar;
    private UUID testId;
    private String testUserId;
    
    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUserId = "user123";
        
        testCalendar = new Calendar();
        testCalendar.setId(testId);
        testCalendar.setName("Test Calendar");
        testCalendar.setUserId(testUserId);
        testCalendar.setTimezone(ZoneId.of("UTC"));
    }
    
    @Test
    void createCalendar_Success() {
        // Given
        when(calendarRepository.existsByUserIdAndName(anyString(), anyString())).thenReturn(false);
        when(calendarRepository.save(any(Calendar.class))).thenReturn(testCalendar);
        
        // When
        Calendar result = calendarService.createCalendar(testCalendar);
        
        // Then
        assertNotNull(result);
        assertEquals(testCalendar.getName(), result.getName());
        assertEquals(testCalendar.getUserId(), result.getUserId());
        verify(calendarRepository).save(testCalendar);
    }
    
    @Test
    void createCalendar_DuplicateName_ThrowsException() {
        // Given
        when(calendarRepository.existsByUserIdAndName(anyString(), anyString())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> calendarService.createCalendar(testCalendar)
        );
        
        assertEquals("Calendar with this name already exists for user", exception.getMessage());
        verify(calendarRepository, never()).save(any(Calendar.class));
    }
    
    @Test
    void getCalendarById_Success() {
        // Given
        when(calendarRepository.findById(testId)).thenReturn(Optional.of(testCalendar));
        
        // When
        Calendar result = calendarService.getCalendarById(testId);
        
        // Then
        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals(testCalendar.getName(), result.getName());
    }
    
    @Test
    void getCalendarById_NotFound_ThrowsException() {
        // Given
        when(calendarRepository.findById(testId)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> calendarService.getCalendarById(testId)
        );
        
        assertTrue(exception.getMessage().contains("Calendar not found"));
    }
    
    @Test
    void getCalendarsByUserId_Success() {
        // Given
        List<Calendar> calendars = List.of(testCalendar);
        when(calendarRepository.findByUserId(testUserId)).thenReturn(calendars);
        
        // When
        List<Calendar> result = calendarService.getCalendarsByUserId(testUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCalendar.getId(), result.get(0).getId());
    }
    
    @Test
    void getCalendarsByUserIdWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Calendar> calendarPage = new PageImpl<>(List.of(testCalendar));
        when(calendarRepository.findByUserId(testUserId, pageable)).thenReturn(calendarPage);
        
        // When
        Page<Calendar> result = calendarService.getCalendarsByUserId(testUserId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCalendar.getId(), result.getContent().get(0).getId());
    }
    
    @Test
    void updateCalendar_Success() {
        // Given
        Calendar updatedCalendar = new Calendar();
        updatedCalendar.setName("Updated Calendar");
        updatedCalendar.setTimezone(ZoneId.of("America/New_York"));
        
        when(calendarRepository.findById(testId)).thenReturn(Optional.of(testCalendar));
        when(calendarRepository.existsByUserIdAndName(anyString(), anyString())).thenReturn(false);
        when(calendarRepository.save(any(Calendar.class))).thenReturn(testCalendar);
        
        // When
        Calendar result = calendarService.updateCalendar(testId, updatedCalendar);
        
        // Then
        assertNotNull(result);
        verify(calendarRepository).save(testCalendar);
    }
    
    @Test
    void deleteCalendar_Success() {
        // Given
        when(calendarRepository.findById(testId)).thenReturn(Optional.of(testCalendar));
        
        // When
        calendarService.deleteCalendar(testId);
        
        // Then
        verify(calendarRepository).delete(testCalendar);
    }
    
    @Test
    void countCalendarsByUserId_Success() {
        // Given
        long expectedCount = 5L;
        when(calendarRepository.countByUserId(testUserId)).thenReturn(expectedCount);
        
        // When
        long result = calendarService.countCalendarsByUserId(testUserId);
        
        // Then
        assertEquals(expectedCount, result);
    }
}