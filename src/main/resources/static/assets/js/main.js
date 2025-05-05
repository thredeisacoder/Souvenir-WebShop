/**
 * Main JavaScript file for The Souvenir website
 * Handles resource loading, error recovery, and performance optimization
 */

// Theo dõi tình trạng tải tài nguyên
const resourceMonitor = {
    failedResources: {},
    cssLoaded: false,
    jsLoaded: false,
    
    // Đánh dấu tài nguyên bị lỗi
    markFailed: function(resourceType, url) {
        this.failedResources[resourceType] = this.failedResources[resourceType] || [];
        this.failedResources[resourceType].push(url);
        console.warn(`Failed to load ${resourceType}: ${url}`);
    },
    
    // Cố gắng tải lại tài nguyên từ nguồn local
    retryLocalResource: function(resourceType, url, localUrl) {
        console.log(`Trying to load ${resourceType} from local source: ${localUrl}`);
        
        if (resourceType === 'css') {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = localUrl;
            document.head.appendChild(link);
        } else if (resourceType === 'js') {
            const script = document.createElement('script');
            script.src = localUrl;
            document.head.appendChild(script);
        }
    },
    
    // Kiểm tra và khắc phục các tài nguyên bị thiếu
    checkMissingResources: function() {
        // Kiểm tra Bootstrap CSS
        if (!document.querySelector('link[href*="bootstrap"]')) {
            this.retryLocalResource('css', 'bootstrap.min.css', '/assets/css/bootstrap.min.css');
        }
        
        // Kiểm tra Font Awesome
        if (!document.querySelector('link[href*="font-awesome"]')) {
            this.retryLocalResource('css', 'all.min.css', '/assets/css/all.min.css');
        }
        
        // Kiểm tra Bootstrap JS
        if (typeof bootstrap === 'undefined') {
            this.retryLocalResource('js', 'bootstrap.bundle.min.js', '/assets/js/bootstrap.bundle.min.js');
        }
    }
};

// Cải thiện hiệu suất tải trang
const performanceOptimizer = {
    // Hoãn tải các hình ảnh không nằm trên viewport
    lazyLoadImages: function() {
        if ('IntersectionObserver' in window) {
            const imgObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        const src = img.getAttribute('data-src');
                        
                        if (src) {
                            img.src = src;
                            img.removeAttribute('data-src');
                        }
                        
                        observer.unobserve(img);
                    }
                });
            });
            
            // Áp dụng lazy loading cho hình ảnh có thuộc tính data-src
            document.querySelectorAll('img[data-src]').forEach(img => {
                imgObserver.observe(img);
            });
        } else {
            // Fallback cho trình duyệt không hỗ trợ IntersectionObserver
            document.querySelectorAll('img[data-src]').forEach(img => {
                img.src = img.getAttribute('data-src');
                img.removeAttribute('data-src');
            });
        }
    },
    
    // Tối ưu các sự kiện scroll và resize
    debounce: function(func, wait) {
        let timeout;
        return function() {
            const context = this;
            const args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                func.apply(context, args);
            }, wait);
        };
    }
};

// Khởi tạo khi trang đã tải xong
document.addEventListener('DOMContentLoaded', function() {
    // Ẩn loader
    setTimeout(() => {
        const loader = document.getElementById('page-loader');
        if (loader) {
            loader.style.opacity = '0';
            setTimeout(() => {
                loader.style.display = 'none';
            }, 300);
        }
    }, 500);
    
    // Kiểm tra tài nguyên
    resourceMonitor.checkMissingResources();
    
    // Áp dụng lazy loading
    performanceOptimizer.lazyLoadImages();
    
    // Xử lý các sự kiện scroll với debounce
    const debouncedScroll = performanceOptimizer.debounce(function() {
        // Xử lý sự kiện scroll ở đây
    }, 100);
    
    window.addEventListener('scroll', debouncedScroll);
    
    // Cập nhật dropdown menu khi click
    const dropdownToggles = document.querySelectorAll('.dropdown-toggle');
    dropdownToggles.forEach(toggle => {
        toggle.addEventListener('click', function() {
            const dropdownMenu = this.nextElementSibling;
            if (typeof bootstrap === 'undefined') {
                // Fallback nếu Bootstrap JS không tải được
                if (dropdownMenu.classList.contains('show')) {
                    dropdownMenu.classList.remove('show');
                } else {
                    dropdownMenu.classList.add('show');
                }
            }
        });
    });
    
    console.log('Main.js loaded successfully');
});

// Xử lý trường hợp trang treo khi tải
window.addEventListener('load', function() {
    // Đánh dấu trang đã tải xong
    document.documentElement.classList.add('page-loaded');
    
    // Ẩn loader nếu vẫn còn hiển thị
    const loader = document.getElementById('page-loader');
    if (loader) {
        loader.style.display = 'none';
    }
});

// Phát hiện bất kỳ lỗi JavaScript nào
window.addEventListener('error', function(event) {
    console.error('JavaScript error detected:', event.message, 'at', event.filename, 'line', event.lineno);
    
    // Có thể thêm logic để báo cáo lỗi về server ở đây
});