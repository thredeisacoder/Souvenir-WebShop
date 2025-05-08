/**
 * Xử lý validation cho các form checkout
 */

// Đối tượng chứa các thông báo lỗi được tải từ server
let errorMessages = {};

// Tải các thông báo lỗi từ server khi trang được load
document.addEventListener('DOMContentLoaded', function() {
    // Tải thông báo lỗi từ server
    loadErrorMessages();
    
    // Xử lý form thêm địa chỉ mới
    const addAddressForm = document.getElementById('addAddressForm');
    const saveAddressButton = document.getElementById('saveAddressBtn');
    
    if (addAddressForm && saveAddressButton) {
        console.log('Form và nút Lưu địa chỉ đã được tìm thấy');
        
        // Xử lý sự kiện click vào nút "Lưu địa chỉ"
        saveAddressButton.addEventListener('click', function(event) {
            console.log('Nút Lưu địa chỉ được click');
            // Ngăn chặn form submit mặc định
            event.preventDefault();
            
            // Validate form trước khi submit
            if (!validateAddressForm(addAddressForm)) {
                console.log('Form không hợp lệ, dừng việc submit');
                return false;
            }
            
            // Combine địa chỉ từ các thành phần nhỏ
            combineAddressComponents();
            
            // Nếu không có lỗi, submit form
            console.log('Form hợp lệ, đang submit...');
            addAddressForm.submit();
        });
        
        // Cũng xử lý sự kiện submit của form để tránh submit khi còn lỗi
        addAddressForm.addEventListener('submit', function(event) {
            // Cần ngăn chặn submit mặc định và để cho xử lý click trên nút xử lý
            event.preventDefault();
            
            // Validate form trước khi submit
            if (!validateAddressForm(addAddressForm)) {
                console.log('Form không hợp lệ, dừng việc submit');
                return false;
            }
            
            // Combine địa chỉ từ các thành phần nhỏ
            combineAddressComponents();
            
            // Tiếp tục submit nếu hợp lệ
            console.log('Form hợp lệ, đang submit...');
            // Form sẽ tự submit vì chúng ta không gọi preventDefault nếu hợp lệ
        });
    }
    
    // Debug nếu không tìm thấy form hoặc nút
    if (!addAddressForm) {
        console.error('Không tìm thấy form addAddressForm');
    }
    if (!saveAddressButton) {
        console.error('Không tìm thấy nút Lưu địa chỉ');
    }
    
    // Đăng ký xử lý sự kiện khi modal hiển thị
    const addAddressModal = document.getElementById('addAddressModal');
    if (addAddressModal) {
        addAddressModal.addEventListener('shown.bs.modal', function() {
            console.log('Modal đã hiển thị, xóa các thông báo lỗi cũ');
            // Xóa các thông báo lỗi cũ khi modal hiển thị
            const form = document.getElementById('addAddressForm');
            if (form) {
                clearFormErrors(form);
            }
        });
    }
});

/**
 * Validate các trường trong form địa chỉ
 * @param {HTMLElement} form - Form element
 * @returns {boolean} Kết quả validation
 */
function validateAddressForm(form) {
    // Kiểm tra các trường bắt buộc
    const errors = {};
    
    // Kiểm tra địa chỉ đường
    const streetAddress = document.getElementById('streetAddress').value.trim();
    if (!streetAddress) {
        errors.streetAddress = getErrorMessage("INVALID_ADDRESSLINE");
    }
    
    // Kiểm tra phường/xã
    const wardCommune = document.getElementById('wardCommune').value.trim();
    if (!wardCommune) {
        errors.wardCommune = getErrorMessage("INVALID_WARD_COMMUNE");
    }
    
    // Kiểm tra quận/huyện
    const district = document.getElementById('district').value.trim();
    if (!district) {
        errors.district = getErrorMessage("INVALID_DISTRICT");
    }
    
    // Kiểm tra tỉnh/thành phố
    const provinceCity = document.getElementById('provinceCity').value;
    if (!provinceCity) {
        errors.provinceCity = getErrorMessage("INVALID_PROVINCE_CITY");
    }
    
    // Kiểm tra quốc gia
    const country = document.getElementById('country').value;
    if (!country) {
        errors.country = getErrorMessage("INVALID_COUNTRY");
    }
    
    // Kiểm tra mã bưu điện
    const zipCode = document.getElementById('zipCode').value.trim();
    if (!zipCode) {
        errors.zipCode = getErrorMessage("INVALID_ZIPCODE");
    }
    
    console.log('Kết quả validation:', errors);
    
    // Nếu có lỗi, hiển thị tất cả lỗi
    if (Object.keys(errors).length > 0) {
        displayFormErrors(form, errors);
        return false;
    }
    
    return true;
}

/**
 * Tải thông báo lỗi từ server
 */
function loadErrorMessages() {
    fetch('/error-messages.json')
        .then(response => {
            if (!response.ok) {
                throw new Error('Không thể tải thông báo lỗi');
            }
            return response.json();
        })
        .then(data => {
            // Chuyển đổi mảng thành object để dễ truy cập
            const messages = {};
            if (data && data.errors) {
                data.errors.forEach(error => {
                    messages[error.messageCode] = error.description;
                });
            }
            errorMessages = messages;
            console.log('Đã tải thông báo lỗi:', errorMessages);
        })
        .catch(error => {
            console.error('Lỗi khi tải thông báo lỗi:', error);
        });
}

/**
 * Lấy thông báo lỗi theo message code
 * @param {string} messageCode - Mã lỗi
 * @returns {string} Thông báo lỗi
 */
function getErrorMessage(messageCode) {
    // Nếu không có message code hoặc thông báo lỗi chưa được tải
    if (!messageCode || Object.keys(errorMessages).length === 0) {
        // Fallback messages nếu không tìm thấy từ server
        const fallbackMessages = {
            "INVALID_ADDRESSLINE": "Vui lòng nhập số nhà, tên đường",
            "INVALID_WARD_COMMUNE": "Vui lòng nhập phường/xã",
            "INVALID_DISTRICT": "Vui lòng nhập quận/huyện",
            "INVALID_ZIPCODE": "Vui lòng nhập mã bưu điện",
            "INVALID_PROVINCE_CITY": "Vui lòng chọn tỉnh/thành phố",
            "INVALID_COUNTRY": "Vui lòng chọn quốc gia"
        };
        
        return fallbackMessages[messageCode] || `Vui lòng kiểm tra lại thông tin ${messageCode}`;
    }
    
    return errorMessages[messageCode] || messageCode;
}

/**
 * Xóa tất cả thông báo lỗi hiện tại
 * @param {HTMLElement} form - Form element
 */
function clearFormErrors(form) {
    // Xóa tất cả class is-invalid
    form.querySelectorAll('.is-invalid').forEach(el => {
        el.classList.remove('is-invalid');
    });
    
    // Xóa nội dung của các phần tử invalid-feedback
    form.querySelectorAll('.invalid-feedback').forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
}

/**
 * Hiển thị tất cả các lỗi bên dưới trường tương ứng
 * @param {HTMLElement} form - Form element
 * @param {Object} errors - Object chứa các lỗi theo tên trường
 */
function displayFormErrors(form, errors) {
    // Xóa tất cả thông báo lỗi cũ
    clearFormErrors(form);
    
    // Thêm thông báo lỗi mới
    for (const [fieldName, errorMessage] of Object.entries(errors)) {
        const field = form.querySelector(`#${fieldName}`);
        if (field) {
            // Thêm class lỗi cho trường input
            field.classList.add('is-invalid');
            
            // Tìm và hiển thị thẻ invalid-feedback bên dưới trường hiện tại
            const errorElement = document.getElementById(`${fieldName}-error`) || 
                               field.nextElementSibling;
            
            if (errorElement && errorElement.classList.contains('invalid-feedback')) {
                errorElement.textContent = errorMessage;
                errorElement.style.display = 'block';
            } else {
                // Fallback: Tạo element mới nếu không tìm thấy
                const newErrorElement = document.createElement('div');
                newErrorElement.className = 'invalid-feedback';
                newErrorElement.textContent = errorMessage;
                newErrorElement.style.display = 'block';
                field.parentNode.insertBefore(newErrorElement, field.nextSibling);
            }
        } else {
            console.warn(`Không tìm thấy trường ${fieldName} để hiển thị lỗi`);
        }
    }
    
    // Cuộn đến trường lỗi đầu tiên
    const firstErrorField = form.querySelector('.is-invalid');
    if (firstErrorField) {
        firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
        firstErrorField.focus();
    }
}

/**
 * Kết hợp các thành phần địa chỉ thành một dòng địa chỉ đầy đủ
 */
function combineAddressComponents() {
    const streetAddress = document.getElementById('streetAddress').value.trim();
    const wardCommune = document.getElementById('wardCommune').value.trim();
    const district = document.getElementById('district').value.trim();
    
    // Kết hợp thành một dòng địa chỉ
    const combinedAddress = [streetAddress, wardCommune, district].filter(Boolean).join(', ');
    document.getElementById('combinedAddressLine').value = combinedAddress;
} 