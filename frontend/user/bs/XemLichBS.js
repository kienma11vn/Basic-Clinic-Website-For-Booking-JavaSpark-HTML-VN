document.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy tên tài khoản bác sĩ (Thống nhất dùng 'username' hoặc 'tenTK' tùy theo logic login của bạn)
    const savedName = sessionStorage.getItem('username') || localStorage.getItem('tenTK') || 'bacsi00';

    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }

    if (savedName) {
        loadDoctorSchedule(savedName);
    }
});

function loadDoctorSchedule(username) {
    const tbody = document.querySelector('#customerTable tbody');
    fetch(`http://localhost:9999/api/bs/lich-kham?tenTK=${username}`)
        .then(response => response.json())
        .then(data => {
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Không có lịch hẹn nào.</td></tr>';
                return;
            }
            tbody.innerHTML = ''; 

            data.forEach(item => {
                let displayRating = "Chưa có";
                const rawData = item.danhGia || "";

                if (rawData) {
                    const dvMatch = rawData.match(/\[DV-(\d+)đ/);
                    const clMatch = rawData.match(/\[CL-(\d+)đ/);
                    const dvScore = dvMatch ? dvMatch[1] : "_";
                    const clScore = clMatch ? clMatch[1] : "_";
                    if (dvMatch || clMatch) {
                        displayRating = `Phục vụ: ${dvScore}<br>Khám chữa bệnh: ${clScore}`;
                    }
                }

                const row = `
                    <tr>
                        <td>${item.maDatLich}</td>
                        <td>${item.ngayDat}</td>
						<td>${item.tenBenhNhan}</td>
                        <td>${item.tenDV}</td>
                        <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
                        <td class="status-${item.khachDenKham}">${translateStatus(item.khachDenKham)}</td>
                        <td>${displayRating}</td>
						<td>${item.trieuChung}</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });
        })
        .catch(err => {
            console.error("Lỗi:", err);
            tbody.innerHTML = '<tr><td colspan="6">Lỗi kết nối server.</td></tr>';
        });
}

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

// --- CHỨC NĂNG TÌM KIẾM ---
function findCustomer() {
    document.getElementById('searchModal').style.display = 'flex';
}

function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

function executeSearch() {
    const filterMa = document.getElementById('searchMaDL').value.toLowerCase().trim();
    const filterBN = document.getElementById('searchBN').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    const filterDV = document.getElementById('searchDV').value.toLowerCase().trim();
    const filterKhachDen = document.getElementById('searchKhachDen').value;

    const tr = document.querySelectorAll("#customerTable tbody tr");

    tr.forEach(row => {
        const td = row.getElementsByTagName("td");
        if (td.length > 0) {
            const txtMa = td[0].textContent.toLowerCase();
            const txtNgay = td[1].textContent;
            const txtBN = td[2].textContent.toLowerCase();
            const txtDV = td[3].textContent.toLowerCase();
            const txtKhachDen = td[5].textContent; // Cột "Khách đến khám"

            let showRow = true;

            if (filterMa && !txtMa.includes(filterMa)) showRow = false;
            if (filterBN && !txtBN.includes(filterBN)) showRow = false;
            if (filterNgay && !txtNgay.includes(filterNgay)) showRow = false;
            if (filterDV && !txtDV.includes(filterDV)) showRow = false;
            if (filterKhachDen && txtKhachDen !== filterKhachDen) showRow = false;

            row.style.display = showRow ? "" : "none";
        }
    });
    closeSearchModal();
}