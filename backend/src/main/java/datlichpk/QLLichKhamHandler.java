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

public class QLLichKhamHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    public static void initRoutes() {
        // 1. Lấy danh sách hoặc Tìm kiếm lịch khám (Giống XemTTLichKham nhưng có lọc SQL)
        get("/api/admin/lich-kham", (req, res) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            String maDL = req.queryParams("maDatLich");
            String tenBN = req.queryParams("tenBenhNhan");
            String tenBS = req.queryParams("tenBacSi");
            String ngayKham = req.queryParams("ngayKham");

            StringBuilder sql = new StringBuilder(
                    "SELECT d.maDatLich, d.ngayDat, nBS.hoTen AS tenBacSi, nKH.hoTen AS tenBenhNhan, "
                    + "dv.tenDV, d.tinhTrang, d.khachDenKham, d.danhGia, d.maBS, d.maDV, d.maKH "
                    + "FROM DatLich d "
                    + "JOIN KhachHang k ON d.maKH = k.maKH JOIN Nguoi nKH ON k.maTK = nKH.maTK "
                    + "JOIN BacSi b ON d.maBS = b.maBS JOIN Nguoi nBS ON b.maTK = nBS.maTK "
                    + "JOIN DichVu dv ON d.maDV = dv.maDV WHERE 1=1"
            );

            if (maDL != null && !maDL.isEmpty()) {
                sql.append(" AND d.maDatLich LIKE ?");
            }
            if (tenBN != null && !tenBN.isEmpty()) {
                sql.append(" AND nKH.hoTen LIKE ?");
            }
            if (tenBS != null && !tenBS.isEmpty()) {
                sql.append(" AND nBS.hoTen LIKE ?");
            }

            // Sử dụng CONVERT để ép kiểu ngày về chuỗi YYYY-MM-DD trước khi so sánh
            if (ngayKham != null && !ngayKham.isEmpty()) {
                sql.append(" AND CONVERT(VARCHAR, d.ngayDat, 23) = ?");
            }

            sql.append(" ORDER BY d.ngayDat DESC");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                PreparedStatement pstmt = conn.prepareStatement(sql.toString());
                int idx = 1;
                if (maDL != null && !maDL.isEmpty()) {
                    pstmt.setString(idx++, "%" + maDL + "%");
                }
                if (tenBN != null && !tenBN.isEmpty()) {
                    pstmt.setString(idx++, "%" + tenBN + "%");
                }
                if (tenBS != null && !tenBS.isEmpty()) {
                    pstmt.setString(idx++, "%" + tenBS + "%");
                }
                if (ngayKham != null && !ngayKham.isEmpty()) {
                    pstmt.setString(idx++, ngayKham); // Giá trị từ input date đã là YYYY-MM-DD
                }

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenBenhNhan", rs.getString("tenBenhNhan"));
                    item.put("tenDV", rs.getString("tenDV"));
                    item.put("tinhTrang", rs.getString("tinhTrang"));
                    item.put("khachDenKham", rs.getString("khachDenKham"));
                    item.put("danhGia", rs.getString("danhGia"));
                    // Lưu lại các mã ID để phục vụ việc Sửa
                    item.put("maBS", rs.getString("maBS"));
                    item.put("maDV", rs.getString("maDV"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // 2. Thêm lịch khám mới
        post("/api/admin/lich-kham", (req, res) -> {
            Map<String, String> body = gson.fromJson(req.body(), Map.class);
            String maKH = body.get("maKH");
            String maBS = body.get("maBS");
            String maDV = body.get("maDV");
            String ngayKham = body.get("ngayKham"); // yyyy-MM-dd
            String gioKham = body.get("gioKham");   // HH:mm
            String trieuChung = body.get("trieuChung");

            // Gộp ngày và giờ để lưu vào cột ngayDat
            String fullDateTime = ngayKham + " " + gioKham;

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // 1. Kiểm tra trùng lịch cho Bác sĩ (Tham khảo logic DangKyLichHandler)
                String checkSql = "SELECT COUNT(*) FROM DatLich WHERE maBS = ? AND ngayDat = ? AND tinhTrang NOT IN (N'XNHuy', N'KHuy')";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, maBS);
                checkStmt.setString(2, fullDateTime);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"Bác sĩ đã có lịch vào giờ này!\"}";
                }

                // 2. Chèn dữ liệu (Sử dụng cột trieuChung có sẵn trong SQL)
                String sql = "INSERT INTO DatLich (maDatLich, maKH, maBS, maDV, ngayDat, tinhTrang, trieuChung) VALUES (?, ?, ?, ?, ?, N'DaXN', ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, "DL" + System.currentTimeMillis());
                pstmt.setString(2, maKH);
                pstmt.setString(3, maBS);
                pstmt.setString(4, maDV);
                pstmt.setString(5, fullDateTime);
                pstmt.setString(6, trieuChung);

                pstmt.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // 3. Sửa lịch khám
        put("/api/admin/lich-kham/:maDatLich", (req, res) -> {
            String maDL = req.params(":maDatLich");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);

            String maKH = data.get("maKH");
            String maBS = data.get("maBS");
            String maDV = data.get("maDV");
            String ngayDat = data.get("ngayDat"); // Định dạng "yyyy-MM-dd HH:mm"
            String trieuChung = data.get("trieuChung");
            String tinhTrang = data.get("tinhTrang");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // 1. Kiểm tra xung đột lịch cho Bác sĩ (trừ lịch hiện tại)
                String checkSql = "SELECT COUNT(*) FROM DatLich WHERE maBS = ? AND ngayDat = ? AND maDatLich <> ? AND tinhTrang NOT IN (N'XNHuy', N'KHuy')";
                PreparedStatement psCheck = conn.prepareStatement(checkSql);
                psCheck.setString(1, maBS);
                psCheck.setString(2, ngayDat);
                psCheck.setString(3, maDL);
                ResultSet rs = psCheck.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    res.status(400);
                    return "{\"status\":\"error\", \"message\":\"Bác sĩ đã có lịch khác vào thời gian này!\"}";
                }

                // 2. Cập nhật đa thông tin
                String sql = "UPDATE DatLich SET maKH = ?, maBS = ?, maDV = ?, ngayDat = ?, trieuChung = ?, tinhTrang = ? WHERE maDatLich = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, maKH);
                pstmt.setString(2, maBS);
                pstmt.setString(3, maDV);
                pstmt.setString(4, ngayDat);
                pstmt.setString(5, trieuChung);
                pstmt.setString(6, tinhTrang);
                pstmt.setString(7, maDL);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    return "{\"status\":\"success\"}";
                } else {
                    res.status(404);
                    return "{\"status\":\"error\", \"message\":\"Không tìm thấy lịch khám cần sửa\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // 4. Xóa lịch khám
        delete("/api/admin/lich-kham/:id", (req, res) -> {
            String maDL = req.params(":id");
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Lưu ý: Cần xóa dữ liệu liên quan ở bảng XacNhan trước nếu có ràng buộc FK
                PreparedStatement p1 = conn.prepareStatement("DELETE FROM XacNhan WHERE maDatLich=?");
                p1.setString(1, maDL);
                p1.executeUpdate();

                PreparedStatement p2 = conn.prepareStatement("DELETE FROM DatLich WHERE maDatLich=?");
                p2.setString(1, maDL);
                p2.executeUpdate();
                return "{\"status\":\"success\"}";
            } catch (Exception e) {
                return "{\"status\":\"error\"}";
            }
        });

        // 1. API: Lấy danh sách Khách hàng và Chuyên khoa (Dùng khi mở Modal thêm mới)
        get("/api/admin/form-init-data", (req, res) -> {
            Map<String, Object> data = new HashMap<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Lấy danh sách bệnh nhân: JOIN KhachHang với Nguoi để lấy HoTen
                List<Map<String, String>> customers = new ArrayList<>();
                String sqlKH = "SELECT k.maKH, n.hoTen FROM KhachHang k JOIN Nguoi n ON k.maTK = n.maTK";
                ResultSet rsKH = conn.createStatement().executeQuery(sqlKH);
                while (rsKH.next()) {
                    Map<String, String> kh = new HashMap<>();
                    kh.put("maKH", rsKH.getString("maKH"));
                    kh.put("hoTen", rsKH.getString("hoTen"));
                    customers.add(kh);
                }
                data.put("customers", customers);

                // Lấy danh sách chuyên khoa từ bảng DichVu
                List<String> specialties = new ArrayList<>();
                ResultSet rsCK = conn.createStatement().executeQuery("SELECT DISTINCT tenCK FROM DichVu");
                while (rsCK.next()) {
                    specialties.add(rsCK.getString("tenCK"));
                }
                data.put("specialties", specialties);
            }
            return gson.toJson(data);
        });

        // 2. API: Lấy Bác sĩ và Dịch vụ theo Chuyên khoa (Cascading Dropdown)
        get("/api/admin/details-by-specialty", (req, res) -> {
            String ck = req.queryParams("specialty");
            Map<String, Object> data = new HashMap<>();
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Lấy Bác sĩ: JOIN BacSi với Nguoi để lấy HoTen theo Chuyên khoa
                List<Map<String, String>> doctors = new ArrayList<>();
                String sqlBS = "SELECT b.maBS, n.hoTen FROM BacSi b JOIN Nguoi n ON b.maTK = n.maTK WHERE b.tenCK = ?";
                PreparedStatement psBS = conn.prepareStatement(sqlBS);
                psBS.setString(1, ck);
                ResultSet rsBS = psBS.executeQuery();
                while (rsBS.next()) {
                    Map<String, String> bs = new HashMap<>();
                    bs.put("maBS", rsBS.getString("maBS"));
                    bs.put("hoTen", rsBS.getString("hoTen"));
                    doctors.add(bs);
                }
                data.put("doctors", doctors);

                // Lấy Dịch vụ thuộc chuyên khoa
                List<Map<String, String>> services = new ArrayList<>();
                PreparedStatement psDV = conn.prepareStatement("SELECT maDV, tenDV FROM DichVu WHERE tenCK = ?");
                psDV.setString(1, ck);
                ResultSet rsDV = psDV.executeQuery();
                while (rsDV.next()) {
                    Map<String, String> dv = new HashMap<>();
                    dv.put("maDV", rsDV.getString("maDV"));
                    dv.put("tenDV", rsDV.getString("tenDV"));
                    services.add(dv);
                }
                data.put("services", services);
            }
            return gson.toJson(data);
        });
    }
}
