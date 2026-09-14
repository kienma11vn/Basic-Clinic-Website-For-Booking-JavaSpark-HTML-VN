document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'letan00';
    const userDisplay = document.getElementById('usernameDisplay');
    if (userDisplay) userDisplay.innerText = savedName;

    // Thiết lập năm hiện tại làm mặc định
    const currentYear = new Date().getFullYear();
    const yearInput = document.getElementById('filterYear');
    if (yearInput) yearInput.value = currentYear;

    findNam();
});

function findNam() {
    const selectedYear = document.getElementById('filterYear').value;
    if (!selectedYear) {
        alert("Vui lòng nhập năm!");
        return;
    }

    fetch(`http://localhost:9999/api/admin/thong-ke-nam?nam=${selectedYear}`)
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('thongKeBody');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td>Năm ${data.nam}</td>
                        <td style="font-weight: bold;">${data.tongLich || 0}</td>
                        <td style="color: red; font-weight: bold;">${data.soHuy || 0}</td>
                        <td style="color: green; font-weight: bold;">${data.soDenKham || 0}</td>
                    </tr>
                `;
            }
        })
        .catch(err => {
            console.error("Lỗi:", err);
            alert("Không thể kết nối đến máy chủ.");
        });
}

function createReport() {
    const selectedYear = document.getElementById('filterYear').value;
    if (!selectedYear) {
        alert("Vui lòng nhập năm để xuất báo cáo!");
        return;
    }
    // Chuyển hướng để tải file báo cáo
    window.location.href = `http://localhost:9999/api/admin/xuat-bao-cao-nam?nam=${selectedYear}`;
}