package com.example.Employee_manager.controller;

import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.entity.User;
import com.example.Employee_manager.service.EmployeeService;
import com.example.Employee_manager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile Management", description = "APIs for managing user profiles and avatars")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @GetMapping("/current-user")
    @Operation(summary = "Get current user profile information", 
               description = "Returns the profile information of the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get user information
            Optional<User> userOpt = userService.getUserByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Get employee information
            Optional<Employee> employeeOpt = employeeService.findByEmail(username);
            Employee employee = employeeOpt.orElse(null);
            
            Map<String, Object> profileData = new HashMap<>();
            
            if (employee != null) {
                // User has employee record
                profileData.put("id", employee.getId());
                profileData.put("name", employee.getName());
                profileData.put("surname", employee.getSurname());
                profileData.put("email", employee.getEmail());
                profileData.put("role", employee.getRole());
                profileData.put("employeeNumber", employee.getEmployeeNumber());
                profileData.put("gravatarUrl", employee.getGravatarUrl());
                
                // Department information
                if (employee.getDepartment() != null) {
                    profileData.put("departmentName", employee.getDepartment().getName());
                } else {
                    profileData.put("departmentName", null);
                }
                
                // Manager information
                if (employee.getManager() != null) {
                    profileData.put("managerName", employee.getManager().getName() + " " + employee.getManager().getSurname());
                } else {
                    profileData.put("managerName", null);
                }
            } else {
                // User without employee record
                profileData.put("id", user.getId());
                profileData.put("name", user.getUsername().split("@")[0]);
                profileData.put("surname", "User");
                profileData.put("email", user.getUsername());
                profileData.put("role", user.getRole());
                profileData.put("employeeNumber", "USR-" + user.getId());
                profileData.put("departmentName", null);
                profileData.put("managerName", null);
                
                // Generate Gravatar URL for user
                profileData.put("gravatarUrl", generateGravatarUrl(user.getUsername()));
            }
            
            return ResponseEntity.ok(profileData);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload-profile-picture")
    @Operation(summary = "Upload profile picture for current user", 
               description = "Uploads a profile picture for the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, String>> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be an image"));
            }
            
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be less than 5MB"));
            }
            
            // Find employee by email
            Optional<Employee> employeeOpt = employeeService.findByEmail(username);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Employee employee = employeeOpt.get();
            
            // Upload file using employee service
            var result = employeeService.uploadProfilePicture(employee.getId(), file);
            String fileName = result.getProfilePicturePath();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile picture uploaded successfully");
            response.put("fileName", fileName);
            response.put("gravatarUrl", result.getGravatarUrl());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload profile picture"));
        }
    }

    private String generateGravatarUrl(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "https://www.gravatar.com/avatar/placeholder?s=200&d=identicon";
        }
        
        try {
            String emailLower = email.trim().toLowerCase();
            byte[] emailBytes = emailLower.getBytes();

            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(emailBytes);

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return "https://www.gravatar.com/avatar/" + hexString.toString() + "?s=200&d=identicon";
        } catch (java.security.NoSuchAlgorithmException e) {
            return "https://www.gravatar.com/avatar/placeholder?s=200&d=identicon";
        }
    }
}
