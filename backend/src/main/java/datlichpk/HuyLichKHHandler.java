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

public class HuyLichKHHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. API lấy danh sách lịch có thể hủy (Chưa đến khám - No)
        get("/api/lich-co-the-huy", (req, res) -> {
            res.type("application/json");
            String tenTK = req.queryParams("tenTK");
            List<Map<String, String>> list = new ArrayList<>();

            String sql = "SELECT d.maDatLich, d.ngayDat, n.hoTen AS tenBacSi, dv.tenDV, d.tinhTrang "
                    + "FROM DatLich d "
                    + "JOIN KhachHang k ON d.maKH = k.maKH "
                    + "JOIN TaiKhoan t ON k.maTK = t.maTK "
                    + "JOIN BacSi b ON d.maBS = b.maBS "
                    + "JOIN Nguoi n ON b.maTK = n.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE t.tenTK = ? AND (d.khachDenKham = 'No' OR d.khachDenKham IS NULL) AND d.tinhTrang NOT IN ('Huy', 'KHuy', 'XNHuy') "
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
                    item.put("tinhTrang", rs.getString("tinhTrang"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 2. API thực hiện hủy lịch
        post("/api/huy-lich-kham", (req, res) -> {
            res.type("application/json");

            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maDL = data.get("maDatLich");
            Map<String, String> responseMap = new HashMap<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "UPDATE DatLich SET tinhTrang = 'Huy' "
                        + "WHERE maDatLich = ? "
                        + "AND (TRIM(khachDenKham) = 'No' OR khachDenKham IS NULL)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, maDL);
                int rows = pstmt.executeUpdate();
                
                if (rows > 0) {
                    responseMap.put("status", "success");
                } else {
                    responseMap.put("status", "error");
                    responseMap.put("message", "Không thể hủy (Lịch đã khám hoặc không tồn tại)");
                }
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                responseMap.put("status", "error");
                responseMap.put("message", e.getMessage());
            }
            return gson.toJson(responseMap);
        });
    }
}
