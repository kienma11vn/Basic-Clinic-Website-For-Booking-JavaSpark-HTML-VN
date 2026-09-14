document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'letan00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }

    loadAllBookings();
});

function loadAllBookings() {
    const tbody = document.querySelector('#customerTable tbody');
    fetch('http://localhost:9999/api/lt/danh-sach-lich-kham')
        .then(response => response.json())
        .then(data => {
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Không có lịch hẹn nào.</td></tr>';
                return;
            }
            tbody.innerHTML = ''; 

            data.forEach(item => {
				let danhGiaHtml = "Chưa có";
				
				// Nếu có dữ liệu đánh giá
				if (item.danhGia && item.danhGia.trim() !== "" && item.danhGia !== "Chưa có") {
					// Tạo liên kết có thể nhấn vào, truyền toàn bộ chuỗi đánh giá vào hàm viewDetail
					danhGiaHtml = `<span class="clickable-id" onclick="viewRatingDetail('${item.danhGia.replace(/'/g, "\\'")}')">Xem</span>`;
				}

				const row = `
					<tr>
						<td>${item.maDatLich}</td>
						<td>${item.ngayDat}</td>
						<td>${item.tenBacSi}</td>
						<td>${item.tenBenhNhan}</td>
						<td>${item.tenDV}</td>
						<td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
						<td class="status-${item.khachDenKham}">${translateStatus(item.khachDenKham)}</td>
						<td>${danhGiaHtml}</td>
					</tr>
				`;
				tbody.innerHTML += row;
			});
        })
        .catch(err => {
            console.error("Lỗi tải dữ liệu:", err);
            tbody.innerHTML = '<tr><td colspan="8">Lỗi kết nối server.</td></tr>';
        });
}

function translateStatus(status) {
    const statuses = {
        'ChoXN': 'Chờ xác nhận',
        'DaXN': 'Đã xác nhận',
        'DaKham': 'Đã đến khám',
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
    const fMa = document.getElementById('searchMaDL').value.toLowerCase().trim();
    const fNgay = document.getElementById('searchNgay').value;
    const fBN = document.getElementById('searchBenhNhan').value.toLowerCase().trim();
    const fBS = document.getElementById('searchBacSi').value.toLowerCase().trim();

    const tr = document.querySelectorAll("#customerTable tbody tr");

    tr.forEach(row => {
        const td = row.getElementsByTagName("td");
        if (td.length > 0) {
            const txtMa = td[0].textContent.toLowerCase();
            const txtNgay = td[1].textContent;
            const txtBS = td[2].textContent.toLowerCase();
            const txtBN = td[3].textContent.toLowerCase();

            let matches = true;
            if (fMa && !txtMa.includes(fMa)) matches = false;
            if (fNgay && !txtNgay.includes(fNgay)) matches = false;
            if (fBN && !txtBN.includes(fBN)) matches = false;
            if (fBS && !txtBS.includes(fBS)) matches = false;

            row.style.display = matches ? "" : "none";
        }
    });
    closeSearchModal();
}

// Hàm hiển thị chi tiết đánh giá
function viewRatingDetail(fullText) {
    const modal = document.getElementById('ratingDetailModal');
    const content = document.getElementById('ratingContent');
    
    // 1. Regex để lấy điểm và nội dung Phục vụ (DV)
    // Cấu trúc: [DV-{điểm}đ: {nội dung}]
    const dvRegex = /\[DV-(\d+)đ:\s*(.*?)\]/;
    const dvMatch = fullText.match(dvRegex);
    
    // 2. Regex để lấy điểm và nội dung Khám chữa bệnh (CL)
    // Cấu trúc: [CL-{điểm}đ: {nội dung}]
    const clRegex = /\[CL-(\d+)đ:\s*(.*?)\]/;
    const clMatch = fullText.match(clRegex);

    // Trích xuất dữ liệu hoặc để mặc định nếu không khớp
    const diemDV = dvMatch ? dvMatch[1] : "0";
    const gopYDV = (dvMatch && dvMatch[2]) ? dvMatch[2].trim() : "Không có nội dung góp ý.";
    
    const diemCL = clMatch ? clMatch[1] : "0";
    const gopYCL = (clMatch && clMatch[2]) ? clMatch[2].trim() : "Không có nội dung góp ý.";

    content.innerHTML = `
        <div style="line-height: 1.6;">
            <div style="background: #f9f9f9; padding: 12px; border-radius: 8px; border-left: 4px solid #be5c00; margin-bottom: 15px;">
                <p><strong>⭐ Phục vụ:</strong> <span style="font-size: 1.1em; color: #be5c00;">${diemDV}/10</span></p>
                <p style="margin-top: 5px; color: #333;"><strong>Góp ý:</strong> ${gopYDV}</p>
            </div>
            
            <div style="background: #f9f9f9; padding: 12px; border-radius: 8px; border-left: 4px solid #149c3d;">
                <p><strong>⭐ Khám chữa bệnh:</strong> <span style="font-size: 1.1em; color: #149c3d;">${diemCL}/10</span></p>
                <p style="margin-top: 5px; color: #333;"><strong>Góp ý:</strong> ${gopYCL}</p>
            </div>
        </div>
    `;
    modal.style.display = 'flex';
}

function closeRatingModal() {
    document.getElementById('ratingDetailModal').style.display = 'none';
}