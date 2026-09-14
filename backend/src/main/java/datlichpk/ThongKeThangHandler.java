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

public class ThongKeThangHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static Gson gson = new Gson();

    public static void initRoutes() {
        // API 1: Thống kê số liệu theo tháng
        get("/api/admin/thong-ke-thang", (req, res) -> {
            String monthYear = req.queryParams("thang"); // Định dạng "yyyy-MM" từ input type="month"
            String[] parts = monthYear.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);

            Map<String, Object> result = new HashMap<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT "
                        + "COUNT(*) as tongLich, "
                        + "SUM(CASE WHEN tinhTrang IN ('Huy', 'XNHuy', 'KHuy') THEN 1 ELSE 0 END) as soHuy, "
                        + "SUM(CASE WHEN khachDenKham = 'Yes' THEN 1 ELSE 0 END) as soDenKham "
                        + "FROM DatLich WHERE MONTH(ngayDat) = ? AND YEAR(ngayDat) = ?";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, month);
                pstmt.setInt(2, year);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    result.put("thang", monthYear);
                    result.put("tongLich", rs.getInt("tongLich"));
                    result.put("soHuy", rs.getInt("soHuy"));
                    result.put("soDenKham", rs.getInt("soDenKham"));
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
            return gson.toJson(result);
        });

        // API 2: Xuất báo cáo tháng (.txt)
        get("/api/admin/xuat-bao-cao-thang", (req, res) -> {
            String monthYear = req.queryParams("thang");
            String[] parts = monthYear.split("-");

            StringBuilder content = new StringBuilder();
            content.append("BÁO CÁO THỐNG KÊ THÁNG: ").append(monthYear).append("\n");
            content.append("---------------------------\n");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT COUNT(*) as tong, "
                        + "SUM(CASE WHEN khachDenKham = 'Yes' THEN 1 ELSE 0 END) as den, "
                        + "SUM(CASE WHEN tinhTrang IN ('Huy', 'XNHuy', 'KHuy') THEN 1 ELSE 0 END) as huy "
                        + "FROM DatLich WHERE MONTH(ngayDat) = ? AND YEAR(ngayDat) = ?";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(parts[1]));
                pstmt.setInt(2, Integer.parseInt(parts[0]));
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    content.append("- Tổng số ca đặt trong tháng: ").append(rs.getInt("tong")).append("\n");
                    content.append("- Tổng ca đã đến khám: ").append(rs.getInt("den")).append("\n");
                    content.append("- Tổng ca đã hủy: ").append(rs.getInt("huy")).append("\n");
                }

                res.header("Content-Disposition", "attachment; filename=BaoCaoThang_" + monthYear + ".txt");
                res.type("text/plain; charset=utf-8");
                return content.toString();
            }
        });
    }
}
