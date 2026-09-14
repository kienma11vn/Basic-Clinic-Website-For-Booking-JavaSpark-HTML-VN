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

public class DoiMKHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        post("/api/doi-mat-khau-truc-tiep", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, String> data = gson.fromJson(req.body(), Map.class);
                String tenTK = data.get("tenTK");
                String oldPass = data.get("oldPass");
                String newPass = data.get("newPass");

                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    // 1. Kiểm tra mật khẩu cũ có đúng không
                    String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenTK = ? AND matKhau = ?";
                    PreparedStatement psCheck = conn.prepareStatement(checkSql);
                    psCheck.setString(1, tenTK);
                    psCheck.setString(2, oldPass);
                    ResultSet rs = psCheck.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        // 2. Nếu đúng thì mới cập nhật mật khẩu mới
                        String updateSql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
                        PreparedStatement psUpdate = conn.prepareStatement(updateSql);
                        psUpdate.setString(1, newPass);
                        psUpdate.setString(2, tenTK);
                        psUpdate.executeUpdate();

                        return "{\"status\":\"success\", \"message\":\"Đổi mật khẩu thành công!\"}";
                    } else {
                        res.status(401);
                        return "{\"status\":\"error\", \"message\":\"Mật khẩu cũ không chính xác!\"}";
                    }
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}";
            }
        });
    }
}
