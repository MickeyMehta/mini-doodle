package com.doodle.repository;

import com.doodle.domain.Calendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, UUID> {
    
    List<Calendar> findByUserId(String userId);
    
    Page<Calendar> findByUserId(String userId, Pageable pageable);
    
    Optional<Calendar> findByIdAndUserId(UUID id, String userId);
    
    @Query("SELECT c FROM Calendar c WHERE c.userId = :userId AND c.name LIKE %:name%")
    List<Calendar> findByUserIdAndNameContaining(@Param("userId") String userId, @Param("name") String name);
    
    boolean existsByUserIdAndName(String userId, String name);
    
    @Query("SELECT COUNT(c) FROM Calendar c WHERE c.userId = :userId")
    long countByUserId(@Param("userId") String userId);
}