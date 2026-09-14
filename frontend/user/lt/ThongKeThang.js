document.addEventListener('DOMContentLoaded', () => {
    const savedName = sessionStorage.getItem('username') || 'letan00';
    document.getElementById('usernameDisplay').innerText = savedName;

    // Thiết lập tháng hiện tại làm mặc định (Định dạng yyyy-MM)
    const now = new Date();
    const currentMonth = now.toISOString().slice(0, 7); 
    document.getElementById('filterMonth').value = currentMonth;

    findThang();
});

function findThang() {
    const selectedMonth = document.getElementById('filterMonth').value;
    if (!selectedMonth) {
        alert("Vui lòng chọn tháng!");
        return;
    }

    fetch(`http://localhost:9999/api/admin/thong-ke-thang?thang=${selectedMonth}`)
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('thongKeBody');
            tbody.innerHTML = `
                <tr>
                    <td>Tháng ${data.thang}</td>
                    <td style="font-weight: bold;">${data.tongLich || 0}</td>
                    <td style="font-weight: bold; color: red;">${data.soHuy || 0}</td>
                    <td style="font-weight: bold; color: green;">${data.soDenKham || 0}</td>
                </tr>
            `;
        })
        .catch(err => console.error("Lỗi:", err));
}

function createReport() {
    const selectedMonth = document.getElementById('filterMonth').value;
    if (!selectedMonth) return alert("Vui lòng chọn tháng!");
    window.location.href = `http://localhost:9999/api/admin/xuat-bao-cao-thang?thang=${selectedMonth}`;
}