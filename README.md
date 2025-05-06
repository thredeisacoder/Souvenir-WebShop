# Souvenir-WebShop

## Tổng quan
Souvenir-WebShop là một ứng dụng web hiện đại được thiết kế để quản lý và trưng bày các sản phẩm quà lưu niệm, cho phép người dùng dễ dàng duyệt, tìm kiếm và mua sắm trực tuyến. Dự án được xây dựng với Spring Boot và tối ưu trải nghiệm người dùng thông qua thiết kế responsive sử dụng SASS/SCSS.

## Công nghệ sử dụng
- **Backend**: Spring Boot, Spring Security, Spring Data JPA/Hibernate
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Styling**: SASS/SCSS
- **Cơ sở dữ liệu**: SQL Server
- **Build tool**: Maven
- **Ngôn ngữ**: Java 17+
- **Công cụ hỗ trợ**: Node.js (cho việc biên dịch SASS)

## Cấu trúc dự án
```
Souvenir-WebShop/
├── src/
│   ├── main/
│   │   ├── java/project/demo/          # Mã nguồn Java
│   │   │   ├── config/                 # Cấu hình ứng dụng
│   │   │   ├── controller/             # Các controller xử lý request
│   │   │   ├── enums/                  # Các enum định nghĩa trạng thái, loại,...
│   │   │   ├── exception/              # Xử lý ngoại lệ
│   │   │   ├── model/                  # Các entity và DTO
│   │   │   ├── repository/             # Repository truy xuất dữ liệu
│   │   │   ├── scheduler/              # Các tác vụ lập lịch
│   │   │   ├── security/               # Cấu hình bảo mật
│   │   │   └── service/                # Xử lý logic nghiệp vụ
│   │   └── resources/
│   │       ├── static/                 # Tài nguyên tĩnh
│   │       │   └── assets/
│   │       │       ├── css/            # CSS đã biên dịch
│   │       │       ├── img/            # Hình ảnh
│   │       │       ├── js/             # JavaScript
│   │       │       └── scss/           # Mã nguồn SASS
│   │       ├── templates/              # Template Thymeleaf
│   │       │   ├── account/            # Trang quản lý tài khoản
│   │       │   ├── auth/               # Trang đăng nhập/đăng ký
│   │       │   ├── blog/               # Trang blog
│   │       │   ├── cart/               # Trang giỏ hàng
│   │       │   ├── checkout/           # Quy trình thanh toán
│   │       │   ├── error/              # Trang lỗi
│   │       │   ├── home/               # Trang chủ
│   │       │   ├── layouts/            # Layout chung
│   │       │   ├── products/           # Trang sản phẩm
│   │       │   └── promotions/         # Trang khuyến mãi
│   │       └── application.properties  # Cấu hình ứng dụng
│   └── test/                           # Mã nguồn kiểm thử
├── pom.xml                             # Cấu hình Maven
└── package.json                        # Cấu hình Node.js
```

## Bắt đầu

### Yêu cầu cài đặt
- Java 17 hoặc cao hơn
- Maven
- Node.js và npm
- SQL Server 2019 hoặc cao hơn

### Cài đặt
1. Clone repository
```bash
git clone https://github.com/thredeisacoder/Souvenir-WebShop.git
cd Souvenir-WebShop
```

2. Cài đặt dependencies Java
```bash
mvn install
```

3. Cài đặt dependencies Node.js
```bash
npm install
```

4. Cấu hình SQL Server
- Tạo cơ sở dữ liệu mới có tên 'souvenir'
- Đảm bảo SQL Server đang chạy trên localhost:1433
- Cập nhật thông tin đăng nhập cơ sở dữ liệu trong `application.properties` nếu cần

### Chạy ứng dụng

1. Khởi động ứng dụng Spring Boot:
```bash
mvn spring-boot:run
```

2. Biên dịch SASS trong chế độ theo dõi:
```bash
npm run sass
```

Ứng dụng sẽ được triển khai tại `http://localhost:8080`

### Cấu hình cơ sở dữ liệu
Dự án sử dụng SQL Server với cấu hình mặc định như sau:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=souvenir;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=your_password
```

Các thông số cơ sở dữ liệu quan trọng:
- Máy chủ: localhost
- Cổng: 1433
- Tên database: souvenir
- Phương thức xác thực: SQL Server Authentication
- Tên người dùng mặc định: sa
- Mật khẩu mặc định: your_password (thay đổi cho môi trường production)

### Phát triển SASS
- File SASS được đặt tại `src/main/resources/static/assets/scss/`
- File SASS chính là `main.scss`
- CSS được biên dịch tự động vào `src/main/resources/static/assets/css/`
- Thay đổi trong file SASS sẽ được theo dõi và biên dịch tự động khi bạn chạy:
```bash
npm run sass
```

### Cấu hình ứng dụng
Các cài đặt chính trong `application.properties`:
```properties
server.port=8080
spring.thymeleaf.cache=false
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Tính năng dự án
- **Giao diện người dùng:**
  - Thiết kế responsive hiện đại sử dụng SASS/SCSS
  - Trải nghiệm mua sắm trực quan với hình ảnh chất lượng cao
  - Blog thông tin về sản phẩm và xu hướng lưu niệm
- **Chức năng chính:**
  - Tìm kiếm và lọc sản phẩm theo nhiều tiêu chí
  - Giỏ hàng và quy trình thanh toán mượt mà
  - Đăng ký, đăng nhập và quản lý tài khoản
  - Theo dõi đơn hàng và lịch sử mua hàng
  - Khuyến mãi và mã giảm giá
- **Bảo mật:**
  - Xác thực và phân quyền với Spring Security
  - Bảo vệ dữ liệu người dùng
  - Giao dịch thanh toán an toàn

## Phát triển
- Templates được cache trong môi trường production nhưng tắt trong development để hỗ trợ hot-reloading
- Ghi log SQL được bật để giúp debug các thao tác với cơ sở dữ liệu
- Templates Thymeleaf hỗ trợ mã hóa UTF-8 để hiển thị ký tự đúng
- Schema cơ sở dữ liệu được cập nhật tự động thông qua JPA/Hibernate

## Lưu ý triển khai Production
- Thay đổi mật khẩu cơ sở dữ liệu trong môi trường production
- Cân nhắc bật cache template trong production
- Rà soát cài đặt bảo mật SQL Server
- Cấu hình SSL/TLS phù hợp cho kết nối cơ sở dữ liệu
- Điều chỉnh cài đặt `ddl-auto` dựa trên nhu cầu triển khai

## Đóng góp
1. Fork repository
2. Tạo nhánh feature
3. Commit thay đổi của bạn
4. Push lên nhánh
5. Tạo Pull Request

## Giấy phép
Dự án này được cấp phép theo Giấy phép MIT - xem file LICENSE để biết chi tiết
