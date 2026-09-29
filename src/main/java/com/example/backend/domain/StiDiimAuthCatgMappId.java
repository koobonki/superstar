package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * STI_DIIM_AUTH_CATG_MAPP 복합 PK
 * - AUTH_GRP_ID
 * - DATA_CATG_CD
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class StiDiimAuthCatgMappId implements Serializable {

    @Column(name = "AUTH_GRP_ID", nullable = false, length = 255)
    private String authGrpId;

    @Column(name = "DATA_CATG_CD", nullable = false, length = 255)
    private String dataCatgCd;

    public StiDiimAuthCatgMappId(String authGrpId, String dataCatgCd) {
        this.authGrpId = authGrpId;
        this.dataCatgCd = dataCatgCd;
    }
}
