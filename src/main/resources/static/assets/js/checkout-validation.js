/**
 * Xử lý validation cho các form checkout
 */

// Tải các thông báo lỗi từ server khi trang được load
document.addEventListener('DOMContentLoaded', function() {
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
            const errors = validationErrors.validateAddressForm(addAddressForm);
            if (Object.keys(errors).length > 0) {
                console.log('Form không hợp lệ, dừng việc submit');
                validationErrors.displayFormErrors(addAddressForm, errors);
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
            const errors = validationErrors.validateAddressForm(addAddressForm);
            if (Object.keys(errors).length > 0) {
                console.log('Form không hợp lệ, dừng việc submit');
                validationErrors.displayFormErrors(addAddressForm, errors);
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
                validationErrors.clearFormErrors(form);
            }
        });
    }
});

/**
 * Combine các thành phần địa chỉ thành một chuỗi địa chỉ đầy đủ
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