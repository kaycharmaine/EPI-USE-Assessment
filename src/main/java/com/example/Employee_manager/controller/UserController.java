package com.example.Employee_manager.controller;

import com.example.Employee_manager.dto.EmployeeUserDTO;
import com.example.Employee_manager.entity.User;
import com.example.Employee_manager.service.EmployeeUserService;
import com.example.Employee_manager.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing system users (Admin only)")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeUserService employeeUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users (Admin only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to retrieve")
            @PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user", description = "Create a new user account (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<User> createUser(
            @Parameter(description = "User data to create")
            @RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        String role = userData.get("role");

        if (username == null || password == null || role == null) {
            return ResponseEntity.badRequest().build();
        }

        User createdUser = userService.createUser(username, password, role);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update an existing user's information (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update")
            @PathVariable Long id,
            @Parameter(description = "Updated user data")
            @RequestBody Map<String, Object> userData) {
        String username = (String) userData.get("username");
        String password = (String) userData.get("password");
        String role = (String) userData.get("role");
        Boolean enabled = (Boolean) userData.get("enabled");

        if (username == null || role == null) {
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = userService.updateUser(id, username, password, role, enabled != null ? enabled : true);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user by ID (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Cannot delete the last admin")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get users by role", description = "Get all users with a specific role (Admin only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    public ResponseEntity<List<User>> getUsersByRole(
            @Parameter(description = "Role to filter by")
            @PathVariable String role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initialize default users", description = "Create default users if none exist (Admin only)")
    @ApiResponse(responseCode = "200", description = "Default users initialized successfully")
    public ResponseEntity<String> initializeDefaultUsers() {
        userService.initializeDefaultUsers();
        return ResponseEntity.ok("Default users initialized successfully");
    }

    @PostMapping("/create-employee-user")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create employee with user account", description = "Create a new employee and associated user account (Admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee and user created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists")
    })
    public ResponseEntity<EmployeeUserDTO> createEmployeeWithUser(@Valid @RequestBody EmployeeUserDTO employeeUserDTO) {
        EmployeeUserDTO createdEmployeeUser = employeeUserService.createEmployeeWithUser(employeeUserDTO);
        return ResponseEntity.ok(createdEmployeeUser);
    }

    @GetMapping("/check-employee-user/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if employee has user account", description = "Check if an employee email already has a user account (Admin only)")
    @ApiResponse(responseCode = "200", description = "Check completed successfully")
    public ResponseEntity<Boolean> checkEmployeeHasUser(@PathVariable String email) {
        boolean hasUser = employeeUserService.employeeHasUserAccount(email);
        return ResponseEntity.ok(hasUser);
    }

    @GetMapping("/get-user-by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by email", description = "Get user account by email address (Admin only)")
    @ApiResponse(responseCode = "200", description = "User found")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/with-employee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user with employee data", description = "Get user account with associated employee data (Admin only)")
    @ApiResponse(responseCode = "200", description = "User with employee data found")
    public ResponseEntity<EmployeeUserDTO> getUserWithEmployee(@PathVariable Long id) {
        EmployeeUserDTO userWithEmployee = employeeUserService.getUserWithEmployee(id);
        return ResponseEntity.ok(userWithEmployee);
    }

    @PutMapping("/{id}/with-employee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user with employee data", description = "Update both user account and employee data (Admin only)")
    @ApiResponse(responseCode = "200", description = "User and employee updated successfully")
    public ResponseEntity<EmployeeUserDTO> updateUserWithEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUserDTO employeeUserDTO) {
        EmployeeUserDTO updatedUserWithEmployee = employeeUserService.updateUserWithEmployee(id, employeeUserDTO);
        return ResponseEntity.ok(updatedUserWithEmployee);
    }
}
