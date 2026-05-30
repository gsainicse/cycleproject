package com.cycleproject.b2b.models;

public class RegisterRequest {
    private String email;
    private String password;
    private String businessName;
    private String ownerName;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String gstNumber;

    public RegisterRequest(String email, String password, String businessName, String ownerName,
                           String phone, String address, String city, String state,
                           String pincode, String gstNumber) {
        this.email = email;
        this.password = password;
        this.businessName = businessName;
        this.ownerName = ownerName;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.gstNumber = gstNumber;
    }

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getBusinessName() { return businessName; }
    public String getOwnerName() { return ownerName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getGstNumber() { return gstNumber; }
}
