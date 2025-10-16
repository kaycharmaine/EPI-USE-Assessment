package com.example.Employee_manager.repository;

import com.example.Employee_manager.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find employee by employee number
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
    
    // Find employee by email
    Optional<Employee> findByEmail(String email);

    // Find employees by manager
    List<Employee> findByManagerId(Long managerId);

    // Find employees without a manager (CEO position)
    List<Employee> findByManagerIsNull();

    // Check if employee number already exists (for validation)
    boolean existsByEmployeeNumber(String employeeNumber);

    // Find by name or surname containing (for search)
    List<Employee> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCase(String name, String surname);

    // Custom query to get full hierarchy with departments
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager LEFT JOIN FETCH e.department ORDER BY e.manager.id NULLS FIRST, e.id")
    List<Employee> findAllWithManagers();

    // Advanced search with multiple criteria
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager LEFT JOIN FETCH e.department WHERE " +
           "(:searchTerm IS NULL OR :searchTerm = '' OR " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.employeeNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.role) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:role IS NULL OR :role = '' OR LOWER(e.role) LIKE LOWER(CONCAT('%', :role, '%'))) AND " +
           "(:minSalary IS NULL OR e.salary >= :minSalary) AND " +
           "(:maxSalary IS NULL OR e.salary <= :maxSalary) AND " +
           "(:hasManager IS NULL OR " +
           "(:hasManager = true AND e.manager IS NOT NULL) OR " +
           "(:hasManager = false AND e.manager IS NULL))")
    List<Employee> findEmployeesWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("role") String role,
            @Param("minSalary") Double minSalary,
            @Param("maxSalary") Double maxSalary,
            @Param("hasManager") Boolean hasManager);

    // Find employees by role
    List<Employee> findByRoleContainingIgnoreCase(String role);

    // Find employees by salary range
    List<Employee> findBySalaryBetween(Double minSalary, Double maxSalary);

    // Find employees by department
    List<Employee> findByDepartmentId(Long departmentId);

    // Find employees by department name
    @Query("SELECT e FROM Employee e WHERE e.department.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);
}