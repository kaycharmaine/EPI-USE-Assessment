package com.example.Employee_manager.dto;

import java.util.Objects;

public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private boolean enabled;
    
    // Optional employee information if user has corresponding employee record
    private String name;
    private String surname;
    private String employeeNumber;
    private String email;
    private Long managerId;
    private String managerName;
    private Long departmentId;
    private String departmentName;
    private boolean hasEmployeeRecord;

    // Constructors
    public UserDTO() {}

    public UserDTO(Long id, String username, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public boolean isHasEmployeeRecord() {
        return hasEmployeeRecord;
    }

    public void setHasEmployeeRecord(boolean hasEmployeeRecord) {
        this.hasEmployeeRecord = hasEmployeeRecord;
    }

    // Helper methods
    public String getDisplayName() {
        if (name != null && surname != null && !name.isEmpty() && !surname.isEmpty()) {
            return name + " " + surname;
        }
        return username;
    }

    public String getDisplayRole() {
        if (role != null) {
            switch (role) {
                case "ROLE_ADMIN":
                    return "Admin";
                case "ROLE_MANAGER":
                    return "Manager";
                case "ROLE_USER":
                    return "User";
                default:
                    return role;
            }
        }
        return "Unknown";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id) && Objects.equals(username, userDTO.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", enabled=" + enabled +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", hasEmployeeRecord=" + hasEmployeeRecord +
                '}';
    }
}
