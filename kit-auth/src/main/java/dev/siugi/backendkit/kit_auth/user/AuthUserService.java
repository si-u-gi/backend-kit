
package dev.siugi.backendkit.kit_auth.user;

import java.util.Optional;

/**
 * 각 서비스가 자신의 회원 저장소에 맞게 구현하는 인터페이스.
 *
 * kit-auth는 구체적인 DB, JPA 엔티티, Repository에 의존하지 않는다.
 */
public interface AuthUserService {

    /**
     * 이메일로 회원을 조회한다.
     *
     * @param email 조회할 이메일
     * @return 회원이 존재하면 AuthUser, 없으면 Optional.empty()
     */
    Optional<AuthUser> findByEmail(String email);

    /**
     * 새 회원을 저장한다.
     *
     * 전달받는 passwordHash는 이미 해시 처리된 비밀번호다.
     *
     * @param email 회원 이메일
     * @param passwordHash BCrypt 등으로 해시된 비밀번호
     * @return DB에 저장된 회원 정보. 생성된 회원 ID를 포함해야 한다.
     */
    AuthUser createUser(String email, String passwordHash);
}
