package com.example.Employee_manager.controller;

import com.example.Employee_manager.dto.EmployeeDTO;
import com.example.Employee_manager.service.HierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hierarchy")
@Tag(name = "Organization Hierarchy", description = "APIs for managing and visualizing organizational hierarchy")
public class HierarchyController {

    @Autowired
    private HierarchyService hierarchyService;

    @GetMapping("/tree")
    @Operation(summary = "Get organization hierarchy tree", description = "Get the complete organizational hierarchy as a tree structure")
    @ApiResponse(responseCode = "200", description = "Hierarchy tree retrieved successfully")
    public ResponseEntity<List<HierarchyService.HierarchyNode>> getHierarchyTree() {
        List<HierarchyService.HierarchyNode> tree = hierarchyService.buildHierarchyTree();
        return ResponseEntity.ok(tree);
    }

    @GetMapping("/path/{employeeId}")
    @Operation(summary = "Get hierarchy path", description = "Get the complete hierarchy path from CEO to a specific employee")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hierarchy path retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<List<EmployeeDTO>> getHierarchyPath(
            @Parameter(description = "ID of the employee")
            @PathVariable Long employeeId) {
        List<EmployeeDTO> path = hierarchyService.getHierarchyPath(employeeId);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/subordinates/{managerId}")
    @Operation(summary = "Get all subordinates", description = "Get all subordinates (direct and indirect) of a manager")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subordinates retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Manager not found")
    })
    public ResponseEntity<List<EmployeeDTO>> getAllSubordinates(
            @Parameter(description = "ID of the manager")
            @PathVariable Long managerId) {
        List<EmployeeDTO> subordinates = hierarchyService.getAllSubordinates(managerId);
        return ResponseEntity.ok(subordinates);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get hierarchy statistics", description = "Get statistics about the organizational hierarchy")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getHierarchyStatistics() {
        Map<String, Object> statistics = hierarchyService.getHierarchyStatistics();
        return ResponseEntity.ok(statistics);
    }
}


