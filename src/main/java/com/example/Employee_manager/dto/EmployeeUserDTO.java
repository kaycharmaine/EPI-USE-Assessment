package com.example.Employee_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "Combined Employee and User data for creating new system users")
public class EmployeeUserDTO {

    @Schema(description = "Employee name", example = "John")
    private String name;

    @Schema(description = "Employee surname", example = "Doe")
    private String surname;

    @Schema(description = "Employee birth date", example = "1990-01-15")
    private LocalDate birthDate;

    @Schema(description = "Employee number", example = "12345")
    @NotBlank(message = "Employee number is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Employee number must be exactly 5 digits")
    private String employeeNumber;

    @Schema(description = "Employee salary", example = "50000.00")
    private Double salary;

    @Schema(description = "Employee role/position", example = "Software Developer")
    private String role;

    @Schema(description = "Employee email", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Manager ID", example = "1")
    private Long managerId;

    // User fields
    @Schema(description = "Username for login", example = "john.doe")
    private String username;

    @Schema(description = "Password for login", example = "password123")
    private String password;

    @Schema(description = "User role", example = "ROLE_USER")
    private String userRole;

    @Schema(description = "Whether user account is enabled", example = "true")
    private Boolean enabled = true;

    // Constructors
    public EmployeeUserDTO() {}

    public EmployeeUserDTO(String name, String surname, String email, String username, String password, String userRole) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.userRole = userRole;
    }

    // Getters and Setters
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}


