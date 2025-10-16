package com.example.Employee_manager.service;

import com.example.Employee_manager.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HierarchyService {

    @Autowired
    private EmployeeService employeeService;

    public static class HierarchyNode {
        private EmployeeDTO employee;
        private List<HierarchyNode> children;
        private int level;

        public HierarchyNode(EmployeeDTO employee, int level) {
            this.employee = employee;
            this.children = new ArrayList<>();
            this.level = level;
        }

        // Getters and setters
        public EmployeeDTO getEmployee() { return employee; }
        public void setEmployee(EmployeeDTO employee) { this.employee = employee; }
        public List<HierarchyNode> getChildren() { return children; }
        public void setChildren(List<HierarchyNode> children) { this.children = children; }
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        
        // Backward compatibility - map employee to user-like structure
        public EmployeeDTO getUser() { return employee; }
        public void setUser(EmployeeDTO employee) { this.employee = employee; }
    }

    public List<HierarchyNode> buildHierarchyTree() {
        // Get all employees
        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();
        Map<Long, HierarchyNode> nodeMap = new HashMap<>();
        List<HierarchyNode> structuredNodes = new ArrayList<>();

        // Create nodes for all employees
        for (EmployeeDTO employee : allEmployees) {
            HierarchyNode node = new HierarchyNode(employee, 0);
            nodeMap.put(employee.getId(), node);
        }

        // Separate employees by role
        List<EmployeeDTO> ceos = new ArrayList<>();
        List<EmployeeDTO> managers = new ArrayList<>();
        List<EmployeeDTO> employees = new ArrayList<>();

        for (EmployeeDTO employee : allEmployees) {
            String role = employee.getRole() != null ? employee.getRole().toLowerCase() : "";
            if (role.contains("ceo") || role.contains("chief executive officer") || role.contains("admin")) {
                ceos.add(employee);
            } else if (role.contains("manager")) {
                managers.add(employee);
            } else {
                employees.add(employee);
            }
        }

        // Build structured hierarchy: CEO -> Managers -> Employees
        
        // Level 0: CEO (only one CEO expected)
        if (!ceos.isEmpty()) {
            EmployeeDTO ceo = ceos.get(0); // Take the first CEO
            HierarchyNode ceoNode = nodeMap.get(ceo.getId());
            ceoNode.setLevel(0);
            structuredNodes.add(ceoNode);

            // Level 1: All Managers
            for (EmployeeDTO manager : managers) {
                HierarchyNode managerNode = nodeMap.get(manager.getId());
                managerNode.setLevel(1);
                ceoNode.getChildren().add(managerNode);
            }

            // Level 2: All Employees under their respective managers
            for (EmployeeDTO employee : employees) {
                HierarchyNode employeeNode = nodeMap.get(employee.getId());
                employeeNode.setLevel(2);
                
                // Find the employee's manager
                if (employee.getManagerId() != null) {
                    // Find manager node by manager ID
                    HierarchyNode managerNode = findManagerNodeById(ceoNode.getChildren(), employee.getManagerId());
                    if (managerNode != null) {
                        managerNode.getChildren().add(employeeNode);
                    } else {
                        // If manager not found, add to first manager as fallback
                        if (!ceoNode.getChildren().isEmpty()) {
                            ceoNode.getChildren().get(0).getChildren().add(employeeNode);
                        }
                    }
                } else {
                    // If no manager assigned, add to first manager as fallback
                    if (!ceoNode.getChildren().isEmpty()) {
                        ceoNode.getChildren().get(0).getChildren().add(employeeNode);
                    }
                }
            }
        } else {
            // No CEO found, create a virtual CEO or use managers as root
            for (EmployeeDTO manager : managers) {
                HierarchyNode managerNode = nodeMap.get(manager.getId());
                managerNode.setLevel(0);
                structuredNodes.add(managerNode);
                
                // Add employees under this manager
                for (EmployeeDTO employee : employees) {
                    if (employee.getManagerId() != null && employee.getManagerId().equals(manager.getId())) {
                        HierarchyNode employeeNode = nodeMap.get(employee.getId());
                        employeeNode.setLevel(1);
                        managerNode.getChildren().add(employeeNode);
                    }
                }
            }
        }

        return structuredNodes;
    }

    // Helper method to find manager node by ID
    private HierarchyNode findManagerNodeById(List<HierarchyNode> managerNodes, Long managerId) {
        for (HierarchyNode managerNode : managerNodes) {
            if (managerNode.getEmployee().getId().equals(managerId)) {
                return managerNode;
            }
        }
        return null;
    }


    public List<EmployeeDTO> getHierarchyPath(Long employeeId) {
        List<EmployeeDTO> path = new ArrayList<>();
        EmployeeDTO employee = employeeService.getEmployeeById(employeeId);
        
        while (employee != null) {
            path.add(0, employee); // Add to beginning to maintain hierarchy order
            if (employee.getManagerId() != null) {
                employee = employeeService.getEmployeeById(employee.getManagerId());
            } else {
                employee = null;
            }
        }
        
        return path;
    }

    public List<EmployeeDTO> getAllSubordinates(Long managerId) {
        List<EmployeeDTO> allSubordinates = new ArrayList<>();
        List<EmployeeDTO> directSubordinates = employeeService.getSubordinates(managerId);
        
        for (EmployeeDTO subordinate : directSubordinates) {
            allSubordinates.add(subordinate);
            allSubordinates.addAll(getAllSubordinates(subordinate.getId()));
        }
        
        return allSubordinates;
    }

    public Map<String, Object> getHierarchyStatistics() {
        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();
        
        int totalEmployees = allEmployees.size();
        int employeesWithManagers = (int) allEmployees.stream()
                .filter(employee -> employee.getManagerId() != null)
                .count();
        int employeesWithoutManagers = totalEmployees - employeesWithManagers;
        
        // Count employees by role
        int ceoCount = (int) allEmployees.stream().filter(emp -> 
            emp.getRole() != null && (emp.getRole().toLowerCase().contains("ceo") || 
            emp.getRole().toLowerCase().contains("chief executive officer"))).count();
        int managerCount = (int) allEmployees.stream().filter(emp -> 
            emp.getRole() != null && emp.getRole().toLowerCase().contains("manager")).count();
        int otherCount = totalEmployees - ceoCount - managerCount;
        
        // Find the deepest level
        List<HierarchyNode> rootNodes = buildHierarchyTree();
        int maxDepth = 0;
        for (HierarchyNode rootNode : rootNodes) {
            maxDepth = Math.max(maxDepth, getMaxDepth(rootNode));
        }
        
        // Count users at each level
        Map<Integer, Integer> levelCounts = new HashMap<>();
        for (HierarchyNode rootNode : rootNodes) {
            countUsersByLevel(rootNode, levelCounts);
        }
        
        return Map.of(
            "totalUsers", totalEmployees,
            "usersWithManagers", employeesWithManagers,
            "usersWithoutManagers", employeesWithoutManagers,
            "adminCount", ceoCount,
            "managerCount", managerCount,
            "userCount", otherCount,
            "maxDepth", maxDepth,
            "levelCounts", levelCounts
        );
    }

    private int getMaxDepth(HierarchyNode node) {
        if (node.getChildren().isEmpty()) {
            return node.getLevel();
        }
        
        int maxChildDepth = 0;
        for (HierarchyNode child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, getMaxDepth(child));
        }
        
        return maxChildDepth;
    }

    private void countUsersByLevel(HierarchyNode node, Map<Integer, Integer> levelCounts) {
        levelCounts.merge(node.getLevel(), 1, Integer::sum);
        for (HierarchyNode child : node.getChildren()) {
            countUsersByLevel(child, levelCounts);
        }
    }
}
