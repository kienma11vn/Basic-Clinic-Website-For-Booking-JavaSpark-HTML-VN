let allServices = [];
let currentMode = 'view';

document.addEventListener('DOMContentLoaded', () => {
    loadServices();
    const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
});

async function loadServices(query = '') {
    const res = await fetch(`http://localhost:9999/api/admin/dich-vu${query}`);
    allServices = await res.json();
    renderTable(allServices);
}

function renderTable(data) {
    const tbody = document.querySelector('#customerTable tbody');
    tbody.innerHTML = data.map(item => {
        let iconHtml = '';
        let idContent = '';

        // Kiểm tra chế độ hiện tại để quyết định định dạng mã dịch vụ
        if (currentMode === 'edit') {
            iconHtml = '<span class="action-icon edit-icon">✏️</span>';
            idContent = `<a href="javascript:void(0)" class="clickable-id" onclick="handleIdClick('${item.maDV}')">${item.maDV}</a>`;
        } else if (currentMode === 'delete') {
            iconHtml = '<span class="action-icon delete-icon">❌</span>';
            idContent = `<a href="javascript:void(0)" class="clickable-id" onclick="handleIdClick('${item.maDV}')">${item.maDV}</a>`;
        } else {
            // Chế độ bình thường: không có icon và không có link click
            iconHtml = ''; 
            idContent = `<span>${item.maDV}</span>`;
        }

        return `
        <tr>
            <td>
                <div class="id-container">
                    ${iconHtml}
                    ${idContent}
                </div>
            </td>
            <td>${item.tenDV}</td>
            <td>${item.moTaDV || ''}</td>
            <td>${item.giaDV ? item.giaDV.toLocaleString() : 0} VNĐ</td>
            <td>${item.tenCK}</td>
        </tr>`;
    }).join('');
}

let selectedMaDV = '';

function handleIdClick(maDV) {
    const service = allServices.find(s => s.maDV === maDV);
    
    if (currentMode === 'edit') {
        openModal('edit', service);
    } 
    else if (currentMode === 'delete') {
        // Lưu thông tin vào biến tạm
        selectedMaDV = maDV;
        
        // Hiển thị thông tin lên Modal xóa
        document.getElementById('delMaDV').innerText = service.maDV;
        document.getElementById('delTenDV').innerText = service.tenDV;
        
        // Mở Modal xóa
        document.getElementById('deleteModal').style.display = 'flex';
    }
}

async function confirmDelete() {
    try {
        const res = await fetch(`http://localhost:9999/api/admin/dich-vu/${selectedMaDV}`, {
            method: 'DELETE'
        });
        
        const result = await res.json();
        if (result.status === 'success') {
            alert("Xóa dịch vụ thành công!");
        } else {
            alert("Lỗi: " + (result.message || "Không thể xóa dịch vụ này."));
        }
    } catch (error) {
        console.error("Lỗi khi xóa:", error);
        alert("Có lỗi xảy ra khi kết nối đến server.");
    } finally {
        selectedMaDV = ''; // Reset biến tạm
        closeModal();      // Đóng modal và render lại bảng
        loadServices();    // Tải lại dữ liệu mới
    }
}

async function executeDelete(maDV) {
    await fetch(`http://localhost:9999/api/admin/dich-vu/${maDV}`, { method: 'DELETE' });
    loadServices();
}

function openModal(mode, data = null) {
    document.getElementById('serviceModal').style.display = 'flex';
    if (mode === 'edit' && data) {
        document.getElementById('modalTitle').innerText = "Cập nhật dịch vụ";
        document.getElementById('formMaDV').value = data.maDV;
        document.getElementById('tenDV').value = data.tenDV;
        document.getElementById('moTaDV').value = data.moTaDV;
        document.getElementById('giaDV').value = data.giaDV;
        document.getElementById('tenCK').value = data.tenCK;
    } else {
        document.getElementById('modalTitle').innerText = "Thêm dịch vụ mới";
        document.getElementById('serviceForm').reset();
        document.getElementById('formMaDV').value = '';
    }
}

async function saveService(event) {
    event.preventDefault();
    const maDV = document.getElementById('formMaDV').value;
    const data = {
        tenDV: document.getElementById('tenDV').value,
        moTaDV: document.getElementById('moTaDV').value,
        giaDV: document.getElementById('giaDV').value,
        tenCK: document.getElementById('tenCK').value
    };

    const method = maDV ? 'PUT' : 'POST';
    const url = maDV ? `http://localhost:9999/api/admin/dich-vu/${maDV}` : `http://localhost:9999/api/admin/dich-vu`;

    await fetch(url, {
        method: method,
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    closeModal();
    loadServices();
}

function findDV() {
    currentMode = 'view';
    document.getElementById('searchModal').style.display = 'flex';
}

// Thực hiện tìm kiếm khi nhấn nút trong Modal
async function executeSearch() {
    const ma = document.getElementById('searchMaDV').value;
    const ten = document.getElementById('searchTenDV').value;
    const ck = document.getElementById('searchChuyenKhoa').value;

    let query = `?maDV=${encodeURIComponent(ma)}&tenDV=${encodeURIComponent(ten)}&tenCK=${encodeURIComponent(ck)}`;
    
    await loadServices(query);
    closeModal();
}

function addDV() { 
    currentMode = 'view'; // Thêm mới không cần click vào bảng
    openModal('add'); 
}

function editDV() { 
    currentMode = 'edit'; 
	alert("Chế độ Sửa: Vui lòng nhấn vào Mã dịch vụ ✏️ để chỉnh sửa.");
    renderTable(allServices); 
}

function deleteDV() { 
    currentMode = 'delete'; 
	alert("Chế độ Xóa: Vui lòng nhấn vào Mã dịch vụ ❌ để xóa.");
    renderTable(allServices); 
}

// Hàm đóng modal cũng nên reset lại trạng thái bảng về bình thường
function closeModal() {
    document.getElementById('serviceModal').style.display = 'none';
    document.getElementById('searchModal').style.display = 'none';
    document.getElementById('deleteModal').style.display = 'none'; // Thêm dòng này
    
    currentMode = 'view';
    renderTable(allServices);
}