const API_URL = "http://localhost:9999/api";

document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'khachhang00';
	
	// Rằng buộc ngày khám trước ngày đặt 2 ngày
	const ngayKhamInput = document.getElementById('editNgayKham');
	if (ngayKhamInput) {
        // Lấy ngày hiện tại dưới định dạng YYYY-MM-DD
        const today = new Date().toLocaleDateString('en-CA'); 
        
        // Gán vào thuộc tính min
        ngayKhamInput.setAttribute('min', today);
        
        // (Tùy chọn) Nếu muốn mặc định chọn luôn ngày hôm nay
        ngayKhamInput.value = today;
    }
	
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }
    document.getElementById('editChuyenKhoa').addEventListener('change', (e) => {
		filterBS_DV_ByCK(e.target.value);
	});
    loadTable(savedName);
    loadChuyenKhoas();
});

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

// 1. Tải danh sách - Đã sửa đường dẫn theo Java Handler
function loadTable(username) {
    fetch(`${API_URL}/danh-sach-sua-lich?tenTK=${username}`)
        .then(res => {
            if (!res.ok) throw new Error("Lỗi HTTP: " + res.status);
            return res.json();
        })
        .then(data => {
            const tbody = document.querySelector('#lichKHTable tbody');
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Bạn chưa có lịch hẹn nào.</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(item => `
                <tr>
                    <td><span class="clickable-id" style="color:blue; cursor:pointer; text-decoration:underline;" 
                        onclick="openEditModal('${item.maDatLich}')">${item.maDatLich}</span></td>
                    <td>${item.ngayDat}</td>
                    <td>${item.tenBacSi}</td> <td>${item.tenDV}</td>
                    <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
                </tr>
            `).join('');
        })
        .catch(err => {
            console.error("Lỗi tải bảng:", err);
            document.querySelector('#lichKHTable tbody').innerHTML = '<tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>';
        });
}

async function loadChuyenKhoas() {
    try {
        const res = await fetch(`${API_URL}/chuyen-khoa`);
        const list = await res.json();
        const ckSelect = document.getElementById('editChuyenKhoa');
        if(ckSelect) {
            ckSelect.innerHTML = '<option value="">-- Chọn chuyên khoa --</option>';
            list.forEach(ck => {
                ckSelect.innerHTML += `<option value="${ck}">${ck}</option>`;
            });
        }
    } catch (err) {
        console.error("Lỗi tải chuyên khoa:", err);
    }
}

// 4. Mở Modal
async function openEditModal(maDL) {
    try {
        const res = await fetch(`${API_URL}/chi-tiet-sua-lich/${maDL}`);
        const data = await res.json();
		
		const statusFromServer = data.tinhTrang ? data.tinhTrang.trim() : "";

        // 2. Kiểm tra điều kiện sửa (So sánh chính xác chuỗi N'ChoXN')
        if (statusFromServer !== 'ChoXN') {
            alert(`Lịch khám này đã ở trạng thái [${translateStatus(statusFromServer)}].\nBạn chỉ được phép sửa lịch đang ở trạng thái [Chờ xác nhận].`);
            return;
        }

        // 3. Nếu hợp lệ, tiến hành đổ dữ liệu vào Form
        document.getElementById('editMaDL').value = data.maDatLich;
        
        // Tách ngày và giờ từ chuỗi ngayDat (ví dụ: "2023-10-25 08:00:00.0")
        const fullDate = data.ngayDat.split(' '); 
        document.getElementById('editNgayKham').value = fullDate[0];
        if (fullDate[1]) {
            document.getElementById('editGioKham').value = fullDate[1].substring(0, 8);
        }

        document.getElementById('editChuyenKhoa').value = data.tenCK;
        document.getElementById('editTrieuChung').value = data.trieuChung || "";

        // Tải danh sách bác sĩ/dịch vụ theo chuyên khoa hiện tại
        await filterBS_DV_ByCK(data.tenCK, data.maBS, data.maDV);

        document.getElementById('editModal').style.display = 'flex';
		} catch (err) {
			console.error("Lỗi khi mở Modal:", err);
			alert("Không thể lấy thông tin lịch hẹn. Vui lòng kiểm tra kết nối Server.");
		}
}

// Các hàm bổ trợ filter và submit giữ nguyên logic cũ nhưng đảm bảo ID khớp HTML
async function filterBS_DV_ByCK(tenCK, currentBS = null, currentDV = null) {
    const bsSelect = document.getElementById('editBacSi');
    const dvSelect = document.getElementById('editDichVu');
    if (!tenCK) return;

    const [resBS, resDV] = await Promise.all([
        fetch(`${API_URL}/bac-si-theo-ck?tenCK=${tenCK}`),
        fetch(`${API_URL}/dich-vu-theo-ck?tenCK=${tenCK}`)
    ]);

    const listBS = await resBS.json();
    const listDV = await resDV.json();

    bsSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
    listBS.forEach(bs => { bsSelect.innerHTML += `<option value="${bs.maBS}">${bs.hoTen}</option>`; });

    dvSelect.innerHTML = '<option value="">-- Chọn dịch vụ --</option>';
    listDV.forEach(dv => { dvSelect.innerHTML += `<option value="${dv.maDV}">${dv.tenDV}</option>`; });

    bsSelect.disabled = false;
    dvSelect.disabled = false;
    if (currentBS) bsSelect.value = currentBS;
    if (currentDV) dvSelect.value = currentDV;
}

function closeModal() {
    document.getElementById('editModal').style.display = 'none';
}

document.getElementById('editForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const data = {
        maDatLich: document.getElementById('editMaDL').value,
        ngay: document.getElementById('editNgayKham').value,
        gio: document.getElementById('editGioKham').value,
        maBS: document.getElementById('editBacSi').value,
        maDV: document.getElementById('editDichVu').value,
        trieuChung: document.getElementById('editTrieuChung').value
    };

    try {
        const res = await fetch(`${API_URL}/cap-nhat-lich`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();

        if (result.status === 'success') {
            alert("Cập nhật thành công!");
            closeModal();
            loadTable(sessionStorage.getItem('username')); // Tải lại bảng
        } else {
            alert("Lỗi: " + result.message);
        }
    } catch (err) {
        console.error("Lỗi kết nối:", err);
        alert("Không thể kết nối đến server.");
    }
});

// --- CHỨC NĂNG TÌM KIẾM ---

// 1. Mở Modal tìm kiếm
function findLK() {
    document.getElementById('searchModal').style.display = 'flex';
}

// 2. Đóng Modal tìm kiếm
function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

// 3. Logic tìm kiếm thực tế
function executeSearch() {
    // Lấy giá trị từ các ô input
    const filterMa = document.getElementById('searchMaDL').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    const filterBS = document.getElementById('searchBacSi').value.toLowerCase().trim();
    const filterDV = document.getElementById('searchDV').value.toLowerCase().trim();

    const table = document.getElementById("lichKHTable");
    const tr = table.getElementsByTagName("tr");

    // Lặp qua tất cả các hàng trong bảng (bỏ qua hàng tiêu đề index 0)
    for (let i = 1; i < tr.length; i++) {
        let showRow = true;
        const td = tr[i].getElementsByTagName("td");
        
        if (td.length > 0) {
            const txtMa = td[0].textContent.toLowerCase();
            const txtNgay = td[1].textContent;
            const txtBS = td[2].textContent.toLowerCase();
            const txtDV = td[3].textContent.toLowerCase();

            // Kiểm tra từng điều kiện (Nếu ô nhập có dữ liệu thì mới so sánh)
            if (filterMa && !txtMa.includes(filterMa)) showRow = false;
            if (filterNgay && !txtNgay.includes(filterNgay)) showRow = false;
            if (filterBS && !txtBS.includes(filterBS)) showRow = false;
            if (filterDV && !txtDV.includes(filterDV)) showRow = false;

            // Hiển thị hoặc ẩn hàng dựa trên kết quả lọc
            tr[i].style.display = showRow ? "" : "none";
        }
    }
    
    // Đóng modal sau khi tìm
    closeSearchModal();
}