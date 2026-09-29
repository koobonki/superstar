package com.example.backend.service.impl;

import com.example.backend.domain.StiDiimAuthCatgMapp;
import com.example.backend.domain.StiDiimAuthCatgMappId;
import com.example.backend.dto.StiDiimAuthCatgMappDto;
import com.example.backend.mapper.StiDiimAuthCatgMappMapper;
import com.example.backend.repository.jpa.StiDiimAuthCatgMappRepository;
import com.example.backend.repository.mybatis.StiDiimAuthCatgMappMybatisMapper;
import com.example.backend.service.StiDiimAuthCatgMappService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * STI_DIIM_AUTH_CATG_MAPP 저장 서비스 구현체
 * - 기존 iBatis INSERT 동작을 JPA save 로직으로 대체
 */
@Service
@RequiredArgsConstructor
public class StiDiimAuthCatgMappServiceImpl implements StiDiimAuthCatgMappService {

    private final StiDiimAuthCatgMappRepository repository;
    private final StiDiimAuthCatgMappMybatisMapper mybatisMapper;
    private final StiDiimAuthCatgMappMapper mapper;

    /**
     * 조회는 MyBatis로 처리
     */
    @Override
    @Transactional(readOnly = true)
    public List<StiDiimAuthCatgMappDto.SelectResponse> getMappings(String authGrpId) {
        return mybatisMapper.selectMappings(authGrpId);
    }

    @Override
    @Transactional
    public StiDiimAuthCatgMappDto.CreateResponse create(StiDiimAuthCatgMappDto.CreateRequest request, String loginId) {
        StiDiimAuthCatgMappId id = new StiDiimAuthCatgMappId(request.authGrpId(), request.dataCatgCd2());
        if (repository.existsById(id)) {
            throw new IllegalArgumentException("이미 존재하는 PK입니다. authGrpId=" + request.authGrpId()
                    + ", dataCatgCd=" + request.dataCatgCd2());
        }

        LocalDateTime now = LocalDateTime.now();
        StiDiimAuthCatgMapp entity = mapper.toEntity(request, loginId, now);
        StiDiimAuthCatgMapp saved = repository.save(entity);
        return mapper.toCreateResponse(saved);
    }
}
