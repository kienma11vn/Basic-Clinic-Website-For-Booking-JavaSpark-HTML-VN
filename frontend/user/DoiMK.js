// Trong file DoiMK.js
document.getElementById('doimk-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    // Lấy đúng khóa 'username' từ sessionStorage
    const tenTK = sessionStorage.getItem('username'); 
    const oldPass = document.getElementById('oldPass').value;
    const newPass = document.getElementById('newPass').value;
    const confirmPass = document.getElementById('confirmPass').value;

    if (!tenTK) {
        alert("Phiên làm việc hết hạn, vui lòng đăng nhập lại!");
        // Kiểm tra lại đường dẫn này cho đúng với cấu trúc thư mục của bạn
        window.location.href = '../bandau/DangNhapGD.html'; 
        return;
    }

    if (newPass !== confirmPass) {
        alert("Mật khẩu mới và xác nhận không khớp!");
        return;
    }

    const btn = document.getElementById('btnSubmit');
    btn.innerText = "Đang xử lý...";
    btn.disabled = true;

    try {
        const response = await fetch('http://localhost:9999/api/doi-mat-khau-truc-tiep', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                tenTK: tenTK, // Gửi tenTK lên để Backend dùng trong câu lệnh WHERE
                oldPass: oldPass,
                newPass: newPass
            })
        });

        const result = await response.json();

        if (response.ok && result.status === 'success') {
            alert("Đổi mật khẩu thành công!");
            logout(); 
        } else {
            alert(result.message || "Mật khẩu cũ không chính xác!");
        }
    } catch (error) {
        alert("Lỗi kết nối server!");
    } finally {
        btn.innerText = "Cập nhật mật khẩu";
        btn.disabled = false;
    }
});

function logout() {
    sessionStorage.removeItem('username');
    window.location.href = '../PK_TrangChu.html';
}