package com.example.Employee_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Schema(description = "Employee request object for create/update operations")
public class EmployeeRequest {

    @NotBlank(message = "Name is mandatory")
    @Schema(description = "First name", example = "John", required = true)
    private String name;

    @NotBlank(message = "Surname is mandatory")
    @Schema(description = "Last name", example = "Doe", required = true)
    private String surname;

    @NotNull(message = "Birth date is mandatory")
    @Past(message = "Birth date must be in the past")
    @Schema(description = "Birth date", example = "1990-01-15", required = true)
    private LocalDate birthDate;

    @NotBlank(message = "Employee number is mandatory")
    @Schema(description = "Employee number", example = "EMP001", required = true)
    private String employeeNumber;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    @Schema(description = "Salary", example = "75000.0", required = true)
    private Double salary;

    @NotBlank(message = "Role/Position is mandatory")
    @Schema(description = "Role/Position", example = "Software Developer", required = true)
    private String role;

    @Schema(description = "Manager ID", example = "1")
    private Long managerId;

    @Email(message = "Email should be valid")
    @Schema(description = "Email for Gravatar", example = "john.doe@company.com")
    private String email;

    @Schema(description = "Department ID", example = "1")
    private Long departmentId;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}