document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username');
    
    if (!savedName || savedName === 'khachhang00') {
        alert("Vui lòng đăng nhập để thực hiện chức năng này!");
        // window.location.href = 'DangNhap.html'; // Mở ra nếu cần redirect
        return;
    }

    document.getElementById('usernameDisplay').innerText = savedName;
    loadCompletedBookings(savedName);
});

function translateStatus(status) {
    switch (status) {
        case 'Yes': return 'Đã đến khám';
        case 'No': return 'Không đến khám';
        default: return status;
    }
}

function loadCompletedBookings(username) {
    const tbody = document.querySelector('#danhGiaTable tbody');
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Đang tải dữ liệu...</td></tr>';

    fetch(`http://localhost:9999/api/kh/lich-da-kham?tenTK=${username}`)
        .then(res => res.json())
        .then(data => {
            tbody.innerHTML = '';
            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Không có lịch khám nào đã hoàn thành.</td></tr>';
                return;
            }

            data.forEach(item => {
                const hasDV = item.danhGia && item.danhGia.includes("[DV-");
                const statusText = hasDV 
                    ? '<span style="color:green;">Đã đánh giá</span>' 
                    : '<span style="color:gray;">Chưa đánh giá</span>';
                
                const safeRating = (item.danhGia || "").replace(/'/g, "\\'");

                tbody.innerHTML += `
                    <tr>
                        <td>
                            <a href="#" onclick="openRatingModal('${item.maDatLich}', '${safeRating}')" 
                               style="color: blue; font-weight: bold; text-decoration: underline;">
                               ${item.maDatLich}
                            </a>
                        </td>
                        <td>${item.tenBacSi || 'N/A'}</td>
                        <td>${item.ngayDat || 'N/A'}</td>
                        <td>${item.tenDV || 'N/A'}</td> <td>${translateStatus(item.khachDenKham)}</td>
                        <td>${statusText}</td>
                    </tr>`;
            });
        });

}

function openRatingModal(maDL, oldRating) {
    document.getElementById('modalMaDL').innerText = maDL;
    document.getElementById('oldRatingStore').value = oldRating;

    // --- LOGIC TRÍCH XUẤT NỘI DUNG CŨ ---
    let oldComment = "";
    let oldScore = "10"; // Mặc định nếu không tìm thấy

    if (oldRating && oldRating.includes("[DV-")) {
        // Regex tìm nội dung giữa dấu ':' và dấu ']'
        const commentMatch = oldRating.match(/\[DV-\d+đ:\s*(.*?)\]/);
        if (commentMatch && commentMatch[1]) {
            oldComment = commentMatch[1].trim();
        }

        // Regex tìm số điểm (ví dụ: lấy số 10 từ [DV-10đ...])
        const scoreMatch = oldRating.match(/\[DV-(\d+)đ/);
        if (scoreMatch && scoreMatch[1]) {
            oldScore = scoreMatch[1];
        }
    }

    // Hiển thị lên Form
    document.getElementById('comment').value = oldComment;
    document.getElementById('score').value = oldScore;
    
    document.getElementById('ratingModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('ratingModal').style.display = 'none';
}

function saveRating() {
    const maDL = document.getElementById('modalMaDL').innerText;
    const score = document.getElementById('score').value;
    const comment = document.getElementById('comment').value.trim();
    const oldData = document.getElementById('oldRatingStore').value;

    if (!comment) {
        alert("Vui lòng nhập nội dung đánh giá!");
        return;
    }

    // Tạo tag mới cho Dịch Vụ
    const newDVTag = `[DV-${score}đ: ${comment}]`;
    
    // LOGIC GỘP CHUỖI:
    // 1. Xóa bỏ tag [DV-...] cũ nếu có trong chuỗi oldData bằng Regex
    // 2. Giữ lại tag [CL-...] (nếu có)
    // 3. Thêm tag DV mới vào cuối
    let finalString = oldData.replace(/\[DV-.*?\]/g, "").trim(); 
    finalString = (finalString + " " + newDVTag).trim();

    const payload = { 
        maDatLich: maDL, 
        noiDungMoi: finalString 
    };

    fetch(`http://localhost:9999/api/kh/luu-danh-gia`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(result => {
        if (result.status === 'success') {
            alert("Lưu đánh giá thái độ phục vụ thành công!");
            closeModal();
            // Tải lại bảng để cập nhật trạng thái "Đã có"
            loadCompletedBookings(sessionStorage.getItem('username'));
        } else {
            alert("Lỗi: " + (result.message || "Không thể lưu"));
        }
    })
    .catch(err => {
        console.error("Lỗi khi lưu:", err);
        alert("Lỗi kết nối server!");
    });
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
    const filterMa = document.getElementById('searchMaDL').value.toLowerCase().trim();
    const filterNgay = document.getElementById('searchNgay').value;
    const filterBS = document.getElementById('searchBacSi').value.toLowerCase().trim();
    const filterDV = document.getElementById('searchDV').value.toLowerCase().trim();

    const table = document.getElementById("danhGiaTable"); // Đã sửa ID bảng
    const tr = table.getElementsByTagName("tr");

    for (let i = 1; i < tr.length; i++) {
        let showRow = true;
        const td = tr[i].getElementsByTagName("td");
        
        if (td.length > 0) {
            // Thứ tự cột trong HTML: 0: Mã, 1: Bác sĩ, 2: Ngày, 3: Dịch vụ
            const txtMa = td[0].textContent.toLowerCase();
            const txtBS = td[1].textContent.toLowerCase();
            const txtNgay = td[2].textContent;
            const txtDV = td[3].textContent.toLowerCase();

            if (filterMa && !txtMa.includes(filterMa)) showRow = false;
            if (filterBS && !txtBS.includes(filterBS)) showRow = false;
            if (filterNgay && !txtNgay.includes(filterNgay)) showRow = false;
            if (filterDV && !txtDV.includes(filterDV)) showRow = false;

            tr[i].style.display = showRow ? "" : "none";
        }
    }
    closeSearchModal();
}