const API_URL = '/api/students';

// DOM Elements
const studentForm = document.getElementById('student-form');
const studentList = document.getElementById('student-list');
const formTitle = document.getElementById('form-title');
const submitBtn = document.getElementById('submit-btn');
const cancelBtn = document.getElementById('cancel-btn');

// Form Inputs
const idInput = document.getElementById('student-db-id');
const studentIdInput = document.getElementById('studentId');
const nameInput = document.getElementById('name');
const ageInput = document.getElementById('age');
const courseInput = document.getElementById('course');

// Load Data on Start
document.addEventListener('DOMContentLoaded', fetchStudents);

// Fetch all students
async function fetchStudents() {
    try {
        const response = await fetch(API_URL);
        const students = await response.json();
        renderTable(students);
    } catch (error) {
        console.error('Error fetching students:', error);
    }
}

// Render Table
function renderTable(students) {
    studentList.innerHTML = '';
    
    if (students.length === 0) {
        studentList.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No students found. Add one to get started!</td></tr>`;
        return;
    }

    students.forEach(student => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${student.studentId}</strong></td>
            <td>${student.name}</td>
            <td>${student.age}</td>
            <td><span style="background: rgba(99, 102, 241, 0.2); color: #a5b4fc; padding: 0.3rem 0.6rem; border-radius: 4px; font-size: 0.85rem;">${student.course}</span></td>
            <td class="actions-cell">
                <button class="btn action-btn edit-btn" onclick="editStudent(${student.id})">Edit</button>
                <button class="btn action-btn delete-btn" onclick="deleteStudent(${student.id})">Delete</button>
            </td>
        `;
        studentList.appendChild(tr);
    });
}

// Add or Update Student
studentForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const studentData = {
        studentId: studentIdInput.value,
        name: nameInput.value,
        age: parseInt(ageInput.value),
        course: courseInput.value
    };

    const dbId = idInput.value;

    try {
        if (dbId) {
            // Update
            await fetch(`${API_URL}/${dbId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            });
        } else {
            // Create
            await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(studentData)
            });
        }
        
        resetForm();
        fetchStudents();
    } catch (error) {
        console.error('Error saving student:', error);
    }
});

// Delete Student
async function deleteStudent(id) {
    if (!confirm('Are you sure you want to delete this student?')) return;
    
    try {
        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        fetchStudents();
    } catch (error) {
        console.error('Error deleting student:', error);
    }
}

// Edit Student - Populate Form
async function editStudent(id) {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        const student = await response.json();
        
        idInput.value = student.id;
        studentIdInput.value = student.studentId;
        nameInput.value = student.name;
        ageInput.value = student.age;
        courseInput.value = student.course;
        
        formTitle.textContent = 'Edit Student';
        submitBtn.textContent = 'Update Student';
        cancelBtn.classList.remove('hidden');
        
        // Scroll to form on mobile
        studentForm.scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        console.error('Error fetching student details:', error);
    }
}

// Reset Form
cancelBtn.addEventListener('click', resetForm);

function resetForm() {
    studentForm.reset();
    idInput.value = '';
    formTitle.textContent = 'Add New Student';
    submitBtn.textContent = 'Save Student';
    cancelBtn.classList.add('hidden');
}
