package com.saffron.auth.repository;

import com.saffron.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {

    List<UserRole> findByUserId(String userId);
}
