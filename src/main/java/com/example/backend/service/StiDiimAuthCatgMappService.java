package com.example.backend.service;

import com.example.backend.dto.StiDiimAuthCatgMappDto;

import java.util.List;

/**
 * STI_DIIM_AUTH_CATG_MAPP 서비스 인터페이스
 * - SELECT: MyBatis
 * - INSERT: JPA
 */
public interface StiDiimAuthCatgMappService {

    List<StiDiimAuthCatgMappDto.SelectResponse> getMappings(String authGrpId);

    StiDiimAuthCatgMappDto.CreateResponse create(StiDiimAuthCatgMappDto.CreateRequest request, String loginId);
}
