package com.example.Employee_manager.service;

import com.example.Employee_manager.dto.EmployeeUserDTO;
import com.example.Employee_manager.dto.EmployeeRequest;
import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EmployeeUserService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    public EmployeeUserDTO createEmployeeWithUser(EmployeeUserDTO employeeUserDTO) {
        // Check if employee email already has a user account
        if (userService.employeeHasUser(employeeUserDTO.getEmail())) {
            throw new RuntimeException("Employee with email " + employeeUserDTO.getEmail() + " already has a user account");
        }

        // Create EmployeeRequest
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setName(employeeUserDTO.getName());
        employeeRequest.setSurname(employeeUserDTO.getSurname());
        employeeRequest.setBirthDate(employeeUserDTO.getBirthDate());
        employeeRequest.setEmployeeNumber(employeeUserDTO.getEmployeeNumber());
        employeeRequest.setSalary(employeeUserDTO.getSalary());
        employeeRequest.setRole(employeeUserDTO.getRole());
        employeeRequest.setEmail(employeeUserDTO.getEmail());
        employeeRequest.setManagerId(employeeUserDTO.getManagerId());

        // Save employee
        var savedEmployeeDTO = employeeService.createEmployee(employeeRequest);

        // Create User entity
        User user = new User();
        user.setUsername(employeeUserDTO.getEmail()); // Always use email as username
        user.setPassword(employeeUserDTO.getPassword());
        user.setRole(employeeUserDTO.getUserRole());
        user.setEnabled(employeeUserDTO.getEnabled() != null ? employeeUserDTO.getEnabled() : true);

        // Save user
        User savedUser = userService.createUserWithEmployee(user, null);

        // Return the combined DTO
        EmployeeUserDTO result = new EmployeeUserDTO();
        result.setName(savedEmployeeDTO.getName());
        result.setSurname(savedEmployeeDTO.getSurname());
        result.setBirthDate(savedEmployeeDTO.getBirthDate());
        result.setEmployeeNumber(savedEmployeeDTO.getEmployeeNumber());
        result.setSalary(savedEmployeeDTO.getSalary());
        result.setRole(savedEmployeeDTO.getRole());
        result.setEmail(savedEmployeeDTO.getEmail());
        result.setManagerId(savedEmployeeDTO.getManagerId());
        result.setUsername(savedUser.getUsername());
        result.setUserRole(savedUser.getRole());
        result.setEnabled(savedUser.isEnabled());

        return result;
    }

    public boolean employeeHasUserAccount(String email) {
        return userService.employeeHasUser(email);
    }

    public EmployeeUserDTO getUserWithEmployee(Long userId) {
        try {
            // Get user by ID
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Get employee by email (username)
            var employeeDTO = employeeService.getEmployeeByUsername(user.getUsername());
            if (employeeDTO == null) {
                throw new RuntimeException("No employee found for user: " + user.getUsername());
            }

            // Create combined DTO
            EmployeeUserDTO result = new EmployeeUserDTO();
            result.setName(employeeDTO.getName());
            result.setSurname(employeeDTO.getSurname());
            result.setBirthDate(employeeDTO.getBirthDate());
            result.setEmployeeNumber(employeeDTO.getEmployeeNumber());
            result.setSalary(employeeDTO.getSalary());
            result.setRole(employeeDTO.getRole());
            result.setEmail(employeeDTO.getEmail());
            result.setManagerId(employeeDTO.getManagerId());
            result.setUsername(user.getUsername());
            result.setUserRole(user.getRole());
            result.setEnabled(user.isEnabled());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error getting user with employee data: " + e.getMessage(), e);
        }
    }

    public EmployeeUserDTO updateUserWithEmployee(Long userId, EmployeeUserDTO employeeUserDTO) {
        // Get existing user
        User existingUser = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Find existing employee
        var existingEmployeeDTO = employeeService.getEmployeeByUsername(existingUser.getUsername());
        
        if (existingEmployeeDTO == null) {
            throw new RuntimeException("No employee found for user: " + existingUser.getUsername());
        }

        // Update employee
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setName(employeeUserDTO.getName());
        employeeRequest.setSurname(employeeUserDTO.getSurname());
        employeeRequest.setBirthDate(employeeUserDTO.getBirthDate());
        employeeRequest.setEmployeeNumber(employeeUserDTO.getEmployeeNumber());
        employeeRequest.setSalary(employeeUserDTO.getSalary());
        employeeRequest.setRole(employeeUserDTO.getRole());
        employeeRequest.setEmail(employeeUserDTO.getEmail());
        employeeRequest.setManagerId(employeeUserDTO.getManagerId());

        // Update employee
        var updatedEmployeeDTO = employeeService.updateEmployee(existingEmployeeDTO.getId(), employeeRequest);

        // Update user
        User updatedUser = userService.updateUser(
                userId,
                employeeUserDTO.getEmail(), // Always use email as username
                employeeUserDTO.getPassword(),
                employeeUserDTO.getUserRole(),
                employeeUserDTO.getEnabled() != null ? employeeUserDTO.getEnabled() : true
        );

        // Return combined DTO
        EmployeeUserDTO result = new EmployeeUserDTO();
        result.setName(updatedEmployeeDTO.getName());
        result.setSurname(updatedEmployeeDTO.getSurname());
        result.setBirthDate(updatedEmployeeDTO.getBirthDate());
        result.setEmployeeNumber(updatedEmployeeDTO.getEmployeeNumber());
        result.setSalary(updatedEmployeeDTO.getSalary());
        result.setRole(updatedEmployeeDTO.getRole());
        result.setEmail(updatedEmployeeDTO.getEmail());
        result.setManagerId(updatedEmployeeDTO.getManagerId());
        result.setUsername(updatedUser.getUsername());
        result.setUserRole(updatedUser.getRole());
        result.setEnabled(updatedUser.isEnabled());

        return result;
    }
}
