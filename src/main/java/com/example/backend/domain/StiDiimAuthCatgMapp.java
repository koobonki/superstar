package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DMDS_ADM.STI_DIIM_AUTH_CATG_MAPP 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "STI_DIIM_AUTH_CATG_MAPP", schema = "DMDS_ADM")
public class StiDiimAuthCatgMapp {

    @EmbeddedId
    private StiDiimAuthCatgMappId id;

    @Column(name = "USER_MEMO_TXT")
    private String userMemoTxt;

    @Column(name = "USER_YN")
    private String userYn;

    @Column(name = "CRT_TM")
    private LocalDateTime crtTm;

    @Column(name = "CRT_USER_ID")
    private String crtUserId;

    @Column(name = "CHG_TM")
    private LocalDateTime chgTm;

    @Column(name = "CHG_USER_ID")
    private String chgUserId;
}
