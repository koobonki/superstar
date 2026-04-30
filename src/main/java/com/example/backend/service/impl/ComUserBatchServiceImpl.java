package com.example.backend.service.impl;

import com.example.backend.domain.ComUser;
import com.example.backend.dto.ComUserBatchDto;
import com.example.backend.mapper.ComUserBatchMapper;
import com.example.backend.repository.jpa.ComUserRepository;
import com.example.backend.service.ComUserBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 목록 조회/일괄 저장 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ComUserBatchServiceImpl implements ComUserBatchService {

    private final ComUserRepository comUserRepository;
    private final ComUserBatchMapper comUserBatchMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ComUserBatchDto.ComUserRowResponse> getComUsers() {
        return comUserRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(comUserBatchMapper::toComUserRowResponse)
                .toList();
    }

    @Override
    @Transactional
    public ComUserBatchDto.BatchSaveResponse saveComUsers(List<ComUserBatchDto.ComUserUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) {
            return new ComUserBatchDto.BatchSaveResponse(0, 0, 0, List.of());
        }

        Map<Long, ComUserBatchDto.ComUserUpdateRequest> deduplicated = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        for (ComUserBatchDto.ComUserUpdateRequest request : requestList) {
            if (request == null || request.id() == null) {
                errors.add("id가 없는 항목은 저장할 수 없습니다.");
                continue;
            }
            deduplicated.put(request.id(), request);
        }

        List<Long> targetIds = new ArrayList<>(deduplicated.keySet());
        Map<Long, ComUser> entityMap = comUserRepository.findAllById(targetIds)
                .stream()
                .collect(LinkedHashMap::new, (map, entity) -> map.put(entity.getId(), entity), Map::putAll);

        int updatedCount = 0;
        for (Long id : targetIds) {
            ComUserBatchDto.ComUserUpdateRequest request = deduplicated.get(id);
            ComUser entity = entityMap.get(id);

            if (entity == null) {
                errors.add("id=" + id + " 사용자를 찾을 수 없습니다.");
                continue;
            }

            boolean changed = false;
            if (request.userName() != null && !request.userName().equals(entity.getUserName())) {
                entity.setUserName(request.userName());
                changed = true;
            }

            if (request.role() != null && !request.role().equals(entity.getRole())) {
                entity.setRole(request.role());
                changed = true;
            }

            if (changed) {
                updatedCount++;
            }
        }

        int requestedCount = deduplicated.size();
        int skippedCount = requestedCount - updatedCount;
        return new ComUserBatchDto.BatchSaveResponse(
                requestedCount,
                updatedCount,
                skippedCount,
                errors
        );
    }
}
