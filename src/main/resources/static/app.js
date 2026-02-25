const API_BASE = '/api/v1';
let currentToken = localStorage.getItem('token');
let currentSnippetId = null;

async function safeParse(res) {
    const text = await res.text();
    try {
        if (res.status === 401 || res.status === 500) {
            // Check if error is auth related and prompt/auto-fix
            const data = JSON.parse(text);
            if (data.message && (data.message.includes('User not found') || data.message.includes('identifier'))) {
                console.warn('Auth issue detected, clearing storage');
                localStorage.clear();
            }
            return data;
        }
        return JSON.parse(text);
    } catch (e) {
        return { message: text };
    }
}

// UI Helpers
function showNotification(message, type = 'success') {
    const container = document.getElementById('notification-container');
    const note = document.createElement('div');
    note.className = `notification ${type}`;
    note.innerText = message;
    container.appendChild(note);
    setTimeout(() => note.remove(), 3000);
}

function showModal(content, onConfirm = null) {
    const overlay = document.getElementById('modal-overlay');
    const body = document.getElementById('modal-content');
    const footer = document.getElementById('modal-footer');
    const header = overlay.querySelector('.modal-header');

    header.innerText = 'CodeMaster Engine';
    body.innerHTML = content;
    footer.innerHTML = '';

    if (onConfirm) {
        const cancelBtn = document.createElement('button');
        cancelBtn.className = 'btn btn-outline';
        cancelBtn.innerText = 'Cancel';
        cancelBtn.onclick = closeModal;

        const confirmBtn = document.createElement('button');
        confirmBtn.className = 'btn btn-primary';
        confirmBtn.style.backgroundColor = 'var(--danger)';
        confirmBtn.innerText = 'Confirm';
        confirmBtn.onclick = async () => {
            await onConfirm();
            closeModal();
        };

        footer.appendChild(cancelBtn);
        footer.appendChild(confirmBtn);
    } else {
        const closeBtn = document.createElement('button');
        closeBtn.className = 'btn btn-primary';
        closeBtn.innerText = 'Close';
        closeBtn.onclick = closeModal;
        footer.appendChild(closeBtn);
    }

    overlay.classList.add('active');
}

function closeModal() {
    document.getElementById('modal-overlay').classList.remove('active');
}

// Auth
function validateEmail(email) {
    return String(email)
        .toLowerCase()
        .match(
            /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
        );
}

function updateValidationStatus() {
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const user = document.getElementById('reg-username').value;
    const pass = document.getElementById('reg-password').value;
    const btn = document.getElementById('register-btn');

    let isValid = true;

    // Name check
    if (name.length < 2) {
        document.getElementById('reg-name-error').innerText = name ? 'Name too short' : '';
        isValid = false;
    } else {
        document.getElementById('reg-name-error').innerText = '';
    }

    // Email check
    if (!validateEmail(email)) {
        document.getElementById('reg-email-error').innerText = email ? 'Invalid email format' : '';
        isValid = false;
    } else {
        document.getElementById('reg-email-error').innerText = '';
    }

    // Username check
    if (user.length < 3) {
        document.getElementById('reg-username-error').innerText = user ? 'Username too short' : '';
        isValid = false;
    } else {
        document.getElementById('reg-username-error').innerText = '';
    }

    // Password check
    if (pass.length < 8) {
        document.getElementById('reg-password-error').innerText = pass ? 'Minimum 8 characters' : '';
        isValid = false;
    } else {
        document.getElementById('reg-password-error').innerText = '';
    }

    btn.disabled = !isValid;
}

// Add listeners for real-time validation if on login page
if (document.getElementById('register-form')) {
    ['reg-name', 'reg-email', 'reg-username', 'reg-password'].forEach(id => {
        document.getElementById(id).addEventListener('input', updateValidationStatus);
    });
}

async function login() {
    const email = document.getElementById('email').value;
    const pass = document.getElementById('password').value;

    if (!email || !pass) {
        showNotification('Please fill in all fields', 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, password: pass })
        });
        if (!res.ok) {
            const data = await safeParse(res);
            throw new Error(data.message || 'Invalid credentials');
        }
        const data = await safeParse(res);
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        window.location.href = 'index.html';
    } catch (e) {
        showNotification(e.message, 'error');
    }
}

async function register() {
    const name = document.getElementById('reg-name').value;
    const email = document.getElementById('reg-email').value;
    const user = document.getElementById('reg-username').value;
    const pass = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: name, email: email, username: user, password: pass, role: role })
        });
        const data = await safeParse(res);
        if (!res.ok) throw new Error(data.message || 'Registration failed');

        showNotification('Registration successful! Please login.');
        // After successful registration, toggle back to login
        if (typeof toggleMode === 'function') toggleMode();
    } catch (e) {
        showNotification(e.message, 'error');
    }
}

function logout() {
    localStorage.clear();
    window.location.href = 'login.html';
}

// Snippets
async function loadSnippets() {
    if (!currentToken) return;
    try {
        const res = await fetch(`${API_BASE}/snippets`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const data = await safeParse(res);
        const list = document.getElementById('snippet-list');
        list.innerHTML = data.map(s => `
            <div class="snippet-item ${currentSnippetId == s.id ? 'active' : ''}" onclick="selectSnippet(${s.id})">
                <div style="font-weight: 600;">${s.title}</div>
                <div style="font-size: 0.7rem; color: var(--text-muted);">${s.language} | v${s.activeVersionNumber}</div>
            </div>
        `).join('');
    } catch (e) {
        showNotification('Failed to load snippets', 'error');
    }
}

async function selectSnippet(id) {
    currentSnippetId = id;
    try {
        const res = await fetch(`${API_BASE}/snippets/${id}`, {
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        const s = await safeParse(res);
        document.getElementById('snippet-title').value = s.title;
        document.getElementById('code-editor').value = s.currentContent;
        document.getElementById('btn-delete-snippet').style.display = 'inline-flex';
        loadSnippets(); // refresh list to show active
        loadVersions(id);
    } catch (e) {
        showNotification('Error loading snippet', 'error');
    }
}

async function saveSnippet() {
    const title = document.getElementById('snippet-title').value;
    const content = document.getElementById('code-editor').value;
    if (!title || !content) {
        showNotification('Title and content required', 'error');
        return;
    }

    const url = currentSnippetId ? `${API_BASE}/snippets/${currentSnippetId}` : `${API_BASE}/snippets`;
    const method = currentSnippetId ? 'PUT' : 'POST';

    try {
        const res = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ title, content, language: 'Java' })
        });
        const data = await safeParse(res);
        if (!res.ok) throw new Error(data.message || 'Save failed');
        currentSnippetId = data.id;
        showNotification('Snippet saved successfully!');
        loadSnippets();
        loadVersions(currentSnippetId);
    } catch (e) {
        showNotification('Save failed', 'error');
    }
}

async function validateCode() {
    const content = document.getElementById('code-editor').value;
    try {
        const res = await fetch(`${API_BASE}/snippets/validate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ content })
        });
        const errors = await safeParse(res);
        if (Array.isArray(errors)) {
            if (errors.length === 0) {
                showModal('<div style="color: var(--success); text-align: center;"><h3 style="margin-bottom: 0.5rem;">Syntax Valid!</h3>No errors found in your Java code.</div>');
            } else {
                showModal('<h3 style="color: var(--danger); margin-bottom: 1rem;">Syntax Errors Found</h3>' +
                    errors.map(err => `<div style="margin-bottom: 0.5rem; padding: 0.5rem; background: #fff1f2; border-radius: 4px;">${err}</div>`).join(''));
            }
        } else {
            showNotification(errors.message || 'Validation failed', 'error');
        }
    } catch (e) {
        showNotification('Validation service error', 'error');
    }
}

async function loadVersions(id) {
    const res = await fetch(`${API_BASE}/snippets/${id}/versions`, {
        headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const versions = await safeParse(res);

    // Also need snippet for active version check
    const sRes = await fetch(`${API_BASE}/snippets/${id}`, {
        headers: { 'Authorization': `Bearer ${currentToken}` }
    });
    const snippet = await safeParse(sRes);

    const list = document.getElementById('version-list');

    let html = '';
    versions.forEach(v => {
        html += `
            <div class="version-item" id="v-item-${v.versionNumber}">
                <span>Version ${v.versionNumber}</span>
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn btn-outline" style="padding: 0.2rem 0.4rem; font-size: 0.7rem;" onclick="rollback(${id}, ${v.versionNumber})">Restore</button>
                    ${v.versionNumber !== snippet.activeVersionNumber ?
                `<button class="btn btn-danger" style="padding: 0.2rem 0.4rem; font-size: 0.7rem;" onclick="confirmDeleteEnhanced(${v.id}, ${v.versionNumber})">Delete</button>` : ''}
                </div>
            </div>
        `;
    });
    list.innerHTML = html;
}

function confirmDeleteEnhanced(versionId, vNum) {
    const modal = document.querySelector('.modal');
    modal.classList.add('modal-pulse');
    showModal(`Confirm permanent deletion of **Version ${vNum}**? This cannot be undone.`,
        () => deleteVersionEnhanced(versionId, vNum));
    setTimeout(() => modal.classList.remove('modal-pulse'), 300);
}

async function deleteVersionEnhanced(versionId, vNum) {
    try {
        const res = await fetch(`${API_BASE.replace('/snippets', '')}/versions/${versionId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const data = await safeParse(res);
            throw new Error(data.message || 'Deletion failed');
        }

        const item = document.getElementById(`v-item-${vNum}`);
        if (item) {
            item.classList.add('fade-out');
            setTimeout(() => {
                showNotification(`Version ${vNum} purged`);
                loadVersions(currentSnippetId);
            }, 500);
        } else {
            loadVersions(currentSnippetId);
        }
    } catch (e) {
        showNotification(e.message, 'error');
    }
}

async function rollback(id, v) {
    try {
        const res = await fetch(`${API_BASE}/snippets/${id}/rollback`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentToken}`
            },
            body: JSON.stringify({ versionNumber: v })
        });
        showNotification(`Rolled back to Version ${v}`);
        selectSnippet(id);
    } catch (e) {
        showNotification('Rollback failed', 'error');
    }
}

async function deleteSnippet() {
    try {
        const res = await fetch(`${API_BASE}/snippets/${currentSnippetId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${currentToken}` }
        });
        if (!res.ok) {
            const data = await safeParse(res);
            throw new Error(data.message || 'Deletion failed');
        }

        const item = Array.from(document.querySelectorAll('.snippet-item'))
            .find(i => i.onclick.toString().includes(currentSnippetId));

        if (item) {
            item.classList.add('fade-out');
            setTimeout(() => {
                showNotification('Snippet deleted successfully');
                newSnippet();
            }, 500);
        } else {
            showNotification('Snippet deleted successfully');
            newSnippet();
        }
    } catch (e) {
        showNotification(e.message, 'error');
    }
}

function confirmDeleteSnippet() {
    showModal(`Are you sure you want to delete the **ENTIRE SNIPPET** and all its history?`, () => deleteSnippet());
}

function newSnippet() {
    currentSnippetId = null;
    document.getElementById('snippet-title').value = '';
    document.getElementById('code-editor').value = '';
    document.getElementById('version-list').innerHTML = '';
    document.getElementById('btn-delete-snippet').style.display = 'none';
    loadSnippets();
}

// Initial Load
if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname.endsWith('/')) {
    if (!currentToken) window.location.href = 'login.html';
    else {
        const userDisplay = document.getElementById('user-display');
        if (userDisplay) userDisplay.innerText = localStorage.getItem('username');
        loadSnippets();
    }
}
