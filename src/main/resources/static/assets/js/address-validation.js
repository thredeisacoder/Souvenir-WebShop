/**
 * Xử lý validation cho các form địa chỉ
 */

// Đối tượng chứa các thông báo lỗi được tải từ server
let errorMessages = {};

// Tải các thông báo lỗi từ server khi trang được load
document.addEventListener('DOMContentLoaded', function() {
    // Tải thông báo lỗi từ server
    loadErrorMessages();
    
    // Xử lý form thêm địa chỉ mới
    setupAddAddressForm();
    
    // Xử lý form chỉnh sửa địa chỉ
    setupEditAddressForm();
});

/**
 * Thiết lập xử lý form thêm địa chỉ
 */
function setupAddAddressForm() {
    const addAddressForm = document.getElementById('addAddressForm');
    const saveButton = document.querySelector('button[form="addAddressForm"]');
    
    if (addAddressForm && saveButton) {
        saveButton.addEventListener('click', function(event) {
            // Ngăn chặn form submit mặc định
            event.preventDefault();
            
            // Validate form trước khi submit
            if (!validateAddressForm(addAddressForm, false)) {
                console.log('Form không hợp lệ, dừng việc submit');
                return false;
            }
            
            // Combine địa chỉ từ các thành phần nhỏ
            combineAddressComponents();
            
            // Nếu không có lỗi, submit form
            console.log('Form hợp lệ, đang submit...');
            addAddressForm.submit();
        });
    }
}

/**
 * Thiết lập xử lý form chỉnh sửa địa chỉ
 */
function setupEditAddressForm() {
    const editAddressForm = document.getElementById('editAddressForm');
    const updateButton = document.querySelector('button[form="editAddressForm"]');
    
    if (editAddressForm && updateButton) {
        updateButton.addEventListener('click', function(event) {
            // Ngăn chặn form submit mặc định
            event.preventDefault();
            
            // Validate form trước khi submit
            if (!validateAddressForm(editAddressForm, true)) {
                console.log('Form chỉnh sửa không hợp lệ, dừng việc submit');
                return false;
            }
            
            // Combine địa chỉ từ các thành phần nhỏ
            combineEditAddressComponents();
            
            // Nếu không có lỗi, submit form
            console.log('Form chỉnh sửa hợp lệ, đang submit...');
            editAddressForm.submit();
        });
    }
}

/**
 * Validate các trường trong form địa chỉ
 * @param {HTMLElement} form - Form element
 * @param {boolean} isEditForm - Có phải là form chỉnh sửa không
 * @returns {boolean} Kết quả validation
 */
function validateAddressForm(form, isEditForm) {
    // Prefix cho id của các trường
    const prefix = isEditForm ? 'edit' : '';
    
    // Kiểm tra các trường bắt buộc
    const errors = {};
    
    // Kiểm tra địa chỉ đường
    const streetAddress = document.getElementById(prefix + (prefix ? 'S' : 's') + 'treetAddress').value.trim();
    if (!streetAddress) {
        errors.streetAddress = getErrorMessage("INVALID_ADDRESSLINE");
    }
    
    // Kiểm tra phường/xã
    const wardCommune = document.getElementById(prefix + 'wardCommune').value.trim();
    if (!wardCommune) {
        errors.wardCommune = getErrorMessage("INVALID_WARD_COMMUNE");
    }
    
    // Kiểm tra quận/huyện
    const district = document.getElementById(prefix + 'district').value.trim();
    if (!district) {
        errors.district = getErrorMessage("INVALID_DISTRICT");
    }
    
    // Kiểm tra tỉnh/thành phố
    const provinceCity = document.getElementById(prefix + 'provinceCity').value;
    if (!provinceCity) {
        errors.provinceCity = getErrorMessage("INVALID_PROVINCE_CITY");
    }
    
    // Kiểm tra quốc gia
    const country = document.getElementById(prefix + 'country').value;
    if (!country) {
        errors.country = getErrorMessage("INVALID_COUNTRY");
    }
    
    // Kiểm tra mã bưu điện
    const zipCode = document.getElementById(prefix + 'zipCode').value.trim();
    if (!zipCode) {
        errors.zipCode = getErrorMessage("INVALID_ZIPCODE");
    }
    
    console.log('Kết quả validation:', errors);
    
    // Nếu có lỗi, hiển thị tất cả lỗi
    if (Object.keys(errors).length > 0) {
        displayFormErrors(form, errors, isEditForm);
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
 * @param {boolean} isEditForm - Có phải là form chỉnh sửa không
 */
function displayFormErrors(form, errors, isEditForm) {
    // Xóa tất cả thông báo lỗi cũ
    clearFormErrors(form);
    
    // Thêm tiền tố cho ID trường nếu là form chỉnh sửa
    const prefix = isEditForm ? 'edit' : '';
    
    // Thêm thông báo lỗi mới
    for (const [fieldName, errorMessage] of Object.entries(errors)) {
        // Xác định ID của trường input dựa vào tên trường và loại form
        let fieldId;
        if (fieldName === 'streetAddress') {
            fieldId = prefix + (prefix ? 'S' : 's') + 'treetAddress';
        } else {
            fieldId = prefix + fieldName;
        }
        
        const field = form.querySelector(`#${fieldId}`);
        if (field) {
            // Thêm class lỗi cho trường input
            field.classList.add('is-invalid');
            
            // Tìm và hiển thị thẻ invalid-feedback bên dưới trường hiện tại
            const errorElement = document.getElementById(`${fieldId}-error`) || 
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
            console.warn(`Không tìm thấy trường ${fieldId} để hiển thị lỗi`);
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

/**
 * Kết hợp các thành phần địa chỉ thành một dòng địa chỉ đầy đủ cho form chỉnh sửa
 */
function combineEditAddressComponents() {
    const streetAddress = document.getElementById('editStreetAddress').value.trim();
    const wardCommune = document.getElementById('editWardCommune').value.trim();
    const district = document.getElementById('editDistrict').value.trim();
    
    // Kết hợp thành một dòng địa chỉ
    const combinedAddress = [streetAddress, wardCommune, district].filter(Boolean).join(', ');
    document.getElementById('editCombinedAddressLine').value = combinedAddress;
} 