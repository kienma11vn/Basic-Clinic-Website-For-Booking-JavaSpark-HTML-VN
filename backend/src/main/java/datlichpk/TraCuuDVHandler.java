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

public class TraCuuDVHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static Gson gson = new Gson();

    public static void initRoutes() {
        // API 1: Lấy danh sách dịch vụ có lọc (Bỏ JOIN vì không có bảng ChuyenKhoa)
        get("/api/dich-vu", (req, res) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            String tenDV = req.queryParams("tenDV");
            String giaMax = req.queryParams("giaMax");
            String tenCK = req.queryParams("maCK"); // maCK từ JS lúc này sẽ chứa tên chuyên khoa

            StringBuilder sql = new StringBuilder(
                    "SELECT maDV, tenDV, moTaDV, giaDV, tenCK FROM DichVu WHERE 1=1"
            );

            if (tenDV != null && !tenDV.isEmpty()) {
                sql.append(" AND tenDV LIKE N'%").append(tenDV).append("%'");
            }
            if (giaMax != null && !giaMax.isEmpty()) {
                sql.append(" AND giaDV <= ").append(giaMax);
            }
            if (tenCK != null && !tenCK.isEmpty()) {
                sql.append(" AND tenCK = N'").append(tenCK).append("'");
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maDV", rs.getString("maDV"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("moTaDV", rs.getString("moTaDV"));
                    item.put("giaDV", rs.getDouble("giaDV"));
                    item.put("tenCK", rs.getString("tenCK"));
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return gson.toJson(list);
        });

        // API 2: Lấy danh sách chuyên khoa DUY NHẤT từ bảng DichVu
        get("/api/dv-chuyen-khoa-list", (req, res) -> {
            List<Map<String, String>> list = new ArrayList<>();
            // Lấy danh sách tên chuyên khoa không trùng lặp hiện có trong bảng DichVu
            String sql = "SELECT DISTINCT tenCK FROM DichVu WHERE tenCK IS NOT NULL";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    String ten = rs.getString("tenCK");
                    item.put("maCK", ten); // Gán tên vào mã để JS gửi ngược lại lọc
                    item.put("tenCK", ten);
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return gson.toJson(list);
        });
    }
}
