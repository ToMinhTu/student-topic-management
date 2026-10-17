
package com.example.student_topic_management.repository;

import com.example.student_topic_management.entity.RegistrationRound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface RegistrationRoundRepository
        extends JpaRepository<RegistrationRound, Long> {

    Page<RegistrationRound> findAllByOrderByCreatedAtDescIdDesc(
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM RegistrationRound r
        WHERE r.id = :id
    """)
    Optional<RegistrationRound> findByIdForUpdate(
            @Param("id") Long id
    );
}
