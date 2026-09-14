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

public class HoTroKHBSHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API: Lấy toàn bộ danh sách yêu cầu (Dùng maTK để JOIN)
        get("/api/admin/danh-sach-yeu-cau", (req, res) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // JOIN bảng YeuCau với TaiKhoan qua maTK
                String sql = "SELECT YC.*, TK.loaiTK, TK.tenTK FROM YeuCau YC "
                        + "JOIN TaiKhoan TK ON YC.maTK = TK.maTK "
                        + "ORDER BY YC.ngayYC DESC";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maYC", rs.getString("maYC"));
                    map.put("tenTK", rs.getString("tenTK")); // Lấy tên hiển thị
                    map.put("loaiTK", rs.getString("loaiTK"));
                    map.put("noiDungYC", rs.getString("noiDungYC"));
                    map.put("noiDungTL", rs.getString("noiDungTL"));
                    map.put("ngayYC", rs.getTimestamp("ngayYC").toString());
                    list.add(map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return gson.toJson(list);
        });

        // API: Cập nhật nội dung trả lời
        post("/api/tra-loi-yeu-cau", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maYC = data.get("maYC");
            String noiDungTL = data.get("noiDungTL");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "UPDATE YeuCau SET noiDungTL = ?, ngayTL = GETDATE() WHERE maYC = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, noiDungTL);
                ps.setString(2, maYC);
                int updated = ps.executeUpdate();
                return updated > 0 ? "{\"status\":\"success\"}" : "{\"status\":\"fail\"}";
            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
