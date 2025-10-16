package com.example.Employee_manager.service;

import com.example.Employee_manager.dto.EmployeeDTO;
import com.example.Employee_manager.dto.EmployeeRequest;
import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.entity.Department;
import com.example.Employee_manager.repository.EmployeeRepository;
import com.example.Employee_manager.repository.DepartmentRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Convert Entity to DTO
    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setName(employee.getName());
        dto.setSurname(employee.getSurname());
        dto.setBirthDate(employee.getBirthDate());
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setSalary(employee.getSalary());
        dto.setRole(employee.getRole());
        dto.setEmail(employee.getEmail());
        dto.setGravatarUrl(employee.getGravatarUrl());
        dto.setProfilePicturePath(employee.getProfilePicturePath());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getName() + " " + employee.getManager().getSurname());
        }

        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        return dto;
    }

    // Convert Request to Entity
    private Employee convertToEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setSurname(request.getSurname());
        employee.setBirthDate(request.getBirthDate());
        employee.setEmployeeNumber(request.getEmployeeNumber());
        employee.setSalary(request.getSalary());
        employee.setRole(request.getRole());
        employee.setEmail(request.getEmail());
        
        // Set department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }
        
        return employee;
    }

    // Get all employees
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAllWithManagers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employee by ID
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return convertToDTO(employee);
    }

    // Create new employee
    public EmployeeDTO createEmployee(EmployeeRequest request) {
        // Check if employee number already exists
        if (employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new RuntimeException("Employee number already exists: " + request.getEmployeeNumber());
        }

        Employee employee = convertToEntity(request);

        // Set manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

            // Prevent self-management
            if (request.getManagerId().equals(employee.getId())) {
                throw new RuntimeException("Employee cannot be their own manager");
            }

            employee.setManager(manager);
        } else if (request.getDepartmentId() != null) {
            // Auto-assign department manager if no manager is specified
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
            
            if (department.getManager() != null) {
                employee.setManager(department.getManager());
            }
        } else {
            // Auto-assign CEO as default manager for employees with manager roles
            if (isManagerRole(employee.getRole())) {
                Employee ceo = findCEO();
                if (ceo != null) {
                    employee.setManager(ceo);
                }
            }
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    // Update employee
    public EmployeeDTO updateEmployee(Long id, EmployeeRequest request) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Check if employee number is being changed and if it already exists
        if (!existingEmployee.getEmployeeNumber().equals(request.getEmployeeNumber()) &&
                employeeRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new RuntimeException("Employee number already exists: " + request.getEmployeeNumber());
        }

        // Update fields
        existingEmployee.setName(request.getName());
        existingEmployee.setSurname(request.getSurname());
        existingEmployee.setBirthDate(request.getBirthDate());
        existingEmployee.setEmployeeNumber(request.getEmployeeNumber());
        existingEmployee.setSalary(request.getSalary());
        existingEmployee.setRole(request.getRole());
        existingEmployee.setEmail(request.getEmail());

        // Update department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
            existingEmployee.setDepartment(department);
        } else {
            existingEmployee.setDepartment(null);
        }

        // Update manager
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

            // Prevent self-management
            if (request.getManagerId().equals(id)) {
                throw new RuntimeException("Employee cannot be their own manager");
            }

            existingEmployee.setManager(manager);
        } else if (request.getDepartmentId() != null) {
            // Auto-assign department manager if no manager is specified
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));
            
            if (department.getManager() != null) {
                existingEmployee.setManager(department.getManager());
            } else {
                // Auto-assign CEO as default manager for employees with manager roles
                if (isManagerRole(existingEmployee.getRole())) {
                    Employee ceo = findCEO();
                    if (ceo != null) {
                        existingEmployee.setManager(ceo);
                    } else {
                        existingEmployee.setManager(null);
                    }
                } else {
                    existingEmployee.setManager(null);
                }
            }
        } else {
            // Auto-assign CEO as default manager for employees with manager roles
            if (isManagerRole(existingEmployee.getRole())) {
                Employee ceo = findCEO();
                if (ceo != null) {
                    existingEmployee.setManager(ceo);
                } else {
                    existingEmployee.setManager(null);
                }
            } else {
                existingEmployee.setManager(null);
            }
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return convertToDTO(updatedEmployee);
    }

    // Delete employee
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Check if this employee is a manager for other employees
        List<Employee> subordinates = employeeRepository.findByManagerId(id);
        if (!subordinates.isEmpty()) {
            throw new RuntimeException("Cannot delete employee who is a manager. Reassign subordinates first.");
        }

        employeeRepository.delete(employee);
    }

    // Search employees by name or surname
    public List<EmployeeDTO> searchEmployees(String searchTerm) {
        return employeeRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(searchTerm, searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get organization hierarchy
    public List<EmployeeDTO> getOrganizationHierarchy() {
        // Get all employees with their managers
        return employeeRepository.findAllWithManagers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employees without manager (potential CEOs)
    public List<EmployeeDTO> getEmployeesWithoutManager() {
        return employeeRepository.findByManagerIsNull()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get subordinates of a manager
    public List<EmployeeDTO> getSubordinates(Long managerId) {
        return employeeRepository.findByManagerId(managerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employees for a specific user based on their role
    public List<EmployeeDTO> getEmployeesForUser(String username, String role) {
        System.out.println("getEmployeesForUser called with username: " + username + ", role: " + role);
        
        if ("ROLE_ADMIN".equals(role)) {
            // Admin can see all employees except themselves
            return getAllEmployees().stream()
                    .filter(emp -> !emp.getEmail().equals(username + "@company.com")) // Filter out admin's own record
                    .collect(Collectors.toList());
        } else if ("ROLE_MANAGER".equals(role)) {
            // Manager can only see employees in their department
            // First, find the manager's employee record by exact email match
            System.out.println("Looking for manager employee with email: " + username);
            
            Employee managerEmployee = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getEmail() != null && emp.getEmail().equals(username))
                    .findFirst()
                    .orElse(null);
            
            System.out.println("Found manager employee: " + (managerEmployee != null ? managerEmployee.getName() + " " + managerEmployee.getSurname() + " (ID: " + managerEmployee.getId() + ")" : "null"));
            
            if (managerEmployee != null && managerEmployee.getDepartment() != null) {
                // Get all employees in the manager's department
                List<EmployeeDTO> departmentEmployees = employeeRepository.findAll().stream()
                        .filter(emp -> emp.getDepartment() != null && 
                                emp.getDepartment().getId().equals(managerEmployee.getDepartment().getId()))
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                
                System.out.println("Found " + departmentEmployees.size() + " employees in department " + managerEmployee.getDepartment().getName());
                return departmentEmployees;
            }
            return new ArrayList<>();
        } else {
            // Regular users see no employees (or only their own record)
            return new ArrayList<>();
        }
    }

    // Get employee by username (for finding manager's own record)
    public EmployeeDTO getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findAll().stream()
                .filter(emp -> emp.getEmail() != null && emp.getEmail().equals(username))
                .findFirst()
                .orElse(null);
        
        return employee != null ? convertToDTO(employee) : null;
    }

    // Upload profile picture
    public EmployeeDTO uploadProfilePicture(Long id, MultipartFile file) throws Exception {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Delete old profile picture if exists
        if (employee.getProfilePicturePath() != null) {
            fileUploadService.deleteProfilePicture(employee.getProfilePicturePath());
        }

        // Upload new profile picture
        String filename = fileUploadService.uploadProfilePicture(file, id);
        employee.setProfilePicturePath(filename);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    // Delete profile picture
    public EmployeeDTO deleteProfilePicture(Long id) throws Exception {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        if (employee.getProfilePicturePath() != null) {
            fileUploadService.deleteProfilePicture(employee.getProfilePicturePath());
            employee.setProfilePicturePath(null);
            Employee savedEmployee = employeeRepository.save(employee);
            return convertToDTO(savedEmployee);
        }

        return convertToDTO(employee);
    }

    // Advanced search with filters
    public List<EmployeeDTO> searchEmployeesWithFilters(String searchTerm, String role, 
                                                       Double minSalary, Double maxSalary, 
                                                       Boolean hasManager) {
        return employeeRepository.findEmployeesWithFilters(searchTerm, role, minSalary, maxSalary, hasManager)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employees by role
    public List<EmployeeDTO> getEmployeesByRole(String role) {
        return employeeRepository.findByRoleContainingIgnoreCase(role)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employees by salary range
    public List<EmployeeDTO> getEmployeesBySalaryRange(Double minSalary, Double maxSalary) {
        return employeeRepository.findBySalaryBetween(minSalary, maxSalary)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all unique roles
    public List<String> getAllRoles() {
        return employeeRepository.findAll()
                .stream()
                .map(Employee::getRole)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // Get employees by department
    public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get employees by department name
    public List<EmployeeDTO> getEmployeesByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartmentName(departmentName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get salary statistics
    public java.util.Map<String, Object> getSalaryStatistics() {
        List<Employee> employees = employeeRepository.findAll();
        
        if (employees.isEmpty()) {
            return java.util.Map.of(
                "min", 0.0,
                "max", 0.0,
                "avg", 0.0,
                "count", 0
            );
        }

        double minSalary = employees.stream().mapToDouble(Employee::getSalary).min().orElse(0.0);
        double maxSalary = employees.stream().mapToDouble(Employee::getSalary).max().orElse(0.0);
        double avgSalary = employees.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);

        return java.util.Map.of(
            "min", minSalary,
            "max", maxSalary,
            "avg", avgSalary,
            "count", employees.size()
        );
    }

    // Initialize default employees
    public void initializeDefaultEmployees() {
        // Only create default employees if no employees exist
        if (employeeRepository.count() == 0) {
            // Create default CEO Admin employee
            EmployeeRequest adminEmployee = new EmployeeRequest();
            adminEmployee.setName("CEO");
            adminEmployee.setSurname("Admin");
            adminEmployee.setEmail("admin@gmail.com");
            adminEmployee.setEmployeeNumber("ADMIN001");
            adminEmployee.setBirthDate(java.time.LocalDate.of(1985, 1, 1));
            adminEmployee.setSalary(150000.0);
            adminEmployee.setRole("Chief Executive Officer");
            adminEmployee.setManagerId(null); // CEO has no manager
            
            createEmployee(adminEmployee);
        }
    }

    // Get all managers (employees with manager roles)
    public List<EmployeeDTO> getAllManagers() {
        // Get employees with manager-related roles
        return employeeRepository.findAll()
                .stream()
                .filter(emp -> emp.getRole() != null && 
                        (emp.getRole().toLowerCase().contains("manager") || 
                         emp.getRole().toLowerCase().contains("lead") ||
                         emp.getRole().toLowerCase().contains("director") ||
                         emp.getRole().toLowerCase().contains("supervisor")))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // Find employee by email
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    // Check if a role is a manager role
    private boolean isManagerRole(String role) {
        if (role == null) return false;
        String lowerRole = role.toLowerCase();
        return lowerRole.contains("manager") || 
               lowerRole.contains("lead") || 
               lowerRole.contains("director") || 
               lowerRole.contains("supervisor") ||
               lowerRole.contains("chief") ||
               lowerRole.contains("head");
    }

    // Find the CEO in the system
    private Employee findCEO() {
        return employeeRepository.findAll()
                .stream()
                .filter(emp -> emp.getRole() != null && 
                        (emp.getRole().toLowerCase().contains("ceo") || 
                         emp.getRole().toLowerCase().contains("chief executive officer")))
                .findFirst()
                .orElse(null);
    }

    // Ensure all managers have CEO as their manager and are assigned to departments
    public void ensureManagerAssignments() {
        Employee ceo = findCEO();
        if (ceo == null) {
            return; // No CEO found, cannot assign managers
        }

        List<Employee> allEmployees = employeeRepository.findAll();
        
        for (Employee employee : allEmployees) {
            boolean needsUpdate = false;
            
            // Check if employee has a manager role
            if (isManagerRole(employee.getRole())) {
                // Ensure CEO is assigned as manager
                if (employee.getManager() == null || !Long.valueOf(employee.getManager().getId()).equals(ceo.getId())) {
                    employee.setManager(ceo);
                    needsUpdate = true;
                }
                
                // If this manager is assigned to a department, ensure they are in that department
                if (employee.getDepartment() == null) {
                    // Check if this employee is managing a department
                    Optional<Department> managedDepartment = departmentRepository.findByManagerId(employee.getId());
                    if (managedDepartment.isPresent()) {
                        employee.setDepartment(managedDepartment.get());
                        needsUpdate = true;
                    }
                }
            }
            
            if (needsUpdate) {
                employeeRepository.save(employee);
            }
        }
    }
}