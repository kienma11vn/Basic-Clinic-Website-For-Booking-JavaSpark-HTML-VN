let currentMaDL = '';
let allData = [];

document.addEventListener('DOMContentLoaded', () => {
    // Tự động kiểm tra quá hạn khi vào trang
    fetch('http://localhost:9999/api/lt/cap-nhat-qua-han').then(() => loadData());
    
    const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
});

function loadData() {
    fetch('http://localhost:9999/api/lt/danh-sach-den-kham')
        .then(res => res.json())
        .then(data => {
            allData = data;
            renderTable(data);
        });
}

function translateKDK(status) {
    if (status === 'NULL' || status === null) return 'Chưa xác nhận';
    if (status === 'Yes') return 'Đã đến khám';
    if (status === 'No') return 'Không đến khám';
    return status;
}

function renderTable(data) {
    const tbody = document.querySelector('#customerTable tbody');
    tbody.innerHTML = data.map(item => `
        <tr>
            <td class="clickable-id" onclick="openActionModal('${item.maDatLich}')">${item.maDatLich}</td>
            <td>${item.ngayDat}</td>
            <td>${item.tenBacSi}</td>
            <td>${item.tenBenhNhan}</td>
            <td>${item.tenDV}</td>
            <td class="status-${item.khachDenKham}">${translateKDK(item.khachDenKham)}</td>
        </tr>
    `).join('');
}

function openActionModal(maDL) {
    currentMaDL = maDL;
    document.getElementById('selectedMaDLText').innerText = "Mã đặt lịch: " + maDL;
    document.getElementById('actionModal').style.display = 'flex';
}

function confirmArrival() {
    const username = sessionStorage.getItem('username');
    fetch('http://localhost:9999/api/lt/xac-nhan-den-kham', {
        method: 'POST',
        body: JSON.stringify({ maDatLich: currentMaDL, username: username })
    })
    .then(res => res.json())
    .then(res => {
        if(res.status === 'success') {
            alert("Đã xác nhận khách đến khám!");
            closeModal('actionModal');
            loadData();
        } else alert(res.message);
    });
}

function findCustomer() {
    document.getElementById('searchModal').style.display = 'flex';
}

function applySearch() {
    const filters = {
        maDL: document.getElementById('sMaDL').value.toLowerCase(),
        ngay: document.getElementById('sNgay').value,
        bs: document.getElementById('sBacSi').value.toLowerCase(),
        kh: document.getElementById('sBenhNhan').value.toLowerCase(),
        dv: document.getElementById('sDichVu').value.toLowerCase(),
        tt: document.getElementById('sTinhTrang').value
    };

    const filtered = allData.filter(item => 
        item.maDatLich.toLowerCase().includes(filters.maDL) &&
        (filters.ngay === '' || item.ngayDat.includes(filters.ngay)) &&
        item.tenBacSi.toLowerCase().includes(filters.bs) &&
        item.tenBenhNhan.toLowerCase().includes(filters.kh) &&
        item.tenDV.toLowerCase().includes(filters.dv) &&
        (filters.tt === '' || item.khachDenKham === filters.tt)
    );
    renderTable(filtered);
    closeModal('searchModal');
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}