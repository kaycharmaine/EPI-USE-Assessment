package com.example.Employee_manager.service;

import com.example.Employee_manager.dto.DepartmentDTO;
import com.example.Employee_manager.dto.DepartmentRequest;
import com.example.Employee_manager.entity.Department;
import com.example.Employee_manager.entity.Employee;
import com.example.Employee_manager.entity.User;
import com.example.Employee_manager.repository.DepartmentRepository;
import com.example.Employee_manager.repository.EmployeeRepository;
import com.example.Employee_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    // Convert Entity to DTO
    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setIsActive(department.getIsActive());
        
        if (department.getManager() != null) {
            dto.setManagerId(department.getManager().getId());
            dto.setManagerName(department.getManager().getName() + " " + department.getManager().getSurname());
        }
        
        // Count employees in this department
        long employeeCount = departmentRepository.countEmployeesInDepartment(department.getId());
        dto.setEmployeeCount(employeeCount);
        
        return dto;
    }

    // Convert Request to Entity
    private Department convertToEntity(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        return department;
    }

    // Get all departments
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAllWithManagers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get department by ID
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return convertToDTO(department);
    }

    // Create new department
    public DepartmentDTO createDepartment(DepartmentRequest request) {
        // Check if department name already exists
        if (departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department name already exists: " + request.getName());
        }

        Department department = convertToEntity(request);

        // Set manager if provided
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

            // Validate that the employee has manager role
            validateManagerRole(manager);

            // Check if this manager is already managing another department
            departmentRepository.findByManagerId(request.getManagerId())
                    .ifPresent(existingDept -> {
                        throw new RuntimeException("Employee " + manager.getName() + " " + manager.getSurname() + 
                                " is already managing department: " + existingDept.getName());
                    });

            department.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    // Update department
    public DepartmentDTO updateDepartment(Long id, DepartmentRequest request) {
        Department existingDepartment = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Check if department name is being changed and if it already exists
        if (!existingDepartment.getName().equals(request.getName()) &&
                departmentRepository.existsByName(request.getName())) {
            throw new RuntimeException("Department name already exists: " + request.getName());
        }

        // Update fields
        existingDepartment.setName(request.getName());
        existingDepartment.setDescription(request.getDescription());

        // Update manager
        if (request.getManagerId() != null) {
            Employee manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + request.getManagerId()));

            // Validate that the employee has manager role
            validateManagerRole(manager);

            // Check if this manager is already managing another department (excluding current department)
            departmentRepository.findByManagerId(request.getManagerId())
                    .ifPresent(existingDept -> {
                        if (!existingDept.getId().equals(id)) {
                            throw new RuntimeException("Employee " + manager.getName() + " " + manager.getSurname() + 
                                    " is already managing department: " + existingDept.getName());
                        }
                    });

            existingDepartment.setManager(manager);
        } else {
            existingDepartment.setManager(null);
        }

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        return convertToDTO(updatedDepartment);
    }

    // Delete department
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        // Check if there are employees assigned to this department
        long employeeCount = departmentRepository.countEmployeesInDepartment(id);
        if (employeeCount > 0) {
            throw new RuntimeException("Cannot delete department with assigned employees. " + 
                    "Please reassign " + employeeCount + " employees to other departments first.");
        }

        departmentRepository.delete(department);
    }

    // Get departments without managers
    public List<DepartmentDTO> getDepartmentsWithoutManagers() {
        return departmentRepository.findDepartmentsWithoutManagers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get departments with managers
    public List<DepartmentDTO> getDepartmentsWithManagers() {
        return departmentRepository.findDepartmentsWithManagers()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Assign manager to department
    public DepartmentDTO assignManagerToDepartment(Long departmentId, Long managerId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));

        Employee manager = employeeRepository.findById(managerId).orElse(null);
        
        // If manager not found in employee table, check if it's a virtual manager (user without employee record)
        if (manager == null) {
            User managerUser = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + managerId));
            
            // Create and save an actual employee record for this manager
            Employee newManager = new Employee();
            newManager.setName(managerUser.getUsername().split("@")[0]);
            newManager.setSurname("User");
            newManager.setEmail(managerUser.getUsername());
            newManager.setEmployeeNumber("USR-" + managerUser.getId());
            newManager.setRole(managerUser.getRole());
            newManager.setDepartment(department);
            
            // Set CEO as default manager for this new manager
            Employee ceo = findCEO();
            if (ceo != null) {
                newManager.setManager(ceo);
            }
            
            manager = employeeRepository.save(newManager);
        } else {
            // If manager exists in employee table, ensure they are assigned to the department
            // and have CEO as their manager if they have a manager role
            manager.setDepartment(department);
            
            // Ensure CEO is assigned as manager for employees with manager roles
            if (isManagerRole(manager.getRole())) {
                Employee ceo = findCEO();
                if (ceo != null && manager.getManager() == null) {
                    manager.setManager(ceo);
                }
            }
            
            manager = employeeRepository.save(manager);
        }
        // Note: No role validation needed since we're using null manager_id logic
        
        final Employee finalManager = manager;

        // Check if this manager is already managing another department
        departmentRepository.findByManagerId(managerId)
                .ifPresent(existingDept -> {
                    if (!existingDept.getId().equals(departmentId)) {
                        throw new RuntimeException("Employee " + finalManager.getName() + " " + finalManager.getSurname() + 
                                " is already managing department: " + existingDept.getName());
                    }
                });

        department.setManager(manager);
        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    // Remove manager from department
    public DepartmentDTO removeManagerFromDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + departmentId));

        department.setManager(null);
        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    // Get department by manager ID
    public DepartmentDTO getDepartmentByManagerId(Long managerId) {
        return departmentRepository.findByManagerId(managerId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    // Check if an employee has a manager role
    private boolean hasManagerRole(Employee employee) {
        // Check if employee has a role containing "manager" in the Employee table
        return employee.getRole() != null && employee.getRole().toLowerCase().contains("manager");
    }

    // Validate that an employee has manager role
    private void validateManagerRole(Employee employee) {
        boolean hasManagerRole = hasManagerRole(employee);
        
        if (!hasManagerRole) {
            throw new RuntimeException("Employee " + employee.getName() + " " + employee.getSurname() + 
                    " does not have a manager role. Only employees with roles containing 'manager' can be assigned as department managers.");
        }
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

    // Get all potential managers (employees with "Manager" in their role who are not already department managers)
    public List<Employee> getAvailableManagers() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> availableManagers = new ArrayList<>();
        
        // Find all employees with "Manager" in their role, excluding CEO
        for (Employee emp : allEmployees) {
            // Check if this employee has "Manager" in their role
            boolean hasManagerRole = emp.getRole() != null && emp.getRole().toLowerCase().contains("manager");
            // Exclude CEO
            boolean isNotCEO = emp.getRole() == null || (!emp.getRole().toLowerCase().contains("ceo") && 
                                                         !emp.getRole().toLowerCase().contains("chief executive officer"));
            
            if (hasManagerRole && isNotCEO) {
                // Check if this employee is already a department manager
                boolean isAlreadyManager = departmentRepository.findByManagerId(emp.getId()).isPresent();
                if (!isAlreadyManager) {
                    availableManagers.add(emp);
                }
            }
        }
        
        // If no employees without managers found, create virtual manager records for users without corresponding employees
        if (availableManagers.isEmpty()) {
            List<User> allUsers = userRepository.findAll();
            
            for (User user : allUsers) {
                // Check if this user already has an employee record
                boolean hasEmployeeRecord = allEmployees.stream()
                        .anyMatch(emp -> emp.getEmail().equals(user.getUsername()) || Long.valueOf(emp.getId()).equals(user.getId()));
                
                if (!hasEmployeeRecord) {
                    // Check if this user is already a department manager
                    boolean isAlreadyManager = departmentRepository.findByManagerId(user.getId()).isPresent();
                    // Check if this user has "Manager" in their role
                    boolean hasManagerRole = user.getRole() != null && user.getRole().toLowerCase().contains("manager");
                    if (!isAlreadyManager && hasManagerRole) {
                        // Create a virtual employee record for this user
                        Employee virtualManager = new Employee();
                        virtualManager.setId(user.getId());
                        virtualManager.setName(user.getUsername().split("@")[0]);
                        virtualManager.setSurname("User");
                        virtualManager.setEmail(user.getUsername());
                        virtualManager.setEmployeeNumber("USR-" + user.getId());
                        virtualManager.setRole(user.getRole());
                        availableManagers.add(virtualManager);
                    }
                }
            }
        }
        
        return availableManagers;
    }

    // Alternative method to get all managers (for debugging) - employees with null manager_id
    public List<Employee> getAllManagers() {
        List<Employee> allManagers = new ArrayList<>();
        
        // Get all employees without managers (null manager_id) and exclude CEO
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> employeesWithoutManager = allEmployees.stream()
                .filter(emp -> emp.getManager() == null && 
                              !"CEO".equalsIgnoreCase(emp.getRole()) &&
                              !"Chief Executive Officer".equalsIgnoreCase(emp.getRole()) &&
                              hasManagerRole(emp))
                .collect(Collectors.toList());
        
        allManagers.addAll(employeesWithoutManager);
        
        // Add virtual managers for users who don't have employee records
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            // Check if this user already has an employee record
            boolean hasEmployeeRecord = allEmployees.stream()
                    .anyMatch(emp -> emp.getEmail().equals(user.getUsername()) || Long.valueOf(emp.getId()).equals(user.getId()));
            
            if (!hasEmployeeRecord) {
                // Check if this user has a manager role
                boolean hasManagerRole = user.getRole() != null && user.getRole().toLowerCase().contains("manager");
                if (hasManagerRole) {
                    // Create a virtual employee record for this user
                    Employee virtualManager = new Employee();
                    virtualManager.setId(user.getId());
                    virtualManager.setName(user.getUsername().split("@")[0]);
                    virtualManager.setSurname("User");
                    virtualManager.setEmail(user.getUsername());
                    virtualManager.setEmployeeNumber("USR-" + user.getId());
                    virtualManager.setRole(user.getRole());
                    allManagers.add(virtualManager);
                }
            }
        }
        
        return allManagers;
    }

    // Get user-employee mapping for debugging
    public java.util.Map<String, Object> getUserEmployeeMapping() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<com.example.Employee_manager.entity.User> allUsers = userRepository.findAll();
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> mappings = new java.util.ArrayList<>();
        
        for (Employee emp : allEmployees) {
            java.util.Map<String, Object> mapping = new java.util.HashMap<>();
            mapping.put("employeeId", emp.getId());
            mapping.put("employeeName", emp.getName() + " " + emp.getSurname());
            mapping.put("employeeEmail", emp.getEmail());
            
            // Try to find matching user by email
            com.example.Employee_manager.entity.User userByEmail = userRepository.findByUsername(emp.getEmail()).orElse(null);
            if (userByEmail != null) {
                mapping.put("userByEmail", userByEmail.getId() + " - " + userByEmail.getUsername() + " (" + userByEmail.getRole() + ")");
            } else {
                mapping.put("userByEmail", "Not found");
            }
            
            // Try to find matching user by ID
            com.example.Employee_manager.entity.User userById = userRepository.findById(emp.getId()).orElse(null);
            if (userById != null) {
                mapping.put("userById", userById.getId() + " - " + userById.getUsername() + " (" + userById.getRole() + ")");
            } else {
                mapping.put("userById", "Not found");
            }
            
            mappings.add(mapping);
        }
        
        result.put("employees", mappings);
        result.put("totalEmployees", allEmployees.size());
        result.put("totalUsers", allUsers.size());
        result.put("users", allUsers.stream().map(u -> u.getId() + " - " + u.getUsername() + " (" + u.getRole() + ")").collect(Collectors.toList()));
        
        return result;
    }


    // Initialize default departments
    public void initializeDefaultDepartments() {
        // Only create default departments if no departments exist
        if (departmentRepository.count() == 0) {
            // Create Engineering department
            DepartmentRequest engineeringDept = new DepartmentRequest();
            engineeringDept.setName("Engineering");
            engineeringDept.setDescription("Software development and engineering team");
            
            createDepartment(engineeringDept);

            // Create Human Resources department
            DepartmentRequest hrDept = new DepartmentRequest();
            hrDept.setName("Human Resources");
            hrDept.setDescription("Human resources and talent management");
            
            createDepartment(hrDept);

            // Create Finance department
            DepartmentRequest financeDept = new DepartmentRequest();
            financeDept.setName("Finance");
            financeDept.setDescription("Financial planning and accounting");
            
            createDepartment(financeDept);

            // Create Marketing department
            DepartmentRequest marketingDept = new DepartmentRequest();
            marketingDept.setName("Marketing");
            marketingDept.setDescription("Marketing and brand management");
            
            createDepartment(marketingDept);
        }
    }
}
