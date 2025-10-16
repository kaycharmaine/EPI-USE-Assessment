package com.example.Employee_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Department Data Transfer Object")
public class DepartmentDTO {
    
    @Schema(description = "Department ID", example = "1")
    private Long id;
    
    @Schema(description = "Department name", example = "Engineering")
    private String name;
    
    @Schema(description = "Department description", example = "Software development and engineering team")
    private String description;
    
    @Schema(description = "Manager ID", example = "5")
    private Long managerId;
    
    @Schema(description = "Manager name", example = "John Smith")
    private String managerName;
    
    @Schema(description = "Number of employees in department", example = "15")
    private Long employeeCount;
    
    @Schema(description = "Whether the department is active", example = "true")
    private Boolean isActive;
    
    public DepartmentDTO() {}
    
    public DepartmentDTO(Long id, String name, String description, Long managerId, String managerName, Long employeeCount, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
        this.employeeCount = employeeCount;
        this.isActive = isActive;
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Long getEmployeeCount() {
        return employeeCount;
    }
    
    public void setEmployeeCount(Long employeeCount) {
        this.employeeCount = employeeCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "DepartmentDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", managerName='" + managerName + '\'' +
                ", employeeCount=" + employeeCount +
                ", isActive=" + isActive +
                '}';
    }
}

