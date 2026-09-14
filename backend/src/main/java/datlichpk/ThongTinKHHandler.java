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

public class ThongTinKHHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API 1: Lấy thông tin cá nhân dựa trên username (tenTK)
        get("/api/get-thong-tin/:username", (req, res) -> {
            String username = req.params(":username");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT k.maKH, t.emailDK, n.soCCCD, n.hoTen, n.ngaySinh, n.gioiTinh, n.SDT, n.diaChi "
                        + "FROM TaiKhoan t "
                        + "JOIN KhachHang k ON t.maTK = k.maTK "
                        + "LEFT JOIN Nguoi n ON t.maTK = n.maTK "
                        + "WHERE t.tenTK = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("maKH", rs.getString("maKH"));
                    map.put("emailDK", rs.getString("emailDK"));
                    map.put("soCCCD", rs.getString("soCCCD"));
                    map.put("hoTen", rs.getString("hoTen"));
                    map.put("ngaySinh", rs.getString("ngaySinh"));
                    map.put("gioiTinh", rs.getString("gioiTinh"));
                    map.put("SDT", rs.getString("SDT"));
                    map.put("diaChi", rs.getString("diaChi"));
                    return gson.toJson(map);
                }
                res.status(404);
                return "{\"message\":\"Không tìm thấy dữ liệu\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });

        // API 2: Cập nhật thông tin cá nhân
        post("/api/update-thong-tin", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String username = data.get("username");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Lấy maTK từ username
                String getMaTK = "SELECT maTK FROM TaiKhoan WHERE tenTK = ?";
                PreparedStatement ps1 = conn.prepareStatement(getMaTK);
                ps1.setString(1, username);
                ResultSet rs = ps1.executeQuery();

                if (rs.next()) {
                    String maTK = rs.getString("maTK");
                    // Kiểm tra xem đã có bản ghi trong bảng Nguoi chưa, nếu chưa thì INSERT, có rồi thì UPDATE
                    String sqlUpdate = "IF EXISTS (SELECT 1 FROM Nguoi WHERE maTK = ?) "
                            + "UPDATE Nguoi SET soCCCD=?, hoTen=?, ngaySinh=?, gioiTinh=?, SDT=?, diaChi=? WHERE maTK=? "
                            + "ELSE "
                            + "INSERT INTO Nguoi (soCCCD, hoTen, ngaySinh, gioiTinh, SDT, diaChi, maTK) VALUES (?,?,?,?,?,?,?)";

                    PreparedStatement ps2 = conn.prepareStatement(sqlUpdate);
                    ps2.setString(1, maTK); // Tham số 1 cho IF EXISTS

					// Dữ liệu cho UPDATE (Tham số 2 -> 7) và INSERT (Tham số 9 -> 14)
                    ps2.setString(2, data.get("soCCCD"));
                    ps2.setString(9, data.get("soCCCD"));
                    ps2.setString(3, data.get("hoTen"));
                    ps2.setString(10, data.get("hoTen"));
                    ps2.setString(4, data.get("ngaySinh"));
                    ps2.setString(11, data.get("ngaySinh"));
                    ps2.setString(5, data.get("gioiTinh"));
                    ps2.setString(12, data.get("gioiTinh"));
                    ps2.setString(6, data.get("SDT"));
                    ps2.setString(13, data.get("SDT"));
                    ps2.setString(7, data.get("diaChi"));
                    ps2.setString(14, data.get("diaChi"));

					// THAM SỐ 8: Cho điều kiện WHERE maTK=? của lệnh UPDATE
                    ps2.setString(8, maTK);

					// THAM SỐ 15: Cho cột maTK của lệnh INSERT
                    ps2.setString(15, maTK);

                    ps2.executeUpdate();
                    return "{\"status\":\"success\"}";
                }
                return "{\"status\":\"error\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
