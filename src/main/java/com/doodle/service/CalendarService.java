package com.doodle.service;

import com.doodle.domain.Calendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CalendarService {
    
    Calendar createCalendar(Calendar calendar);
    
    Calendar getCalendarById(UUID id);
    
    Calendar getCalendarByIdAndUserId(UUID id, String userId);
    
    List<Calendar> getCalendarsByUserId(String userId);
    
    Page<Calendar> getCalendarsByUserId(String userId, Pageable pageable);
    
    Calendar updateCalendar(UUID id, Calendar calendar);
    
    void deleteCalendar(UUID id);
    
    boolean existsByUserIdAndName(String userId, String name);
    
    long countCalendarsByUserId(String userId);
}