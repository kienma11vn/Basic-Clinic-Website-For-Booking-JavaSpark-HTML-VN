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
import com.google.gson.Gson;
import java.sql.*;
import java.util.*;
import io.github.cdimascio.dotenv.Dotenv;

public class QLKhachHangHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. API Lấy danh sách khách hàng (Read)
        get("/api/admin/khach-hang", (req, res) -> {
            res.type("application/json");
            List<Map<String, Object>> list = new ArrayList<>();
            String sql = "SELECT TK.maTK, KH.maKH, TK.emailDK, N.hoTen, N.ngaySinh, N.SDT, N.diaChi, N.gioiTinh, TK.tenTK, TK.matKhau, N.soCCCD "
                    + "FROM KhachHang KH "
                    + "JOIN TaiKhoan TK ON KH.maTK = TK.maTK "
                    + "JOIN Nguoi N ON TK.maTK = N.maTK";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maTK", rs.getString("maTK"));
                    map.put("maKH", rs.getString("maKH"));
                    map.put("emailDK", rs.getString("emailDK"));
                    map.put("hoTen", rs.getString("hoTen"));
                    map.put("ngaySinh", rs.getString("ngaySinh") != null ? rs.getString("ngaySinh").split(" ")[0] : "");
                    map.put("SDT", rs.getString("SDT"));
                    map.put("diaChi", rs.getString("diaChi"));
                    map.put("gioiTinh", rs.getString("gioiTinh"));
                    map.put("tenTK", rs.getString("tenTK"));
                    map.put("matKhau", rs.getString("matKhau"));
                    map.put("soCCCD", rs.getString("soCCCD"));
                    list.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
            return gson.toJson(list);
        });

        // 2. API Thêm khách hàng mới (Create)
        post("/api/admin/khach-hang", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);

            // Lấy dữ liệu từ Request Body gửi từ JS
            String tenTK = data.get("tenTK");
            if (tenTK == null || tenTK.length() < 8) {
                // Nếu SDT làm TK không đủ 8 ký tự, ta prefix thêm để thỏa mãn CHECK (LEN(tenTK) BETWEEN 8 AND 20)
                tenTK = "user_" + data.get("SDT");
            }
            String hoTen = data.get("hoTen");
            String emailDK = data.get("emailDK");
            String soCCCD = data.get("soCCCD");
            String ngaySinh = data.get("ngaySinh");
            String SDT = data.get("SDT");
            String diaChi = data.get("diaChi");
            String gioiTinh = data.get("gioiTinh");

            // Tạo mã định danh tự động
            String maMoi = "KH" + System.currentTimeMillis();
            String matKhauMacDinh = "12345678abc"; // Mật khẩu mặc định cho khách hàng mới

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false); // Bắt đầu Transaction

                try {
                    // Bước 1: Chèn vào bảng TaiKhoan
                    String sqlTK = "INSERT INTO TaiKhoan (maTK, tenTK, matKhau, loaiTK, emailDK) VALUES (?, ?, ?, 'KH', ?)";
                    PreparedStatement psTK = conn.prepareStatement(sqlTK);
                    psTK.setString(1, maMoi);
                    psTK.setString(2, tenTK);
                    psTK.setString(3, matKhauMacDinh);
                    psTK.setString(4, emailDK);
                    psTK.executeUpdate();

                    // Bước 2: Chèn vào bảng Nguoi
                    String sqlNguoi = "INSERT INTO Nguoi (soCCCD, maTK, hoTen, ngaySinh, diaChi, SDT, gioiTinh) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement psNguoi = conn.prepareStatement(sqlNguoi);
                    psNguoi.setString(1, soCCCD);
                    psNguoi.setString(2, maMoi);
                    psNguoi.setString(3, hoTen);
                    psNguoi.setString(4, ngaySinh);
                    psNguoi.setString(5, diaChi);
                    psNguoi.setString(6, SDT);
                    psNguoi.setString(7, gioiTinh);
                    psNguoi.executeUpdate();

                    // Bước 3: Chèn vào bảng KhachHang
                    String sqlKH = "INSERT INTO KhachHang (maKH, maTK) VALUES (?, ?)";
                    PreparedStatement psKH = conn.prepareStatement(sqlKH);
                    psKH.setString(1, maMoi);
                    psKH.setString(2, maMoi);
                    psKH.executeUpdate();

                    conn.commit(); // Hoàn tất lưu dữ liệu
                    return "{\"status\":\"success\"}";

                } catch (SQLException e) {
                    conn.rollback(); // Nếu lỗi, hoàn tác lại toàn bộ
                    e.printStackTrace();
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"Lỗi SQL: " + e.getMessage() + "\"}";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // 3. API Cập nhật thông tin khách hàng (Update - PUT)
        put("/api/admin/khach-hang/:maTK", (req, res) -> {
            res.type("application/json");
            String maTK = req.params(":maTK");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);

            String hoTen = data.get("hoTen");
            String emailDK = data.get("emailDK");
            String soCCCD = data.get("soCCCD");
            String SDT = data.get("SDT");
            String diaChi = data.get("diaChi");
            String gioiTinh = data.get("gioiTinh");
            String ngaySinh = data.get("ngaySinh");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false);
                try {
                    // Cập nhật bảng TaiKhoan (Email)
                    String sqlTK = "UPDATE TaiKhoan SET emailDK = ? WHERE maTK = ?";
                    PreparedStatement psTK = conn.prepareStatement(sqlTK);
                    psTK.setString(1, emailDK);
                    psTK.setString(2, maTK);
                    psTK.executeUpdate();

                    // Cập nhật bảng Nguoi (Thông tin cá nhân)
                    String sqlNguoi = "UPDATE Nguoi SET hoTen = ?, soCCCD = ?, SDT = ?, diaChi = ?, gioiTinh = ?, ngaySinh = ? WHERE maTK = ?";
                    PreparedStatement psNguoi = conn.prepareStatement(sqlNguoi);
                    psNguoi.setString(1, hoTen);
                    psNguoi.setString(2, soCCCD);
                    psNguoi.setString(3, SDT);
                    psNguoi.setString(4, diaChi);
                    psNguoi.setString(5, gioiTinh);
                    psNguoi.setString(6, ngaySinh);
                    psNguoi.setString(7, maTK);
                    psNguoi.executeUpdate();

                    conn.commit();
                    return "{\"status\":\"success\"}";
                } catch (SQLException e) {
                    conn.rollback();
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

// 4. API Xóa khách hàng (Delete - DELETE)
        delete("/api/admin/khach-hang/:maTK", (req, res) -> {
            res.type("application/json");
            String maTK = req.params(":maTK");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false);
                try {
                    // Xóa theo thứ tự ngược lại với lúc thêm để tránh lỗi FK
                    // 1. Xóa trong KhachHang
                    String sqlKH = "DELETE FROM KhachHang WHERE maTK = ?";
                    PreparedStatement psKH = conn.prepareStatement(sqlKH);
                    psKH.setString(1, maTK);
                    psKH.executeUpdate();

                    // 2. Xóa trong Nguoi
                    String sqlNguoi = "DELETE FROM Nguoi WHERE maTK = ?";
                    PreparedStatement psNguoi = conn.prepareStatement(sqlNguoi);
                    psNguoi.setString(1, maTK);
                    psNguoi.executeUpdate();

                    // 3. Xóa trong TaiKhoan
                    String sqlTK = "DELETE FROM TaiKhoan WHERE maTK = ?";
                    PreparedStatement psTK = conn.prepareStatement(sqlTK);
                    psTK.setString(1, maTK);
                    psTK.executeUpdate();

                    conn.commit();
                    return "{\"status\":\"success\"}";
                } catch (SQLException e) {
                    conn.rollback();
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
