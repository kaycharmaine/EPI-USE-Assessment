package com.example.Employee_manager.controller;

import com.example.Employee_manager.service.DepartmentService;
import com.example.Employee_manager.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/test")
    public String test() {
        return "Spring Boot is working! " + System.currentTimeMillis();
    }

    @GetMapping("/test-department-counts")
    public String testDepartmentCounts() {
        StringBuilder result = new StringBuilder();
        result.append("Department Employee Counts:\n");
        
        List<com.example.Employee_manager.dto.DepartmentDTO> departments = departmentService.getAllDepartments();
        for (com.example.Employee_manager.dto.DepartmentDTO dept : departments) {
            result.append(String.format("- %s: %d employees\n", dept.getName(), dept.getEmployeeCount()));
        }
        
        return result.toString();
    }

    @GetMapping("/ensure-manager-assignments")
    public String ensureManagerAssignments() {
        try {
            employeeService.ensureManagerAssignments();
            return "Manager assignments updated successfully! All managers now have CEO as their manager and are assigned to their departments.";
        } catch (Exception e) {
            return "Error updating manager assignments: " + e.getMessage();
        }
    }

    @GetMapping("/debug-managers")
    public String debugManagers() {
        StringBuilder result = new StringBuilder();
        result.append("=== DEBUG MANAGERS ===\n\n");
        
        // Show all employees
        result.append("All Employees:\n");
        List<com.example.Employee_manager.dto.EmployeeDTO> allEmployees = employeeService.getAllEmployees();
        for (com.example.Employee_manager.dto.EmployeeDTO emp : allEmployees) {
            result.append(String.format("- %s %s (%s) - Role: %s\n", 
                emp.getName(), emp.getSurname(), emp.getEmployeeNumber(), emp.getRole()));
        }
        
        result.append(String.format("\nTotal employees: %d\n\n", allEmployees.size()));
        
        // Show available managers
        result.append("Available Managers:\n");
        try {
            List<com.example.Employee_manager.entity.Employee> availableManagers = departmentService.getAvailableManagers();
            for (com.example.Employee_manager.entity.Employee manager : availableManagers) {
                result.append(String.format("- %s %s (%s) - Role: %s\n", 
                    manager.getName(), manager.getSurname(), manager.getEmployeeNumber(), manager.getRole()));
            }
            result.append(String.format("\nTotal available managers: %d\n", availableManagers.size()));
        } catch (Exception e) {
            result.append("ERROR getting available managers: " + e.getMessage() + "\n");
        }
        
        return result.toString();
    }
}