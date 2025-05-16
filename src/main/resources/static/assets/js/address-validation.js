/**
 * Xử lý validation cho các form địa chỉ
 */

// Tải các thông báo lỗi từ server khi trang được load
document.addEventListener('DOMContentLoaded', function() {
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
            const errors = validationErrors.validateAddressForm(addAddressForm, false);
            if (Object.keys(errors).length > 0) {
                console.log('Form không hợp lệ, dừng việc submit');
                validationErrors.displayFormErrors(addAddressForm, errors, false);
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
            const errors = validationErrors.validateAddressForm(editAddressForm, true);
            if (Object.keys(errors).length > 0) {
                console.log('Form chỉnh sửa không hợp lệ, dừng việc submit');
                validationErrors.displayFormErrors(editAddressForm, errors, true);
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
 * Combine các thành phần địa chỉ thành một chuỗi địa chỉ đầy đủ cho form thêm mới
 */
function combineAddressComponents() {
    const streetAddress = document.getElementById('streetAddress').value.trim();
    const wardCommune = document.getElementById('wardCommune').value.trim();
    const district = document.getElementById('district').value.trim();
    const provinceCity = document.getElementById('provinceCity').value;
    const country = document.getElementById('country').value;
    const zipCode = document.getElementById('zipCode').value.trim();
    
    // Combine thành địa chỉ đầy đủ
    const fullAddress = `${streetAddress}, ${wardCommune}, ${district}, ${provinceCity}, ${country} ${zipCode}`;
    
    // Cập nhật vào trường địa chỉ đầy đủ
    const fullAddressField = document.getElementById('fullAddress');
    if (fullAddressField) {
        fullAddressField.value = fullAddress;
    }
}

/**
 * Combine các thành phần địa chỉ thành một chuỗi địa chỉ đầy đủ cho form chỉnh sửa
 */
function combineEditAddressComponents() {
    const streetAddress = document.getElementById('editStreetAddress').value.trim();
    const wardCommune = document.getElementById('editWardCommune').value.trim();
    const district = document.getElementById('editDistrict').value.trim();
    const provinceCity = document.getElementById('editProvinceCity').value;
    const country = document.getElementById('editCountry').value;
    const zipCode = document.getElementById('editZipCode').value.trim();
    
    // Combine thành địa chỉ đầy đủ
    const fullAddress = `${streetAddress}, ${wardCommune}, ${district}, ${provinceCity}, ${country} ${zipCode}`;
    
    // Cập nhật vào trường địa chỉ đầy đủ
    const fullAddressField = document.getElementById('editFullAddress');
    if (fullAddressField) {
        fullAddressField.value = fullAddress;
    }
} 