package com.example.backend.mapper;

import com.example.backend.domain.ComUser;
import com.example.backend.dto.ComUserBatchDto;
import org.springframework.stereotype.Component;

/**
 * ComUser 배치 처리용 Mapper
 * - Entity <-> DTO 변환을 담당
 */
@Component
public class ComUserBatchMapper {

    public ComUserBatchDto.ComUserRowResponse toComUserRowResponse(ComUser entity) {
        return new ComUserBatchDto.ComUserRowResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getUserName(),
                entity.getRole()
        );
    }
}
