package com.cycleproject.service;

import com.cycleproject.dto.ApiResponse;
import com.cycleproject.entity.CustomerGroup;
import com.cycleproject.entity.User;
import com.cycleproject.repository.CustomerGroupRepository;
import com.cycleproject.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerGroupRepository groupRepository;

    public UserService(UserRepository userRepository, CustomerGroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    public List<User> getPendingUsers() {
        return userRepository.findByApprovalStatus(User.ApprovalStatus.PENDING);
    }

    public List<User> getApprovedCustomers() {
        return userRepository.findByApprovalStatusAndRole(User.ApprovalStatus.APPROVED, User.Role.CUSTOMER);
    }

    public ApiResponse approveUser(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ApiResponse(false, "User not found");

        CustomerGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) return new ApiResponse(false, "Customer group not found");

        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        user.setCustomerGroup(group);
        userRepository.save(user);

        return new ApiResponse(true, "User approved successfully");
    }

    public ApiResponse rejectUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ApiResponse(false, "User not found");

        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        userRepository.save(user);

        return new ApiResponse(true, "User rejected");
    }

    public ApiResponse changeCustomerGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new ApiResponse(false, "User not found");

        CustomerGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) return new ApiResponse(false, "Customer group not found");

        user.setCustomerGroup(group);
        userRepository.save(user);

        return new ApiResponse(true, "Customer group updated successfully");
    }

    public List<User> getCustomersByGroup(Long groupId) {
        return userRepository.findByCustomerGroupId(groupId);
    }
}
