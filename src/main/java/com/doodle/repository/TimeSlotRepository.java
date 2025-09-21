package com.doodle.repository;

import com.doodle.domain.TimeSlot;
import com.doodle.domain.enums.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.calendar.id = :calendarId " +
           "AND ts.startTime >= :startTime AND ts.endTime <= :endTime " +
           "ORDER BY ts.startTime")
    Page<TimeSlot> findByCalendarIdAndTimeRange(
        @Param("calendarId") UUID calendarId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.calendar.id = :calendarId " +
           "AND ts.status = :status " +
           "AND ts.startTime >= :startTime AND ts.endTime <= :endTime " +
           "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlots(
        @Param("calendarId") UUID calendarId,
        @Param("status") SlotStatus status,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(ts) > 0 FROM TimeSlot ts WHERE ts.calendar.id = :calendarId " +
           "AND (:excludeSlotId IS NULL OR ts.id != :excludeSlotId) " +
           "AND ((ts.startTime <= :startTime AND ts.endTime > :startTime) " +
           "OR (ts.startTime < :endTime AND ts.endTime >= :endTime) " +
           "OR (ts.startTime >= :startTime AND ts.endTime <= :endTime))")
    boolean existsOverlappingSlot(
        @Param("calendarId") UUID calendarId,
        @Param("excludeSlotId") UUID excludeSlotId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ts FROM TimeSlot ts " +
           "WHERE ts.calendar.userId IN :userIds " +
           "AND ts.startTime >= :startTime AND ts.endTime <= :endTime " +
           "AND ts.status = 'BUSY' " +
           "ORDER BY ts.startTime")
    List<TimeSlot> findBusySlotsByUsers(
        @Param("userIds") List<String> userIds,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT DATE(ts.startTime) as date, COUNT(ts) as count " +
           "FROM TimeSlot ts " +
           "WHERE ts.calendar.id = :calendarId " +
           "AND ts.startTime >= :startTime AND ts.endTime <= :endTime " +
           "GROUP BY DATE(ts.startTime) " +
           "ORDER BY date")
    List<Object[]> getSlotCountByDate(
        @Param("calendarId") UUID calendarId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ts FROM TimeSlot ts " +
           "WHERE ts.calendar.id = :calendarId " +
           "AND ts.status = :status")
    Page<TimeSlot> findByCalendarIdAndStatus(
        @Param("calendarId") UUID calendarId,
        @Param("status") SlotStatus status,
        Pageable pageable);
}