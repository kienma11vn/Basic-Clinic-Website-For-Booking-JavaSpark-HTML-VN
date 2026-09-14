let currentMaDL = '';
let allData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadData();
    const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;
});

function translateStatus(status) {
    const statuses = {
        'ChoXN': 'Chờ xác nhận',
        'DaXN': 'Đã xác nhận',
        'DaKham': 'Đã khám',
        'Huy': 'Chưa xác nhận hủy',
		'XNHuy': 'Đã xác nhận hủy',
		'KHuy': 'Không xác nhận hủy',
		'Yes': 'Đã đến khám',
		'No': 'Không đến khám'
    };
    return statuses[status] || status;
}

function loadData() {
    fetch('http://localhost:9999/api/lt/danh-sach-xac-nhan')
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

function openActionModal(maDL) {
    currentMaDL = maDL;
    document.getElementById('selectedMaDLText').innerText = "Đang chọn mã: " + maDL;
    document.getElementById('actionModal').style.display = 'flex';
}

function checkAvailability() {
    fetch(`http://localhost:9999/api/lt/kiem-tra-kha-dung/${currentMaDL}`)
        .then(res => res.json())
        .then(res => alert(res.status === 'available' ? "Lịch này khả dụng!" : res.message));
}

function confirmAppointment() {
    const username = sessionStorage.getItem('username');
    fetch('http://localhost:9999/api/lt/xac-nhan-dat-lich', {
        method: 'POST',
        body: JSON.stringify({ maDatLich: currentMaDL, username: username })
    })
    .then(res => res.json())
    .then(res => {
        if(res.status === 'success') {
            alert("Xác nhận thành công!");
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