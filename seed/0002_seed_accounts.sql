INSERT INTO TaiKhoan (maTK, tenTK, matKhau, loaiTK, emailDK) VALUES 
('TK_LT01', N'letan01_pk', '12345678', N'LT', 'letan01@clinic.com'),
('TK_LT02', N'letan02_pk', '12345678', N'LT', 'letan02@clinic.com'),
('TK_LT03', N'letan03_pk', '12345678', N'LT', 'letan03@clinic.com');

INSERT INTO LeTan (maNV, maTK) VALUES 
('NV01', 'TK_LT01'),
('NV02', 'TK_LT02'),
('NV03', 'TK_LT03');

INSERT INTO TaiKhoan (maTK, tenTK, matKhau, loaiTK, emailDK) VALUES 
('TK_BS01', N'bs_ngoai01', 'password123', N'BS', 'ngoai01@clinic.com'),
('TK_BS02', N'bs_ngoai02', 'password123', N'BS', 'ngoai02@clinic.com'),
('TK_BS03', N'bs_ngoai03', 'password123', N'BS', 'ngoai03@clinic.com'),
('TK_BS04', N'bs_ngoai04', 'password123', N'BS', 'ngoai04@clinic.com'),
('TK_BS05', N'bs_ngoai05', 'password123', N'BS', 'ngoai05@clinic.com'),
('TK_BS06', N'bs_ngoai06', 'password123', N'BS', 'ngoai06@clinic.com'),
('TK_BS07', N'bs_ngoai07', 'password123', N'BS', 'ngoai07@clinic.com'),
('TK_BS08', N'bs_ngoai08', 'password123', N'BS', 'ngoai08@clinic.com');

INSERT INTO BacSi (maBS, maTK, tenCK, chucDanh, thamNien) VALUES 
('BS01', 'TK_BS01', N'Ngo?i T?ng Quát', N'Th?c s? - Bác s?', 10),
('BS02', 'TK_BS02', N'Ngo?i Ch?n Th??ng Ch?nh Hình', N'Bác s? Chuyên khoa I', 8),
('BS03', 'TK_BS03', N'Ngo?i Th?n Kinh', N'Ti?n s? - Bác s?', 15),
('BS04', 'TK_BS04', N'Ngo?i Ti?t Ni?u', N'Bác s? Chuyên khoa II', 12),
('BS05', 'TK_BS05', N'Ngo?i L?ng Ng?c - Tim M?ch', N'Th?c s? - Bác s?', 7),
('BS06', 'TK_BS06', N'Ngo?i Tiêu Hóa', N'Bác s?', 5),
('BS07', 'TK_BS07', N'Ngo?i Nhi', N'Th?c s? - Bác s?', 9),
('BS08', 'TK_BS08', N'Ngo?i Ung B??u', N'Bác s? Chuyên khoa I', 11);

-- Thêm thông tin cho 3 Lễ tân
INSERT INTO Nguoi (soCCCD, maTK, hoTen, ngaySinh, diaChi, SDT, gioiTinh) VALUES 
('001095001234', 'TK_LT01', N'Nguyễn Thùy Linh', '1998-05-15', N'123 Cầu Giấy, Hà Nội', '0912345678', N'Nu'),
('001096005678', 'TK_LT02', N'Lê Thị Mai', '1995-10-20', N'45 Long Biên, Hà Nội', '0923456789', N'Nu'),
('001097009012', 'TK_LT03', N'Trần Thu Hà', '1997-03-12', N'88 Đống Đa, Hà Nội', '0934567890', N'Nu');

-- Thêm thông tin cho 8 Bác sĩ chuyên khoa Ngoại
INSERT INTO Nguoi (soCCCD, maTK, hoTen, ngaySinh, diaChi, SDT, gioiTinh) VALUES 
('001080001111', 'TK_BS01', N'Nguyễn Văn An', '1980-01-01', N'12 Hai Bà Trưng, Hà Nội', '0987654321', N'Nam'),
('001082002222', 'TK_BS02', N'Trần Hữu Bình', '1982-03-15', N'56 Lý Thường Kiệt, Hà Nội', '0981122334', N'Nam'),
('001075003333', 'TK_BS03', N'Phạm Minh Chính', '1975-07-20', N'90 Lê Duẩn, Hà Nội', '0982233445', N'Nam'),
('001078004444', 'TK_BS04', N'Lê Hoàng Long', '1978-11-30', N'22 Kim Mã, Hà Nội', '0983344556', N'Nam'),
('001085005555', 'TK_BS05', N'Vũ Đức Thành', '1985-05-10', N'34 Nguyễn Trãi, Hà Nội', '0984455667', N'Nam'),
('001090006666', 'TK_BS06', N'Đặng Văn Hùng', '1990-09-25', N'77 Giải Phóng, Hà Nội', '0985566778', N'Nam'),
('001083007777', 'TK_BS07', N'Ngô Quý Phước', '1983-12-05', N'15 Tây Sơn, Hà Nội', '0986677889', N'Nam'),
('001081008888', 'TK_BS08', N'Bùi Quang Vinh', '1981-04-18', N'102 Bà Triệu, Hà Nội', '0987788990', N'Nam');
GO