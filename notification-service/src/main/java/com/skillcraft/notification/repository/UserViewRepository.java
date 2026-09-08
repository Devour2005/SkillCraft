package com.skillcraft.notification.repository;

import com.skillcraft.notification.domain.UserView;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserViewRepository extends JpaRepository<UserView, Long> {
}
