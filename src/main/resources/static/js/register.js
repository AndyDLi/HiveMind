/**
 * HiveMind Registration Page JavaScript
 * Handles form validation, submission, and UI interactions
 */

document.addEventListener('DOMContentLoaded', () => {
    initParticles();
    initForm();
    initPasswordToggles();
});

/* ============================================
   Particle Animation
   ============================================ */
function initParticles() {
    const particlesContainer = document.getElementById('particles');
    if (!particlesContainer) return;

    const particleCount = 30;

    for (let i = 0; i < particleCount; i++) {
        createParticle(particlesContainer, i);
    }
}

function createParticle(container, index) {
    const particle = document.createElement('div');
    particle.className = 'particle';

    // Random position
    particle.style.left = Math.random() * 100 + '%';

    // Random size
    const size = Math.random() * 4 + 2;
    particle.style.width = size + 'px';
    particle.style.height = size + 'px';

    // Random animation delay and duration
    particle.style.animationDelay = (Math.random() * 8) + 's';
    particle.style.animationDuration = (Math.random() * 4 + 6) + 's';

    // Random color between cyan and purple
    const colors = ['#00d4ff', '#7b2cbf', '#c77dff', '#00e5ff'];
    particle.style.background = colors[Math.floor(Math.random() * colors.length)];

    container.appendChild(particle);
}

/* ============================================
   Form Initialization
   ============================================ */
function initForm() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    const inputs = form.querySelectorAll('input');

    // Add input event listeners for real-time validation
    inputs.forEach(input => {
        input.addEventListener('input', () => {
            if (input.value.trim()) {
                clearFieldError(input.id);
                validateField(input);
            } else {
                // Reset to neutral state when input is cleared
                resetFieldState(input.id);
            }
        });

        input.addEventListener('blur', () => {
            if (input.value.trim()) {
                validateField(input);
            } else {
                // Reset to neutral state when leaving empty field
                resetFieldState(input.id);
            }
        });
    });

    // Form submission
    form.addEventListener('submit', handleSubmit);
}

/* ============================================
   Password Toggle
   ============================================ */
function initPasswordToggles() {
    const toggleButtons = document.querySelectorAll('.toggle-password');

    toggleButtons.forEach(button => {
        button.addEventListener('click', () => {
            const input = button.parentElement.querySelector('input');
            const isPassword = input.type === 'password';

            input.type = isPassword ? 'text' : 'password';
            button.classList.toggle('active', isPassword);
        });
    });
}

/* ============================================
   Field Validation
   ============================================ */
function validateField(input) {
    const fieldId = input.id;
    const value = input.value.trim();
    let isValid = true;
    let errorMessage = '';

    switch (fieldId) {
        case 'firstName':
            if (!value) {
                isValid = false;
                errorMessage = 'First Name is Required';
            } else if (value.length > 100) {
                isValid = false;
                errorMessage = 'First Name Must be 100 Characters or Less';
            }
            break;

        case 'lastName':
            if (!value) {
                isValid = false;
                errorMessage = 'Last Name is Required';
            } else if (value.length > 100) {
                isValid = false;
                errorMessage = 'Last Name Must be 100 Characters or Less';
            }
            break;

        case 'email':
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!value) {
                isValid = false;
                errorMessage = 'Email is Required';
            } else if (!emailRegex.test(value)) {
                isValid = false;
                errorMessage = 'Please Enter a Valid Email Address';
            }
            break;

        case 'plainPassword':
            if (!value) {
                isValid = false;
                errorMessage = 'Password is Required';
            } else if (value.length < 8) {
                isValid = false;
                errorMessage = 'Password Must Be at Least 8 Characters';
            } else if (value.length > 64) {
                isValid = false;
                errorMessage = 'Password Must Be 64 Characters or Less';
            }

            // Also validate confirm password if it has a value
            const confirmInput = document.getElementById('confirmPlainPassword');
            if (confirmInput && confirmInput.value) {
                validateField(confirmInput);
            }
            break;

        case 'confirmPlainPassword':
            const passwordValue = document.getElementById('plainPassword').value;
            if (!value) {
                isValid = false;
                errorMessage = 'Please Confirm Your Password';
            } else if (value !== passwordValue) {
                isValid = false;
                errorMessage = 'Passwords Do Not Match';
            }
            break;
    }

    if (isValid) {
        setFieldSuccess(fieldId);
    } else {
        setFieldError(fieldId, errorMessage);
    }

    return isValid;
}

function setFieldError(fieldId, message) {
    const input = document.getElementById(fieldId);
    const errorElement = document.getElementById(fieldId + 'Error');

    if (input) {
        input.classList.remove('success', 'neutral');
        input.classList.add('error');
    }

    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('visible');
    }
}

function setFieldSuccess(fieldId) {
    const input = document.getElementById(fieldId);
    const errorElement = document.getElementById(fieldId + 'Error');

    if (input) {
        input.classList.remove('error', 'neutral');
        input.classList.add('success');
    }

    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('visible');
    }
}

function resetFieldState(fieldId) {
    const input = document.getElementById(fieldId);
    const errorElement = document.getElementById(fieldId + 'Error');

    if (input) {
        input.classList.remove('error', 'success');
        input.classList.add('neutral');
    }

    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('visible');
    }
}

function clearFieldError(fieldId) {
    const input = document.getElementById(fieldId);
    const errorElement = document.getElementById(fieldId + 'Error');

    if (input) {
        input.classList.remove('error');
    }

    if (errorElement) {
        errorElement.classList.remove('visible');
    }
}

/* ============================================
   Form Validation
   ============================================ */
function validateForm() {
    const fields = ['firstName', 'lastName', 'email', 'plainPassword', 'confirmPlainPassword'];
    let isValid = true;

    fields.forEach(fieldId => {
        const input = document.getElementById(fieldId);
        if (input && !validateField(input)) {
            isValid = false;
        }
    });

    return isValid;
}

/* ============================================
   Form Submission
   ============================================ */
async function handleSubmit(event) {
    event.preventDefault();

    // Hide any existing alerts
    hideAlerts();

    // Validate form
    if (!validateForm()) {
        showError('Please Fix the Errors Above');
        return;
    }

    const submitBtn = document.getElementById('submitBtn');

    // Get form data
    const formData = {
        firstName: document.getElementById('firstName').value.trim(),
        lastName: document.getElementById('lastName').value.trim(),
        email: document.getElementById('email').value.trim(),
        plainPassword: document.getElementById('plainPassword').value,
        confirmPlainPassword: document.getElementById('confirmPlainPassword').value
    };

    // Set loading state
    setLoadingState(true);

    try {
        const response = await fetch('/api/user/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(formData)
        });

        if (response.ok) {
            const userData = await response.json();
            setSuccessState();
            showSuccess(`Welcome, ${userData.firstName}! Redirecting to Login...`);

            // Redirect to login page after a delay
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
        } else {
            let errorMessage = 'Registration Failed. Please Try Again.';

            // Clone response so we can read it as text if JSON parsing fails
            const responseClone = response.clone();

            try {
                const errorData = await response.json();

                // Handle ValidationErrorResponse (has errors map)
                if (errorData.errors && typeof errorData.errors === 'object') {
                    // Map field errors to UI
                    const fieldMapping = {
                        'firstName': 'firstName',
                        'lastName': 'lastName',
                        'email': 'email',
                        'plainPassword': 'plainPassword',
                        'confirmPlainPassword': 'confirmPlainPassword'
                    };

                    Object.entries(errorData.errors).forEach(([field, message]) => {
                        const mappedField = fieldMapping[field];
                        if (mappedField) {
                            setFieldError(mappedField, message);
                        }
                    });

                    errorMessage = errorData.message || 'Please Fix the Validation Errors';
                }
                // Handle ErrorResponse (has message only)
                else if (errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (e) {
                // Response might not be JSON, use the cloned response
                try {
                    const textError = await responseClone.text();
                    if (textError) {
                        errorMessage = textError;
                    }
                } catch (textParseError) {
                    // Keep default error message
                }
            }

            // Handle specific error cases based on HTTP status codes
            if (response.status === 409) {
                setFieldError('email', 'This email is already registered');
                errorMessage = 'An Account With this Email Already Exists';
            } else if (response.status === 400 && errorMessage.toLowerCase().includes('passwords do not match')) {
                setFieldError('confirmPlainPassword', 'Passwords Do Not Match');
            }

            showError(errorMessage);
            setLoadingState(false);
        }
    } catch (error) {
        console.error('Registration error:', error);
        showError('Unable to Connect to Server. Please Check Your Connection and Try Again');
        setLoadingState(false);
    }
}

/* ============================================
   UI State Management
   ============================================ */
function setLoadingState(loading) {
    const submitBtn = document.getElementById('submitBtn');
    const inputs = document.querySelectorAll('#registerForm input');

    if (loading) {
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
        inputs.forEach(input => input.disabled = true);
    } else {
        submitBtn.classList.remove('loading', 'success');
        submitBtn.disabled = false;
        inputs.forEach(input => input.disabled = false);
    }
}

function setSuccessState() {
    const submitBtn = document.getElementById('submitBtn');
    submitBtn.classList.remove('loading');
    submitBtn.classList.add('success');
}

/* ============================================
   Alert Management
   ============================================ */
function showError(message) {
    const errorAlert = document.getElementById('errorAlert');
    const errorMessage = document.getElementById('errorMessage');
    const successAlert = document.getElementById('successAlert');

    if (successAlert) {
        successAlert.classList.remove('visible');
    }

    if (errorAlert && errorMessage) {
        errorMessage.textContent = message;
        errorAlert.classList.add('visible');

        // Scroll to error if not visible
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function showSuccess(message) {
    const successAlert = document.getElementById('successAlert');
    const successMessage = document.getElementById('successMessage');
    const errorAlert = document.getElementById('errorAlert');

    if (errorAlert) {
        errorAlert.classList.remove('visible');
    }

    if (successAlert && successMessage) {
        successMessage.textContent = message;
        successAlert.classList.add('visible');
    }
}

function hideAlerts() {
    const errorAlert = document.getElementById('errorAlert');
    const successAlert = document.getElementById('successAlert');

    if (errorAlert) {
        errorAlert.classList.remove('visible');
    }

    if (successAlert) {
        successAlert.classList.remove('visible');
    }
}