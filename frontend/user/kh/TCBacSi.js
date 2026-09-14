document.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy tên tài khoản từ localStorage (đã lưu khi đăng nhập thành công)
    // Nếu không có, mặc định hiện 'khachhang01' như trong ảnh mẫu
    const savedName = sessionStorage.getItem('username') || 'khachhang00';

    // 2. Hiển thị lên giao diện
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }
	// Load danh sách ngay khi mở trang
	loadDoctors();
});

function translateStatus(status) {
    const statuses = {
        'ChoXN': 'Chờ xác nhận',
        'DaXN': 'Đã xác nhận',
        'DaKham': 'Đã đến khám',
        'Huy': 'Chưa xác nhận hủy',
        'XNHuy': 'Đã xác nhận hủy',
        'KHuy': 'Không xác nhận hủy',
        'Yes': 'Đã đến khám',
        'No': 'Không đến khám',
		'Nu': 'Nữ'
    };
    return statuses[status] || status;
}

async function loadDoctors() {
    // 1. Lấy giá trị từ các ô input khác
    const name = document.getElementById('findName').value.trim();;
    const ck = document.getElementById('findCK').value.trim(); // Lưu ý: ID nên khớp với HTML của bạn (findCK)
    const cd = document.getElementById('findCD').value.trim();
    const tn = document.getElementById('findTN').value;
    
    // 2. Lấy giá trị từ Drop-list Giới tính (SỬA Ở ĐÂY)
    const gt = document.getElementById('findGT').value;

    // 3. Xây dựng URL (Sử dụng encodeURIComponent để tránh lỗi tiếng Việt có dấu)
    const url = `http://localhost:9999/api/bac-si?name=${encodeURIComponent(name)}&ck=${encodeURIComponent(ck)}&cd=${encodeURIComponent(cd)}&tn=${tn}&gt=${encodeURIComponent(gt)}`;
    const tbody = document.querySelector('#customerTable tbody');
    try {
        const response = await fetch(url);
        const data = await response.json();
        tbody.innerHTML = "";

        data.forEach(bs => {
            const row = `<tr>
                <td>${bs.maBS}</td>
                <td class="clickable-id" onclick="showDetail('${bs.maBS}')">${bs.hoTen}</td>
                <td>${bs.tenCK}</td>
                <td>${bs.chucDanh}</td>
                <td>${bs.thamNien} năm</td>
            </tr>`;
            tbody.innerHTML += row;
        });
    } catch (err) {
        console.error("Lỗi tải bác sĩ:", err);
		tbody.innerHTML = '<tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>';
    }
}

// Hàm hiển thị chi tiết bác sĩ khi click vào tên
async function showDetail(maBS) {
    try {
        const response = await fetch(`http://localhost:9999/api/bac-si/${maBS}`);
        const bs = await response.json();
        
        const detailHtml = `
            <h3 style="border-bottom: 2px solid #79bbfd; padding-bottom: 10px; margin-bottom: 10px;">Thông tin chi tiết</h3>
            <p><strong>Mã bác sĩ:</strong> ${bs.maBS}</p>
            <p><strong>Họ tên:</strong> ${bs.hoTen}</p>
            <p><strong>Chuyên khoa:</strong> ${bs.tenCK}</p>
            <p><strong>Chức danh:</strong> ${bs.chucDanh}</p>
            <p><strong>Thâm niên:</strong> ${bs.thamNien} năm</p>
            <p><strong>Ngày sinh:</strong> ${new Date(bs.ngaySinh).toLocaleDateString('vi-VN')}</p>
            <p><strong>Số điện thoại:</strong> ${bs.SDT}</p>
            <p><strong>Giới tính:</strong> ${translateStatus(bs.gioiTinh)}</p>
            <div class="modal-actions">
                <button class="action-btn" onclick="document.getElementById('detailModal').style.display='none'" style="width: 100%; background: #423c41; color: white;">Đóng</button>
            </div>
        `;
        document.getElementById('detailContent').innerHTML = detailHtml;
        document.getElementById('detailModal').style.display = 'flex';
    } catch (error) {
        alert("Không thể lấy thông tin chi tiết bác sĩ!");
    }
}

// Hàm mở Modal tìm kiếm
function openSearchModal() {
    document.getElementById('searchModal').style.display = 'flex';
}

// Hàm đóng Modal tìm kiếm
function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

// Hàm thực hiện tìm kiếm từ Modal
function executeSearch() {
    loadDoctors(); // Gọi lại hàm load với các tham số mới
    closeSearchModal();
}