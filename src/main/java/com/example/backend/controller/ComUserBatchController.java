package com.example.backend.controller;

import com.example.backend.service.ComUserListSaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
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

    private final ComUserListSaveService comUserListSaveService;

    @Operation(
            summary = "사용자 목록 조회",
            description = "수정 가능한 사용자 목록을 반환합니다.",
            security = @SecurityRequirement(name = "oauth2")
    )
    @GetMapping
    public ResponseEntity<List<ComUserRowResponse>> getComUsers() {
        List<ComUserListSaveService.ComUserRow> rows = comUserListSaveService.getUserRows();

        List<ComUserRowResponse> response = rows.stream()
                .map(row -> new ComUserRowResponse(
                        row.id(),
                        row.userId(),
                        row.userName(),
                        row.role()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "수정된 사용자 목록 저장",
            description = "조회한 목록에서 수정된 항목을 리스트로 받아 일괄 저장합니다.",
            security = @SecurityRequirement(name = "oauth2")
    )
    @PostMapping("/save")
    public ResponseEntity<BatchSaveResponse> saveComUsers(@RequestBody List<ComUserUpdateRequest> request) {
        ComUserListSaveService.BatchSaveResult result = comUserListSaveService.saveUpdatedRows(
                request.stream()
                        .map(item -> new ComUserListSaveService.ComUserUpdateItem(
                                item.id(),
                                item.userName(),
                                item.role()
                        ))
                        .toList()
        );

        return ResponseEntity.ok(new BatchSaveResponse(
                result.requestedCount(),
                result.updatedCount(),
                result.skippedCount(),
                result.errors()
        ));
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
     * 일괄 저장 응답 DTO
     */
    public record BatchSaveResponse(
            int requestedCount,
            int updatedCount,
            int skippedCount,
            List<String> errors
    ) {
    }
}
