# 🏥 Hệ Thống Đặt Lịch Khám Bệnh - PHÒNG KHÁM AN TÂM

Dự án website quản lý và đặt lịch khám bệnh trực tuyến dành cho **Phòng Khám An Tâm**. Đây là ứng dụng được xây dựng phục vụ **mục đích giáo dục và học tập** phục vụ cho hệ thống quản lý trong lĩnh vực y tế, minh họa mô hình kiến trúc Web Service chuẩn kết hợp giữa Backend bằng **Java Spark** (RESTful API) và Frontend thuần (**HTML5 / CSS3 / JavaScript**).

<img width="1366" height="637" alt="image" src="https://github.com/user-attachments/assets/baceaa04-446d-4ae2-8214-c2164c9e05fd" />
<img width="1366" height="607" alt="image" src="https://github.com/user-attachments/assets/2ae4d85a-b2ce-4918-bba3-21b1059c7ddb" />
<img width="1366" height="757" alt="image" src="https://github.com/user-attachments/assets/63b634e2-208e-401a-bfc4-f0ad6baacf31" />
<img width="1366" height="757" alt="image" src="https://github.com/user-attachments/assets/0193973e-4ff3-46c6-b8ad-5d778eb9abf6" />
<img width="1366" height="757" alt="image" src="https://github.com/user-attachments/assets/7058ced6-8e9e-4826-b45a-6c19b0b2ceae" />

## 🛠 1. Công Nghệ Sử Dụng

### Backend

* **Ngôn ngữ:** Java 11 / Java 17

* **Framework:** [Spark Java](https://sparkjava.com/) (Micro-framework cho RESTful API)

* **Thư viện & Công cụ:**

  * `Google Gson`: Parse và định dạng dữ liệu JSON.

  * `dotenv-java`: Quản lý cấu hình biến môi trường (`.env`).

  * `JDBC`: Kết nối và xử lý giao dịch CSDL (MySQL / SQL Server).

  * `Maven`: Quản lý phụ thuộc và build dự án.

### Frontend

* **Core:** HTML5, CSS3, JavaScript (ES6+)

* **UI Framework:** Bootstrap 5.3

* **Icon & Font:** Google Material Symbols, Segoe UI

### Database

* **RDBMS:** MySQL 8.0+ / Microsoft SQL Server

* **Quản lý giao dịch:** ACID Transaction (sử dụng `conn.setAutoCommit(false)`, `commit()`, `rollback()`)

## 📁 2. Cấu Trúc Thư Mục Dự Án

```
├── seed/   # Chứa các file SQL để khởi tạo Database
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── datlichpk/
│   │               ├── DatLichPK.java             # File main khởi chạy server & các API chính
│   │               ├── DangKyLichHandler.java     # Xử lý đăng ký lịch
│   │               ├── QLBacSiHandler.java        # Quản lý bác sĩ
│   │               ├── QLKhachHangHandler.java    # Quản lý khách hàng
│   │               ├── QLDichVuHandler.java       # Quản lý dịch vụ
│   │               ├── ThongKeNgayHandler.java    # Báo cáo thống kê theo ngày
│   │               ├── ThongKeThangHandler.java   # Báo cáo thống kê theo tháng
│   │               ├── ThongKeNamHandler.java     # Báo cáo thống kê theo năm
│   │               └── ...                        # Các Route Handlers khác
│   ├── .env                                       # File cấu hình biến môi trường
│   └── pom.xml                                    # File cấu hình Maven Dependencies
│
└── frontend/
    ├── PK_TrangChu.html                           # Trang chủ hệ thống
    ├── PK_TrangChu.js                             # Logic chuyển hướng & carousel
    ├── PK_TrangChu.css                            # Custom style trang chủ
    ├── Shared.css                                 # Style dùng chung (Headers, Footers, Buttons)
    ├── img1.jpg, img2.jpg, img3.jpg               # Hình ảnh slideshow
    ├── bandau/                                    # Giao diện khởi đầu & xác thực
    │   ├── DangKyGD.html                          # Giao diện Đăng ký
    │   ├── DangNhapGD.html                        # Giao diện Đăng nhập
    │   └── KhoiPhucMKGD.html                      # Giao diện Khôi phục mật khẩu
	└── user/                                      
	    └── ...                                    # Giao diện các Người dùng
```

## ✨ 3. Tính Năng Chính Theo Phân Quyền

| Vai trò | Tính năng nổi bật | 
 | ----- | ----- | 
| **🙋‍♂️ Bệnh nhân (Role: `KH`)** | • Đăng ký tài khoản, đăng nhập, khôi phục mật khẩu.  • Tra cứu đội ngũ bác sĩ chuyên khoa và bảng giá dịch vụ.  • Đăng ký đặt lịch khám, chỉnh sửa hoặc hủy lịch hẹn.  • Xem lịch sử lượt đã đến khám và gửi đánh giá dịch vụ. | 
| **👨‍⚕️ Bác sĩ (Role: `BS`)** | • Xem lịch phân công làm việc / khám bệnh.  • Xem danh sách bệnh nhân đã đăng ký khám.  • Hỗ trợ, giải đáp thắc mắc của bệnh nhân. | 
| **🛡️ Lễ tân  (Role: `LT`)** | • Duyệt lịch hẹn, xác nhận khách đã đến khám (`XNDenKham`).  • Quản lý danh mục Bác sĩ, Dịch vụ y tế và Hồ sơ khách hàng (CRUD).  • Thống kê doanh thu và lượt khám theo **Ngày**, **Tháng**, **Năm**. | 

## ⚙️ 4. Hướng Dẫn Cài Đặt & Khởi Chạy

### Step 1: Chuẩn bị môi trường

* Cài đặt **JDK 11** hoặc cao hơn.

* Cài đặt **Apache Maven 3.x**.

* Cài đặt & khởi chạy CSDL **MySQL** hoặc **SQL Server**.

### Step 2: Khởi tạo Cơ sở dữ liệu

Chạy các câu lệnh SQL để tạo cấu trúc bảng cơ bản:

```
CREATE DATABASE IF NOT EXISTS PhongKhamAnTam;
USE PhongKhamAnTam;

CREATE TABLE TaiKhoan (
    maTK NVARCHAR(50) PRIMARY KEY, -- Mã tài khoản [cite: 3]
    tenTK NVARCHAR(20) NOT NULL CHECK (LEN(tenTK) BETWEEN 8 AND 20), -- Tên đăng nhập 8-20 kí tự
    matKhau NVARCHAR(15) NOT NULL CHECK (LEN(matKhau) BETWEEN 6 AND 15), -- Mật khẩu 6-15 kí tự 
    loaiTK NVARCHAR(10) CHECK (loaiTK IN (N'LT', N'KH', N'BS')), -- Loại tài khoản 
    emailDK NVARCHAR(100) -- Email đăng ký [cite: 3]
);

CREATE TABLE Nguoi (
    soCCCD NVARCHAR(20) PRIMARY KEY, -- Số căn cước công dân  
    maTK NVARCHAR(50), 
    hoTen NVARCHAR(100), -- Họ tên  
    ngaySinh DATETIME, -- Ngày sinh  
    diaChi NVARCHAR(255), -- Địa chỉ  
    SDT NVARCHAR(10) CHECK (LEN(SDT) = 10), -- Số điện thoại 10 kí tự  
    gioiTinh NVARCHAR(5) CHECK (gioiTinh IN (N'Nam', N'Nu')), -- Giới tính  
    CONSTRAINT FK_Nguoi_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);

CREATE TABLE LeTan (
    maNV NVARCHAR(50) PRIMARY KEY, -- Mã nhân viên lễ tân  
    maTK NVARCHAR(50),
    CONSTRAINT FK_LeTan_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

CREATE TABLE KhachHang (
    maKH NVARCHAR(50) PRIMARY KEY, -- Mã khách hàng  
    maTK NVARCHAR(50),
    CONSTRAINT FK_KhachHang_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

CREATE TABLE BacSi (
    maBS NVARCHAR(50) PRIMARY KEY, -- Mã bác sĩ  
    maTK NVARCHAR(50),
    tenCK NVARCHAR(100), -- Tên chuyên khoa  
    chucDanh NVARCHAR(100), -- Chức danh  
    thamNien TINYINT CHECK (thamNien >= 0), -- Thâm niên không dưới 0  
    CONSTRAINT FK_BacSi_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);

CREATE TABLE DichVu (
    maDV NVARCHAR(50) PRIMARY KEY, -- Mã dịch vụ  
    tenDV NVARCHAR(100), -- Tên dịch vụ 
    moTaDV NVARCHAR(MAX), -- Mô tả dịch vụ  
    giaDV FLOAT, -- Giá dịch vụ  13]
    tenCK NVARCHAR(100) -- Tên chuyên khoa  
);

CREATE TABLE DatLich (
    maDatLich NVARCHAR(50) PRIMARY KEY, -- Mã đặt lịch khám  
    maKH NVARCHAR(50),
    maBS NVARCHAR(50),
    maDV NVARCHAR(50),
    ngayDat DATETIME, -- Ngày đặt lịch khám  
    tinhTrang NVARCHAR(20) CHECK (tinhTrang IN (N'ChoXN', N'DaXN', N'Huy')), -- Tình trạng  
    khachDenKham NVARCHAR(5) CHECK (khachDenKham IN (N'Yes', N'No')), -- Đánh dấu khách đến  
    danhGia NVARCHAR(MAX), -- Ghi nhận đánh giá  
    trieuChung NVARCHAR(200), -- Triệu chứng tối đa 200 kí tự  
    CONSTRAINT FK_DatLich_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH), -- Khóa ngoài liên kết KhachHang  
    CONSTRAINT FK_DatLich_BacSi FOREIGN KEY (maBS) REFERENCES BacSi(maBS), -- Khóa ngoài liên kết BacSi  
    CONSTRAINT FK_DatLich_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV) -- Khóa ngoài liên kết DichVu  
);
```

### Step 3: Cấu hình Backend (`.env`)

Tạo file `.env` tại thư mục `backend/` (cùng cấp với `pom.xml`):

```
PORT=9999
DB_URL=jdbc:mysql://localhost:3306/PhongKhamAnTam?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
DB_USER=root
DB_PASS=123456
```

### Step 4: Chạy Server Backend

Mở terminal tại thư mục `backend/`:

```
# Tải dependencies và biên dịch
mvn clean install

# Khởi chạy server Spark
mvn exec:java -Dexec.mainClass="datlichpk.DatLichPK"
```

Server backend sẽ chạy tại đường dẫn: `http://localhost:9999`

### Step 5: Chạy Giao diện Frontend

1. Truy cập vào thư mục `frontend/`.

2. Mở file `PK_TrangChu.html` bằng trình duyệt web (Chrome, Edge, Firefox) hoặc sử dụng extension **Live Server** trong VS Code để khởi chạy.

## 🎓 5. Ý Nghĩa Học Tập (Educational Purpose)

Dự án này được thiết kế để phục vụ học tập và thực hành các kiến thức:

1. **RESTful API:** Cách tạo các endpoint HTTP (`GET`, `POST`, `PUT`, `DELETE`) đơn giản với Java Spark Framework.

2. **Database Transaction:** Áp dụng commit/rollback để bảo đảm tính toàn vẹn dữ liệu khi thực hiện chèn dữ liệu đồng thời vào nhiều bảng (`TaiKhoan` và `KhachHang`).

3. **AJAX & Fetch API:** Kết nối bất đồng bộ giữa Frontend JavaScript thuần và Backend Java API.

4. **CORS & Middleware:** Cấu hình Cross-Origin Resource Sharing để cho phép Frontend gọi API từ domain/port khác.

© 2026 **PHÒNG KHÁM AN TÂM** - Dự án giáo dục phi thương mại.
