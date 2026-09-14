document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'bacsi00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }
    // Tải danh sách mặc định
    fetchBenhNhan();
});

async function fetchBenhNhan(filters = {}) {
    const tenTK = sessionStorage.getItem('username') || 'bacsi00';
    let url = `http://localhost:9999/api/bs/danh-sach-benh-nhan?tenTK=${tenTK}`;
    
    if (filters.hoTen) url += `&hoTen=${encodeURIComponent(filters.hoTen)}`;
    if (filters.ngaySinh) url += `&ngaySinh=${filters.ngaySinh}`;
    if (filters.gioiTinh) url += `&gioiTinh=${filters.gioiTinh}`;

    try {
        const response = await fetch(url);
        const data = await response.json();
        renderTable(data);
    } catch (error) {
        console.error("Lỗi tải dữ liệu:", error);
    }
}

function renderTable(data) {
    const tbody = document.querySelector('#customerTable tbody');
    tbody.innerHTML = '';

    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Không tìm thấy bệnh nhân nào.</td></tr>';
        return;
    }

    data.forEach(bn => {
        const row = `
            <tr>
                <td>${bn.hoTen}</td>
                <td>${new Date(bn.ngaySinh).toLocaleDateString('vi-VN')}</td>
                <td>${bn.gioiTinh}</td>
                <td>${bn.sdt}</td>
                <td>${bn.email}</td>
            </tr>
        `;
        tbody.innerHTML += row;
    });
}

// Hàm mở Modal tìm kiếm
function findCustomer() {
    document.getElementById('searchModal').style.display = 'flex';
}

function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

// Thực hiện tìm kiếm từ Modal
function executeSearch() {
    const filters = {
        hoTen: document.getElementById('searchBN').value,
        ngaySinh: document.getElementById('searchNgay').value,
        gioiTinh: document.getElementById('searchGioiTinh').value
    };
    fetchBenhNhan(filters);
    closeSearchModal();
}