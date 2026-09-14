document.addEventListener('DOMContentLoaded', () => {
    const btnStart = document.getElementById('btnStart');
    const authButtons = document.getElementById('authButtons');
    const clinicCarousel = document.getElementById('clinicCarousel');

    btnStart.addEventListener('click', () => {
        btnStart.classList.add('d-none');
        authButtons.classList.remove('d-none');
        authButtons.classList.add('d-flex');
    });

    // Tìm nút Đăng ký (nút thứ 1 trong danh sách các nút của authButtons)
    const btnRegis = authButtons.querySelectorAll('.btn')[0];
    btnRegis.addEventListener('click', () => {
        // Chuyển hướng sang file DangKyGD.html nằm trong thư mục bandau
        window.location.href = 'bandau/DangKyGD.html';
    });

    // Tìm nút Đăng nhập (nút thứ 2 trong danh sách các nút của authButtons)
    const btnLogin = authButtons.querySelectorAll('.btn')[1];
    btnLogin.addEventListener('click', () => {
        // Chuyển hướng sang file DangNhapGD.html nằm trong thư mục bandau
        window.location.href = 'bandau/DangNhapGD.html';
    });

    // Tìm nút KPMK (nút thứ 3 trong danh sách các nút của authButtons)
    const btnKPMK = authButtons.querySelectorAll('.btn')[2];
    btnKPMK.addEventListener('click', () => {
        // Chuyển hướng sang file KhoiPhucMKGD.html nằm trong thư mục bandau
        window.location.href = 'bandau/KhoiPhucMKGD.html';
    });

    // Khởi tạo Bootstrap Carousel thủ công để có thể kiểm soát các sự kiện
    const myCarousel = new bootstrap.Carousel(clinicCarousel, {
        interval: 5000, // Thời gian chuyển slide là 5 giây (5000ms)
        ride: 'carousel'
    });

    // Lắng nghe sự kiện khi một slide mới được hiển thị
    clinicCarousel.addEventListener('slide.bs.carousel', function (event) {
        // Tùy chỉnh hiệu ứng hiển thị overlay nếu cần, 
        // nhưng với CSS hiện tại, hiệu ứng đã được xử lý thông qua opacity của .bottom-fade-overlay
        // khi .carousel-item.active thay đổi.
    });
});