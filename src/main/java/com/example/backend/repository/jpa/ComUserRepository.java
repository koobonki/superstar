package com.example.backend.repository.jpa;

import com.example.backend.domain.ComUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 조회용 JPA Repository
 * -> DB에서 ComUser를 읽을 때 사용
 */
public interface ComUserRepository extends JpaRepository<ComUser, Long> {

    /**
     * user_id로 사용자 1명을 조회
     * 없으면 Optional.empty() 반환
     */
    Optional<ComUser> findByUserId(String userId);
}
