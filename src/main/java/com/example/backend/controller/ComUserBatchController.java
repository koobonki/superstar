package com.example.backend.controller;

import com.example.backend.dto.ComUserBatchDto;
import com.example.backend.service.ComUserBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 조회 후 프론트에서 수정된 리스트를 다시 전달받아 저장하는 배치 컨트롤러
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/com-users")
public class ComUserBatchController {

    private final ComUserBatchService comUserBatchService;

    @Operation(
            summary = "사용자 목록 조회",
            description = "수정 가능한 사용자 목록을 반환합니다.",
            security = @SecurityRequirement(name = "oauth2")
    )
    @GetMapping
    public ResponseEntity<List<ComUserBatchDto.ComUserRowResponse>> getComUsers() {
        return ResponseEntity.ok(comUserBatchService.getComUsers());
    }

    @Operation(
            summary = "수정된 사용자 목록 저장",
            description = "조회한 목록에서 수정된 항목을 리스트로 받아 일괄 저장합니다.",
            security = @SecurityRequirement(name = "oauth2")
    )
    @PostMapping("/save")
    public ResponseEntity<ComUserBatchDto.BatchSaveResponse> saveComUsers(
            @RequestBody List<@Valid ComUserBatchDto.ComUserUpdateRequest> request
    ) {
        return ResponseEntity.ok(comUserBatchService.saveComUsers(request));
    }
}
