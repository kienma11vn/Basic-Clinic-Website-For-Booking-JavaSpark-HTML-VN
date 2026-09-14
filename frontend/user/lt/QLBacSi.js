let allDoctors = [];
let currentMode = 'view'; // 'view' hoặc 'delete'
let selectedMaBS = '';

document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'letan00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) displayElement.innerText = savedName;

    loadDoctorList();
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

async function loadDoctorList() {
    try {
        const response = await fetch('http://localhost:9999/api/admin/ds-bac-si');
        allDoctors = await response.json();
        renderTable(allDoctors);
    } catch (error) {
        console.error('Lỗi khi tải danh sách:', error);
    }
}

function renderTable(data) {
    const tbody = document.getElementById('doctorTableBody');
    tbody.innerHTML = '';

    data.forEach(doc => {
        const tr = document.createElement('tr');
        
        // --- KHẮC PHỤC TẠI ĐÂY ---
        let modeIcon = ''; 
        if (currentMode === 'delete') {
            modeIcon = '<span>❌</span>';
        } else if (currentMode === 'edit') {
            modeIcon = '<span>✏️</span>';
        }

        tr.innerHTML = `
            <td>${doc.maBS}</td> 
            <td>
                <p style="margin-left:45px;">${modeIcon}</p><a href="#" class="doctor-link" data-id="${doc.maBS}">${doc.hoTen}</a>
            </td>
            <td>${doc.tenCK}</td>
            <td>${doc.chucDanh}</td>
            <td>${doc.thamNien} năm</td>
        `;
        tbody.appendChild(tr);

        // Giữ nguyên phần addEventListener bên dưới của bạn...
        tr.querySelector('.doctor-link').addEventListener('click', (e) => {
            e.preventDefault();
            if (currentMode === 'delete') confirmDelete(doc);
            else if (currentMode === 'edit') showEditModal(doc);
            else showDetail(doc);
        });
    });
}

// Mở modal thêm bác sĩ
async function addBS() {
    // 1. Chuyển Mode về view để tránh xung đột icon nếu đang ở chế độ xóa/sửa
    currentMode = 'view';
    renderTable(allDoctors);

    // 2. Reset tiêu đề và cấu hình nút Lưu cho chế độ THÊM
    const modal = document.getElementById('addDoctorModal');
    modal.querySelector('h3').innerText = "Thêm Bác Sĩ Mới";
    
    const saveBtn = modal.querySelector('.modal-actions .add');
    saveBtn.innerText = "Lưu thông tin";
    saveBtn.onclick = executeAddDoctor; // Gán lại hàm thêm mới

    // 3. Mở khóa và xóa trống các trường nhập liệu
    document.getElementById('addTenTK').disabled = false;
    document.getElementById('addTenTK').value = "";
    document.getElementById('addHoTen').value = "";
    document.getElementById('addEmail').value = "";
    document.getElementById('addCCCD').value = "";
    document.getElementById('addCD').value = "";
    document.getElementById('addTN').value = "";
    document.getElementById('addSDT').value = "";
    document.getElementById('addNgaySinh').value = "";
    document.getElementById('addGT').value = "Nam";
    document.getElementById('addDiaChi').value = "";

    // 4. Tải danh sách chuyên khoa
    await loadChuyenKhoaToSelect('addCK');
    
    modal.style.display = 'flex';
}

// Gửi dữ liệu thêm bác sĩ
async function executeAddDoctor() {
    const data = {
        tenTK: document.getElementById('addTenTK').value,
        hoTen: document.getElementById('addHoTen').value,
        email: document.getElementById('addEmail').value,
        soCCCD: document.getElementById('addCCCD').value,
        tenCK: document.getElementById('addCK').value,
        chucDanh: document.getElementById('addCD').value,
        thamNien: document.getElementById('addTN').value,
        sdt: document.getElementById('addSDT').value,
        ngaySinh: document.getElementById('addNgaySinh').value,
        gioiTinh: document.getElementById('addGT').value,
        diaChi: document.getElementById('addDiaChi').value
    };

    // Kiểm tra sơ bộ (Validation)
    if (!data.tenTK || !data.hoTen || !data.soCCCD) {
        alert("Vui lòng nhập đầy đủ các thông tin bắt buộc!");
        return;
    }

    try {
        const response = await fetch('http://localhost:9999/api/admin/them-bac-si', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        if (result.status === 'success') {
            alert("Thêm bác sĩ thành công! Mã bác sĩ: " + result.maBS);
            closeModal('addDoctorModal');
            loadDoctorList(); // Load lại bảng để cập nhật dữ liệu mới
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (error) {
        console.error('Lỗi khi thêm bác sĩ:', error);
        alert("Không thể kết nối đến server!");
    }
}

function editBS() {
    if (currentMode !== 'edit') {
        currentMode = 'edit';
        alert("Chế độ Sửa: Vui lòng nhấn vào Họ tên bác sĩ ✏ để chỉnh sửa thông tin.");
    } else {
        currentMode = 'view';
    }
    renderTable(allDoctors);
}

async function showEditModal(doc) {
    const modal = document.getElementById('addDoctorModal');
    
    // 1. Đổi giao diện Modal sang chế độ CHỈNH SỬA
    modal.querySelector('h3').innerText = "Chỉnh sửa thông tin Bác sĩ";
    
    const saveBtn = modal.querySelector('.modal-actions .add');
    saveBtn.innerText = "Cập nhật thay đổi";
    saveBtn.onclick = () => executeUpdate(doc.maBS); // Gán hàm cập nhật

    // 2. Tải chuyên khoa trước khi điền dữ liệu
    await loadChuyenKhoaToSelect('addCK');

    // 3. Điền dữ liệu bác sĩ vào form
    document.getElementById('addTenTK').value = doc.tenTK;
    document.getElementById('addTenTK').disabled = true; // Không cho sửa Username (khóa chính/duy nhất)
    
    document.getElementById('addHoTen').value = doc.hoTen;
    document.getElementById('addEmail').value = doc.emailDK || doc.email || ""; 
    document.getElementById('addCCCD').value = doc.soCCCD;
    document.getElementById('addCK').value = doc.tenCK;
    document.getElementById('addCD').value = doc.chucDanh;
    document.getElementById('addTN').value = doc.thamNien;
    document.getElementById('addSDT').value = doc.SDT;
    
    // Xử lý định dạng ngày tháng (YYYY-MM-DD) cho thẻ input type="date"
    if (doc.ngaySinh) {
        const datePart = doc.ngaySinh.includes(" ") ? doc.ngaySinh.split(" ")[0] : doc.ngaySinh;
        document.getElementById('addNgaySinh').value = datePart;
    }
    
    document.getElementById('addGT').value = doc.gioiTinh === 'Nu' ? 'Nu' : 'Nam';
    document.getElementById('addDiaChi').value = doc.diaChi;

    modal.style.display = 'flex';
}

// Hàm bổ trợ để tải chuyên khoa (tách ra từ addBS cũ)
async function loadChuyenKhoaToSelect(selectId) {
    const select = document.getElementById(selectId);
    select.innerHTML = '<option value="">-- Chọn chuyên khoa --</option>';
    try {
        const response = await fetch('http://localhost:9999/api/admin/danh-sach-chuyen-khoa');
        const dsCK = await response.json();
        dsCK.forEach(ck => {
            const option = document.createElement('option');
            option.value = ck; option.textContent = ck;
            select.appendChild(option);
        });
    } catch (e) { console.error(e); }
}

async function executeUpdate(maBS) {
    const data = {
        hoTen: document.getElementById('addHoTen').value,
        email: document.getElementById('addEmail').value,
        soCCCD: document.getElementById('addCCCD').value,
        tenCK: document.getElementById('addCK').value,
        chucDanh: document.getElementById('addCD').value,
        thamNien: document.getElementById('addTN').value,
        sdt: document.getElementById('addSDT').value,
        ngaySinh: document.getElementById('addNgaySinh').value,
        gioiTinh: document.getElementById('addGT').value,
        diaChi: document.getElementById('addDiaChi').value
    };

    try {
        const response = await fetch(`http://localhost:9999/api/admin/sua-bac-si/${maBS}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        if (result.status === 'success') {
            alert("Cập nhật thông tin thành công!");
            closeModal('addDoctorModal');
            currentMode = 'view';
            loadDoctorList();
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (error) {
        alert("Lỗi kết nối server!");
    }
}

// Hàm đổi chế độ khi nhấn nút "Xóa bác sĩ"
function deleteBS() {
    if (currentMode === 'view') {
        currentMode = 'delete';
        alert("Chế độ Xóa: Vui lòng nhấn vào Họ tên bác sĩ ❌ để xóa bác sĩ.");
    } else {
        currentMode = 'view';
    }
    renderTable(allDoctors); // Vẽ lại bảng để hiện/ẩn icon
}

// Hàm hiển thị modal xác nhận xóa
function confirmDelete(doc) {
    selectedMaBS = doc.maBS; // Lưu mã BS đang chọn để xóa
    document.getElementById('delMaBS').innerText = doc.maBS;
    document.getElementById('delHoTen').innerText = doc.hoTen;
    document.getElementById('deleteConfirmModal').style.display = 'flex';
}

// Hàm thực hiện gọi API xóa (Bạn cần bổ sung logic này tương ứng với server của bạn)
async function executeDelete() {
    try {
        const response = await fetch(`http://localhost:9999/api/admin/xoa-bac-si/${selectedMaBS}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        if (result.status === 'success') {
            alert("Đã xóa bác sĩ thành công!");
            closeModal('deleteConfirmModal');
            currentMode = 'view'; // Quay về chế độ xem sau khi xóa
            loadDoctorList(); 
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (error) {
        console.error('Lỗi khi xóa:', error);
        alert("Không thể kết nối server!");
    }
}

function openSearchModal() {
    const modal = document.getElementById('searchModal');
    if (modal) {
        modal.style.display = 'flex'; // Dùng flex để căn giữa theo CSS của TCBacSi
    }
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
	currentMode = 'view';
	let selectedMaBS = '';
	renderTable(allDoctors);
}

// Hàm thực hiện tìm kiếm
function executeSearch() {
    // Lấy giá trị từ các ô input
    const maBS = document.getElementById('findMaBS').value.toLowerCase().trim();
    const hoTen = document.getElementById('findName').value.toLowerCase().trim();
    const tenCK = document.getElementById('findCK').value.toLowerCase().trim();
    const chucDanh = document.getElementById('findCD').value.toLowerCase().trim();
    const thamNien = document.getElementById('findTN').value.trim();
    const gioiTinh = document.getElementById('findGT').value;

    const filtered = allDoctors.filter(doc => {
        // Kiểm tra an toàn: nếu thuộc tính của doc bị null/undefined thì gán là chuỗi rỗng
        const dMa = (doc.maBS || "").toLowerCase();
        const dTen = (doc.hoTen || "").toLowerCase();
        const dCK = (doc.tenCK || "").toLowerCase();
        const dCD = (doc.chucDanh || "").toLowerCase();
        const dTN = (doc.thamNien || "").toString();
        const dGT = (doc.gioiTinh || "");

        // So sánh
        const matchMa = maBS === "" || dMa.includes(maBS);
        const matchTen = hoTen === "" || dTen.includes(hoTen);
        const matchCK = tenCK === "" || dCK.includes(tenCK);
        const matchCD = chucDanh === "" || dCD.includes(chucDanh);
        const matchTN = thamNien === "" || dTN === thamNien;
        const matchGT = gioiTinh === "" || dGT === gioiTinh;

        return matchMa && matchTen && matchCK && matchCD && matchTN && matchGT;
    });

    renderTable(filtered);
    // Đóng modal - Lưu ý: không dùng closeModal() vì hàm đó của bạn đang reset currentMode về view 
    // và render lại allDoctors (làm mất kết quả tìm kiếm).
    document.getElementById('searchModal').style.display = 'none';
}

function showDetail(doc) {
    const modal = document.getElementById('doctorModal');
    const detail = document.getElementById('modalDetail');
    
    // Hiển thị đầy đủ thông tin bảo mật và cá nhân như yêu cầu trước đó
    detail.innerHTML = `
        <div style="border-bottom: 1px solid #ddd; padding-bottom: 10px; margin-bottom: 10px;">
            <h3 style="text-align: center;"><strong>Thông tin bảo mật</strong></h3>
            <p>Mã tài khoản: ${doc.maTK}</p>
            <p>Tên đăng nhập: ${doc.tenTK}</p>
            <p>Mật khẩu: <span style="color:red">${doc.matKhau}</span></p>
        </div>
        <div>
            <h3 style="text-align: center;"><strong>Thông tin cá nhân</strong></h3>
            <p>CCCD: ${doc.soCCCD}</p>
            <p>Ngày sinh: ${new Date(doc.ngaySinh).toLocaleDateString('vi-VN')}</p>
            <p>Số điện thoại: ${doc.SDT}</p>
            <p>Giới tính: ${translateStatus(doc.gioiTinh)}</p>
            <p>Địa chỉ: ${doc.diaChi}</p>
        </div>
    `;
    modal.style.display = 'flex';
}