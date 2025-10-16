// Global variables
let employees = [];
let managers = [];
let roles = [];
let editId = null;
let currentFilters = {};

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    loadData();
    setupEventListeners();
    loadStatistics();
});

function setupEventListeners() {
    // Search input
    document.getElementById('searchInput').addEventListener('input', debounce(performSearch, 300));
    
    // Sort and filter dropdowns
    document.getElementById('sortBy').addEventListener('change', applyFilters);
    document.getElementById('sortOrder').addEventListener('change', applyFilters);
    document.getElementById('roleFilter').addEventListener('change', applyFilters);
    
    // File upload
    const profilePicture = document.getElementById('profilePicture');
    profilePicture.addEventListener('change', handleFileUpload);
    
    // Drag and drop for file upload
    const uploadArea = document.querySelector('.upload-area');
    uploadArea.addEventListener('dragover', handleDragOver);
    uploadArea.addEventListener('dragleave', handleDragLeave);
    uploadArea.addEventListener('drop', handleDrop);
}

// Load all data
async function loadData() {
    try {
        showLoading();
        
        // Get current user info from the page
        const currentUser = {
            username: /*[[${username}]]*/ 'admin',
            role: /*[[${role}]]*/ 'ROLE_ADMIN'
        };
        
        // Load employees based on user role
        const employeesResponse = await fetch(`/api/employees/my-employees?username=${currentUser.username}&role=${currentUser.role}`);
        employees = await employeesResponse.json();
        
        // Load managers for dropdown (only if admin)
        if (currentUser.role === 'ROLE_ADMIN') {
            const allEmployeesResponse = await fetch('/api/employees');
            const allEmployees = await allEmployeesResponse.json();
            managers = allEmployees.filter(emp => emp.role.toLowerCase().includes('manager') || emp.role.toLowerCase().includes('lead'));
        } else {
            managers = employees.filter(emp => emp.role.toLowerCase().includes('manager') || emp.role.toLowerCase().includes('lead'));
        }
        
        // Load roles
        const rolesResponse = await fetch('/api/employees/roles');
        roles = await rolesResponse.json();
        
        populateRoleDropdowns();
        populateManagerDropdown();
        displayEmployees(employees);
        
    } catch (error) {
        console.error('Error loading data:', error);
        showNotification('Failed to load employee data', 'error');
    }
}

// Load statistics
async function loadStatistics() {
    try {
        // Load employee statistics
        const employeesResponse = await fetch('/api/employees');
        const employees = await employeesResponse.json();
        
        document.getElementById('totalEmployees').textContent = employees.length;
        
        const managers = employees.filter(emp => emp.role.toLowerCase().includes('manager')).length;
        document.getElementById('totalManagers').textContent = managers;
        
        const avgSalary = employees.length > 0 
            ? employees.reduce((sum, emp) => sum + (emp.salary || 0), 0) / employees.length 
            : 0;
        document.getElementById('avgSalary').textContent = '$' + Math.round(avgSalary).toLocaleString();
        
        // Load hierarchy statistics
        const hierarchyResponse = await fetch('/api/hierarchy/statistics');
        const hierarchyStats = await hierarchyResponse.json();
        document.getElementById('maxDepth').textContent = hierarchyStats.maxDepth || 0;
        
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

// Populate role dropdowns
function populateRoleDropdowns() {
    const roleFilter = document.getElementById('roleFilter');
    const advancedRoleFilter = document.getElementById('advancedRoleFilter');
    
    roles.forEach(role => {
        const option1 = new Option(role, role);
        const option2 = new Option(role, role);
        roleFilter.add(option1);
        advancedRoleFilter.add(option2);
    });
}

// Populate manager dropdown
function populateManagerDropdown() {
    const managerSelect = document.getElementById('manager');
    managerSelect.innerHTML = '<option value="">No Manager</option>';
    
    employees.forEach(emp => {
        const option = new Option(`${emp.name} ${emp.surname} - ${emp.role}`, emp.id);
        managerSelect.appendChild(option);
    });
}

// Display employees in table
function displayEmployees(employeeList) {
    const tbody = document.getElementById('employeeTableBody');
    
    if (employeeList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No employees found</td></tr>';
        return;
    }
    
    tbody.innerHTML = employeeList.map(emp => `
        <tr>
            <td>
                <img src="${emp.gravatarUrl || '/images/default-avatar.png'}" 
                     alt="${emp.name}" 
                     class="employee-avatar" 
                     style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;">
            </td>
            <td>${emp.employeeNumber}</td>
            <td>${emp.name} ${emp.surname}</td>
            <td>${emp.role}</td>
            <td>R${emp.salary ? emp.salary.toLocaleString() : '0'}</td>
            <td>${emp.managerName || '-'}</td>
            <td>${emp.birthDate || '-'}</td>
            <td>
                <div class="table-actions">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewEmployee(${emp.id})" title="View">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-warning" onclick="editEmployee(${emp.id})" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-info" onclick="uploadProfilePicture(${emp.id})" title="Upload Photo">
                        <i class="fas fa-camera"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteEmployee(${emp.id})" title="Delete">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Search functionality
function performSearch() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    
    if (searchTerm === '') {
        displayEmployees(employees);
        return;
    }
    
    const filtered = employees.filter(emp =>
        emp.name.toLowerCase().includes(searchTerm) ||
        emp.surname.toLowerCase().includes(searchTerm) ||
        emp.role.toLowerCase().includes(searchTerm) ||
        emp.employeeNumber.toLowerCase().includes(searchTerm) ||
        (emp.email && emp.email.toLowerCase().includes(searchTerm))
    );
    
    displayEmployees(filtered);
}

// Apply filters and sorting
function applyFilters() {
    let filtered = [...employees];
    
    // Search filter
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    if (searchTerm) {
        filtered = filtered.filter(emp =>
            emp.name.toLowerCase().includes(searchTerm) ||
            emp.surname.toLowerCase().includes(searchTerm) ||
            emp.role.toLowerCase().includes(searchTerm) ||
            emp.employeeNumber.toLowerCase().includes(searchTerm) ||
            (emp.email && emp.email.toLowerCase().includes(searchTerm))
        );
    }
    
    // Role filter
    const roleFilter = document.getElementById('roleFilter').value;
    if (roleFilter) {
        filtered = filtered.filter(emp => emp.role === roleFilter);
    }
    
    // Sort
    const sortBy = document.getElementById('sortBy').value;
    const sortOrder = document.getElementById('sortOrder').value;
    
    if (sortBy) {
        filtered.sort((a, b) => {
            let aVal = a[sortBy];
            let bVal = b[sortBy];
            
            if (typeof aVal === 'string') {
                aVal = aVal.toLowerCase();
                bVal = bVal.toLowerCase();
            }
            
            if (sortOrder === 'asc') {
                return aVal > bVal ? 1 : -1;
            } else {
                return aVal < bVal ? 1 : -1;
            }
        });
    }
    
    displayEmployees(filtered);
}

// Clear all filters
function clearFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('sortBy').value = '';
    document.getElementById('sortOrder').value = 'asc';
    document.getElementById('roleFilter').value = '';
    displayEmployees(employees);
}

// Advanced search
async function performAdvancedSearch() {
    const searchTerm = document.getElementById('advancedSearchTerm').value;
    const role = document.getElementById('advancedRoleFilter').value;
    const minSalary = document.getElementById('minSalary').value;
    const maxSalary = document.getElementById('maxSalary').value;
    const hasManager = document.getElementById('managerStatus').value;
    
    const params = new URLSearchParams();
    if (searchTerm) params.append('searchTerm', searchTerm);
    if (role) params.append('role', role);
    if (minSalary) params.append('minSalary', minSalary);
    if (maxSalary) params.append('maxSalary', maxSalary);
    if (hasManager) params.append('hasManager', hasManager);
    
    try {
        const response = await fetch(`/api/employees/search/advanced?${params}`);
        const results = await response.json();
        
        displaySearchResults(results);
    } catch (error) {
        console.error('Error performing advanced search:', error);
        showNotification('Search failed', 'error');
    }
}

// Display search results
function displaySearchResults(results) {
    const resultsDiv = document.getElementById('searchResults');
    
    if (results.length === 0) {
        resultsDiv.innerHTML = '<p class="text-muted">No employees found matching your criteria.</p>';
        return;
    }
    
    resultsDiv.innerHTML = `
        <h6>Found ${results.length} employee(s)</h6>
        <div class="list-group">
            ${results.map(emp => `
                <div class="list-group-item">
                    <div class="d-flex w-100 justify-content-between align-items-center">
                        <div class="d-flex align-items-center">
                            <img src="${emp.gravatarUrl || '/images/default-avatar.png'}" 
                                 alt="${emp.name}" 
                                 class="rounded-circle me-3" 
                                 style="width: 40px; height: 40px; object-fit: cover;">
                            <div>
                                <h6 class="mb-1">${emp.name} ${emp.surname}</h6>
                                <small class="text-muted">${emp.role} - ${emp.employeeNumber}</small>
                            </div>
                        </div>
                        <div>
                            <button class="btn btn-sm btn-outline-primary" onclick="viewEmployee(${emp.id})">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-warning" onclick="editEmployee(${emp.id})">
                                <i class="fas fa-edit"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
    `;
}

// Load hierarchy tree
async function loadHierarchyTree() {
    try {
        const response = await fetch('/api/hierarchy/tree');
        const tree = await response.json();
        
        displayHierarchyTree(tree);
    } catch (error) {
        console.error('Error loading hierarchy tree:', error);
        showNotification('Failed to load organization chart', 'error');
    }
}

// Display hierarchy tree
function displayHierarchyTree(nodes) {
    const container = document.getElementById('hierarchyTree');
    
    if (nodes.length === 0) {
        container.innerHTML = '<p class="text-muted text-center">No employees found to display in organization chart.</p>';
        return;
    }
    
    container.innerHTML = nodes.map(node => renderTreeNode(node)).join('');
}

// Render a tree node
function renderTreeNode(node) {
    const employee = node.employee;
    const levelClass = `level-${node.level}`;
    
    let html = `
        <div class="tree-node ${levelClass}">
            <div class="employee-card">
                <img src="${employee.gravatarUrl || '/images/default-avatar.png'}" 
                     alt="${employee.name}" 
                     class="employee-avatar">
                <div class="employee-info">
                    <h6>${employee.name} ${employee.surname}</h6>
                    <small>${employee.role}</small><br>
                    <small class="text-muted">${employee.employeeNumber}</small>
                </div>
                <div class="ms-auto">
                    <button class="btn btn-sm btn-outline-primary" onclick="viewEmployee(${employee.id})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-warning" onclick="editEmployee(${employee.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
    
    if (node.children && node.children.length > 0) {
        html += '<div class="ms-4">';
        html += node.children.map(child => renderTreeNode(child)).join('');
        html += '</div>';
    }
    
    return html;
}

// Employee CRUD operations
async function saveEmployee() {
    const form = document.getElementById('employeeForm');
    const formData = new FormData(form);
    
    const employeeData = {
        name: document.getElementById('firstName').value,
        surname: document.getElementById('lastName').value,
        birthDate: document.getElementById('birthDate').value,
        employeeNumber: document.getElementById('employeeNumber').value,
        salary: parseFloat(document.getElementById('salary').value),
        role: document.getElementById('role').value,
        email: document.getElementById('email').value,
        managerId: document.getElementById('manager').value || null
    };
    
    try {
        const url = editId ? `/api/employees/${editId}` : '/api/employees';
        const method = editId ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(employeeData)
        });
        
        if (response.ok) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('employeeModal'));
            modal.hide();
            
            // Handle profile picture upload if file is selected
            const profilePicture = document.getElementById('profilePicture');
            if (profilePicture.files.length > 0 && !editId) {
                const savedEmployee = await response.json();
                await uploadProfilePictureFile(savedEmployee.id, profilePicture.files[0]);
            }
            
            loadData();
            showNotification(
                editId ? 'Employee updated successfully!' : 'Employee added successfully!',
                'success'
            );
            
            editId = null;
            form.reset();
        } else {
            const error = await response.json();
            showNotification(error.message || 'Failed to save employee', 'error');
        }
    } catch (error) {
        console.error('Error saving employee:', error);
        showNotification('Failed to save employee', 'error');
    }
}

async function editEmployee(id) {
    try {
        const response = await fetch(`/api/employees/${id}`);
        const emp = await response.json();
        
        document.getElementById('firstName').value = emp.name;
        document.getElementById('lastName').value = emp.surname;
        document.getElementById('birthDate').value = emp.birthDate;
        document.getElementById('employeeNumber').value = emp.employeeNumber;
        document.getElementById('salary').value = emp.salary;
        document.getElementById('role').value = emp.role;
        document.getElementById('email').value = emp.email || '';
        document.getElementById('manager').value = emp.managerId || '';
        
        editId = id;
        document.getElementById('modalTitle').textContent = 'Edit Employee';
        
        const modal = new bootstrap.Modal(document.getElementById('employeeModal'));
        modal.show();
        
    } catch (error) {
        console.error('Error loading employee:', error);
        showNotification('Failed to load employee data', 'error');
    }
}

async function deleteEmployee(id) {
    if (!confirm('Are you sure you want to delete this employee?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/employees/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            loadData();
            showNotification('Employee deleted successfully!', 'success');
        } else {
            const error = await response.json();
            showNotification(error.message || 'Failed to delete employee', 'error');
        }
    } catch (error) {
        console.error('Error deleting employee:', error);
        showNotification('Failed to delete employee', 'error');
    }
}

async function uploadProfilePicture(id) {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            await uploadProfilePictureFile(id, file);
        }
    };
    
    input.click();
}

async function uploadProfilePictureFile(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    
    try {
        const response = await fetch(`/api/employees/${id}/upload-profile-picture`, {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            loadData();
            showNotification('Profile picture uploaded successfully!', 'success');
        } else {
            showNotification('Failed to upload profile picture', 'error');
        }
    } catch (error) {
        console.error('Error uploading profile picture:', error);
        showNotification('Failed to upload profile picture', 'error');
    }
}

// File upload handlers
function handleFileUpload(event) {
    const file = event.target.files[0];
    if (file) {
        updateUploadArea(file.name);
    }
}

function handleDragOver(event) {
    event.preventDefault();
    event.currentTarget.classList.add('dragover');
}

function handleDragLeave(event) {
    event.currentTarget.classList.remove('dragover');
}

function handleDrop(event) {
    event.preventDefault();
    event.currentTarget.classList.remove('dragover');
    
    const files = event.dataTransfer.files;
    if (files.length > 0) {
        document.getElementById('profilePicture').files = files;
        updateUploadArea(files[0].name);
    }
}

function updateUploadArea(fileName) {
    const uploadArea = document.querySelector('.upload-area');
    uploadArea.innerHTML = `
        <i class="fas fa-check-circle fa-2x text-success mb-2"></i>
        <p class="mb-0">${fileName}</p>
        <small class="text-muted">Click to change</small>
    `;
}

// Utility functions
function showLoading() {
    const tbody = document.getElementById('employeeTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center"><i class="fas fa-spinner fa-spin me-2"></i>Loading...</td></tr>';
}

function showNotification(message, type = 'info') {
    const toast = document.getElementById('notificationToast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastMessage.textContent = message;
    
    // Update toast styling based on type
    const toastHeader = toast.querySelector('.toast-header');
    const icon = toastHeader.querySelector('i');
    
    icon.className = `fas me-2 ${
        type === 'success' ? 'fa-check-circle text-success' :
        type === 'error' ? 'fa-exclamation-circle text-danger' :
        'fa-info-circle text-primary'
    }`;
    
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Tab event listeners
document.getElementById('hierarchy-tab').addEventListener('click', function() {
    loadHierarchyTree();
});

// View employee (placeholder)
function viewEmployee(id) {
    // This could open a detailed view modal or navigate to a detail page
    showNotification('View employee functionality - to be implemented', 'info');
}
