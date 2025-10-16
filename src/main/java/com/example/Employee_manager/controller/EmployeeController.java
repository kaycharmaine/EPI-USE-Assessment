package com.example.Employee_manager.controller;

import com.example.Employee_manager.dto.EmployeeDTO;
import com.example.Employee_manager.dto.EmployeeRequest;
import com.example.Employee_manager.service.EmployeeService;
import com.example.Employee_manager.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employee hierarchy and organizational structure")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieve a list of all employees with their managers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @Parameter(description = "ID of the employee to retrieve")
            @PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PostMapping
    @Operation(summary = "Create new employee", description = "Create a new employee record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Parameter(description = "Employee data to create")
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO createdEmployee = employeeService.createEmployee(request);
        return ResponseEntity.ok(createdEmployee);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee's information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "ID of the employee to update")
            @PathVariable Long id,
            @Parameter(description = "Updated employee data")
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Delete an employee by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete employee who is a manager")
    })
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID of the employee to delete")
            @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees", description = "Search employees by name, surname, role, or employee number")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<EmployeeDTO>> searchEmployees(
            @Parameter(description = "Search term")
            @RequestParam String q) {
        List<EmployeeDTO> employees = employeeService.searchEmployees(q);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/hierarchy")
    @Operation(summary = "Get organization hierarchy", description = "Get the complete organizational hierarchy with manager relationships")
    @ApiResponse(responseCode = "200", description = "Hierarchy retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getOrganizationHierarchy() {
        List<EmployeeDTO> hierarchy = employeeService.getOrganizationHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/no-manager")
    @Operation(summary = "Get employees without manager", description = "Get employees who don't have a manager (e.g., CEO)")
    @ApiResponse(responseCode = "200", description = "Employees without manager retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesWithoutManager() {
        List<EmployeeDTO> employees = employeeService.getEmployeesWithoutManager();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/manager/{managerId}/subordinates")
    @Operation(summary = "Get subordinates", description = "Get all subordinates of a specific manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subordinates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Manager not found")
    })
    public ResponseEntity<List<EmployeeDTO>> getSubordinates(
            @Parameter(description = "ID of the manager")
            @PathVariable Long managerId) {
        List<EmployeeDTO> subordinates = employeeService.getSubordinates(managerId);
        return ResponseEntity.ok(subordinates);
    }

    @PostMapping("/{id}/upload-profile-picture")
    @Operation(summary = "Upload profile picture", description = "Upload a profile picture for an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or employee not found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDTO> uploadProfilePicture(
            @Parameter(description = "ID of the employee")
            @PathVariable Long id,
            @Parameter(description = "Profile picture file")
            @RequestParam("file") MultipartFile file) {
        try {
            EmployeeDTO updatedEmployee = employeeService.uploadProfilePicture(id, file);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/profile-picture")
    @Operation(summary = "Delete profile picture", description = "Delete the profile picture of an employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile picture deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<EmployeeDTO> deleteProfilePicture(
            @Parameter(description = "ID of the employee")
            @PathVariable Long id) {
        try {
            EmployeeDTO updatedEmployee = employeeService.deleteProfilePicture(id);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search/advanced")
    @Operation(summary = "Advanced search employees", description = "Search employees with multiple filters")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<EmployeeDTO>> searchEmployeesAdvanced(
            @Parameter(description = "Search term for name, surname, employee number, role, or email")
            @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Filter by role")
            @RequestParam(required = false) String role,
            @Parameter(description = "Minimum salary")
            @RequestParam(required = false) Double minSalary,
            @Parameter(description = "Maximum salary")
            @RequestParam(required = false) Double maxSalary,
            @Parameter(description = "Filter by manager status (true=has manager, false=no manager)")
            @RequestParam(required = false) Boolean hasManager) {
        List<EmployeeDTO> employees = employeeService.searchEmployeesWithFilters(
                searchTerm, role, minSalary, maxSalary, hasManager);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Get a list of all unique roles in the organization")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = employeeService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/statistics/salary")
    @Operation(summary = "Get salary statistics", description = "Get salary statistics (min, max, average)")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<java.util.Map<String, Object>> getSalaryStatistics() {
        java.util.Map<String, Object> statistics = employeeService.getSalaryStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/filter/role/{role}")
    @Operation(summary = "Get employees by role", description = "Get all employees with a specific role")
    @ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByRole(
            @Parameter(description = "Role to filter by")
            @PathVariable String role) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByRole(role);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/filter/salary")
    @Operation(summary = "Get employees by salary range", description = "Get employees within a salary range")
    @ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesBySalaryRange(
            @Parameter(description = "Minimum salary")
            @RequestParam Double minSalary,
            @Parameter(description = "Maximum salary")
            @RequestParam Double maxSalary) {
        List<EmployeeDTO> employees = employeeService.getEmployeesBySalaryRange(minSalary, maxSalary);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/my-employees")
    @Operation(summary = "Get employees for current user", description = "Get employees based on user role (Admin: all except self, Manager: direct reports only)")
    @ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getMyEmployees(
            @Parameter(description = "Username of the current user")
            @RequestParam String username,
            @Parameter(description = "Role of the current user")
            @RequestParam String role) {
        List<EmployeeDTO> employees = employeeService.getEmployeesForUser(username, role);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/managers")
    @Operation(summary = "Get all managers", description = "Get all employees and users who are managers")
    @ApiResponse(responseCode = "200", description = "Managers retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getAllManagers() {
        List<EmployeeDTO> managers = employeeService.getAllManagers();
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get employees by department", description = "Get all employees in a specific department")
    @ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(
            @Parameter(description = "ID of the department")
            @PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/department/name/{departmentName}")
    @Operation(summary = "Get employees by department name", description = "Get all employees in a department by name")
    @ApiResponse(responseCode = "200", description = "Employees retrieved successfully")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartmentName(
            @Parameter(description = "Name of the department")
            @PathVariable String departmentName) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentName(departmentName);
        return ResponseEntity.ok(employees);
    }
}