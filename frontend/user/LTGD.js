document.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy tên tài khoản từ localStorage (đã lưu khi đăng nhập thành công)
    // Nếu không có, mặc định hiện 'letan00' như trong ảnh mẫu
    const savedName = sessionStorage.getItem('username') || 'letan00';
    
    // 2. Hiển thị lên giao diện
    const displayElement = document.getElementById('usernameDisplay');
    if (displayElement) {
        displayElement.innerText = savedName;
    }
});

// Hàm đăng xuất
function logout() {
    sessionStorage.removeItem('username');
    window.location.href = '../PK_TrangChu.html';
}