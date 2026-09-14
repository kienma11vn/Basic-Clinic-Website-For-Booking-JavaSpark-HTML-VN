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

public class XNDatLKHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. Lấy danh sách lịch khám (ChoXN và DaXN)
        get("/api/lt/danh-sach-xac-nhan", (req, res) -> {
            List<Map<String, String>> list = new ArrayList<>();
            String sql = "SELECT d.maDatLich, d.ngayDat, nBS.hoTen AS tenBacSi, nKH.hoTen AS tenBenhNhan, dv.tenDV, d.tinhTrang "
                    + "FROM DatLich d "
                    + "JOIN BacSi b ON d.maBS = b.maBS JOIN Nguoi nBS ON b.maTK = nBS.maTK "
                    + "JOIN KhachHang k ON d.maKH = k.maKH JOIN Nguoi nKH ON k.maTK = nKH.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE d.tinhTrang IN ('ChoXN', 'DaXN') ORDER BY d.ngayDat DESC";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang").trim());
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 2. Kiểm tra khả dụng (Bác sĩ không trùng lịch cùng giờ)
        get("/api/lt/kiem-tra-kha-dung/:maDL", (req, res) -> {
            String maDL = req.params(":maDL");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT maBS, ngayDat FROM DatLich WHERE maDatLich = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, maDL);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String checkSql = "SELECT COUNT(*) FROM DatLich WHERE maBS = ? AND ngayDat = ? AND maDatLich != ? AND tinhTrang = 'DaXN'";
                    PreparedStatement psCheck = conn.prepareStatement(checkSql);
                    psCheck.setString(1, rs.getString("maBS"));
                    psCheck.setTimestamp(2, rs.getTimestamp("ngayDat"));
                    psCheck.setString(3, maDL);
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        return "{\"status\":\"unavailable\", \"message\":\"Bác sĩ đã có lịch khác được xác nhận vào giờ này!\"}";
                    }
                }
                return "{\"status\":\"available\"}";
            }
        });

        // 3 & 4. Xác nhận đặt lịch và Lưu mã NV Lễ tân
        post("/api/lt/xac-nhan-dat-lich", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maDL = data.get("maDatLich");
            String tenTK_LT = data.get("username"); // Tên tài khoản lễ tân đang đăng nhập

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false);
                try {
                    // Lấy maNV từ tenTK
                    String sqlNV = "SELECT maNV FROM LeTan l JOIN TaiKhoan t ON l.maTK = t.maTK WHERE t.tenTK = ?";
                    PreparedStatement psNV = conn.prepareStatement(sqlNV);
                    psNV.setString(1, tenTK_LT);
                    ResultSet rsNV = psNV.executeQuery();
                    if (!rsNV.next()) {
                        return "{\"status\":\"error\", \"message\":\"Không tìm thấy mã nhân viên\"}";
                    }
                    String maNV = rsNV.getString("maNV");

                    // Cập nhật trạng thái DatLich
                    String sqlUp = "UPDATE DatLich SET tinhTrang = 'DaXN' WHERE maDatLich = ? AND tinhTrang = 'ChoXN'";
                    PreparedStatement psUp = conn.prepareStatement(sqlUp);
                    psUp.setString(1, maDL);
                    int updated = psUp.executeUpdate();

                    if (updated > 0) {
                        // Ghi nhận vào bảng XacNhan
                        String sqlXN = "IF EXISTS (SELECT 1 FROM XacNhan WHERE maDatLich = ?) "
                                + "UPDATE XacNhan SET xnDat = ? WHERE maDatLich = ? "
                                + "ELSE INSERT INTO XacNhan (maDatLich, xnDat) VALUES (?, ?)";
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
                        return "{\"status\":\"error\", \"message\":\"Lịch đã được xác nhận trước đó hoặc không tồn tại.\"}";
                    }
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }
        });
    }
}
