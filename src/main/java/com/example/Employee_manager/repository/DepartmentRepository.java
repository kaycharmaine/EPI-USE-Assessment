package com.example.Employee_manager.repository;

import com.example.Employee_manager.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // Find department by name
    Optional<Department> findByName(String name);
    
    // Check if department name exists
    boolean existsByName(String name);
    
    // Find departments with managers
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager WHERE d.manager IS NOT NULL")
    List<Department> findDepartmentsWithManagers();
    
    // Find departments without managers
    @Query("SELECT d FROM Department d WHERE d.manager IS NULL")
    List<Department> findDepartmentsWithoutManagers();
    
    // Find department by manager ID
    @Query("SELECT d FROM Department d WHERE d.manager.id = :managerId")
    Optional<Department> findByManagerId(@Param("managerId") Long managerId);
    
    // Count employees in department (including the manager)
    @Query("SELECT " +
           "  (SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId) + " +
           "  (SELECT CASE WHEN d.manager IS NOT NULL THEN 1 ELSE 0 END FROM Department d WHERE d.id = :departmentId)")
    long countEmployeesInDepartment(@Param("departmentId") Long departmentId);
    
    // Find all departments with manager information
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager ORDER BY d.name")
    List<Department> findAllWithManagers();
    
}
