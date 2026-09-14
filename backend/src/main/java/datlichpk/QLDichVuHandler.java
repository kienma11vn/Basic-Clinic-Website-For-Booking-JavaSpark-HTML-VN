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

public class QLDichVuHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // Lấy danh sách hoặc tìm kiếm
        get("/api/admin/dich-vu", (req, res) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            String maDV = req.queryParams("maDV");
            String tenDV = req.queryParams("tenDV");
            String tenCK = req.queryParams("tenCK");

            String sql = "SELECT * FROM DichVu WHERE 1=1";
            if (maDV != null && !maDV.isEmpty()) {
                sql += " AND maDV LIKE ?";
            }
            if (tenDV != null && !tenDV.isEmpty()) {
                sql += " AND tenDV LIKE ?";
            }
            if (tenCK != null && !tenCK.isEmpty()) {
                sql += " AND tenCK LIKE ?";
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                int idx = 1;
                if (maDV != null && !maDV.isEmpty()) {
                    pstmt.setString(idx++, "%" + maDV + "%");
                }
                if (tenDV != null && !tenDV.isEmpty()) {
                    pstmt.setString(idx++, "%" + tenDV + "%");
                }
                if (tenCK != null && !tenCK.isEmpty()) {
                    pstmt.setString(idx++, "%" + tenCK + "%");
                }

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maDV", rs.getString("maDV"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("moTaDV", rs.getString("moTaDV"));
                    item.put("giaDV", rs.getDouble("giaDV"));
                    item.put("tenCK", rs.getString("tenCK"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // Thêm dịch vụ
        post("/api/admin/dich-vu", (req, res) -> {
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            String sql = "INSERT INTO DichVu (maDV, tenDV, moTaDV, giaDV, tenCK) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "DV" + System.currentTimeMillis()); // Tự sinh mã
                pstmt.setString(2, (String) data.get("tenDV"));
                pstmt.setString(3, (String) data.get("moTaDV"));
                pstmt.setDouble(4, Double.parseDouble(data.get("giaDV").toString()));
                pstmt.setString(5, (String) data.get("tenCK"));
                pstmt.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // Sửa dịch vụ
        put("/api/admin/dich-vu/:id", (req, res) -> {
            String maDV = req.params(":id");
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            String sql = "UPDATE DichVu SET tenDV=?, moTaDV=?, giaDV=?, tenCK=? WHERE maDV=?";
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, (String) data.get("tenDV"));
                pstmt.setString(2, (String) data.get("moTaDV"));
                pstmt.setDouble(3, Double.parseDouble(data.get("giaDV").toString()));
                pstmt.setString(4, (String) data.get("tenCK"));
                pstmt.setString(5, maDV);
                pstmt.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                return "{\"status\":\"error\"}";
            }
        });

        // Xóa dịch vụ
        delete("/api/admin/dich-vu/:id", (req, res) -> {
            String maDV = req.params(":id");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM DichVu WHERE maDV=?");
                pstmt.setString(1, maDV);
                pstmt.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                return "{\"status\":\"error\"}";
            }
        });
    }
}
