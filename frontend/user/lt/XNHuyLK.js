let currentMaDL = '';
let allData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();
    const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
});

function loadData() {
    fetch('http://localhost:9999/api/lt/danh-sach-huy-lich')
        .then(res => res.json())
        .then(data => {
            allData = data;
            renderTable(data);
        });
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
            <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
        </tr>
    `).join('');
}

function translateStatus(status) {
    const st = { 'Huy': 'Chưa xác nhận hủy', 'XNHuy': 'Đã xác nhận hủy', 'KHuy': 'Không xác nhận hủy' };
    return st[status] || status;
}

function openActionModal(maDL) {
    currentMaDL = maDL;
    document.getElementById('selectedMaDLText').innerText = "Mã đang chọn: " + maDL;
    document.getElementById('actionModal').style.display = 'flex';
}

function checkValidHuy() {
    fetch(`http://localhost:9999/api/lt/kiem-tra-hop-le-huy?maDatLich=${currentMaDL}`)
        .then(res => res.json())
        .then(res => alert(res.message));
}

function processHuy(loai) {
    const username = sessionStorage.getItem('username') || 'letan00';
    fetch('http://localhost:9999/api/lt/xac-nhan-huy-lich', {
        method: 'POST',
        body: JSON.stringify({ maDatLich: currentMaDL, loaiXN: loai, username: username })
    })
    .then(res => res.json())
    .then(res => {
        if(res.status === 'success') {
            alert("Xử lý thành công!");
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
        (filters.tt === '' || item.tinhTrang === filters.tt)
    );
    renderTable(filtered);
    closeModal('searchModal');
}

function closeModal(id) {
    document.getElementById(id).style.display = 'none';
}