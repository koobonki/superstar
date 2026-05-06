package com.example.backend.service;

import com.example.backend.dto.StiDiimAuthCatgMappDto;

/**
 * STI_DIIM_AUTH_CATG_MAPP 저장 서비스 인터페이스
 */
public interface StiDiimAuthCatgMappService {

    StiDiimAuthCatgMappDto.CreateResponse create(StiDiimAuthCatgMappDto.CreateRequest request, String loginId);
}
