document.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy tên tài khoản từ sessionStorage
    const savedName = sessionStorage.getItem('username') || 'khachhang00';

    // 2. Hiển thị lên giao diện và GỌI HÀM TẢI DỮ LIỆU
    const displayElement = document.getElementById('usernameDisplay');
    if (savedName && savedName !== 'khachhang00') {
        if (displayElement) {
            displayElement.innerText = savedName;
        }
        // GỌI HÀM NÀY ĐỂ TRUY XUẤT DỮ LIỆU
        fetch('http://localhost:9999/api/lt/cap-nhat-qua-han', { method: 'GET' })
        .then(response => {
            console.log("Đã cập nhật trạng thái quá hạn");
            // 2. Chỉ khi cập nhật xong mới load dữ liệu lên giao diện
            if (savedName) {
                loadBookingHistory(savedName);
            }
        })
        .catch(error => {
            console.error("Lỗi cập nhật:", error);
            if (savedName) loadBookingHistory(savedName);
        });
    } /*else {
        console.error("Không tìm thấy tên tài khoản trong sessionStorage");
        alert("Hãy đăng nhập trước!");
        window.location.href = '../../bandau/DangNhapGD.html';
    } */
	
});

function loadBookingHistory(username) {
	const tbody = document.querySelector('#lichKHTable tbody');
    fetch(`http://localhost:9999/api/lich-su-dat-lich?tenTK=${username}`)
        .then(response => response.json())
        .then(data => {
			// KIỂM TRA DỮ LIỆU TRỐNG
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Bạn chưa có lịch hẹn nào.</td></tr>';
                return;
            }
            tbody.innerHTML = ''; // Xóa dữ liệu cũ

            data.forEach(item => {
                let displayRating = "Chưa có";
                const rawData = item.danhGia || "";

                if (rawData) {
                    // Tìm số đứng sau DV- và CL-
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
                        <td>${item.tenBacSi}</td>
                        <td>${item.tenDV}</td>
                        <td class="status-${item.tinhTrang}">${translateStatus(item.tinhTrang)}</td>
                        <td class="status-${item.khachDenKham}">${translateStatus(item.khachDenKham)}</td>
                        <td>${displayRating}</td>
                    </tr>
                `;
                tbody.innerHTML += row;
            });
        })
        .catch(err => {
            console.error("Lỗi tải bảng:", err);
            document.querySelector('#lichKHTable tbody').innerHTML = '<tr><td colspan="5">Lỗi kết nối server hoặc sai API.</td></tr>';
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