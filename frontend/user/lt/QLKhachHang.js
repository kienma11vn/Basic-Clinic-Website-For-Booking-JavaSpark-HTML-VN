let allCustomers = [];
let currentMode = 'view'; // view, edit, delete

document.addEventListener('DOMContentLoaded', () => {
    loadCustomers();
	
	const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
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

async function loadCustomers() {
    const res = await fetch('http://localhost:9999/api/admin/khach-hang');
    allCustomers = await res.json();
    renderTable(allCustomers);
}

function renderTable(data) {
    const tbody = document.querySelector('#customerTable tbody');
    tbody.innerHTML = data.map(item => {
        let iconHtml = '';
        if (currentMode === 'delete') {
            iconHtml = '<span class="action-icon delete-icon">❌</span>';
        } else if (currentMode === 'edit') {
            iconHtml = '<span class="action-icon edit-icon">✏️</span>';
        }

        return `
        <tr>
            <td>${item.maTK}</td>
            <td>
                <div class="id-container">
                    ${iconHtml}
                    <a href="javascript:void(0)" class="clickable-id" 
                       onclick="handleIdClick('${item.maTK}')">${item.maKH}</a>
                </div>
            </td>
            <td>${item.emailDK}</td>
            <td>${item.hoTen || ''}</td>
            <td>${item.ngaySinh || ''}</td>
            <td>${item.SDT || ''}</td>
            <td>${item.diaChi || ''}</td>
            <td>${translateStatus(item.gioiTinh) || ''}</td>
        </tr>`;
    }).join('');
}

async function executeAddCustomer() {
    const customerData = {
        tenTK: document.getElementById('add_tenTK').value,
        hoTen: document.getElementById('add_hoTen').value,
        emailDK: document.getElementById('add_emailDK').value,
        soCCCD: document.getElementById('add_soCCCD').value,
        ngaySinh: document.getElementById('add_ngaySinh').value,
        SDT: document.getElementById('add_SDT').value,
        diaChi: document.getElementById('add_diaChi').value,
        gioiTinh: document.getElementById('add_gioiTinh').value
    };

    const res = await fetch('http://localhost:9999/api/admin/khach-hang', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(customerData)
    });
    
    const result = await res.json();
    if (result.status === 'success') {
        alert('Thêm thành công!');
        closeModal();
        loadCustomers();
    }
}

function addCustomer() {
    currentMode = 'view';
    document.getElementById('addCustomerForm').reset();
    document.getElementById('addCustomerModal').style.display = 'flex';
}

// Xử lý khi nhấn nút "Sửa thông tin"
function editCustomer() {
    currentMode = 'edit';
    alert("Chế độ Sửa: Vui lòng nhấn vào Mã khách hàng ✏️ để chỉnh sửa.");
    renderTable(allCustomers);
}

// Hàm này được gọi từ handleIdClick khi currentMode === 'edit'
function openEditModal(customer) {
    document.getElementById('edit_maTK').value = customer.maTK;
    document.getElementById('edit_hoTen').value = customer.hoTen || '';
    document.getElementById('edit_emailDK').value = customer.emailDK || '';
    document.getElementById('edit_soCCCD').value = customer.soCCCD || '';
    document.getElementById('edit_SDT').value = customer.SDT || '';
    document.getElementById('edit_diaChi').value = customer.diaChi || '';
    document.getElementById('edit_gioiTinh').value = customer.gioiTinh || 'Nam';
    document.getElementById('edit_ngaySinh').value = customer.ngaySinh ? customer.ngaySinh.split(' ')[0] : '';
    
    document.getElementById('editCustomerModal').style.display = 'flex';
}

async function executeUpdateCustomer() {
    const maTK = document.getElementById('edit_maTK').value; // Lấy ID từ hidden field
    const updateData = {
        hoTen: document.getElementById('edit_hoTen').value,
        emailDK: document.getElementById('edit_emailDK').value,
        soCCCD: document.getElementById('edit_soCCCD').value,
        SDT: document.getElementById('edit_SDT').value,
        diaChi: document.getElementById('edit_diaChi').value,
        gioiTinh: document.getElementById('edit_gioiTinh').value,
        ngaySinh: document.getElementById('edit_ngaySinh').value
    };

    // Gửi request PUT đến đúng maTK
    const res = await fetch(`http://localhost:9999/api/admin/khach-hang/${maTK}`, {
        method: 'PUT', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateData)
    });

    const result = await res.json();
    if (result.status === 'success') {
        alert('Cập nhật thông tin thành công!');
        closeModal();
        loadCustomers(); // Tải lại bảng dữ liệu
    } else {
        alert('Lỗi: ' + result.message);
    }
}

// Xử lý khi nhấn nút "Xóa khách hàng"
function deleteCustomer() {
    currentMode = 'delete';
    alert("Chế độ Xóa: Vui lòng nhấn vào Mã khách hàng ❌ để xóa.");
    renderTable(allCustomers);
}

// Xử lý khi nhấn vào Mã Khách Hàng trên bảng
function handleIdClick(maTK) {
    const customer = allCustomers.find(c => c.maTK === maTK);
    if (!customer) return;

    if (currentMode === 'edit') {
        openEditModal(customer); // Gọi hàm mở modal sửa riêng
    } else if (currentMode === 'delete') {
        const delModal = document.getElementById('deleteConfirmModal');
        delModal.style.display = 'flex';
        document.getElementById('confirmDeleteBtn').onclick = () => executeDelete(maTK);
    } else {
        // Chế độ View
        document.getElementById('viewMaKH').innerText = customer.maKH;
        document.getElementById('viewTenTK').innerText = customer.tenTK;
        document.getElementById('viewMatKhau').innerText = customer.matKhau;
        document.getElementById('viewAccountModal').style.display = 'flex';
    }
}

async function executeDelete(maTK) {
    try {
        const res = await fetch(`http://localhost:9999/api/admin/khach-hang/${maTK}`, { 
            method: 'DELETE' 
        });
        
        const result = await res.json();
        if (result.status === 'success') {
            alert('Đã xóa khách hàng thành công.');
            closeModal();
            loadCustomers(); // Làm mới danh sách
        } else {
            alert('Không thể xóa: ' + result.message);
        }
    } catch (error) {
        console.error("Lỗi khi xóa:", error);
    }
}

// Hàm mở Modal Thêm/Sửa
function openModal(mode, data = null) {
    const modal = document.getElementById('customerModal');
    modal.style.display = 'flex';
    const tenTKInput = document.getElementById('tenTK');

    if (mode === 'edit' && data) {
        document.getElementById('modalTitle').innerText = "Cập nhật khách hàng";
        document.getElementById('formMaTK').value = data.maTK;
        document.getElementById('hoTen').value = data.hoTen || '';
        document.getElementById('emailDK').value = data.emailDK || '';
        document.getElementById('SDT').value = data.SDT || '';
        document.getElementById('diaChi').value = data.diaChi || '';
        document.getElementById('gioiTinh').value = data.gioiTinh || 'Nam';
        document.getElementById('ngaySinh').value = data.ngaySinh ? data.ngaySinh.split(' ')[0] : '';
        tenTKInput.disabled = true; // Không cho sửa tên tài khoản
    } else {
        document.getElementById('modalTitle').innerText = "Thêm khách hàng mới";
        document.getElementById('customerForm').reset();
        document.getElementById('formMaTK').value = '';
        tenTKInput.disabled = false;
    }
}

// Hàm đóng tất cả các modal
function closeModal() {
    const modals = document.querySelectorAll('.modal, .modal-overlay');
    modals.forEach(m => m.style.display = 'none');
    
    // Reset lại chế độ xem bảng (xóa icon X hoặc cờ lê)
    currentMode = 'view';
    renderTable(allCustomers);
}

// Logic Tìm kiếm (Filter)
function findCustomer() {
    document.getElementById('searchModal').style.display = 'flex';
}

function executeSearch() {
    const fTen = document.getElementById('searchHoTen').value.toLowerCase();
    const fSDT = document.getElementById('searchSDT').value;
    const fEmail = document.getElementById('searchEmail').value.toLowerCase();
    const fDiaChi = document.getElementById('searchDiaChi').value.toLowerCase();
    const fGT = document.getElementById('searchGioiTinh').value;

    const filtered = allCustomers.filter(c => {
        return (!fTen || (c.hoTen || '').toLowerCase().includes(fTen)) &&
               (!fSDT || (c.SDT || '').includes(fSDT)) &&
               (!fEmail || (c.emailDK || '').toLowerCase().includes(fEmail)) &&
               (!fDiaChi || (c.diaChi || '').toLowerCase().includes(fDiaChi)) &&
               (!fGT || c.gioiTinh === fGT);
    });
    renderTable(filtered);
    closeAllModals();
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
    currentMode = 'view';
}