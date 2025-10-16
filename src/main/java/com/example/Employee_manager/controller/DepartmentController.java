package com.example.Employee_manager.controller;

import com.example.Employee_manager.dto.DepartmentDTO;
import com.example.Employee_manager.dto.DepartmentRequest;
import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.repository.EmployeeRepository;
import com.example.Employee_manager.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Department Management", description = "APIs for managing departments and department managers")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieve a list of all departments with their managers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Retrieve a specific department by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department found"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDTO> getDepartmentById(
            @Parameter(description = "ID of the department to retrieve")
            @PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    @PostMapping
    @Operation(summary = "Create new department", description = "Create a new department record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or business rule violation")
    })
    public ResponseEntity<DepartmentDTO> createDepartment(
            @Parameter(description = "Department data to create")
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentDTO createdDepartment = departmentService.createDepartment(request);
        return ResponseEntity.ok(createdDepartment);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Update an existing department's information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @Parameter(description = "ID of the department to update")
            @PathVariable Long id,
            @Parameter(description = "Updated department data")
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentDTO updatedDepartment = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Delete a department by ID (only if no employees are assigned)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete department with assigned employees")
    })
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department to delete")
            @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/without-managers")
    @Operation(summary = "Get departments without managers", description = "Get all departments that don't have assigned managers")
    @ApiResponse(responseCode = "200", description = "Departments without managers retrieved successfully")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsWithoutManagers() {
        List<DepartmentDTO> departments = departmentService.getDepartmentsWithoutManagers();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/with-managers")
    @Operation(summary = "Get departments with managers", description = "Get all departments that have assigned managers")
    @ApiResponse(responseCode = "200", description = "Departments with managers retrieved successfully")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsWithManagers() {
        List<DepartmentDTO> departments = departmentService.getDepartmentsWithManagers();
        return ResponseEntity.ok(departments);
    }

    @PutMapping("/{departmentId}/assign-manager/{managerId}")
    @Operation(summary = "Assign manager to department", description = "Assign a manager to a department (manager must have ROLE_MANAGER)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid manager or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Department or manager not found")
    })
    public ResponseEntity<DepartmentDTO> assignManagerToDepartment(
            @Parameter(description = "ID of the department")
            @PathVariable Long departmentId,
            @Parameter(description = "ID of the manager to assign")
            @PathVariable Long managerId) {
        DepartmentDTO updatedDepartment = departmentService.assignManagerToDepartment(departmentId, managerId);
        return ResponseEntity.ok(updatedDepartment);
    }

    @PutMapping("/{departmentId}/remove-manager")
    @Operation(summary = "Remove manager from department", description = "Remove the current manager from a department")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager removed successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentDTO> removeManagerFromDepartment(
            @Parameter(description = "ID of the department")
            @PathVariable Long departmentId) {
        DepartmentDTO updatedDepartment = departmentService.removeManagerFromDepartment(departmentId);
        return ResponseEntity.ok(updatedDepartment);
    }

    @GetMapping("/manager/{managerId}")
    @Operation(summary = "Get department by manager ID", description = "Get the department managed by a specific manager")
    @ApiResponse(responseCode = "200", description = "Department retrieved successfully")
    public ResponseEntity<DepartmentDTO> getDepartmentByManagerId(
            @Parameter(description = "ID of the manager")
            @PathVariable Long managerId) {
        DepartmentDTO department = departmentService.getDepartmentByManagerId(managerId);
        return ResponseEntity.ok(department);
    }

    @GetMapping("/available-managers")
    @Operation(summary = "Get available managers", description = "Get all employees with ROLE_MANAGER who are not already department managers")
    @ApiResponse(responseCode = "200", description = "Available managers retrieved successfully")
    public ResponseEntity<List<Employee>> getAvailableManagers() {
        List<Employee> availableManagers = departmentService.getAvailableManagers();
        return ResponseEntity.ok(availableManagers);
    }

    @GetMapping("/all-managers")
    @Operation(summary = "Get all managers", description = "Get all employees with ROLE_MANAGER (for debugging)")
    @ApiResponse(responseCode = "200", description = "All managers retrieved successfully")
    public ResponseEntity<List<Employee>> getAllManagers() {
        List<Employee> allManagers = departmentService.getAllManagers();
        return ResponseEntity.ok(allManagers);
    }

    @GetMapping("/debug/employees")
    @Operation(summary = "Get all employees", description = "Get all employees for debugging")
    @ApiResponse(responseCode = "200", description = "All employees retrieved successfully")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> allEmployees = employeeRepository.findAll();
        return ResponseEntity.ok(allEmployees);
    }

    @GetMapping("/debug/user-employee-mapping")
    @Operation(summary = "Get user-employee mapping", description = "Get mapping between users and employees for debugging")
    @ApiResponse(responseCode = "200", description = "User-employee mapping retrieved successfully")
    public ResponseEntity<Map<String, Object>> getUserEmployeeMapping() {
        Map<String, Object> result = departmentService.getUserEmployeeMapping();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/debug/managers")
    @Operation(summary = "Debug managers", description = "Debug endpoint to show all available managers")
    @ApiResponse(responseCode = "200", description = "Manager debug info retrieved successfully")
    public ResponseEntity<Map<String, Object>> debugManagers() {
        List<Employee> availableManagers = departmentService.getAvailableManagers();
        List<Employee> allManagers = departmentService.getAllManagers();
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("availableManagers", availableManagers);
        result.put("allManagers", allManagers);
        result.put("availableCount", availableManagers.size());
        result.put("totalCount", allManagers.size());
        
        return ResponseEntity.ok(result);
    }

}
