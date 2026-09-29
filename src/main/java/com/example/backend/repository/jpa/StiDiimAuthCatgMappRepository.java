package com.example.backend.repository.jpa;

import com.example.backend.domain.StiDiimAuthCatgMapp;
import com.example.backend.domain.StiDiimAuthCatgMappId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * STI_DIIM_AUTH_CATG_MAPP Repository
 */
public interface StiDiimAuthCatgMappRepository extends JpaRepository<StiDiimAuthCatgMapp, StiDiimAuthCatgMappId> {
}
