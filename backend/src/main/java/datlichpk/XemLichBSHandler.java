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

public class XemLichBSHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // API lấy danh sách lịch hẹn dành cho bác sĩ dựa trên tên tài khoản bác sĩ
        get("/api/bs/lich-kham", (req, res) -> {
            res.type("application/json");
            String tenTK = req.queryParams("tenTK");
            List<Map<String, String>> list = new ArrayList<>();

            // Truy vấn lấy dữ liệu lịch khám của bác sĩ đang đăng nhập
            String sql = "SELECT d.maDatLich, d.ngayDat, n.hoTen AS tenBenhNhan, dv.tenDV, "
                    + "d.tinhTrang, d.khachDenKham, d.danhGia, d.trieuChung "
                    + "FROM DatLich d "
                    + "JOIN BacSi b ON d.maBS = b.maBS "
                    + "JOIN TaiKhoan t ON b.maTK = t.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "JOIN KhachHang kh ON d.maKH = kh.maKH " // Join để lấy mã khách hàng
                    + "JOIN Nguoi n ON kh.maTK = n.maTK " // Join để lấy tên từ bảng Người
                    + "WHERE t.tenTK = ? "
                    + "ORDER BY d.ngayDat DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, tenTK);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan")); // Thêm tên bệnh nhân
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang"));
                    item.put("khachDenKham", rs.getString("khachDenKham") == null ? "Chưa xác nhận" : rs.getString("khachDenKham"));
                    item.put("danhGia", rs.getString("danhGia") == null ? "" : rs.getString("danhGia"));
                    item.put("trieuChung", rs.getString("trieuChung") == null ? "Không có" : rs.getString("trieuChung")); // Thêm triệu chứng
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
