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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import io.github.cdimascio.dotenv.Dotenv;

public class XNHuyLKHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. API lấy danh sách lịch yêu cầu hủy
        get("/api/lt/danh-sach-huy-lich", (req, res) -> {
            List<Map<String, String>> list = new ArrayList<>();
            String sql = "SELECT d.maDatLich, d.ngayDat, nBS.hoTen AS tenBacSi, nKH.hoTen AS tenBenhNhan, dv.tenDV, d.tinhTrang "
                    + "FROM DatLich d "
                    + "JOIN BacSi b ON d.maBS = b.maBS JOIN Nguoi nBS ON b.maTK = nBS.maTK "
                    + "JOIN KhachHang k ON d.maKH = k.maKH JOIN Nguoi nKH ON k.maTK = nKH.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV "
                    + "WHERE d.tinhTrang IN ('Huy', 'XNHuy', 'KHuy') ORDER BY d.ngayDat DESC";
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, String> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 2. API Kiểm tra tính hợp lệ (trước 6 tiếng)
        get("/api/lt/kiem-tra-hop-le-huy", (req, res) -> {
            String maDL = req.queryParams("maDatLich");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "SELECT ngayDat FROM DatLich WHERE maDatLich = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, maDL);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String ngayDatStr = rs.getString("ngayDat");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                    LocalDateTime thoiGianHen = LocalDateTime.parse(ngayDatStr, formatter);
                    LocalDateTime bayGio = LocalDateTime.now();

                    long hours = ChronoUnit.HOURS.between(bayGio, thoiGianHen);
                    if (hours >= 6) {
                        return "{\"status\":\"valid\", \"message\":\"Lịch khám hợp lệ để hủy (còn " + hours + " tiếng).\"}";
                    } else {
                        return "{\"status\":\"invalid\", \"message\":\"Không đủ điều kiện hủy (phải trước ít nhất 6 tiếng).\"}";
                    }
                }
            }
            return "{\"status\":\"error\"}";
        });

        // 3. API Xử lý Hủy (XNHuy) hoặc Từ chối (KHuy) - ĐÃ SỬA ĐỂ GIỐNG XNDatLKHandler
        post("/api/lt/xac-nhan-huy-lich", (req, res) -> {
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maDL = data.get("maDatLich");
            String loaiXN = data.get("loaiXN"); // 'XNHuy' hoặc 'KHuy'
            String tenTK_LT = data.get("username"); // Lấy username từ frontend gửi lên

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false);
                try {
                    // --- BƯỚC MỚI: Lấy maNV từ tenTK (Giống XNDatLKHandler) ---
                    String sqlNV = "SELECT maNV FROM LeTan l JOIN TaiKhoan t ON l.maTK = t.maTK WHERE t.tenTK = ?";
                    PreparedStatement psNV = conn.prepareStatement(sqlNV);
                    psNV.setString(1, tenTK_LT);
                    ResultSet rsNV = psNV.executeQuery();

                    if (!rsNV.next()) {
                        return "{\"status\":\"error\", \"message\":\"Không tìm thấy mã nhân viên lễ tân.\"}";
                    }
                    String maNV = rsNV.getString("maNV");

                    // Kiểm tra trạng thái hiện tại (Chỉ xử lý nếu đang là 'Huy')
                    String checkSql = "SELECT tinhTrang FROM DatLich WHERE maDatLich = ?";
                    PreparedStatement psCheck = conn.prepareStatement(checkSql);
                    psCheck.setString(1, maDL);
                    ResultSet rs = psCheck.executeQuery();

                    if (rs.next() && !rs.getString("tinhTrang").equals("Huy")) {
                        return "{\"status\":\"error\", \"message\":\"Lịch này đã được xử lý hoặc không ở trạng thái chờ hủy!\"}";
                    }

                    // Cập nhật bảng DatLich
                    String sqlUp = "UPDATE DatLich SET tinhTrang = ? WHERE maDatLich = ?";
                    PreparedStatement psUp = conn.prepareStatement(sqlUp);
                    psUp.setString(1, loaiXN);
                    psUp.setString(2, maDL);
                    psUp.executeUpdate();

                    // Cập nhật/Chèn vào bảng XacNhan (Cột xnHuy dùng maNV thực tế)
                    String sqlXN = "IF EXISTS (SELECT 1 FROM XacNhan WHERE maDatLich = ?) "
                            + "UPDATE XacNhan SET xnHuy = ? WHERE maDatLich = ? "
                            + "ELSE INSERT INTO XacNhan (maDatLich, xnHuy) VALUES (?, ?)";
                    PreparedStatement psXN = conn.prepareStatement(sqlXN);
                    psXN.setString(1, maDL);
                    psXN.setString(2, maNV); // Đã đổi từ username sang maNV
                    psXN.setString(3, maDL);
                    psXN.setString(4, maDL);
                    psXN.setString(5, maNV); // Đã đổi từ username sang maNV
                    psXN.executeUpdate();

                    conn.commit();
                    return "{\"status\":\"success\"}";
                } catch (Exception e) {
                    conn.rollback();
                    e.printStackTrace();
                    return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
                }
            }
        });
    }
}
