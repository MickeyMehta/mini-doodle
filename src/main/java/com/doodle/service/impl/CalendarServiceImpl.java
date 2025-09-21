package com.doodle.service.impl;

import com.doodle.domain.Calendar;
import com.doodle.exception.custom.ResourceNotFoundException;
import com.doodle.repository.CalendarRepository;
import com.doodle.service.CalendarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CalendarServiceImpl implements CalendarService {
    
    private static final Logger logger = LoggerFactory.getLogger(CalendarServiceImpl.class);
    
    private final CalendarRepository calendarRepository;
    
    @Autowired
    public CalendarServiceImpl(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }
    
    @Override
    public Calendar createCalendar(Calendar calendar) {
        logger.debug("Creating calendar for user: {}", calendar.getUserId());
        
        if (existsByUserIdAndName(calendar.getUserId(), calendar.getName())) {
            throw new IllegalArgumentException("Calendar with this name already exists for user");
        }
        
        Calendar savedCalendar = calendarRepository.save(calendar);
        logger.info("Created calendar with ID: {} for user: {}", savedCalendar.getId(), savedCalendar.getUserId());
        
        return savedCalendar;
    }
    
    @Override
    @Cacheable(value = "calendars", key = "#id")
    @Transactional(readOnly = true)
    public Calendar getCalendarById(UUID id) {
        logger.debug("Fetching calendar with ID: {}", id);
        return calendarRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Calendar not found with ID: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Calendar getCalendarByIdAndUserId(UUID id, String userId) {
        logger.debug("Fetching calendar with ID: {} for user: {}", id, userId);
        return calendarRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Calendar not found with ID: " + id + " for user: " + userId));
    }
    
    @Override
    @Cacheable(value = "userCalendars", key = "#userId")
    @Transactional(readOnly = true)
    public List<Calendar> getCalendarsByUserId(String userId) {
        logger.debug("Fetching calendars for user: {}", userId);
        return calendarRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Calendar> getCalendarsByUserId(String userId, Pageable pageable) {
        logger.debug("Fetching calendars for user: {} with pagination", userId);
        return calendarRepository.findByUserId(userId, pageable);
    }
    
    @Override
    @CacheEvict(value = {"calendars", "userCalendars"}, key = "#id")
    public Calendar updateCalendar(UUID id, Calendar calendar) {
        logger.debug("Updating calendar with ID: {}", id);
        
        Calendar existingCalendar = getCalendarById(id);
        
        if (!calendar.getName().equals(existingCalendar.getName()) &&
            existsByUserIdAndName(existingCalendar.getUserId(), calendar.getName())) {
            throw new IllegalArgumentException("Calendar with this name already exists for user");
        }
        
        existingCalendar.setName(calendar.getName());
        existingCalendar.setTimezone(calendar.getTimezone());
        
        Calendar updatedCalendar = calendarRepository.save(existingCalendar);
        logger.info("Updated calendar with ID: {}", updatedCalendar.getId());
        
        return updatedCalendar;
    }
    
    @Override
    @CacheEvict(value = {"calendars", "userCalendars"}, key = "#id")
    public void deleteCalendar(UUID id) {
        logger.debug("Deleting calendar with ID: {}", id);
        
        Calendar calendar = getCalendarById(id);
        calendarRepository.delete(calendar);
        
        logger.info("Deleted calendar with ID: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndName(String userId, String name) {
        return calendarRepository.existsByUserIdAndName(userId, name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long countCalendarsByUserId(String userId) {
        return calendarRepository.countByUserId(userId);
    }
}