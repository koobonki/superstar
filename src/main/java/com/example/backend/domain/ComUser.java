package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * tb_com_user 테이블과 매핑되는 엔티티 클래스
 * -> DB 한 행(row)을 Java 객체 하나로 다루기 위해 사용
 */
@Entity
@Table(name = "tb_com_user")
@Getter
@Setter
@NoArgsConstructor
public class ComUser {

    // PK 컬럼(ID)
    @Id
    @Column(name = "ID")
    private Long id;

    // 로그인 아이디(user_id)
    @Column(name = "user_id", nullable = false, length = 50, unique = true)
    private String userId;

    // 비밀번호(BCrypt 해시 권장)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // 사용자 이름
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    // 권한 값 (ROLE_ADMIN / ROLE_USER 등)
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    // 생성 시간
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
