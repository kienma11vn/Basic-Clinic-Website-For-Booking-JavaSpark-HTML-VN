let allBookings = [];
let currentMode = 'view';
let selectedMaDL = '';
let addModal = null;

document.addEventListener('DOMContentLoaded', () => {
    loadBookings();
	const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
	
	const ngayKhamInput = document.getElementById('txtNgayKham');
    if (ngayKhamInput) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1); 
        const minDate = tomorrow.toISOString().split('T')[0];
        ngayKhamInput.setAttribute('min', minDate);
    }
});

async function loadBookings(query = '') {
    const res = await fetch(`http://localhost:9999/api/admin/lich-kham${query}`);
    allBookings = await res.json();
    renderTable(allBookings);
}

function translateStatus(status) {
    const statuses = {
        'ChoXN': 'Chờ xác nhận',
        'DaXN': 'Đã xác nhận',
        'DaKham': 'Đã đến khám',
        'Huy': 'Chưa xác nhận hủy',
        'XNHuy': 'Đã xác nhận hủy',
        'KHuy': 'Không xác nhận hủy',
        'Yes': 'Đã đến khám',
        'No': 'Không đến khám'
    };
    return statuses[status] || status;
}

function renderTable(data) {
    const tbody = document.querySelector('#customerTable tbody');
    tbody.innerHTML = data.map(item => {
        let idContent = `<span class="clickable-id">${item.maDatLich}</span>`;
        let iconHtml = '';

        // 1. Xử lý Icon hiển thị phía trên
        if (currentMode === 'edit') {
            iconHtml = '<span class="action-icon edit-icon">✏️</span>';
            idContent = `<a href="javascript:void(0)" class="clickable-id" onclick="openEdit('${item.maDatLich}')">${item.maDatLich}</a>`;
        } else if (currentMode === 'delete') {
            iconHtml = '<span class="action-icon delete-icon">❌</span>';
            idContent = `<a href="javascript:void(0)" class="clickable-id" onclick="openDeleteModal('${item.maDatLich}')">${item.maDatLich}</a>`;
        } else {
            // Chế độ bình thường: không có icon và không có link click
            iconHtml = ''; 
            idContent = `<span>${item.maDatLich}</span>`;
        }

        // 2. Logic trích xuất điểm số (giống XemTTLichKham)
        let danhGiaHienThi = "Chưa có";
        if (item.danhGia && item.danhGia.trim() !== "" && item.danhGia !== "Chưa có") {
            const dvMatch = item.danhGia.match(/\[DV-(\d+)đ/); // Tìm điểm dịch vụ
            const clMatch = item.danhGia.match(/\[CL-(\d+)đ/); // Tìm điểm chất lượng
            
            let points = [];
            if (dvMatch) points.push(`Phục vụ: ${dvMatch[1]}`);
            if (clMatch) points.push(`Khám chữa bệnh: ${clMatch[1]}`);
            
            if (points.length > 0) {
                danhGiaHienThi = `<span class="rating-badge">${points.join('  ')}</span>`;
            } else {
                danhGiaHienThi = item.danhGia; // Nếu không đúng format thì hiện text gốc
            }
        }

        return `
            <tr>
                <td>
                    ${iconHtml}
                    ${idContent}
                </td>
                <td>${item.ngayDat}</td>
                <td>${item.tenBacSi}</td>
                <td>${item.tenBenhNhan}</td>
                <td>${item.tenDV}</td>
                <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
                <td class="status-${item.khachDenKham}">${translateStatus(item.khachDenKham) || 'Chưa xác nhận'}</td>
                <td>${danhGiaHienThi}</td>
            </tr>
        `;
    }).join('');
}

// Điều khiển các nút chức năng
// Hàm mở Modal Thêm mới
async function addLBS() {
    addModal = document.getElementById('addBookingModal');
    addModal.style.display = 'flex';
    
    try {
        // Tải dữ liệu khởi tạo (Bệnh nhân và Chuyên khoa) từ API mới
        const res = await fetch('http://localhost:9999/api/admin/form-init-data');
        const data = await res.json();

        // Đổ dữ liệu vào select Bệnh nhân
        const khSelect = document.getElementById('addMaKH');
        khSelect.innerHTML = '<option value="">-- Chọn bệnh nhân --</option>' + 
            data.customers.map(kh => `<option value="${kh.maKH}">${kh.hoTen} (${kh.maKH})</option>`).join('');

        // Đổ dữ liệu vào select Chuyên khoa
        const ckSelect = document.getElementById('addSpecialty');
        ckSelect.innerHTML = '<option value="">-- Chọn chuyên khoa --</option>' + 
            data.specialties.map(ck => `<option value="${ck}">${ck}</option>`).join('');
            
    } catch (error) {
        console.error("Lỗi khi tải dữ liệu khởi tạo:", error);
    }
}

function editLBS() {
    currentMode = 'edit';
    alert("Chế độ Sửa: Vui lòng nhấn vào Mã đặt lịch ✏️ để chỉnh sửa.");
    renderTable(allBookings);
}

function deleteLBS() {
    currentMode = 'delete';
    alert("Chế độ Xóa: Vui lòng nhấn vào Mã đặt lịch ❌ để xóa.");
    renderTable(allBookings);
}

// Hàm xử lý khi chọn Chuyên khoa (Cascading)
async function handleSpecialtyChange() {
    const specialty = document.getElementById('addSpecialty').value;
    const bsSelect = document.getElementById('addMaBS');
    const dvSelect = document.getElementById('addMaDV');

    if (!specialty) {
        bsSelect.disabled = true;
        dvSelect.disabled = true;
        return;
    }

    try {
        const res = await fetch(`http://localhost:9999/api/admin/details-by-specialty?specialty=${encodeURIComponent(specialty)}`);
        const data = await res.json();

        // Cập nhật danh sách bác sĩ
        bsSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>' + 
            data.doctors.map(bs => `<option value="${bs.maBS}">${bs.hoTen}</option>`).join('');
        bsSelect.disabled = false;

        // Cập nhật danh sách dịch vụ
        dvSelect.innerHTML = '<option value="">-- Chọn dịch vụ --</option>' + 
            data.services.map(dv => `<option value="${dv.maDV}">${dv.tenDV}</option>`).join('');
        dvSelect.disabled = false;

    } catch (error) {
        console.error("Lỗi khi tải bác sĩ/dịch vụ:", error);
    }
}

// Hàm gửi dữ liệu lưu lịch khám
async function executeAddBooking() {
    const payload = {
        maKH: document.getElementById('addMaKH').value,
        maBS: document.getElementById('addMaBS').value,
        maDV: document.getElementById('addMaDV').value,
        // Sửa 'addNgayKham' thành 'txtNgayKham' cho đúng với HTML
        ngayKham: document.getElementById('txtNgayKham').value, 
        gioKham: document.getElementById('txtGioKham').value,
        trieuChung: document.getElementById('txtTrieuChung').value
    };

    if (!payload.maKH || !payload.maBS || !payload.maDV || !payload.ngayKham || !payload.gioKham) {
        alert("Vui lòng điền đầy đủ thông tin!");
        return;
    }

    try {
        const res = await fetch('http://localhost:9999/api/admin/lich-kham', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await res.json();

        if (result.status === 'success') {
            alert("Thêm lịch khám thành công!");
            closeModal();
            loadBookings(); 
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (error) {
        alert("Có lỗi xảy ra khi gửi dữ liệu!");
    }
}

async function executeEditBooking() {
    const maDL = document.getElementById('editMaDL').value;
    const payload = {
        maKH: document.getElementById('editMaKH').value,
        maBS: document.getElementById('editMaBS').value,
        maDV: document.getElementById('editMaDV').value,
        ngayDat: `${document.getElementById('editNgayKham').value} ${document.getElementById('editGioKham').value}`,
        trieuChung: document.getElementById('editTrieuChung').value,
        tinhTrang: "DaXN" // Giữ nguyên hoặc cập nhật trạng thái
    };

    if (!payload.maBS || !payload.maDV || !document.getElementById('editNgayKham').value) {
        alert("Vui lòng chọn đầy đủ Bác sĩ, Dịch vụ và Thời gian!");
        return;
    }

    try {
        const res = await fetch(`http://localhost:9999/api/admin/lich-kham/${maDL}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await res.json();

        if (result.status === 'success') {
            alert("Cập nhật lịch khám thành công!");
            closeModal();
            loadBookings();
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (error) {
        alert("Lỗi kết nối server khi cập nhật!");
    }
}

async function openEdit(id) {
    // 1. Tìm dữ liệu của dòng được chọn trong mảng local
    const item = allBookings.find(b => b.maDatLich === id);
    if (!item) return;

    selectedMaDL = id;
    const modal = document.getElementById('editBookingModal');
    modal.style.display = 'flex';

    // 2. Điền các thông tin cơ bản (Text/Date)
    document.getElementById('editMaDL').value = item.maDatLich;
    document.getElementById('editTenBN').value = item.tenBenhNhan;
    document.getElementById('editMaKH').value = item.maKH;
    document.getElementById('editTrieuChung').value = item.trieuChung || "";

    // Tách ngày và giờ (Giả sử item.ngayDat là "2024-05-20 08:00:00")
    if (item.ngayDat) {
        const parts = item.ngayDat.split(' ');
        document.getElementById('editNgayKham').value = parts[0];
        if (parts[1]) {
            const hour = parts[1].substring(0, 5); 
            document.getElementById('editGioKham').value = hour;
        }
    }

    // 3. Xử lý các Dropdown phụ thuộc (QUAN TRỌNG)
    try {
        // Tải danh sách Chuyên khoa trước
        const resInit = await fetch('http://localhost:9999/api/admin/form-init-data');
        const initData = await resInit.json();
        
        const ckSelect = document.getElementById('editSpecialty');
        ckSelect.innerHTML = '<option value="">-- Chọn chuyên khoa --</option>' + 
            initData.specialties.map(ck => `<option value="${ck}">${ck}</option>`).join('');

        // Gán chuyên khoa cũ (nếu trong đối tượng item có lưu tenChuyenKhoa)
        if (item.tenChuyenKhoa) {
            ckSelect.value = item.tenChuyenKhoa;
            
            // Kích hoạt nạp danh sách Bác sĩ và Dịch vụ dựa trên chuyên khoa này
            await loadDoctorAndServiceForEdit(item.tenChuyenKhoa);
            
            // Sau khi nạp xong danh sách mới gán ID cũ vào
            document.getElementById('editMaBS').value = item.maBS;
            document.getElementById('editMaDV').value = item.maDV;
        }
    } catch (error) {
        console.error("Lỗi truy xuất dữ liệu cũ:", error);
    }
}

async function handleSpecialtyChangeEdit() {
    const specialty = document.getElementById('editSpecialty').value;
    const bsSelect = document.getElementById('editMaBS');
    const dvSelect = document.getElementById('editMaDV');

    if (!specialty) return;

    const res = await fetch(`http://localhost:9999/api/admin/details-by-specialty?specialty=${encodeURIComponent(specialty)}`);
    const data = await res.json();

    bsSelect.innerHTML = data.doctors.map(bs => `<option value="${bs.maBS}">${bs.hoTen}</option>`).join('');
    dvSelect.innerHTML = data.services.map(dv => `<option value="${dv.maDV}">${dv.tenDV}</option>`).join('');
}

function findLBS() { document.getElementById('searchModal').style.display = 'flex'; }

function executeSearch() {
    const ma = document.getElementById('searchMaDL').value.trim();
    const bn = document.getElementById('searchTenBN').value.trim();
    const bs = document.getElementById('searchTenBS').value.trim();
    const ngay = document.getElementById('searchNgayKham').value; // YYYY-MM-DD

    // Tạo query string chuẩn
    let params = new URLSearchParams();
    if (ma) params.append('maDatLich', ma);
    if (bn) params.append('tenBenhNhan', bn);
    if (bs) params.append('tenBacSi', bs);
    if (ngay) params.append('ngayKham', ngay);

    loadBookings(`?${params.toString()}`);
    closeModal();
}

function openDeleteModal(id) {
    const item = allBookings.find(b => b.maDatLich === id);
    if (!item) return;

    selectedMaDL = id; // Lưu ID để dùng cho hàm executeDelete()

    // Điền thông tin vào Modal để người dùng kiểm tra trước khi xóa
    document.getElementById('delMaDL').innerText = item.maDatLich;
    document.getElementById('delTenBN').innerText = item.tenBenhNhan;
    document.getElementById('delNgayDat').innerText = item.ngayDat;

    // Hiển thị Modal
    document.getElementById('deleteModal').style.display = 'flex';
}

// Hàm thực hiện xóa thực sự khi nhấn nút "Xác nhận Xóa" trong Modal
async function executeDelete() {
    if (!selectedMaDL) return;

    try {
        const res = await fetch(`http://localhost:9999/api/admin/lich-kham/${selectedMaDL}`, {
            method: 'DELETE'
        });
        const result = await res.json();

        if (result.status === 'success') {
            alert("Xóa lịch khám thành công!");
        } else {
            alert("Lỗi: " + (result.message || "Không thể xóa lịch khám này."));
        }
    } catch (error) {
        console.error("Lỗi khi xóa:", error);
        alert("Có lỗi xảy ra khi kết nối đến server.");
    } finally {
        selectedMaDL = ''; // Reset biến tạm
        closeModal();      // Đóng modal (hàm này sẽ render lại bảng về mode view)
        loadBookings();    // Tải lại dữ liệu mới
    }
}

function closeModal() {
    document.getElementById('searchModal').style.display = 'none';
    document.getElementById('deleteModal').style.display = 'none';
    if(document.getElementById('addBookingModal')) {
        document.getElementById('addBookingModal').style.display = 'none';
    }
	document.getElementById('editBookingModal').style.display = 'none';
    currentMode = 'view';
	selectedMaDL = '';
    renderTable(allBookings);
}