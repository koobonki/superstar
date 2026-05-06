package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * STI_DIIM_AUTH_CATG_MAPP 저장 DTO 모음
 */
public final class StiDiimAuthCatgMappDto {

    private StiDiimAuthCatgMappDto() {
    }

    /**
     * INSERT 요청 DTO
     * - DATA_CATG_CD2: 기존 iBatis 파라미터 이름 호환
     * - AUTH_USER_YN: USER_YN 컬럼 매핑용
     */
    public record CreateRequest(
            @NotBlank(message = "AUTH_GRP_ID는 필수입니다.")
            String authGrpId,
            @NotBlank(message = "DATA_CATG_CD2는 필수입니다.")
            String dataCatgCd2,
            String userMemoTxt,
            String authUserYn
    ) {
    }

    /**
     * INSERT 응답 DTO
     */
    public record CreateResponse(
            String authGrpId,
            String dataCatgCd,
            String userMemoTxt,
            String userYn,
            LocalDateTime crtTm,
            String crtUserId,
            LocalDateTime chgTm,
            String chgUserId
    ) {
    }
}
