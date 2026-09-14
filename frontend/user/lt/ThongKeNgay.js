document.addEventListener('DOMContentLoaded', () => {
    // 1. Hiển thị tên tài khoản lễ tân từ sessionStorage
    const savedName = sessionStorage.getItem('username') || 'letan00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }

    // 2. Thiết lập ngày mặc định cho ô input là ngày hôm nay
    const dateInput = document.getElementById('filterDate');
    if (dateInput) {
        dateInput.valueAsDate = new Date();
    }

    // 3. Tự động tải dữ liệu thống kê khi vừa vào trang
    findNgay();
});

// Hàm lọc dữ liệu theo ngày
function findNgay() {
    const dateInput = document.getElementById('filterDate');
    if (!dateInput) {
        console.error("Không tìm thấy ô input 'filterDate' trong HTML!");
        return;
    }
    
    const selectedDate = dateInput.value;
    if (!selectedDate) {
        alert("Vui lòng chọn ngày để xem thống kê!");
        return;
    }

    // Gọi API thống kê từ Java Spark Backend (Cổng 9999)
    fetch(`http://localhost:9999/api/admin/thong-ke-ngay?ngay=${selectedDate}`)
        .then(res => {
            if (!res.ok) throw new Error("Không thể kết nối với máy chủ");
            return res.json();
        })
        .then(data => {
            if (data.error) {
                alert("Lỗi server: " + data.error);
                return;
            }
            
            const tbody = document.getElementById('thongKeBody');
            if (tbody) {
                // Đổ dữ liệu vào bảng
                tbody.innerHTML = `
                    <tr>
                        <td>${data.ngay}</td>
                        <td style="font-weight: bold; color: #000;">${data.tongLich || 0}</td>
                        <td style="color: #ce0e00; font-weight: bold;">${data.soHuy || 0}</td>
                        <td style="color: #149c3d; font-weight: bold;">${data.soDenKham || 0}</td>
                    </tr>
                `;
            }
        })
        .catch(err => {
            console.error("Lỗi khi lấy dữ liệu:", err);
            alert("Có lỗi xảy ra khi kết nối API.");
        });
}

// Hàm tạo và tải báo cáo văn bản
function createReport() {
    const dateInput = document.getElementById('filterDate');
    const selectedDate = dateInput ? dateInput.value : null;
    
    if (!selectedDate) {
        alert("Vui lòng chọn ngày để xuất báo cáo!");
        return;
    }

    // Kích hoạt tải file từ API
    window.location.href = `http://localhost:9999/api/admin/xuat-bao-cao?ngay=${selectedDate}`;
}