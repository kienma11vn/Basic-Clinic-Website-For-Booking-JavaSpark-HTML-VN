-- 1. Tạo Cơ sở dữ liệu
CREATE DATABASE PhongKhamAnTam;
GO

USE PhongKhamAnTam;
GO

-- 2. Tạo bảng TaiKhoan (Tài khoản)
CREATE TABLE TaiKhoan (
    maTK NVARCHAR(50) PRIMARY KEY, -- Mã tài khoản  
    tenTK NVARCHAR(20) NOT NULL CHECK (LEN(tenTK) BETWEEN 8 AND 20), -- Tên đăng nhập 8-20 kí tự  
    matKhau NVARCHAR(15) NOT NULL CHECK (LEN(matKhau) BETWEEN 6 AND 15), -- Mật khẩu 6-15 kí tự  
    loaiTK NVARCHAR(10) CHECK (loaiTK IN (N'LT', N'KH', N'BS')), -- Loại tài khoản  
    emailDK NVARCHAR(100) -- Email đăng ký  
);
GO

-- 3. Tạo bảng Nguoi (Người)
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
GO

-- 4. Tạo bảng LeTan (Nhân viên lễ tân)
CREATE TABLE LeTan (
    maNV NVARCHAR(50) PRIMARY KEY, -- Mã nhân viên lễ tân  
    maTK NVARCHAR(50),
    CONSTRAINT FK_LeTan_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

-- 5. Tạo bảng KhachHang (Khách hàng)
CREATE TABLE KhachHang (
    maKH NVARCHAR(50) PRIMARY KEY, -- Mã khách hàng  
    maTK NVARCHAR(50),
    CONSTRAINT FK_KhachHang_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

-- 6. Tạo bảng BacSi (Bác sĩ)
CREATE TABLE BacSi (
    maBS NVARCHAR(50) PRIMARY KEY, -- Mã bác sĩ  
    maTK NVARCHAR(50),
    tenCK NVARCHAR(100), -- Tên chuyên khoa  
    chucDanh NVARCHAR(100), -- Chức danh  
    thamNien TINYINT CHECK (thamNien >= 0), -- Thâm niên không dưới 0  
    CONSTRAINT FK_BacSi_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

-- 7. Tạo bảng DichVu (Dịch vụ)
CREATE TABLE DichVu (
    maDV NVARCHAR(50) PRIMARY KEY, -- Mã dịch vụ  
    tenDV NVARCHAR(100), -- Tên dịch vụ 
    moTaDV NVARCHAR(MAX), -- Mô tả dịch vụ  
    giaDV FLOAT, -- Giá dịch vụ  
    tenCK NVARCHAR(100) -- Tên chuyên khoa  
);
GO

-- 8. Tạo bảng YeuCau (Yêu cầu)
CREATE TABLE YeuCau (
    maYC NVARCHAR(50) PRIMARY KEY, -- Mã yêu cầu  
    maTK NVARCHAR(50),
    noiDungYC NVARCHAR(MAX), -- Nội dung yêu cầu  
    noiDungTL NVARCHAR(MAX), -- Nội dung trả lời  
    ngayYC DATETIME, -- Ngày gửi yêu cầu  
    ngayTL DATETIME, -- Ngày gửi trả lời  
    CONSTRAINT FK_YeuCau_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK) -- Khóa ngoài liên kết TaiKhoan  
);
GO

-- 9. Tạo bảng DatLich (Đặt lịch)
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
GO

CREATE TABLE OTP_KhoiPhuc (
    email NVARCHAR(100) PRIMARY KEY,
    maOTP NVARCHAR(6) NOT NULL,
    thoiGianHetHan DATETIME NOT NULL, 
    daXacNhan BIT DEFAULT 0 -- 0: Chưa xác nhận, 1: Đã xác nhận thành công
);
GO

DECLARE @ConstraintName nvarchar(200);
-- Lấy tên ràng buộc CHECK hiện tại của cột tinhTrang
SELECT @ConstraintName = name 
FROM sys.check_constraints 
WHERE parent_object_id = OBJECT_ID('DatLich') 
AND parent_column_id = (
    SELECT column_id 
    FROM sys.columns 
    WHERE object_id = OBJECT_ID('DatLich') 
    AND name = 'tinhTrang'
);
-- Nếu tồn tại thì xóa nó đi
IF @ConstraintName IS NOT NULL
BEGIN
    EXEC('ALTER TABLE DatLich DROP CONSTRAINT ' + @ConstraintName);
END
-- Thêm lại ràng buộc mới với danh sách giá trị đầy đủ
ALTER TABLE DatLich
ADD CONSTRAINT CK_DatLich_TinhTrang 
CHECK (tinhTrang IN (N'ChoXN', N'DaXN', N'Huy', N'XNHuy', N'KHuy'));

-- 10. Tạo bảng XacNhan (Xác nhận lịch khám từ Lễ tân)
CREATE TABLE XacNhan (
    maDatLich NVARCHAR(50) PRIMARY KEY, -- Khóa chính đồng thời là khóa ngoại liên kết 1-1 hoặc 1-n với DatLich
    xnDat NVARCHAR(50), -- Mã lễ tân xác nhận yêu cầu đặt lịch
    xnDen NVARCHAR(50), -- Mã lễ tân xác nhận khách đã đến phòng khám
    xnHuy NVARCHAR(50), -- Mã lễ tân xác nhận yêu cầu hủy lịch
    
    -- Khóa ngoài liên kết với bảng DatLich
    CONSTRAINT FK_XacNhan_DatLich FOREIGN KEY (maDatLich) REFERENCES DatLich(maDatLich),
    
    -- Khóa ngoài liên kết với bảng LeTan để đảm bảo mã nhân viên tồn tại
    CONSTRAINT FK_XacNhan_LeTan_Dat FOREIGN KEY (xnDat) REFERENCES LeTan(maNV),
    CONSTRAINT FK_XacNhan_LeTan_Den FOREIGN KEY (xnDen) REFERENCES LeTan(maNV),
    CONSTRAINT FK_XacNhan_LeTan_Huy FOREIGN KEY (xnHuy) REFERENCES LeTan(maNV)
);
GO