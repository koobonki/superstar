package com.example.backend.controller;

import com.example.backend.dto.StiDiimAuthCatgMappDto;
import com.example.backend.service.StiDiimAuthCatgMappService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * STI_DIIM_AUTH_CATG_MAPP 등록 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth-categories")
public class StiDiimAuthCatgMappController {

    private final StiDiimAuthCatgMappService service;

    @Operation(
            summary = "권한-데이터카테고리 매핑 조회",
            description = "STI_DIIM_AUTH_CATG_MAPP 조회는 MyBatis로 수행합니다.",
            security = @SecurityRequirement(name = "oauth2")
    )
    @GetMapping("/mappings")
    public ResponseEntity<List<StiDiimAuthCatgMappDto.SelectResponse>> getMappings(
            @RequestParam(required = false) String authGrpId
    ) {
        return ResponseEntity.ok(service.getMappings(authGrpId));
    }

    @Operation(
            summary = "권한-데이터카테고리 매핑 등록",
            description = "기존 iBatis INSERT(I001)와 동일한 컬럼을 JPA로 저장합니다. (insert는 JPA)",
            security = @SecurityRequirement(name = "oauth2")
    )
    @PostMapping("/mappings")
    public ResponseEntity<StiDiimAuthCatgMappDto.CreateResponse> create(
            @RequestBody @Valid StiDiimAuthCatgMappDto.CreateRequest request,
            Authentication authentication
    ) {
        String loginId = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(service.create(request, loginId));
    }
}
