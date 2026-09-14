const API_URL = "http://localhost:9999/api";

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Lấy tên tài khoản từ localStorage (đã lưu khi đăng nhập thành công)
    // Nếu không có, mặc định hiện 'khachhang01' như trong ảnh mẫu
    const savedName = sessionStorage.getItem('username') || 'khachhang00';
	
	// Rằng buộc ngày khám trước ngày đặt 2 ngày
	const ngayKhamInput = document.getElementById('ngayKham');
	if (ngayKhamInput) {
        const tomorrow = new Date();
		tomorrow.setDate(tomorrow.getDate() + 2);
		const minDate = tomorrow.toLocaleDateString('en-CA');

		ngayKhamInput.setAttribute('min', minDate);
    }

    // 2. Hiển thị lên giao diện
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }

    // 1. Load Chuyên khoa
    const resCK = await fetch(`${API_URL}/chuyen-khoa`);
    const listCK = await resCK.json();
    const ckSelect = document.getElementById('chuyenKhoa');
    listCK.forEach(ck => {
        ckSelect.innerHTML += `<option value="${ck}">${ck}</option>`;
    });

    // 2. Sự kiện thay đổi Chuyên khoa -> Lọc Bác sĩ VÀ Dịch vụ
	ckSelect.addEventListener('change', async (e) => {
		const selectedCK = e.target.value;
		const bsSelect = document.getElementById('bacSi');
		const dvSelect = document.getElementById('dichVu');

		// Reset
		bsSelect.innerHTML = '<option value="">-- Chọn bác sĩ --</option>';
		dvSelect.innerHTML = '<option value="">-- Chọn dịch vụ --</option>';

		if (selectedCK) {
			// A. Load Bác sĩ
			const resBS = await fetch(`${API_URL}/bac-si-theo-ck?tenCK=${selectedCK}`);
			const listBS = await resBS.json();
			listBS.forEach(bs => {
				bsSelect.innerHTML += `<option value="${bs.maBS}">${bs.hoTen}</option>`;
			});

			// B. Load Dịch vụ
			const resDV = await fetch(`${API_URL}/dich-vu-theo-ck?tenCK=${selectedCK}`);
			const listDV = await resDV.json();
			listDV.forEach(dv => {
				dvSelect.innerHTML += `<option value="${dv.maDV}">${dv.tenDV}</option>`;
			});

			bsSelect.disabled = false;
			dvSelect.disabled = false;
		} else {
			bsSelect.disabled = true;
			dvSelect.disabled = true;
		}
	});

    // 3. Submit Form
	document.getElementById('formDangKy').addEventListener('submit', async (e) => {
		e.preventDefault();
		const tenTK = sessionStorage.getItem('username') || 'khachhang00';
		
		const data = {
			tenTK: tenTK,
			ngay: document.getElementById('ngayKham').value,
			gio: document.getElementById('gioKham').value,
			maBS: document.getElementById('bacSi').value,
			maDV: document.getElementById('dichVu').value,
			trieuChung: document.getElementById('trieuChung').value
		};

		try {
			const response = await fetch(`${API_URL}/dat-lich`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(data)
			});

			const result = await response.json();
			if (response.ok && result.status === 'success') {
				alert("Đặt lịch thành công!");
				window.location.href = "DatLichKham.html";
			} else {
				alert("Lỗi: " + result.message);
			}
		} catch (error) {
			alert("Lỗi kết nối: " + error.message);
		}
	});
});