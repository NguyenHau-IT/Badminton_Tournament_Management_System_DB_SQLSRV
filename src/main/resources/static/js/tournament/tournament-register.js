/**
 * Tournament Registration Page JavaScript
 * Multi-step form validation and navigation
 */

document.addEventListener('DOMContentLoaded', function() {
    let currentStep = 1;
    const totalSteps = 4;

    // Initialize AOS
    AOS.init({
        duration: 800,
        once: true
    });

    // Navigation functions
    window.nextStep = function() {
        if (validateStep(currentStep)) {
            if (currentStep < totalSteps) {
                currentStep++;
                updateFormDisplay();
            }
        }
    };

    window.previousStep = function() {
        if (currentStep > 1) {
            currentStep--;
            updateFormDisplay();
        }
    };

    function updateFormDisplay() {
        // Update form sections
        document.querySelectorAll('.form-section').forEach((section, index) => {
            section.classList.toggle('active', index + 1 === currentStep);
        });

        // Update progress steps
        document.querySelectorAll('.step').forEach((step, index) => {
            const stepNum = index + 1;
            step.classList.toggle('active', stepNum === currentStep);
            step.classList.toggle('completed', stepNum < currentStep);
        });

        // Update progress line
        const progressPercent = ((currentStep - 1) / (totalSteps - 1)) * 100;
        document.querySelector('.progress-line').style.width = progressPercent + '%';

        // Update navigation buttons
        document.querySelector('.btn-prev').style.display = currentStep === 1 ? 'none' : 'block';
        
        const nextBtn = document.querySelector('.btn-next');
        if (currentStep === totalSteps) {
            nextBtn.textContent = 'Hoàn tất đăng ký';
            nextBtn.onclick = submitForm;
        } else {
            nextBtn.textContent = 'Tiếp theo';
            nextBtn.onclick = nextStep;
        }

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function validateStep(step) {
        const currentSection = document.querySelector(`.form-section:nth-child(${step})`);
        const requiredFields = currentSection.querySelectorAll('[required]');
        let isValid = true;

        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                isValid = false;
                field.classList.add('is-invalid');
                
                // Show error message
                let errorDiv = field.nextElementSibling;
                if (!errorDiv || !errorDiv.classList.contains('invalid-feedback')) {
                    errorDiv = document.createElement('div');
                    errorDiv.className = 'invalid-feedback';
                    errorDiv.textContent = 'Vui lòng điền thông tin này';
                    field.parentNode.appendChild(errorDiv);
                }
            } else {
                field.classList.remove('is-invalid');
            }

            // Remove error on input
            field.addEventListener('input', function() {
                this.classList.remove('is-invalid');
            });
        });

        if (!isValid) {
            alert('Vui lòng điền đầy đủ thông tin bắt buộc!');
        }

        return isValid;
    }

    function submitForm() {
        if (validateStep(currentStep - 1)) {
            // Show loading
            const submitBtn = document.querySelector('.btn-next');
            const originalText = submitBtn.innerHTML;
            submitBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
            submitBtn.disabled = true;

            // Simulate API call
            setTimeout(() => {
                // In production: send actual form data to server
                console.log('Form submitted successfully');
                
                // Show success message
                document.querySelector('.form-content').innerHTML = `
                    <div class="success-message">
                        <i class="bi bi-check-circle-fill success-icon"></i>
                        <h2 class="success-title">Đăng ký thành công!</h2>
                        <p class="success-text">
                            Cảm ơn bạn đã đăng ký tham gia giải đấu.<br>
                            Chúng tôi đã gửi email xác nhận đến địa chỉ của bạn.
                        </p>
                        <a href="/tournaments" class="btn btn-primary">
                            <i class="bi bi-arrow-left"></i> Quay lại danh sách giải đấu
                        </a>
                    </div>
                `;

                // Hide progress bar
                document.querySelector('.progress-bar-container').style.display = 'none';
            }, 2000);
        }
    }

    // Category change handler
    const categorySelect = document.getElementById('category');
    if (categorySelect) {
        categorySelect.addEventListener('change', function() {
            const partnerSection = document.getElementById('partnerSection');
            const selectedCategory = this.value;
            
            // Show partner section for doubles categories
            if (selectedCategory && (selectedCategory.includes('Đôi') || selectedCategory.includes('Mixed'))) {
                partnerSection.style.display = 'block';
            } else {
                partnerSection.style.display = 'none';
            }
        });
    }

    // Add partner button
    window.addPartner = function() {
        document.getElementById('partnerFields').style.display = 'block';
        document.getElementById('addPartnerBtn').style.display = 'none';
    };

    window.removePartner = function() {
        document.getElementById('partnerFields').style.display = 'none';
        document.getElementById('addPartnerBtn').style.display = 'block';
        
        // Clear partner fields
        document.querySelectorAll('#partnerFields input').forEach(input => {
            input.value = '';
        });
    };

    // Form field animations
    const formControls = document.querySelectorAll('.form-control, .form-select');
    formControls.forEach(control => {
        control.addEventListener('focus', function() {
            this.parentElement.querySelector('.form-label')?.classList.add('text-primary');
        });

        control.addEventListener('blur', function() {
            this.parentElement.querySelector('.form-label')?.classList.remove('text-primary');
        });
    });

    // Initialize display
    updateFormDisplay();
});
