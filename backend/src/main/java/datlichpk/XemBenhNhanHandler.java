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

public class XemBenhNhanHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");

    public static void initRoutes() {
        Gson gson = new Gson();

        get("/api/bs/danh-sach-benh-nhan", (req, res) -> {
            res.type("application/json");
            String tenTKBS = req.queryParams("tenTK"); // Tên tài khoản bác sĩ đang đăng nhập

            // Lấy tham số tìm kiếm (nếu có)
            String searchTen = req.queryParamOrDefault("hoTen", "");
            String searchNgay = req.queryParamOrDefault("ngaySinh", "");
            String searchGioiTinh = req.queryParamOrDefault("gioiTinh", "");

            List<Map<String, Object>> list = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Truy vấn kết hợp: Bác sĩ -> Đặt Lịch -> Khách hàng -> Người
                // Điều kiện: tinhTrang = 'DaXN' AND khachDenKham != 'No'
                String sql = "SELECT DISTINCT N.hoTen, N.ngaySinh, N.gioiTinh, N.SDT, TK.emailDK "
                        + "FROM DatLich DL "
                        + "JOIN BacSi BS ON DL.maBS = BS.maBS "
                        + "JOIN TaiKhoan TKBS ON BS.maTK = TKBS.maTK "
                        + "JOIN KhachHang KH ON DL.maKH = KH.maKH "
                        + "JOIN Nguoi N ON KH.maTK = N.maTK "
                        + "JOIN TaiKhoan TK ON KH.maTK = TK.maTK "
                        + "WHERE TKBS.tenTK = ? "
                        + "AND DL.tinhTrang = N'DaXN' "
                        + "AND (DL.khachDenKham IS NULL OR DL.khachDenKham != 'No') ";

                // Thêm bộ lọc tìm kiếm động
                if (!searchTen.isEmpty()) {
                    sql += "AND N.hoTen LIKE ? ";
                }
                if (!searchNgay.isEmpty()) {
                    sql += "AND CONVERT(DATE, N.ngaySinh) = ? ";
                }
                if (!searchGioiTinh.isEmpty()) {
                    sql += "AND N.gioiTinh = ? ";
                }

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tenTKBS);

                int paramIdx = 2;
                if (!searchTen.isEmpty()) {
                    pstmt.setString(paramIdx++, "%" + searchTen + "%");
                }
                if (!searchNgay.isEmpty()) {
                    pstmt.setString(paramIdx++, searchNgay);
                }
                if (!searchGioiTinh.isEmpty()) {
                    pstmt.setString(paramIdx++, searchGioiTinh);
                }

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("hoTen", rs.getString("hoTen"));
                    item.put("ngaySinh", rs.getString("ngaySinh"));
                    item.put("gioiTinh", rs.getString("gioiTinh"));
                    item.put("sdt", rs.getString("SDT"));
                    item.put("email", rs.getString("emailDK"));
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "[]";
            }
            return gson.toJson(list);
        });
    }
}
