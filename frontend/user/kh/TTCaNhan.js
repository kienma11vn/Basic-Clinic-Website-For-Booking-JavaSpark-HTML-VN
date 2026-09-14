let currentData = {}; // Biến lưu trữ dữ liệu hiện tại

document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username');
    if (savedName) {
        document.getElementById('usernameDisplay').innerText = savedName;
        loadThongTin(savedName);
    }
});

// Tải dữ liệu và hiển thị lên bảng
async function loadThongTin(username) {
	const tbody = document.getElementById('infoDisplayBody');
    try {
        const response = await fetch(`http://localhost:9999/api/get-thong-tin/${username}`);
        currentData = await response.json();
        
        const tbody = document.getElementById('infoDisplayBody');
        tbody.innerHTML = `
            <tr>
                <td>${currentData.maKH || ''}</td>
                <td>${currentData.soCCCD || ''}</td>
                <td>${currentData.emailDK || ''}</td>
                <td>${currentData.hoTen || ''}</td>
                <td>${currentData.ngaySinh ? currentData.ngaySinh.split(' ')[0] : ''}</td>
                <td>${currentData.gioiTinh || ''}</td>
                <td>${currentData.SDT || ''}</td>
                <td>${currentData.diaChi || ''}</td>
            </tr>
        `;
    } catch (error) {
        console.error("Lỗi:", error);
		tbody.innerHTML = `
            <tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>
        `;
    }
}

function openModal() {
    // Đổ dữ liệu hiện tại vào các ô input trong Modal
    document.getElementById('editCCCD').value = currentData.soCCCD || '';
    document.getElementById('editHoTen').value = currentData.hoTen || '';
    document.getElementById('editNgaySinh').value = currentData.ngaySinh ? currentData.ngaySinh.split(' ')[0] : '';
    document.getElementById('editGioiTinh').value = currentData.gioiTinh || 'Nam';
    document.getElementById('editSDT').value = currentData.SDT || '';
    document.getElementById('editDiaChi').value = currentData.diaChi || '';
    
    document.getElementById('editModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('editModal').style.display = 'none';
}

async function saveChanges() {
    const username = sessionStorage.getItem('username');
    
    // Thu thập dữ liệu từ các input trong Modal
    const updateData = {
        username: username,
        soCCCD: document.getElementById('editCCCD').value,
        hoTen: document.getElementById('editHoTen').value,
        ngaySinh: document.getElementById('editNgaySinh').value, // Định dạng từ <input type="date"> là YYYY-MM-DD
        gioiTinh: document.getElementById('editGioiTinh').value,
        SDT: document.getElementById('editSDT').value,
        diaChi: document.getElementById('editDiaChi').value
    };

    console.log("Dữ liệu gửi đi:", updateData); // Kiểm tra dữ liệu tại Console (F12)

    try {
        const response = await fetch('http://localhost:9999/api/update-thong-tin', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log("Kết quả từ Server:", result);

        if (result.status === 'success') {
            alert('Cập nhật thành công!');
            closeModal();
            loadThongTin(username); // Tải lại bảng để cập nhật hiển thị mới
        } else {
            alert('Cập nhật thất bại: ' + (result.error || 'Lỗi không xác định'));
        }
    } catch (error) {
        console.error("Lỗi kết nối:", error);
        alert('Không thể kết nối đến máy chủ. Hãy đảm bảo Backend đang chạy tại port 9999.');
    }
}

// Đóng modal khi nhấn ra ngoài vùng trắng
window.onclick = function(event) {
    const modal = document.getElementById('editModal');
    if (event.target == modal) {
        closeModal();
    }
}