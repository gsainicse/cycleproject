package com.cycleproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String businessName;
    private String ownerName;
    @NotBlank
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstNumber;
}
