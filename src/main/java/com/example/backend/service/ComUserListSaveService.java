package com.example.backend.service;

import com.example.backend.domain.ComUser;
import com.example.backend.repository.jpa.ComUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 목록 조회/일괄 저장 서비스
 * - 조회 API로 목록을 내려주고
 * - 프론트에서 수정된 목록(List)을 다시 받아 반영한다.
 */
@Service
@RequiredArgsConstructor
public class ComUserListSaveService {

    private final ComUserRepository comUserRepository;

    /**
     * 수정 가능한 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ComUserRow> getUserRows() {
        return comUserRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(user -> new ComUserRow(
                        user.getId(),
                        user.getUserId(),
                        user.getUserName(),
                        user.getRole()
                ))
                .toList();
    }

    /**
     * 수정된 목록(List)을 받아 일괄 저장
     */
    @Transactional
    public BulkSaveResult saveUpdatedRows(List<ComUserUpdateItem> updateItems) {
        if (updateItems == null || updateItems.isEmpty()) {
            return new BulkSaveResult(0, 0, 0, List.of(), List.of());
        }

        // 같은 id가 여러 번 오면 마지막 값으로 덮어쓰기
        Map<Long, ComUserUpdateItem> deduplicated = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        for (ComUserUpdateItem item : updateItems) {
            if (item == null || item.id() == null) {
                errors.add("id가 없는 항목은 저장할 수 없습니다.");
                continue;
            }
            deduplicated.put(item.id(), item);
        }

        List<Long> targetIds = new ArrayList<>(deduplicated.keySet());
        Map<Long, ComUser> entityMap = comUserRepository.findAllById(targetIds)
                .stream()
                .collect(LinkedHashMap::new, (map, entity) -> map.put(entity.getId(), entity), Map::putAll);

        int updatedCount = 0;
        List<ComUserRow> updatedRows = new ArrayList<>();

        for (Long id : targetIds) {
            ComUserUpdateItem requestItem = deduplicated.get(id);
            ComUser entity = entityMap.get(id);

            if (entity == null) {
                errors.add("id=" + id + " 사용자를 찾을 수 없습니다.");
                continue;
            }

            boolean changed = false;

            if (requestItem.userName() != null && !requestItem.userName().equals(entity.getUserName())) {
                entity.setUserName(requestItem.userName());
                changed = true;
            }

            if (requestItem.role() != null && !requestItem.role().equals(entity.getRole())) {
                entity.setRole(requestItem.role());
                changed = true;
            }

            if (changed) {
                updatedCount++;
                updatedRows.add(new ComUserRow(
                        entity.getId(),
                        entity.getUserId(),
                        entity.getUserName(),
                        entity.getRole()
                ));
            }
        }

        int requestedCount = deduplicated.size();
        int skippedCount = requestedCount - updatedCount;
        return new BulkSaveResult(requestedCount, updatedCount, skippedCount, errors, updatedRows);
    }

    public record ComUserRow(Long id, String userId, String userName, String role) {
    }

    public record ComUserUpdateItem(Long id, String userName, String role) {
    }

    public record BulkSaveResult(
            int requestedCount,
            int updatedCount,
            int skippedCount,
            List<String> errors,
            List<ComUserRow> updatedRows
    ) {
    }
}
