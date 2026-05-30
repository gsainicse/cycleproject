package com.cycleproject.config;

import com.cycleproject.entity.CustomerGroup;
import com.cycleproject.entity.User;
import com.cycleproject.repository.CustomerGroupRepository;
import com.cycleproject.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository, CustomerGroupRepository groupRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default customer groups if not exist
            if (groupRepository.count() == 0) {
                groupRepository.save(CustomerGroup.builder().name("Group A").description("Premium customers").build());
                groupRepository.save(CustomerGroup.builder().name("Group B").description("Regular customers").build());
                groupRepository.save(CustomerGroup.builder().name("Group C").description("New customers").build());
            }

            // Create admin user if not exist
            if (!userRepository.existsByEmail("admin@cycleproject.com")) {
                User admin = User.builder()
                        .email("admin@cycleproject.com")
                        .password(passwordEncoder.encode("admin123"))
                        .businessName("Cycle Project Admin")
                        .ownerName("Admin")
                        .phone("9999999999")
                        .role(User.Role.ADMIN)
                        .approvalStatus(User.ApprovalStatus.APPROVED)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
