document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'bacsi00';
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) displayElement.innerText = savedName;

    const API_URL = "http://localhost:9999/api";

    // --- 1. Hàm tải danh sách liên hệ ---
    const loadContactList = async () => {
		const container = document.getElementById('contactList');
		const savedName = sessionStorage.getItem('username') || 'bacsi00';
		const API_URL = "http://localhost:9999/api";

		try {
			// Gửi yêu cầu đến API
			const res = await fetch(`${API_URL}/danh-sach-yeu-cau/${savedName}`);
			
			// Kiểm tra nếu phản hồi HTTP không thành công (vd: 404 hoặc 500)
			if (!res.ok) {
				throw new Error("Lỗi phản hồi từ server");
			}

			const data = await res.json();
			
			// Trường hợp không có dữ liệu
			if (!data || data.length === 0) {
				container.innerHTML = '<p style="color: #666; font-style: italic; text-align: center;">Chưa có tin nhắn nào.</p>';
				return;
			}

			// Render danh sách yêu cầu hỗ trợ
			container.innerHTML = data.map(item => `
				<div class="contact-item">
					<strong onclick="viewDetail('${item.maYC}')" style="color:#007bff; cursor:pointer; text-decoration:underline;">
						${item.tieuDe}
					</strong>
					<small>Mã: ${item.maYC} | Gửi: ${item.ngayYC}</small><br>
					<small>Ngày trả lời: <b style="color:${item.ngayTL === 'Chưa trả lời' ? 'red' : 'green'}">${item.ngayTL}</b></small>
				</div>
			`).join('');

		} catch (error) {
			// Xử lý khi không kết nối được CSDL hoặc sai API
			console.error("Lỗi tải danh sách yêu cầu hỗ trợ:", error);
			container.innerHTML = `
				<p>
					Lỗi kết nối server hoặc sai API.
				</p>
			`;
		}
	};

    // --- 2. Hàm xem chi tiết ---
    window.viewDetail = async (maYC) => {
        const res = await fetch(`${API_URL}/chi-tiet-yeu-cau/${maYC}`);
        const d = await res.json();
        
        document.getElementById('detTieuDe').innerText = d.tieuDe;
        document.getElementById('detMa').innerText = d.maYC;
        document.getElementById('detNgayYC').innerText = d.ngayYC;
        document.getElementById('detNoiDung').innerText = d.noiDung;
        document.getElementById('detTraLoi').innerText = d.traLoi;
        document.getElementById('detailModal').style.display = 'block';
    };

    // --- 3. Xử lý gửi Form ---
    const form = document.getElementById('formLienHe');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            username: savedName,
            tieuDe: document.getElementById('tieuDe').value,
            noiDung: document.getElementById('contentLH').value
        };

        const res = await fetch(`${API_URL}/gui-yeu-cau`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        const result = await res.json();
        if (result.status === 'success') {
            alert('Gửi yêu cầu thành công!');
            form.reset();
            loadContactList(); // Cập nhật lại danh sách ngay lập tức
        } else {
            alert('Lỗi: ' + result.message);
        }
    });

    // Khởi tạo tải dữ liệu
    loadContactList();
});

// 1. Mở Modal
window.openSearchModal = () => {
    document.getElementById('searchModal').style.display = 'flex';
};

// 2. Đóng Modal
window.closeSearchModal = () => {
    document.getElementById('searchModal').style.display = 'none';
};

// 3. Thực thi tìm kiếm (Lọc trên giao diện giống XemLichKH.js)
window.executeSearch = () => {
    const filterTieuDe = document.getElementById('searchTieuDe').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    
    // Lấy trạng thái từ Checkbox
    const isChuaTLChecked = document.getElementById('chkChuaTL').checked;
    const isDaTLChecked = document.getElementById('chkDaTL').checked;

    const contactItems = document.querySelectorAll('.contact-item');

    contactItems.forEach(item => {
        let showRow = true;
        
        const txtTieuDe = item.querySelector('strong').textContent.toLowerCase();
        const txtInfo = item.querySelector('small').textContent;
        // Kiểm tra trạng thái dựa trên màu sắc hoặc chữ trong thẻ b
        const statusText = item.querySelector('b').textContent; 
        const isAlreadyReplied = (statusText !== 'Chưa trả lời');

        // 1. Lọc theo tiêu đề
        if (filterTieuDe && !txtTieuDe.includes(filterTieuDe)) showRow = false;

        // 2. Lọc theo ngày
        if (filterNgay && !txtInfo.includes(filterNgay)) showRow = false;

        // 3. Lọc theo Checkbox trạng thái
        if (isAlreadyReplied) {
            // Nếu là yêu cầu đã trả lời mà checkbox "Đã trả lời" không tích -> ẩn
            if (!isDaTLChecked) showRow = false;
        } else {
            // Nếu là yêu cầu chưa trả lời mà checkbox "Chưa trả lời" không tích -> ẩn
            if (!isChuaTLChecked) showRow = false;
        }

        item.style.display = showRow ? "block" : "none";
    });

    closeSearchModal();
};