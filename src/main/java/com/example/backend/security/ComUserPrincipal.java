package com.example.backend.security;

import com.example.backend.domain.ComUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 로그인 후 Security 세션에 저장될 사용자 정보 클래스
 * 기본 User에 empno, name 정보를 추가로 담기 위해 만듦
 */
public class ComUserPrincipal extends User {

    // 사번처럼 사용할 값
    private final String empno;

    // 화면/응답에 보여줄 이름
    private final String name;

    public ComUserPrincipal(ComUser user, Collection<? extends GrantedAuthority> authorities) {
        // 부모(User)에는 username, password, authorities를 넣음
        super(user.getUserId(), user.getPassword(), authorities);

        // tb_com_user에 empno 컬럼이 없으므로 user_id를 empno처럼 사용
        this.empno = user.getUserId();

        // 사용자 이름
        this.name = user.getUserName();
    }

    public String getEmpno() {
        return empno;
    }

    public String getName() {
        return name;
    }
}
