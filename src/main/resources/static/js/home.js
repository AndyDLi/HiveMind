/**
 * HiveMind Home Page JavaScript
 * Handles user data, portals, profile management, and UI interactions
 */

// Global state
let currentUser = null;
let userProfile = null;
let editSkills = [];
let portalToDelete = null;
let allPortals = [];

document.addEventListener('DOMContentLoaded', () => {
    initParticles();
    loadUserData();
    initEventListeners();
});

/* ============================================
   Particle Animation
   ============================================ */
function initParticles() {
    const particlesContainer = document.getElementById('particles');
    if (!particlesContainer) return;

    const particleCount = 30;

    for (let i = 0; i < particleCount; i++) {
        createParticle(particlesContainer);
    }
}

function createParticle(container) {
    const particle = document.createElement('div');
    particle.className = 'particle';

    particle.style.left = Math.random() * 100 + '%';

    const size = Math.random() * 4 + 2;
    particle.style.width = size + 'px';
    particle.style.height = size + 'px';

    particle.style.animationDelay = (Math.random() * 8) + 's';
    particle.style.animationDuration = (Math.random() * 4 + 6) + 's';

    const colors = ['#00d4ff', '#7b2cbf', '#c77dff', '#00e5ff'];
    particle.style.background = colors[Math.floor(Math.random() * colors.length)];

    container.appendChild(particle);
}

/* ============================================
   Event Listeners
   ============================================ */
function initEventListeners() {
    // Logout button
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);

    // Profile modal
    document.getElementById('profileBtn').addEventListener('click', openProfileModal);
    document.getElementById('closeProfileModal').addEventListener('click', closeProfileModal);
    document.getElementById('editProfileBtn').addEventListener('click', showProfileEditForm);
    document.getElementById('cancelEditBtn').addEventListener('click', hideProfileEditForm);
    document.getElementById('profileEditForm').addEventListener('submit', handleProfileSave);

    // Skills input
    document.getElementById('skillInput').addEventListener('keydown', handleSkillInput);

    // Bio character counter
    document.getElementById('editBio').addEventListener('input', updateBioCharCount);

    // Create portal modal
    document.getElementById('createPortalBtn').addEventListener('click', openCreatePortalModal);
    document.getElementById('closePortalModal').addEventListener('click', closeCreatePortalModal);
    document.getElementById('cancelPortalBtn').addEventListener('click', closeCreatePortalModal);
    document.getElementById('createPortalForm').addEventListener('submit', handleCreatePortal);

    // Portal description character counter
    document.getElementById('portalDescription').addEventListener('input', updateDescCharCount);

    // Delete modal
    document.getElementById('closeDeleteModal').addEventListener('click', closeDeleteModal);
    document.getElementById('cancelDeleteBtn').addEventListener('click', closeDeleteModal);
    document.getElementById('confirmDeleteBtn').addEventListener('click', handleDeletePortal);

    // Close modals on overlay click
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeAllModals();
            }
        });
    });

    // Close modals on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeAllModals();
        }
    });
}

/* ============================================
   Load User Data
   ============================================ */
async function loadUserData() {
    try {
        const response = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            currentUser = await response.json();
            updateUIWithUserData();
            loadPortals();
        } else if (response.status === 401 || response.status === 403) {
            // Not authenticated, redirect to login
            window.location.href = '/login';
        } else {
            showToast('Failed to Load User Data', 'error');
        }
    } catch (error) {
        console.error('Error Loading User Data:', error);
        showToast('Unable to Connect to Server', 'error');
    }
}

function updateUIWithUserData() {
    if (!currentUser) return;

    // Update welcome message
    document.getElementById('userName').textContent = currentUser.firstName;

    // Update profile info
    document.getElementById('profileName').textContent = `${currentUser.firstName} ${currentUser.lastName}`;
    document.getElementById('profileEmail').textContent = currentUser.email;

    // Update profile data if available
    if (currentUser.profile) {
        userProfile = currentUser.profile;
        document.getElementById('profileBio').textContent = currentUser.profile.bio || 'No Bio Yet. Click Edit to Add One.';
        document.getElementById('totalSessions').textContent = currentUser.profile.totalSessions || 0;
        document.getElementById('profileRating').textContent =
            currentUser.profile.rating > 0 ? currentUser.profile.rating.toFixed(1) : '--';

        // Update skills
        const skillsList = document.getElementById('skillsList');
        if (currentUser.profile.skills && currentUser.profile.skills.length > 0) {
            skillsList.innerHTML = currentUser.profile.skills
                .map(skill => `<span class="skill-tag">${escapeHtml(skill)}</span>`)
                .join('');
        } else {
            skillsList.innerHTML = '<span class="no-skills">No Skills Added Yet</span>';
        }
    }

    // Update portal count
    document.getElementById('totalPortals').textContent = currentUser.portals ? currentUser.portals.length : 0;
}

/* ============================================
   Load Portals
   ============================================ */
async function loadPortals() {
    const portalsGrid = document.getElementById('portalsGrid');
    const loadingState = document.getElementById('portalsLoading');
    const emptyState = document.getElementById('portalsEmpty');

    // Show loading state
    loadingState.style.display = 'flex';
    emptyState.style.display = 'none';

    try {
        const response = await fetch('/api/portals', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            allPortals = await response.json();

            // Hide loading
            loadingState.style.display = 'none';

            if (!allPortals || allPortals.length === 0) {
                emptyState.style.display = 'flex';
                return;
            }

            emptyState.style.display = 'none';

            // Clear existing portal cards (except loading and empty states)
            const existingCards = portalsGrid.querySelectorAll('.portal-card');
            existingCards.forEach(card => card.remove());

            // Portals are already sorted by createdAt DESC from the backend
            // Render portal cards
            allPortals.forEach((portal, index) => {
                const card = createPortalCard(portal, index);
                portalsGrid.appendChild(card);
            });
        } else if (response.status === 401 || response.status === 403) {
            // Not authenticated, redirect to login
            window.location.href = '/login';
        } else {
            loadingState.style.display = 'none';
            showToast('Failed to Load Portals', 'error');
        }
    } catch (error) {
        console.error('Error Loading Portals:', error);
        loadingState.style.display = 'none';
        showToast('Unable to Connect to Server', 'error');
    }
}

function createPortalCard(portal, index) {
    const card = document.createElement('div');
    card.className = 'portal-card';
    card.style.animationDelay = `${index * 0.1}s`;
    card.dataset.portalId = portal.id;

    const creatorName = portal.creator
        ? `${portal.creator.firstName} ${portal.creator.lastName}`
        : `${currentUser.firstName} ${currentUser.lastName}`;

    const creatorInitials = portal.creator
        ? `${portal.creator.firstName[0]}${portal.creator.lastName[0]}`
        : `${currentUser.firstName[0]}${currentUser.lastName[0]}`;

    const createdDate = new Date(portal.createdAt).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
    });

    // Only show delete button if current user is the creator
    const isOwner = portal.creator && currentUser && portal.creator.id === currentUser.id;
    const deleteButtonHtml = isOwner ? `
            <button class="portal-delete-btn" title="Delete Portal" data-portal-id="${portal.id}" data-portal-topic="${escapeHtml(portal.topic)}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
            </button>
        ` : '';

    card.innerHTML = `
        <div class="portal-header">
            <h3 class="portal-topic">${escapeHtml(portal.topic)}</h3>
            ${deleteButtonHtml}
        </div>
        <p class="portal-description">${escapeHtml(portal.description)}</p>
        <div class="portal-footer">
            <div class="portal-creator">
                <div class="creator-avatar">${creatorInitials}</div>
                <span class="creator-name">${escapeHtml(creatorName)}</span>
            </div>
            <span class="portal-date">${createdDate}</span>
        </div>
    `;

    // Add delete button listener only if owner
    if (isOwner) {
        const deleteBtn = card.querySelector('.portal-delete-btn');
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            openDeleteModal(portal.id, portal.topic);
        });
    }

    return card;
}

/* ============================================
   Logout Handler
   ============================================ */
async function handleLogout() {
    try {
        const response = await fetch('/api/users/logout', {
            method: 'POST',
            credentials: 'include'
        });

        if (response.ok) {
            showToast('Logged Out Successfully', 'success');
            setTimeout(() => {
                window.location.href = '/login';
            }, 1000);
        } else {
            showToast('Logout Failed', 'error');
        }
    } catch (error) {
        console.error('Logout Error:', error);
        showToast('Unable to Logout', 'error');
    }
}

/* ============================================
   Profile Modal Functions
   ============================================ */
function openProfileModal() {
    const modal = document.getElementById('profileModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeProfileModal() {
    const modal = document.getElementById('profileModal');
    modal.classList.remove('active');
    document.body.style.overflow = '';
    hideProfileEditForm();
}

function showProfileEditForm() {
    const profileView = document.getElementById('profileView');
    const profileEdit = document.getElementById('profileEditForm');

    // Pre-fill form with current data
    if (userProfile) {
        document.getElementById('editBio').value = userProfile.bio || '';
        editSkills = userProfile.skills ? [...userProfile.skills] : [];
    } else {
        document.getElementById('editBio').value = '';
        editSkills = [];
    }

    updateBioCharCount();
    renderSkillTags();

    profileView.style.display = 'none';
    profileEdit.style.display = 'block';
}

function hideProfileEditForm() {
    const profileView = document.getElementById('profileView');
    const profileEdit = document.getElementById('profileEditForm');

    profileView.style.display = 'block';
    profileEdit.style.display = 'none';

    // Clear alerts
    document.getElementById('profileErrorAlert').classList.remove('visible');

    // Reset loading state
    document.getElementById('saveProfileBtn').classList.remove('loading');
}

function updateBioCharCount() {
    const bio = document.getElementById('editBio').value;
    document.getElementById('bioCharCount').textContent = bio.length;
}

/* ============================================
   Skills Management
   ============================================ */
function handleSkillInput(e) {
    if (e.key === 'Enter' || e.key === ',') {
        e.preventDefault();
        const input = e.target;
        const skill = input.value.trim().replace(',', '');

        if (skill && !editSkills.includes(skill) && editSkills.length < 20) {
            editSkills.push(skill);
            renderSkillTags();
            input.value = '';
        } else if (editSkills.includes(skill)) {
            input.value = '';
        }
    }
}

function renderSkillTags() {
    const container = document.getElementById('skillsTags');
    container.innerHTML = editSkills.map((skill, index) => `
        <div class="skill-tag-input">
            <span>${escapeHtml(skill)}</span>
            <button type="button" onclick="removeSkill(${index})">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="12" height="12">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
            </button>
        </div>
    `).join('');
}

function removeSkill(index) {
    editSkills.splice(index, 1);
    renderSkillTags();
}

/* ============================================
   Profile Save Handler
   ============================================ */
async function handleProfileSave(e) {
    e.preventDefault();

    const saveBtn = document.getElementById('saveProfileBtn');
    const errorAlert = document.getElementById('profileErrorAlert');
    const errorMessage = document.getElementById('profileErrorMessage');

    // Hide previous errors
    errorAlert.classList.remove('visible');

    // Get form data
    const bio = document.getElementById('editBio').value.trim();

    // Validate
    if (!bio) {
        errorMessage.textContent = 'Bio is Required';
        errorAlert.classList.add('visible');
        return;
    }

    if (bio.length > 1500) {
        errorMessage.textContent = 'Bio Cannot Exceed 1500 Characters';
        errorAlert.classList.add('visible');
        return;
    }

    // Set loading state
    saveBtn.classList.add('loading');
    saveBtn.disabled = true;

    const profileData = {
        bio: bio,
        skills: editSkills
    };

    try {
        // Determine if we're creating or updating
        const method = userProfile ? 'PUT' : 'POST';

        const response = await fetch('/api/profiles/me', {
            method: method,
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(profileData)
        });

        if (response.ok) {
            const updatedProfile = await response.json();
            userProfile = updatedProfile;

            // Update currentUser's profile reference
            currentUser.profile = updatedProfile;

            // Update UI
            updateUIWithUserData();
            hideProfileEditForm();
            showToast('Profile Updated Successfully', 'success');
        } else {
            let errorText = 'Failed to Save Profile';
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    errorText = errorData.message;
                } else if (errorData.errors) {
                    errorText = Object.values(errorData.errors).join(', ');
                }
            } catch (e) {
                // Use default error message
            }
            errorMessage.textContent = errorText;
            errorAlert.classList.add('visible');
        }
    } catch (error) {
        console.error('Profile Save Error:', error);
        errorMessage.textContent = 'Unable to Connect to Server';
        errorAlert.classList.add('visible');
    } finally {
        saveBtn.classList.remove('loading');
        saveBtn.disabled = false;
    }
}

/* ============================================
   Create Portal Modal Functions
   ============================================ */
function openCreatePortalModal() {
    const modal = document.getElementById('createPortalModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';

    // Reset form
    document.getElementById('createPortalForm').reset();
    document.getElementById('descCharCount').textContent = '0';
    document.getElementById('portalErrorAlert').classList.remove('visible');
    document.getElementById('portalSuccessAlert').classList.remove('visible');
}

function closeCreatePortalModal() {
    const modal = document.getElementById('createPortalModal');
    modal.classList.remove('active');
    document.body.style.overflow = '';

    // Reset form and states
    document.getElementById('createPortalForm').reset();
    document.getElementById('submitPortalBtn').classList.remove('loading');
}

function updateDescCharCount() {
    const desc = document.getElementById('portalDescription').value;
    document.getElementById('descCharCount').textContent = desc.length;
}

/* ============================================
   Create Portal Handler
   ============================================ */
async function handleCreatePortal(e) {
    e.preventDefault();

    const submitBtn = document.getElementById('submitPortalBtn');
    const errorAlert = document.getElementById('portalErrorAlert');
    const errorMessage = document.getElementById('portalErrorMessage');
    const successAlert = document.getElementById('portalSuccessAlert');
    const successMessage = document.getElementById('portalSuccessMessage');

    // Hide previous alerts
    errorAlert.classList.remove('visible');
    successAlert.classList.remove('visible');

    // Get form data
    const topic = document.getElementById('portalTopic').value.trim();
    const description = document.getElementById('portalDescription').value.trim();

    // Validate
    if (!topic) {
        setFieldError('portalTopic', 'Topic is Required');
        return;
    }

    if (topic.length > 100) {
        setFieldError('portalTopic', 'Topic Must Be 100 Characters or Less');
        return;
    }

    if (!description) {
        setFieldError('portalDescription', 'Description is Required');
        return;
    }

    if (description.length > 1000) {
        setFieldError('portalDescription', 'Description Must Be 1000 Characters or Less');
        return;
    }

    // Set loading state
    submitBtn.classList.add('loading');
    submitBtn.disabled = true;

    const portalData = {
        topic: topic,
        description: description
    };

    try {
        const response = await fetch('/api/portals', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(portalData)
        });

        if (response.ok) {
            const newPortal = await response.json();

            // Add to current user's portals
            if (!currentUser.portals) {
                currentUser.portals = [];
            }
            currentUser.portals.push(newPortal);

            // Update UI
            loadPortals();
            document.getElementById('totalPortals').textContent = currentUser.portals.length;

            // Show success and close modal
            successMessage.textContent = 'Portal Created Successfully!';
            successAlert.classList.add('visible');

            setTimeout(() => {
                closeCreatePortalModal();
            }, 1500);
        } else {
            let errorText = 'Failed to Create Portal';
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    errorText = errorData.message;
                } else if (errorData.errors) {
                    // Handle validation errors
                    Object.entries(errorData.errors).forEach(([field, message]) => {
                        if (field === 'topic') {
                            setFieldError('portalTopic', message);
                        } else if (field === 'description') {
                            setFieldError('portalDescription', message);
                        }
                    });
                    errorText = 'Please Fix the Validation Errors';
                }
            } catch (e) {
                // Use default error message
            }
            errorMessage.textContent = errorText;
            errorAlert.classList.add('visible');
        }
    } catch (error) {
        console.error('Create Portal Error:', error);
        errorMessage.textContent = 'Unable to Connect to Server';
        errorAlert.classList.add('visible');
    } finally {
        submitBtn.classList.remove('loading');
        submitBtn.disabled = false;
    }
}

/* ============================================
   Delete Portal Functions
   ============================================ */
function openDeleteModal(portalId, portalTopic) {
    portalToDelete = portalId;
    document.getElementById('deletePortalTopic').textContent = portalTopic;

    const modal = document.getElementById('deleteModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    modal.classList.remove('active');
    document.body.style.overflow = '';
    portalToDelete = null;

    // Reset button state
    document.getElementById('confirmDeleteBtn').classList.remove('loading');
}

async function handleDeletePortal() {
    if (!portalToDelete) return;

    const deleteBtn = document.getElementById('confirmDeleteBtn');
    deleteBtn.classList.add('loading');
    deleteBtn.disabled = true;

    try {
        const response = await fetch(`/api/portals/${portalToDelete}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (response.ok || response.status === 204) {
            // Remove from local data
            currentUser.portals = currentUser.portals.filter(p => p.id !== portalToDelete);

            // Update UI
            loadPortals();
            document.getElementById('totalPortals').textContent = currentUser.portals.length;

            closeDeleteModal();
            showToast('Portal Deleted Successfully', 'success');
        } else {
            let errorText = 'Failed to Delete Portal';
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    errorText = errorData.message;
                }
            } catch (e) {
                // Use default error message
            }
            showToast(errorText, 'error');
        }
    } catch (error) {
        console.error('Delete Portal Error:', error);
        showToast('Unable to Connect to Server', 'error');
    } finally {
        deleteBtn.classList.remove('loading');
        deleteBtn.disabled = false;
    }
}

/* ============================================
   Modal Utilities
   ============================================ */
function closeAllModals() {
    closeProfileModal();
    closeCreatePortalModal();
    closeDeleteModal();
}

/* ============================================
   Form Utilities
   ============================================ */
function setFieldError(fieldId, message) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('visible');
    }
}

function clearFieldError(fieldId) {
    const errorElement = document.getElementById(fieldId + 'Error');
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('visible');
    }
}

/* ============================================
   Toast Notification
   ============================================ */
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');

    toastMessage.textContent = message;
    toast.className = `toast ${type} visible`;

    setTimeout(() => {
        toast.classList.remove('visible');
    }, 3000);
}

/* ============================================
   Utility Functions
   ============================================ */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Make removeSkill available globally for onclick handler
window.removeSkill = removeSkill;