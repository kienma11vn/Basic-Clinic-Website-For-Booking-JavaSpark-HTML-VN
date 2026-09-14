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

public class XemLichKHHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API lấy danh sách lịch hẹn của khách hàng dựa trên tenTK
        get("/api/lich-su-dat-lich", (req, res) -> {
            res.type("application/json");
            String tenTK = req.queryParams("tenTK");
            List<Map<String, String>> list = new ArrayList<>();

            String sql = "SELECT d.maDatLich, d.ngayDat, n.hoTen AS tenBacSi, dv.tenDV, d.tinhTrang, d.khachDenKham, d.danhGia "
                    + "FROM DatLich d "
                    + "JOIN KhachHang k ON d.maKH = k.maKH "
                    + "JOIN TaiKhoan t ON k.maTK = t.maTK "
                    + "JOIN BacSi b ON d.maBS = b.maBS "
                    + "JOIN Nguoi n ON b.maTK = n.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE t.tenTK = ? "
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
                    item.put("khachDenKham", rs.getString("khachDenKham") == null ? "Chưa xác nhận" : rs.getString("khachDenKham"));
                    item.put("danhGia", rs.getString("danhGia") == null ? "Chưa có" : rs.getString("danhGia"));
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
            return gson.toJson(list);
        });
    }
}
