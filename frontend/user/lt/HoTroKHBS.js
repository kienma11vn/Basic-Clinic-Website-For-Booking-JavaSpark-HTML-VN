document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'letan00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) displayElement.innerText = savedName;

    const API_URL = "http://localhost:9999/api";

    // Khởi tạo tải dữ liệu
    loadContactList();
});

const loadContactList = async () => {
    const container = document.getElementById('contactList');
    try {
        const res = await fetch(`http://localhost:9999/api/admin/danh-sach-yeu-cau`);
        const data = await res.json();

        container.innerHTML = data.map(item => {
            const raw = item.noiDungYC || "";
            const tieuDe = raw.includes("[TITLE]:") ? raw.substring(raw.indexOf("[TITLE]:") + 8, raw.indexOf("[CONTENT]:")) : "Yêu cầu hỗ trợ";
            const noiDung = raw.includes("[CONTENT]:") ? raw.substring(raw.indexOf("[CONTENT]:") + 10) : raw;

            // Phân biệt màu sắc bên trái dựa trên loại tài khoản
            const roleClass = item.loaiTK === 'BS' ? 'border-doctor' : 'border-patient';
            const roleName = item.loaiTK === 'BS' ? 'Bác sĩ' : 'Khách hàng';

            // Trạng thái trả lời
            const isReplied = item.noiDungTL && item.noiDungTL.trim() !== "";
            const statusText = isReplied ? "Đã trả lời" : "Chưa trả lời";
            const statusColor = isReplied ? "status-done" : "status-pending";

            return `
                <div class="contact-item ${roleClass}" onclick="showDetail('${item.maYC}', '${tieuDe.replace(/'/g, "\\'")}', '${noiDung.replace(/'/g, "\\'")}', '${item.tenTK}', '${item.ngayYC}', '${(item.noiDungTL || "").replace(/'/g, "\\'")}')">
                    <div class="info">
                        <strong style="color:#007bff; cursor:pointer; text-decoration:underline;">${tieuDe}</strong>
                        <p><small>Gửi bởi: ${item.tenTK} (<span style="font-weight: bold;">${roleName}</span>) | ${item.ngayYC}</small></p>
                        <b class="${statusColor}">${statusText}</b>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        container.innerHTML = "Lỗi kết nối dữ liệu.";
    }
};

function showDetail(ma, tieuDe, noiDung, nguoiGui, ngay, traLoi) {
    document.getElementById('detMa').innerText = ma;
    document.getElementById('detTieuDe').innerText = tieuDe;
    document.getElementById('detNguoiGui').innerText = nguoiGui;
    document.getElementById('detNgayYC').innerText = ngay;
    document.getElementById('detNoiDung').innerText = noiDung;
    document.getElementById('detTraLoi').innerText = traLoi || "Chưa có phản hồi.";
    document.getElementById('replyInput').value = "";
    document.getElementById('detailModal').style.display = 'flex';
}

const submitReply = async () => {
    const maYC = document.getElementById('detMa').innerText;
    const noiDungTL = document.getElementById('replyInput').value;

    if (!noiDungTL.trim()) return alert("Vui lòng nhập nội dung phản hồi!");

    const res = await fetch('http://localhost:9999/api/tra-loi-yeu-cau', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ maYC, noiDungTL })
    });

    const result = await res.json();
    if (result.status === 'success') {
        alert("Phản hồi đã được gửi!");
        closeDetail();
        loadContactList();
    }
};

function closeDetail() {
    document.getElementById('detailModal').style.display = 'none';
}

// --- Hàm mở Modal tìm kiếm ---
function findLH() {
    document.getElementById('searchModal').style.display = 'flex';
}

// --- Hàm đóng Modal tìm kiếm ---
function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

// --- Logic tìm kiếm chính ---
function executeSearch() {
    const filterTieuDe = document.getElementById('searchTieuDe').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    
    // Lấy trạng thái từ Checkbox
    const isChuaTLChecked = document.getElementById('chkChuaTL').checked;
    const isDaTLChecked = document.getElementById('chkDaTL').checked;

    const contactItems = document.querySelectorAll('.contact-item');

    contactItems.forEach(item => {
        let showRow = true;
        
        // Lấy dữ liệu từ item để so sánh
        const txtTieuDe = item.querySelector('strong').textContent.toLowerCase();
        const txtInfo = item.querySelector('small').textContent; // Chứa ngày gửi
        const statusText = item.querySelector('b').textContent; // "Đã trả lời" hoặc "Chưa trả lời"
        const isAlreadyReplied = (statusText === 'Đã trả lời');

        // 1. Lọc theo tiêu đề
        if (filterTieuDe && !txtTieuDe.includes(filterTieuDe)) showRow = false;

        // 2. Lọc theo ngày (so sánh chuỗi ngày YYYY-MM-DD)
        if (filterNgay && !txtInfo.includes(filterNgay)) showRow = false;

        // 3. Lọc theo Checkbox trạng thái
        if (!isChuaTLChecked && !isAlreadyReplied) showRow = false; // Tắt "Chưa TL" mà item chưa TL -> ẩn
        if (!isDaTLChecked && isAlreadyReplied) showRow = false;    // Tắt "Đã TL" mà item đã TL -> ẩn

        // Hiển thị hoặc ẩn
        item.style.display = showRow ? 'block' : 'none';
    });

    closeSearchModal();
}