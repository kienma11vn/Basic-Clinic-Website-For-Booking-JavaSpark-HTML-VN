/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datlichpk;

/**
 *
 * @author Kien
 */
import static spark.Spark.*;
import java.sql.*;
import com.google.gson.Gson;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;

public class QLBacSiHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");

    public static void initRoutes() {
        Gson gson = new Gson();

        get("/api/admin/ds-bac-si", (req, res) -> {
            res.type("application/json");
            List<Map<String, Object>> list = new ArrayList<>();

            // Câu lệnh JOIN 3 bảng để lấy đầy đủ thông tin yêu cầu
            String sql = "SELECT BS.maBS, BS.tenCK, BS.chucDanh, BS.thamNien, "
                    + "N.hoTen, N.soCCCD, N.ngaySinh, N.SDT, N.gioiTinh, N.diaChi, "
                    + "TK.maTK, TK.tenTK, TK.matKhau "
                    + "FROM BacSi BS "
                    + "JOIN TaiKhoan TK ON BS.maTK = TK.maTK "
                    + "JOIN Nguoi N ON TK.maTK = N.maTK";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maBS", rs.getString("maBS"));
                    item.put("hoTen", rs.getString("hoTen"));
                    item.put("tenCK", rs.getString("tenCK"));
                    item.put("chucDanh", rs.getString("chucDanh"));
                    item.put("thamNien", rs.getInt("thamNien"));
                    // Dữ liệu cho Modal
                    item.put("maTK", rs.getString("maTK"));
                    item.put("tenTK", rs.getString("tenTK"));
                    item.put("matKhau", rs.getString("matKhau"));
                    item.put("soCCCD", rs.getString("soCCCD"));
                    item.put("ngaySinh", rs.getString("ngaySinh"));
                    item.put("SDT", rs.getString("SDT"));
                    item.put("gioiTinh", rs.getString("gioiTinh"));
                    item.put("diaChi", rs.getString("diaChi"));

                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "[]";
            }
            return gson.toJson(list);
        });

        get("/api/admin/danh-sach-chuyen-khoa", (req, res) -> {
            res.type("application/json");
            List<String> dsChuyenKhoa = new ArrayList<>();
            String sql = "SELECT DISTINCT tenCK FROM BacSi WHERE tenCK IS NOT NULL"; // Lấy danh sách không trùng lặp

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dsChuyenKhoa.add(rs.getString("tenCK"));
                }
            }
            return gson.toJson(dsChuyenKhoa);
        });

        post("/api/admin/them-bac-si", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                // Thông tin tài khoản
                String maTK = "TK_" + System.currentTimeMillis(); // Tạo mã tự động
                String tenTK = (String) data.get("tenTK");
                String matKhau = "Macdinh123456"; // Mật khẩu mặc định theo yêu cầu
                String email = (String) data.get("email");

                // Thông tin cá nhân (Nguoi)
                String soCCCD = (String) data.get("soCCCD");
                String hoTen = (String) data.get("hoTen");
                String ngaySinh = (String) data.get("ngaySinh");
                String diaChi = (String) data.get("diaChi");
                String sdt = (String) data.get("sdt");
                String gioiTinh = (String) data.get("gioiTinh");

                // Thông tin chuyên môn (BacSi)
                String maBS = "BS" + (System.currentTimeMillis() % 10000); // Mã bác sĩ đơn giản
                String tenCK = (String) data.get("tenCK");
                String chucDanh = (String) data.get("chucDanh");
                int thamNien = Integer.parseInt(data.get("thamNien").toString());

                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    conn.setAutoCommit(false); // Bắt đầu Transaction
                    try {
                        // 1. Chèn vào TaiKhoan
                        String sqlTK = "INSERT INTO TaiKhoan (maTK, tenTK, matKhau, loaiTK, emailDK) VALUES (?, ?, ?, 'BS', ?)";
                        PreparedStatement psTK = conn.prepareStatement(sqlTK);
                        psTK.setString(1, maTK);
                        psTK.setString(2, tenTK);
                        psTK.setString(3, matKhau);
                        psTK.setString(4, email);
                        psTK.executeUpdate();

                        // 2. Chèn vào Nguoi
                        String sqlNguoi = "INSERT INTO Nguoi (soCCCD, maTK, hoTen, ngaySinh, diaChi, SDT, gioiTinh) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement psNguoi = conn.prepareStatement(sqlNguoi);
                        psNguoi.setString(1, soCCCD);
                        psNguoi.setString(2, maTK);
                        psNguoi.setString(3, hoTen);
                        psNguoi.setString(4, ngaySinh);
                        psNguoi.setString(5, diaChi);
                        psNguoi.setString(6, sdt);
                        psNguoi.setString(7, gioiTinh);
                        psNguoi.executeUpdate();

                        // 3. Chèn vào BacSi
                        String sqlBS = "INSERT INTO BacSi (maBS, maTK, tenCK, chucDanh, thamNien) VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement psBS = conn.prepareStatement(sqlBS);
                        psBS.setString(1, maBS);
                        psBS.setString(2, maTK);
                        psBS.setString(3, tenCK);
                        psBS.setString(4, chucDanh);
                        psBS.setInt(5, thamNien);
                        psBS.executeUpdate();

                        conn.commit(); // Thành công hết thì mới lưu
                        return "{\"status\":\"success\", \"maBS\":\"" + maBS + "\"}";
                    } catch (SQLException e) {
                        conn.rollback(); // Lỗi một cái là hủy hết
                        throw e;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        put("/api/admin/sua-bac-si/:maBS", (req, res) -> {
            res.type("application/json");
            try {
                String maBS = req.params(":maBS");
                Map<String, Object> data = gson.fromJson(req.body(), Map.class);

                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    conn.setAutoCommit(false);
                    try {
                        // 1. Lấy maTK từ maBS
                        String sqlGetTK = "SELECT maTK FROM BacSi WHERE maBS = ?";
                        String maTK = "";
                        try (PreparedStatement ps = conn.prepareStatement(sqlGetTK)) {
                            ps.setString(1, maBS);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                maTK = rs.getString("maTK");
                            }
                        }

                        if (maTK.isEmpty()) {
                            throw new SQLException("Không tìm thấy bác sĩ");
                        }

                        // 2. Cập nhật bảng TaiKhoan (Email)
                        String sqlTK = "UPDATE TaiKhoan SET emailDK = ? WHERE maTK = ?";
                        try (PreparedStatement psTK = conn.prepareStatement(sqlTK)) {
                            psTK.setString(1, (String) data.get("email"));
                            psTK.setString(2, maTK);
                            psTK.executeUpdate();
                        }

                        // 3. Cập nhật bảng Nguoi
                        String sqlNguoi = "UPDATE Nguoi SET hoTen = ?, ngaySinh = ?, diaChi = ?, SDT = ?, gioiTinh = ?, soCCCD = ? WHERE maTK = ?";
                        try (PreparedStatement psNguoi = conn.prepareStatement(sqlNguoi)) {
                            psNguoi.setString(1, (String) data.get("hoTen"));
                            psNguoi.setString(2, (String) data.get("ngaySinh"));
                            psNguoi.setString(3, (String) data.get("diaChi"));
                            psNguoi.setString(4, (String) data.get("sdt"));
                            psNguoi.setString(5, (String) data.get("gioiTinh"));
                            psNguoi.setString(6, (String) data.get("soCCCD"));
                            psNguoi.setString(7, maTK);
                            psNguoi.executeUpdate();
                        }

                        // 4. Cập nhật bảng BacSi
                        String sqlBS = "UPDATE BacSi SET tenCK = ?, chucDanh = ?, thamNien = ? WHERE maBS = ?";
                        try (PreparedStatement psBS = conn.prepareStatement(sqlBS)) {
                            psBS.setString(1, (String) data.get("tenCK"));
                            psBS.setString(2, (String) data.get("chucDanh"));
                            psBS.setInt(3, Integer.parseInt(data.get("thamNien").toString()));
                            psBS.setString(4, maBS);
                            psBS.executeUpdate();
                        }

                        conn.commit();
                        return "{\"status\":\"success\"}";
                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    }
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        delete("/api/admin/xoa-bac-si/:maBS", (req, res) -> {
            res.type("application/json");
            String maBS = req.params(":maBS");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false); // Bắt đầu giao dịch

                try {
                    // 1. Lấy mã tài khoản (maTK) liên kết với bác sĩ này trước khi xóa
                    String getTK = "SELECT maTK FROM BacSi WHERE maBS = ?";
                    String maTK = "";
                    try (PreparedStatement ps = conn.prepareStatement(getTK)) {
                        ps.setString(1, maBS);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            maTK = rs.getString("maTK");
                        }
                    }

                    if (!maTK.isEmpty()) {
                        // 2. Xóa trong bảng BacSi
                        String sql1 = "DELETE FROM BacSi WHERE maBS = ?";
                        try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                            ps1.setString(1, maBS);
                            ps1.executeUpdate();
                        }

                        // 3. Lấy số CCCD từ bảng Nguoi để xóa (nếu cần, hoặc xóa theo maTK)
                        String sql2 = "DELETE FROM Nguoi WHERE maTK = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                            ps2.setString(1, maTK);
                            ps2.executeUpdate();
                        }

                        // 4. Xóa trong bảng TaiKhoan
                        String sql3 = "DELETE FROM TaiKhoan WHERE maTK = ?";
                        try (PreparedStatement ps3 = conn.prepareStatement(sql3)) {
                            ps3.setString(1, maTK);
                            ps3.executeUpdate();
                        }
                    }

                    conn.commit(); // Hoàn tất xóa
                    return "{\"status\":\"success\"}";
                } catch (SQLException e) {
                    conn.rollback(); // Lỗi thì quay xe
                    throw e;
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
