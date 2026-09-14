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

public class DangKyLichHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {

        // 1. Lấy danh sách chuyên khoa duy nhất từ bảng BacSi
        get("/api/chuyen-khoa", (req, res) -> {
            List<String> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT tenCK FROM BacSi");
                while (rs.next()) {
                    list.add(rs.getString("tenCK"));
                }
            }
            return gson.toJson(list);
        });

        // 2. Lấy danh sách bác sĩ theo chuyên khoa
        get("/api/bac-si-theo-ck", (req, res) -> {
            String ck = req.queryParams("tenCK");
            List<Map<String, String>> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT b.maBS, n.hoTen FROM BacSi b JOIN Nguoi n ON b.maTK = n.maTK WHERE b.tenCK = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, ck);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maBS", rs.getString("maBS"));
                    item.put("hoTen", rs.getString("hoTen"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 3. Lấy danh sách dịch vụ
        get("/api/dich-vu-theo-ck", (req, res) -> {
            String ck = req.queryParams("tenCK");
            List<Map<String, String>> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT maDV, tenDV FROM DichVu WHERE tenCK = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, ck);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDV", rs.getString("maDV"));
                    item.put("tenDV", rs.getString("tenDV"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 4. Lưu lịch hẹn
        post("/api/dat-lich", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Hợp nhất Ngày và Giờ từ Frontend gửi lên
                String fullDateTime = data.get("ngay") + " " + data.get("gio");
                String trieuChung = data.get("trieuChung");

                // Kiểm tra trùng lịch (Giữ nguyên logic cũ nhưng dùng fullDateTime)
                String checkSql = "SELECT COUNT(*) FROM DatLich WHERE maBS = ? AND ngayDat = ?";
                PreparedStatement psCheck = conn.prepareStatement(checkSql);
                psCheck.setString(1, data.get("maBS"));
                psCheck.setString(2, fullDateTime);
                ResultSet rsCheck = psCheck.executeQuery();

                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"Bác sĩ đã có lịch vào giờ này!\"}";
                }

                String getKH = "SELECT maKH FROM KhachHang k JOIN TaiKhoan t ON k.maTK = t.maTK WHERE t.tenTK = ?";
                PreparedStatement psKH = conn.prepareStatement(getKH);
                psKH.setString(1, data.get("tenTK"));
                ResultSet rsKH = psKH.executeQuery();

                if (rsKH.next()) {
                    // Thêm trieuChung vào câu lệnh INSERT
                    String sql = "INSERT INTO DatLich (maDatLich, maKH, maBS, maDV, ngayDat, tinhTrang, trieuChung) VALUES (?, ?, ?, ?, ?, N'ChoXN', ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, "DL" + System.currentTimeMillis());
                    pstmt.setString(2, rsKH.getString("maKH"));
                    pstmt.setString(3, data.get("maBS"));
                    pstmt.setString(4, data.get("maDV"));
                    pstmt.setString(5, fullDateTime);
                    pstmt.setString(6, trieuChung);
                    pstmt.executeUpdate();
                    return "{\"status\":\"success\"}";
                }
                return "{\"status\":\"error\", \"message\":\"Không tìm thấy khách hàng\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
