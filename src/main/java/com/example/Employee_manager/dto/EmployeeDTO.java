package com.example.Employee_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Employee Data Transfer Object")
public class EmployeeDTO {

    @Schema(description = "Employee ID", example = "1")
    private Long id;

    @Schema(description = "First name", example = "John")
    private String name;

    @Schema(description = "Last name", example = "Doe")
    private String surname;

    @Schema(description = "Birth date", example = "1990-01-15")
    private LocalDate birthDate;

    @Schema(description = "Employee number", example = "EMP001")
    private String employeeNumber;

    @Schema(description = "Salary", example = "75000.0")
    private Double salary;

    @Schema(description = "Role/Position", example = "Software Developer")
    private String role;

    @Schema(description = "Manager ID", example = "1")
    private Long managerId;

    @Schema(description = "Manager name", example = "Sarah Johnson")
    private String managerName;

    @Schema(description = "Email for Gravatar", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Gravatar URL", example = "https://www.gravatar.com/avatar/abc123...")
    private String gravatarUrl;

    @Schema(description = "Profile picture path", example = "employee_123.jpg")
    private String profilePicturePath;

    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    public EmployeeDTO() {

    }

    public EmployeeDTO(Long id, String name, String surname, LocalDate birthDate, String employeeNumber,
                       Double salary, String role, Long managerId, String managerName, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.employeeNumber = employeeNumber;
        this.salary = salary;
        this.role = role;
        this.managerId = managerId;
        this.managerName = managerName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public void setGravatarUrl(String gravatarUrl) {
        this.gravatarUrl = gravatarUrl;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
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
}