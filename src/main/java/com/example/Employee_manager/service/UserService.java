package com.example.Employee_manager.service;

import com.example.Employee_manager.dto.UserDTO;
import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.entity.User;
import com.example.Employee_manager.repository.EmployeeRepository;
import com.example.Employee_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(String username, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public User updateUser(Long id, String username, String password, String role, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if username is being changed and if it already exists
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        user.setUsername(username);
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setRole(role);
        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Prevent deletion of the last admin
        if ("ROLE_ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equals(u.getRole()) && u.isEnabled())
                    .count();
            if (adminCount <= 1) {
                throw new RuntimeException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .toList();
    }

    public void initializeDefaultUsers() {
        // Only create default users if no users exist
        if (userRepository.count() == 0) {
            createUser("admin@gmail.com", "admin123", "ROLE_ADMIN");
        }
    }

    // Create user with employee details
    public User createUserWithEmployee(User user, com.example.Employee_manager.entity.Employee employee) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Set employee email as username if not provided
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername(employee.getEmail());
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Check if employee already has a user account
    public boolean employeeHasUser(String email) {
        return userRepository.findByUsername(email).isPresent();
    }

    // Convert User entity to UserDTO with employee information
    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setRole(user.getRole());
        userDTO.setEnabled(user.isEnabled());

        // Try to find corresponding employee record
        Optional<Employee> employeeOpt = employeeRepository.findByEmail(user.getUsername());
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            userDTO.setName(employee.getName());
            userDTO.setSurname(employee.getSurname());
            userDTO.setEmployeeNumber(employee.getEmployeeNumber());
            userDTO.setEmail(employee.getEmail());
            userDTO.setHasEmployeeRecord(true);
            
            // Set manager information
            if (employee.getManager() != null) {
                userDTO.setManagerId(employee.getManager().getId());
                userDTO.setManagerName(employee.getManager().getName() + " " + employee.getManager().getSurname());
            }
            
            // Set department information
            if (employee.getDepartment() != null) {
                userDTO.setDepartmentId(employee.getDepartment().getId());
                userDTO.setDepartmentName(employee.getDepartment().getName());
            }
        } else {
            // User doesn't have employee record
            userDTO.setHasEmployeeRecord(false);
            userDTO.setName(user.getUsername().split("@")[0]); // Use username as display name
            userDTO.setEmail(user.getUsername());
        }

        return userDTO;
    }

    // Get all users with their employee information
    public List<UserDTO> getAllUsersWithEmployeeInfo() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .toList();
    }

    // Get users by role with employee information
    public List<UserDTO> getUsersByRoleWithEmployeeInfo(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .map(this::convertToUserDTO)
                .toList();
    }
}
