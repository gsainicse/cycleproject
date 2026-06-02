package com.cycleproject.repository;

import com.cycleproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByApprovalStatus(User.ApprovalStatus status);
    List<User> findByApprovalStatusAndRole(User.ApprovalStatus status, User.Role role);
    List<User> findByRole(User.Role role);
    List<User> findByCustomerGroupId(Long groupId);
}
