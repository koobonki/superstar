package com.example.backend.service;

import com.example.backend.dto.ComUserBatchDto;

import java.util.List;

/**
 * 조회/일괄 저장 서비스 인터페이스
 */
public interface ComUserBatchService {

    List<ComUserBatchDto.ComUserRowResponse> getComUsers();

    ComUserBatchDto.BatchSaveResponse saveComUsers(List<ComUserBatchDto.ComUserUpdateRequest> request);
}
