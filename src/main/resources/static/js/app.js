// Global data
let employees = [];
let editId = null;

// Start application
function init() {
    loadData();
    setupEvents();
}

function setupEvents() {
    // Search on Enter key
    document.getElementById('search').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            doSearch();
        }
    });
}

// Load all data
async function loadData() {
    try {
        showLoading();
        const response = await fetch('/api/employees');
        employees = await response.json();
        showData(employees);
        loadManagers();
        showChart();
    } catch (error) {
        showError('Failed to load data');
    }
}

// Show data in table
function showData(data) {
    const table = document.getElementById('dataTable');

    if (data.length === 0) {
        table.innerHTML = '<tr><td colspan="7" class="text-center">No employees found</td></tr>';
        return;
    }

    table.innerHTML = data.map(emp => `
        <tr>
            <td>
                ${emp.gravatarUrl ?
                    `<img src="${emp.gravatarUrl}" alt="${emp.name}">` :
                    `<div class="bg-secondary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
                        <i class="fas fa-user text-white"></i>
                    </div>`
                }
            </td>
            <td>${emp.employeeNumber}</td>
            <td>${emp.name} ${emp.surname}</td>
            <td>${emp.role}</td>
            <td>R${emp.salary.toLocaleString()}</td>
            <td>${emp.managerName || '-'}</td>
            <td>
                <button class="btn btn-sm btn-warning me-1" onclick="editItem(${emp.id})">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteItem(${emp.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

// Show/Hide form
function showForm() {
    document.getElementById('formContainer').style.display = 'block';
    document.getElementById('mainForm').reset();
    document.getElementById('formTitle').textContent = 'Add Employee';
    editId = null;
}

function hideForm() {
    document.getElementById('formContainer').style.display = 'none';
}

// Load managers for dropdown
async function loadManagers() {
    const select = document.getElementById('manager');

    try {
        const response = await fetch('/api/employees');
        const data = await response.json();

        select.innerHTML = '<option value="">No Manager</option>';

        data.forEach(emp => {
            const option = document.createElement('option');
            option.value = emp.id;
            option.textContent = `${emp.name} ${emp.surname} - ${emp.role}`;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading managers:', error);
    }
}

// Save data
async function saveData(event) {
    event.preventDefault();

    const data = {
        name: document.getElementById('firstName').value,
        surname: document.getElementById('lastName').value,
        birthDate: document.getElementById('birthDate').value,
        employeeNumber: document.getElementById('empNumber').value,
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
            body: JSON.stringify(data)
        });

        if (response.ok) {
            hideForm();
            loadData();
            showMessage(
                editId ? 'Employee updated successfully!' : 'Employee added successfully!',
                'success'
            );
        } else {
            const error = await response.json();
            showMessage(error.error || 'Failed to save employee', 'error');
        }
    } catch (error) {
        showMessage('Failed to save employee', 'error');
    }
}

// Edit item
async function editItem(id) {
    try {
        const response = await fetch(`/api/employees/${id}`);
        const emp = await response.json();

        document.getElementById('firstName').value = emp.name;
        document.getElementById('lastName').value = emp.surname;
        document.getElementById('birthDate').value = emp.birthDate;
        document.getElementById('empNumber').value = emp.employeeNumber;
        document.getElementById('salary').value = emp.salary;
        document.getElementById('role').value = emp.role;
        document.getElementById('email').value = emp.email || '';
        document.getElementById('manager').value = emp.managerId || '';

        editId = id;
        document.getElementById('formContainer').style.display = 'block';
        document.getElementById('formTitle').textContent = 'Edit Employee';

    } catch (error) {
        showMessage('Failed to load employee data', 'error');
    }
}

// Delete item
async function deleteItem(id) {
    if (!confirm('Are you sure you want to delete this employee?')) {
        return;
    }

    try {
        const response = await fetch(`/api/employees/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            loadData();
            showMessage('Employee deleted successfully!', 'success');
        } else {
            const error = await response.json();
            showMessage(error.error || 'Failed to delete employee', 'error');
        }
    } catch (error) {
        showMessage('Failed to delete employee', 'error');
    }
}

// Search
function doSearch() {
    const term = document.getElementById('search').value.trim();

    if (term.length === 0) {
        showData(employees);
        return;
    }

    const filtered = employees.filter(emp =>
    emp.name.toLowerCase().includes(term.toLowerCase()) ||
    emp.surname.toLowerCase().includes(term.toLowerCase()) ||
    emp.role.toLowerCase().includes(term.toLowerCase()) ||
    emp.employeeNumber.toLowerCase().includes(term.toLowerCase())
    );

    showData(filtered);
}

// Show chart
function showChart() {
    const chart = document.getElementById('orgChart');

    if (employees.length === 0) {
        chart.innerHTML = '<div class="text-center text-muted">No employees to display in organization chart</div>';
        return;
    }

    let html = '<div class="chart-container">';

    // Find top level (no manager)
    const topLevel = employees.filter(emp => !emp.managerId);

    topLevel.forEach(emp => {
        html += makeChartNode(emp, 0);
    });

    html += '</div>';
    chart.innerHTML = html;
}

function makeChartNode(emp, level) {
    const children = employees.filter(e => e.managerId === emp.id);
    const indent = level * 40;

    let html = `
        <div class="employee-card" style="margin-left: ${indent}px;">
            ${emp.gravatarUrl ?
                `<img src="${emp.gravatarUrl}" alt="${emp.name}">` :
                `<div class="bg-primary rounded-circle d-inline-flex align-items-center justify-content-center" style="width: 60px; height: 60px;">
                    <i class="fas fa-user text-white"></i>
                </div>`
            }
            <h6 class="mb-1">${emp.name} ${emp.surname}</h6>
            <small class="text-muted d-block">${emp.role}</small>
            <small class="text-muted">${emp.employeeNumber}</small>
        </div>
    `;

    if (children.length > 0) {
        html += '<div class="connector"></div>';
        html += '<div class="subordinates">';
        children.forEach(child => {
            html += makeChartNode(child, level + 1);
        });
        html += '</div>';
    }

    return html;
}

// Tabs
function showTab(tabName) {
    // Hide all tabs
    document.getElementById('listTab').style.display = 'none';
    document.getElementById('chartTab').style.display = 'none';

    // Remove active class from all menu items
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });

    // Set active menu item
    if (tabName === 'list') {
        document.querySelector('.menu-item:nth-child(1)').classList.add('active');
    } else if (tabName === 'chart') {
        document.querySelector('.menu-item:nth-child(2)').classList.add('active');
    }

    // Show selected tab
    document.getElementById(tabName + 'Tab').style.display = 'block';

    // Refresh chart if needed
    if (tabName === 'chart') {
        showChart();
    }
}

// Utility functions
function showLoading() {
    const table = document.getElementById('dataTable');
    table.innerHTML = '<tr><td colspan="7" class="text-center loading"><i class="fas fa-spinner fa-spin me-2"></i>Loading...</td></tr>';
}

function showError(msg) {
    const table = document.getElementById('dataTable');
    table.innerHTML = `<tr><td colspan="7" class="text-center text-danger">${msg}</td></tr>`;
}

function showMessage(msg, type = 'info') {
    const message = document.getElementById('message');
    const title = document.getElementById('messageTitle');
    const text = document.getElementById('messageText');

    // Set style based on type
    message.className = `toast position-fixed top-0 end-0 m-3 ${type}`;
    title.textContent = type.charAt(0).toUpperCase() + type.slice(1);
    text.textContent = msg;

    // Show message
    message.style.display = 'block';

    // Auto hide after 3 seconds
    setTimeout(hideMessage, 3000);
}

function hideMessage() {
    document.getElementById('message').style.display = 'none';
}

// Start when page loads
document.addEventListener('DOMContentLoaded', init);