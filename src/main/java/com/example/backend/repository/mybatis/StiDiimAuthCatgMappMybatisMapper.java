package com.example.backend.repository.mybatis;

import com.example.backend.dto.StiDiimAuthCatgMappDto;
import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * STI_DIIM_AUTH_CATG_MAPP 조회 전용 MyBatis Mapper
 */
@Mapper
public interface StiDiimAuthCatgMappMybatisMapper {

    @Select("""
            <script>
            SELECT
                AUTH_GRP_ID AS authGrpId
                , DATA_CATG_CD AS dataCatgCd
                , USER_MEMO_TXT AS userMemoTxt
                , USER_YN AS userYn
                , CRT_TM AS crtTm
                , CRT_USER_ID AS crtUserId
                , CHG_TM AS chgTm
                , CHG_USER_ID AS chgUserId
            FROM DMDS_ADM.STI_DIIM_AUTH_CATG_MAPP
            <where>
                <if test="authGrpId != null and authGrpId != ''">
                    AUTH_GRP_ID = #{authGrpId}
                </if>
            </where>
            ORDER BY AUTH_GRP_ID, DATA_CATG_CD
            </script>
            """)
    @ConstructorArgs({
            @Arg(column = "authGrpId", javaType = String.class),
            @Arg(column = "dataCatgCd", javaType = String.class),
            @Arg(column = "userMemoTxt", javaType = String.class),
            @Arg(column = "userYn", javaType = String.class),
            @Arg(column = "crtTm", javaType = LocalDateTime.class),
            @Arg(column = "crtUserId", javaType = String.class),
            @Arg(column = "chgTm", javaType = LocalDateTime.class),
            @Arg(column = "chgUserId", javaType = String.class)
    })
    List<StiDiimAuthCatgMappDto.SelectResponse> selectMappings(@Param("authGrpId") String authGrpId);
}
