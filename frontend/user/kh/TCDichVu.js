document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'khachhang00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) displayElement.innerText = savedName;

    loadChuyenKhoas(); 
    loadServices();    
});

async function loadChuyenKhoas() {
    try {
        const response = await fetch('http://localhost:9999/api/dv-chuyen-khoa-list');
        const data = await response.json();
        const select = document.getElementById('findCKDV');
        
        // Reset danh sách (giữ lại option mặc định đầu tiên)
        select.innerHTML = '<option value="">-- Tất cả chuyên khoa --</option>';
        
        data.forEach(ck => {
            const opt = document.createElement('option');
            opt.value = ck.maCK; // Giá trị này bây giờ là tên chuyên khoa (ví dụ: "Nha khoa")
            opt.textContent = ck.tenCK;
            select.appendChild(opt);
        });
    } catch (err) { 
        console.error("Lỗi load chuyên khoa:", err); 
    }
}

function openSearchModal() {
    document.getElementById('searchModal').style.display = 'flex';
}

function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

function executeSearchDV() {
    loadServices();
    closeSearchModal();
}

async function loadServices() {
    // Lấy giá trị từ các input
    const tenDV = document.getElementById('findTenDV').value.trim();
    const giaMax = document.getElementById('findGiaDV').value;
    const maCK = document.getElementById('findCKDV').value;

    // Xây dựng URL động
    let url = new URL('http://localhost:9999/api/dich-vu');
    if (tenDV) url.searchParams.append('tenDV', tenDV);
    if (giaMax) url.searchParams.append('giaMax', giaMax);
    if (maCK) url.searchParams.append('maCK', maCK);
	const tbody = document.querySelector('#customerTable tbody');
    try {
        const response = await fetch(url);
        const data = await response.json();
        tbody.innerHTML = "";

        if (data.length === 0) {
            tbody.innerHTML = "<tr><td colspan='5' style='text-align:center;'>Không tìm thấy dịch vụ nào</td></tr>";
            return;
        }

        data.forEach(dv => {
            const row = `<tr>
                <td>${dv.maDV}</td>
                <td>${dv.tenDV}</td>
                <td>${dv.moTaDV || ''}</td>
                <td>${dv.giaDV ? dv.giaDV.toLocaleString('vi-VN') : '0'} VNĐ</td>
                <td>${dv.tenCK || 'Chưa phân loại'}</td>
            </tr>`;
            tbody.innerHTML += row;
        });
    } catch (err) {
        console.error("Lỗi tải dịch vụ:", err);
        tbody.innerHTML = '<tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>';
    }
}