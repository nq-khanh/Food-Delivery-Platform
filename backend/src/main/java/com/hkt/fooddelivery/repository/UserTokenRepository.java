package com.hkt.fooddelivery.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hkt.fooddelivery.entity.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByTokenHash(String tokenHash);

    List<UserToken> findByUserIdAndType(UUID userId, String type);

    Optional<UserToken> findByTokenHashAndIsRevokedFalse(String tokenHash);

    @Modifying
    @Query("UPDATE UserToken t SET t.isRevoked = true WHERE t.user.id = :userId AND t.type = :type")
    void revokeAllByUserIdAndType(@Param("userId") UUID userId, @Param("type") String type);
}
