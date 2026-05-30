package com.cycleproject.repository;

import com.cycleproject.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    Optional<CustomerGroup> findByName(String name);
    boolean existsByName(String name);
}
