package com.example.backend.mapper;

import com.example.backend.domain.StiDiimAuthCatgMapp;
import com.example.backend.domain.StiDiimAuthCatgMappId;
import com.example.backend.dto.StiDiimAuthCatgMappDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * STI_DIIM_AUTH_CATG_MAPP 매핑 전용 Mapper
 */
@Component
public class StiDiimAuthCatgMappMapper {

    public StiDiimAuthCatgMapp toEntity(StiDiimAuthCatgMappDto.CreateRequest request, String loginId, LocalDateTime now) {
        StiDiimAuthCatgMapp entity = new StiDiimAuthCatgMapp();
        entity.setId(new StiDiimAuthCatgMappId(request.authGrpId(), request.dataCatgCd2()));
        entity.setUserMemoTxt(request.userMemoTxt());
        entity.setUserYn(request.authUserYn());
        entity.setCrtTm(now);
        entity.setCrtUserId(loginId);
        entity.setChgTm(now);
        entity.setChgUserId(loginId);
        return entity;
    }

    public StiDiimAuthCatgMappDto.CreateResponse toCreateResponse(StiDiimAuthCatgMapp entity) {
        return new StiDiimAuthCatgMappDto.CreateResponse(
                entity.getId().getAuthGrpId(),
                entity.getId().getDataCatgCd(),
                entity.getUserMemoTxt(),
                entity.getUserYn(),
                entity.getCrtTm(),
                entity.getCrtUserId(),
                entity.getChgTm(),
                entity.getChgUserId()
        );
    }
}
