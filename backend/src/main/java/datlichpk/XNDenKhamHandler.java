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

public class XNDenKhamHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. API Tự động chuyển khachDenKham sang 'No' nếu quá 30 phút
        // Nên được gọi mỗi khi load trang hoặc định kỳ
        get("/api/lt/cap-nhat-qua-han", (req, res) -> {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "UPDATE DatLich SET khachDenKham = 'No' "
                        + "WHERE khachDenKham IS NULL "
                        + "AND DATEADD(MINUTE, 45, ngayDat) < GETDATE()";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int rows = ps.executeUpdate();
                    return "{\"status\":\"success\", \"updated\":" + rows + "}";
                }
            }
        });

        // 2. API Lấy danh sách lịch khám (Điều kiện: tinhTrang = 'DaXN')
        get("/api/lt/danh-sach-den-kham", (req, res) -> {
            List<Map<String, String>> list = new ArrayList<>();
            String sql = "SELECT d.maDatLich, d.ngayDat, nBS.hoTen AS tenBacSi, nKH.hoTen AS tenBenhNhan, dv.tenDV, d.khachDenKham "
                    + "FROM DatLich d "
                    + "JOIN BacSi b ON d.maBS = b.maBS JOIN Nguoi nBS ON b.maTK = nBS.maTK "
                    + "JOIN KhachHang k ON d.maKH = k.maKH JOIN Nguoi nKH ON k.maTK = nKH.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE d.tinhTrang = 'DaXN' ORDER BY d.ngayDat DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan"));
                    item.put("tenDV", rs.getString("tenDV"));
                    String kdk = rs.getString("khachDenKham");
                    item.put("khachDenKham", kdk == null ? "NULL" : kdk.trim());
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 3. API Xác nhận khách đã đến (Chuyển sang 'Yes' và lưu mã NV)
        post("/api/lt/xac-nhan-den-kham", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maDL = data.get("maDatLich");
            String usernameLT = data.get("username");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false);
                try {
                    // Lấy mã nhân viên lễ tân
                    String sqlNV = "SELECT maNV FROM LeTan l JOIN TaiKhoan t ON l.maTK = t.maTK WHERE t.tenTK = ?";
                    PreparedStatement psNV = conn.prepareStatement(sqlNV);
                    psNV.setString(1, usernameLT);
                    ResultSet rsNV = psNV.executeQuery();
                    if (!rsNV.next()) {
                        return "{\"status\":\"error\", \"message\":\"Không tìm thấy Lễ tân\"}";
                    }
                    String maNV = rsNV.getString("maNV");

                    // Cập nhật khachDenKham = 'Yes' (Chỉ khi đang NULL)
                    String sqlUp = "UPDATE DatLich SET khachDenKham = 'Yes' WHERE maDatLich = ? AND khachDenKham IS NULL";
                    PreparedStatement psUp = conn.prepareStatement(sqlUp);
                    psUp.setString(1, maDL);
                    int updated = psUp.executeUpdate();

                    if (updated > 0) {
                        // Lưu vào cột xnDen của bảng XacNhan
                        String sqlXN = "IF EXISTS (SELECT 1 FROM XacNhan WHERE maDatLich = ?) "
                                + "UPDATE XacNhan SET xnDen = ? WHERE maDatLich = ? "
                                + "ELSE INSERT INTO XacNhan (maDatLich, xnDen) VALUES (?, ?)";
                        PreparedStatement psXN = conn.prepareStatement(sqlXN);
                        psXN.setString(1, maDL);
                        psXN.setString(2, maNV);
                        psXN.setString(3, maDL);
                        psXN.setString(4, maDL);
                        psXN.setString(5, maNV);
                        psXN.executeUpdate();

                        conn.commit();
                        return "{\"status\":\"success\"}";
                    } else {
                        return "{\"status\":\"error\", \"message\":\"Lịch này đã được xác nhận hoặc đã quá hạn.\"}";
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
        });
    }
}
