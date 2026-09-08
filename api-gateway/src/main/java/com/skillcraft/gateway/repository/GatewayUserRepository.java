package com.skillcraft.gateway.repository;

import com.skillcraft.gateway.domain.GatewayUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayUserRepository extends JpaRepository<GatewayUser, Long> {

	Optional<GatewayUser> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
}
