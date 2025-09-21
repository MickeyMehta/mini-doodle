package com.doodle.repository;

import com.doodle.domain.Meeting;
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
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    
    @Query("SELECT m FROM Meeting m WHERE :participantId MEMBER OF m.participants")
    Page<Meeting> findByParticipant(@Param("participantId") String participantId, Pageable pageable);
    
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.timeSlot.startTime >= :startTime AND m.timeSlot.endTime <= :endTime " +
           "ORDER BY m.timeSlot.startTime")
    List<Meeting> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT m FROM Meeting m " +
           "WHERE :participantId MEMBER OF m.participants " +
           "AND m.timeSlot.startTime >= :startTime AND m.timeSlot.endTime <= :endTime " +
           "ORDER BY m.timeSlot.startTime")
    List<Meeting> findByParticipantAndTimeRange(
        @Param("participantId") String participantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.timeSlot.calendar.userId = :userId " +
           "ORDER BY m.timeSlot.startTime")
    Page<Meeting> findByCalendarUserId(@Param("userId") String userId, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Meeting m WHERE :participantId MEMBER OF m.participants")
    long countByParticipant(@Param("participantId") String participantId);
    
    @Query("SELECT m FROM Meeting m " +
           "WHERE m.title LIKE %:title% " +
           "ORDER BY m.timeSlot.startTime")
    List<Meeting> findByTitleContaining(@Param("title") String title);
}