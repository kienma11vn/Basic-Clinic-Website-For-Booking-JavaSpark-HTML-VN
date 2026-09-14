const API_URL = "http://localhost:9999/api";
let selectedMaDL = "";

document.addEventListener('DOMContentLoaded', () => {
    const username = sessionStorage.getItem('username') || 'khachhang00';
    document.getElementById('usernameDisplay').innerText = username;
    loadHuyTable(username);
});

function loadHuyTable(username) {
    const tbody = document.querySelector('#huyLichTable tbody');
    if (!tbody) return;

    fetch(`${API_URL}/lich-co-the-huy?tenTK=${username}`)
        .then(res => {
            if (!res.ok) throw new Error("Sai API hoặc Server không phản hồi");
            return res.json();
        })
        .then(data => {
            tbody.innerHTML = ""; 
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Bạn chưa có lịch hẹn nào có thể hủy.</td></tr>';
                return;
            }
            data.forEach(item => {
                const row = `
                    <tr>
                        <td><a href="#" onclick="openConfirm('${item.maDatLich}', '${item.tinhTrang}')" class="clickable-id">${item.maDatLich}</a></td>
                        <td>${item.ngayDat}</td>
                        <td>${item.tenBacSi}</td>
                        <td>${item.tenDV}</td>
                        <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });
        })
        .catch(err => {
            console.error("Lỗi tải bảng:", err);
            tbody.innerHTML = '<tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>';
        });
}

function translateStatus(status) {
    const statuses = {
        'ChoXN': 'Chờ xác nhận',
        'DaXN': 'Đã xác nhận',
        'Huy': 'Chưa xác nhận hủy',
        'XNHuy': 'Đã xác nhận hủy',
        'KHuy': 'Không xác nhận hủy'
    };
    return statuses[status] || status;
}

// ĐÃ SỬA: Thêm tham số tinhTrang
function openConfirm(maDL, tinhTrang) {
    if (tinhTrang === 'XNHuy' || tinhTrang === 'KHuy') {
        alert("Lịch đã xác nhận, không thể thay đổi!");
        return;
    }
    selectedMaDL = maDL;
    document.getElementById('displayMaDL').innerText = maDL;
    document.getElementById('confirmModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('confirmModal').style.display = 'none';
}

document.getElementById('btnConfirmHuy').addEventListener('click', () => {
    fetch(`${API_URL}/huy-lich-kham`, {
        method: 'POST',
        headers: { 
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({ maDatLich: selectedMaDL })
    })
    .then(res => {
        if (!res.ok) return res.text().then(text => { throw new Error(text) });
        return res.json();
    })
    .then(data => {
        if (data.status === 'success') {
            alert("Gửi yêu cầu hủy lịch thành công!");
            closeModal();
            loadHuyTable(sessionStorage.getItem('username') || 'khachhang00');
        } else {
            alert("Lỗi: " + data.message);
        }
    })
    .catch(err => {
        alert("Không thể thực hiện. Lỗi: " + err.message);
    });
});

function findLK() {
    document.getElementById('searchModal').style.display = 'flex';
}

function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

function executeSearch() {
    const filterMa = document.getElementById('searchMaDL').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    const filterBS = document.getElementById('searchBacSi').value.toLowerCase().trim();
    const filterDV = document.getElementById('searchDV').value.toLowerCase().trim();

    // ĐÃ SỬA: Đổi ID bảng thành huyLichTable cho đúng HTML
    const table = document.getElementById("huyLichTable"); 
    const tr = table.getElementsByTagName("tr");

    for (let i = 1; i < tr.length; i++) {
        let showRow = true;
        const td = tr[i].getElementsByTagName("td");
        
        if (td.length > 0) {
            const txtMa = td[0].textContent.toLowerCase();
            const txtNgay = td[1].textContent;
            const txtBS = td[2].textContent.toLowerCase();
            const txtDV = td[3].textContent.toLowerCase();

            if (filterMa && !txtMa.includes(filterMa)) showRow = false;
            if (filterNgay && !txtNgay.includes(filterNgay)) showRow = false;
            if (filterBS && !txtBS.includes(filterBS)) showRow = false;
            if (filterDV && !txtDV.includes(filterDV)) showRow = false;

            tr[i].style.display = showRow ? "" : "none";
        }
    }
    closeSearchModal();
}