package com.skillcraft.gateway.repository;

import com.skillcraft.gateway.domain.GatewayStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayStudentRepository extends JpaRepository<GatewayStudent, Long> {
}
