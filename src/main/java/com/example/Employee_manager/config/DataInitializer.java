package com.example.Employee_manager.config;

import com.example.Employee_manager.service.UserService;
import com.example.Employee_manager.service.EmployeeService;
import com.example.Employee_manager.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default departments if they don't exist
        departmentService.initializeDefaultDepartments();
        
        // Initialize default users if they don't exist
        userService.initializeDefaultUsers();
        
        // Initialize default employees if they don't exist
        employeeService.initializeDefaultEmployees();
    }
}


