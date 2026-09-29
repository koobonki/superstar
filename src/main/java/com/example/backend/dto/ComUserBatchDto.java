package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * ComUser 목록 조회/저장 DTO 모음
 */
public final class ComUserBatchDto {

    private ComUserBatchDto() {
    }

    /**
     * 조회 응답 DTO
     */
    public record ComUserRowResponse(
            Long id,
            String userId,
            String userName,
            String role
    ) {
    }

    /**
     * 수정 저장 요청 DTO
     */
    public record ComUserUpdateRequest(
            @NotNull(message = "id는 필수입니다.")
            Long id,
            String userName,
            String role
    ) {
    }

    /**
     * Service 계층 내부 전달용 DTO
     */
    public record ComUserUpdateCommand(
            Long id,
            String userName,
            String role
    ) {
    }

    /**
     * Service 조회 결과 DTO
     */
    public record ComUserRowResult(
            Long id,
            String userId,
            String userName,
            String role
    ) {
    }

    /**
     * Service 저장 결과 DTO
     */
    public record BatchSaveResult(
            int requestedCount,
            int updatedCount,
            int skippedCount,
            List<String> errors,
            List<ComUserRowResult> updatedRows
    ) {
    }

    /**
     * 저장 응답 DTO
     */
    public record BatchSaveResponse(
            int requestedCount,
            int updatedCount,
            int skippedCount,
            List<String> errors
    ) {
    }
}
