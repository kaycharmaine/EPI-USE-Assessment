package com.example.Employee_manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Department request object for create/update operations")
public class DepartmentRequest {
    
    @NotBlank(message = "Department name is mandatory")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    @Schema(description = "Department name", example = "Engineering", required = true)
    private String name;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Department description", example = "Software development and engineering team")
    private String description;
    
    @Schema(description = "Manager ID (must have ROLE_MANAGER)", example = "5")
    private Long managerId;
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "DepartmentRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                '}';
    }
}

