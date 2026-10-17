package dev.siugi.backendkit.kit_auth.repository;

import dev.siugi.backendkit.kit_auth.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository
        extends JpaRepository<AuthUser, Long> {

    boolean existsByEmail(String email);
}
