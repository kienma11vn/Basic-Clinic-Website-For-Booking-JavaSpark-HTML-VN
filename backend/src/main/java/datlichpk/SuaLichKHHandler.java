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

public class SuaLichKHHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. API lấy danh sách lịch khám CÓ THỂ SỬA (Chỉ lấy ChoXN)
        get("/api/danh-sach-sua-lich", (req, res) -> {
            res.type("application/json");
            String tenTK = req.queryParams("tenTK");
            List<Map<String, String>> list = new ArrayList<>();

            // Câu lệnh SQL lọc theo tenTK và trạng thái ChoXN
            String sql = "SELECT d.maDatLich, d.ngayDat, n.hoTen AS tenBacSi, dv.tenDV, d.tinhTrang "
                    + "FROM DatLich d "
                    + "JOIN KhachHang k ON d.maKH = k.maKH "
                    + "JOIN TaiKhoan t ON k.maTK = t.maTK "
                    + "JOIN BacSi b ON d.maBS = b.maBS "
                    + "JOIN Nguoi n ON b.maTK = n.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE t.tenTK = ? AND TRIM(d.tinhTrang) = 'ChoXN' " // Lọc tại đây
                    + "ORDER BY d.ngayDat DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tenTK);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang").trim());
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 2. API Lấy chi tiết 1 lịch để sửa (Dùng tham số :maDL)
        get("/api/chi-tiet-sua-lich/:maDL", (req, res) -> {
            res.type("application/json");
            String maDL = req.params(":maDL");
            Map<String, Object> detail = new HashMap<>();

            String sql = "SELECT d.*, dv.tenCK FROM DatLich d "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV WHERE d.maDatLich = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, maDL);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    detail.put("maDatLich", rs.getString("maDatLich"));
                    detail.put("ngayDat", rs.getString("ngayDat"));
                    detail.put("maBS", rs.getString("maBS"));
                    detail.put("maDV", rs.getString("maDV"));
                    detail.put("tenCK", rs.getString("tenCK"));
                    detail.put("trieuChung", rs.getString("trieuChung"));
                    detail.put("tinhTrang", rs.getString("tinhTrang").trim());
                }
            }
            return gson.toJson(detail);
        });

        // API Cập nhật lịch hẹn
        post("/api/cap-nhat-lich", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String fullDateTime = data.get("ngay") + " " + data.get("gio");

                // 1. Kiểm tra trùng lịch (loại trừ chính lịch đang sửa)
                String checkSql = "SELECT COUNT(*) FROM DatLich WHERE maBS = ? AND ngayDat = ? AND maDatLich != ?";
                PreparedStatement psCheck = conn.prepareStatement(checkSql);
                psCheck.setString(1, data.get("maBS"));
                psCheck.setString(2, fullDateTime);
                psCheck.setString(3, data.get("maDatLich"));
                ResultSet rsCheck = psCheck.executeQuery();

                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"Bác sĩ đã có lịch khác vào giờ này!\"}";
                }

                // 2. Cập nhật thông tin bao gồm cả trieuChung
                String sql = "UPDATE DatLich SET maBS = ?, maDV = ?, ngayDat = ?, trieuChung = ? WHERE maDatLich = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, data.get("maBS"));
                pstmt.setString(2, data.get("maDV"));
                pstmt.setString(3, fullDateTime);
                pstmt.setString(4, data.get("trieuChung"));
                pstmt.setString(5, data.get("maDatLich"));

                pstmt.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
