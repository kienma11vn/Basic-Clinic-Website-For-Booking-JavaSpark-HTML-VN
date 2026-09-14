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

public class LienHeHoTroHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API 1: Gửi yêu cầu mới
        post("/api/gui-yeu-cau", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String tenTK = data.get("username");
            String tieuDe = data.get("tieuDe");
            String noiDung = data.get("noiDung");

            // Gộp tiêu đề và nội dung vào 1 cột với ký tự phân cách
            String noiDungTongHop = "[TITLE]:" + tieuDe + "[CONTENT]:" + noiDung;
            String maYC = "YC" + System.currentTimeMillis();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Lấy maTK từ tenTK
                String sqlGetMaTK = "SELECT maTK FROM TaiKhoan WHERE tenTK = ?";
                PreparedStatement psTK = conn.prepareStatement(sqlGetMaTK);
                psTK.setString(1, tenTK);
                ResultSet rs = psTK.executeQuery();

                if (rs.next()) {
                    String maTK = rs.getString("maTK");
                    String sqlInsert = "INSERT INTO YeuCau (maYC, maTK, noiDungYC, ngayYC) VALUES (?, ?, ?, GETDATE())";
                    PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                    psInsert.setString(1, maYC);
                    psInsert.setString(2, maTK);
                    psInsert.setString(3, noiDungTongHop);
                    psInsert.executeUpdate();
                    return "{\"status\":\"success\"}";
                }
                return "{\"status\":\"error\", \"message\":\"User not found\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // API 2: Lấy danh sách yêu cầu của User
        get("/api/danh-sach-yeu-cau/:username", (req, res) -> {
            String username = req.params(":username");
            List<Map<String, Object>> list = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT maYC, noiDungYC, ngayYC, ngayTL FROM YeuCau y JOIN TaiKhoan t ON y.maTK = t.maTK WHERE t.tenTK = ? ORDER BY ngayYC DESC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    String rawContent = rs.getString("noiDungYC");
                    // Tách tiêu đề từ chuỗi gộp
                    String tieuDe = rawContent.contains("[TITLE]:")
                            ? rawContent.substring(rawContent.indexOf("[TITLE]:") + 8, rawContent.indexOf("[CONTENT]:")) : "Không tiêu đề";

                    item.put("maYC", rs.getString("maYC"));
                    item.put("tieuDe", tieuDe);
                    item.put("ngayYC", rs.getTimestamp("ngayYC").toString());
                    item.put("ngayTL", rs.getTimestamp("ngayTL") != null ? rs.getTimestamp("ngayTL").toString() : "Chưa trả lời");
                    list.add(item);
                }
                return gson.toJson(list);
            } catch (Exception e) {
                return "[]";
            }
        });

        // API 3: Lấy chi tiết 1 yêu cầu
        get("/api/chi-tiet-yeu-cau/:maYC", (req, res) -> {
            String maYC = req.params(":maYC");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT * FROM YeuCau WHERE maYC = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, maYC);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Map<String, Object> detail = new HashMap<>();
                    String raw = rs.getString("noiDungYC");
                    detail.put("maYC", rs.getString("maYC"));
                    detail.put("tieuDe", raw.substring(raw.indexOf("[TITLE]:") + 8, raw.indexOf("[CONTENT]:")));
                    detail.put("noiDung", raw.substring(raw.indexOf("[CONTENT]:") + 10));
                    detail.put("traLoi", rs.getString("noiDungTL") != null ? rs.getString("noiDungTL") : "Chưa có phản hồi từ lễ tân.");
                    detail.put("ngayYC", rs.getTimestamp("ngayYC").toString());
                    detail.put("ngayTL", rs.getTimestamp("ngayTL") != null ? rs.getTimestamp("ngayTL").toString() : "---");
                    return gson.toJson(detail);
                }
            }
            return "{}";
        });
    }
}
