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

public class XemTTLichKhamHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API lấy toàn bộ danh sách lịch hẹn để Lễ tân quản lý
        get("/api/lt/danh-sach-lich-kham", (req, res) -> {
            res.type("application/json");
            List<Map<String, String>> list = new ArrayList<>();

            // Truy vấn lấy thêm hoTen của bệnh nhân (người đặt lịch)
            String sql = "SELECT d.maDatLich, d.ngayDat, nBS.hoTen AS tenBacSi, nKH.hoTen AS tenBenhNhan, "
                    + "dv.tenDV, d.tinhTrang, d.khachDenKham, d.danhGia "
                    + "FROM DatLich d "
                    + "JOIN KhachHang k ON d.maKH = k.maKH "
                    + "JOIN Nguoi nKH ON k.maTK = nKH.maTK " // Lấy tên bệnh nhân
                    + "JOIN BacSi b ON d.maBS = b.maBS "
                    + "JOIN Nguoi nBS ON b.maTK = nBS.maTK " // Lấy tên bác sĩ
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "ORDER BY d.ngayDat DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang"));
                    item.put("khachDenKham", rs.getString("khachDenKham") == null ? "No" : rs.getString("khachDenKham"));

                    // Đảm bảo nếu null thì trả về chuỗi rỗng để JS dễ kiểm tra
                    String rawDanhGia = rs.getString("danhGia");
                    item.put("danhGia", (rawDanhGia == null || rawDanhGia.equalsIgnoreCase("null")) ? "" : rawDanhGia);

                    list.add(item);
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
            return gson.toJson(list);
        });
    }
}
