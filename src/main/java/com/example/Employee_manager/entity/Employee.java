package com.example.Employee_manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name is mandatory")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Surname is mandatory")
    @Column(nullable = false)
    private String surname;

    @NotNull(message = "Birthdate is mandatory")
    @Past(message = "Birthdate cannot have passed current date")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @NotBlank(message = " Employee number is mandatory")
    @Column(nullable = false, unique = true)
    private String employeeNumber;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    @Column(nullable = false)
    private Double salary;

    @NotBlank(message = "Role is mandatory")
    @Column(nullable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(unique = true)
    private String email;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    public Employee() {

    }

    public Employee(long id, String name, String surname, LocalDate birthDate, String employeeNumber,
                    Double salary, String role, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.employeeNumber = employeeNumber;
        this.salary = salary;
        this.role = role;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birtDate) {
        this.birthDate = birtDate;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    // Helper method for Gravatar
//    public String getGravatarUrl() {
//        if (email == null || email.trim().isEmpty()) {
//            return null;
//        }
//        try {
//            String emailLower = email.trim().toLowerCase();
//            byte[] emailBytes = emailLower.getBytes();
//
//            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
//            byte[] hashBytes = md.digest(emailBytes);
//
//            // Convert byte array to hex string
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hashBytes) {
//                String hex = Integer.toHexString(0xff & b);
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//
//            return "https://www.gravatar.com/avatar/" + hexString.toString() + "?s=200&d=identicon";
//        } catch (java.security.NoSuchAlgorithmException e) {
//            // Fallback if MD5 is not available (shouldn't happen in standard Java)
//            return null;
//        }
//    }

    public String getGravatarUrl() {
        // If user has uploaded a profile picture, use that instead of Gravatar
        if (profilePicturePath != null && !profilePicturePath.trim().isEmpty()) {
            return "/uploads/profile-pictures/" + profilePicturePath;
        }
        
        // Otherwise, use Gravatar
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
            // Fallback if MD5 is not available (shouldn't happen in standard Java)
            return "https://www.gravatar.com/avatar/placeholder?s=200&d=identicon";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) &&
                Objects.equals(employeeNumber, employee.employeeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, employeeNumber);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", employeeNumber='" + employeeNumber + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}