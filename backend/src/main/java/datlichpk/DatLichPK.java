/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
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

public class DatLichPK {

	// Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS"); // Mật khẩu SQL của bạn
    private static final int PORT = Integer.parseInt(dotenv.get("PORT", "9999")); 

    public static void main(String[] args) {
        port(PORT); // Chạy server tại port từ .env

        // Cấu hình CORS để Frontend có thể gọi API
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            response.type("application/json"); // Đảm bảo mọi phản hồi là JSON
        });

        Gson gson = new Gson();

        // Chạy các chức năng
        DangKyLichHandler.initRoutes();
        XemLichKHHandler.initRoutes();
        SuaLichKHHandler.initRoutes();
        KhoiPhucMKHandler.initRoutes();
        DoiMKHandler.initRoutes();
        HuyLichKHHandler.initRoutes();
        LienHeHoTroHandler.initRoutes();
        TraCuuBSHandler.initRoutes();
        TraCuuDVHandler.initRoutes();
        ThongTinKHHandler.initRoutes();
        XNDatLKHandler.initRoutes();
        XNDenKhamHandler.initRoutes();
        XNHuyLKHandler.initRoutes();
        XemTTLichKhamHandler.initRoutes();
        QLKhachHangHandler.initRoutes();
        QLDichVuHandler.initRoutes();
        QLLichKhamHandler.initRoutes();
        QLBacSiHandler.initRoutes();
        XemLichBSHandler.initRoutes();
        XemBenhNhanHandler.initRoutes();
        HoTroKHBSHandler.initRoutes();
        ThongKeNgayHandler.initRoutes();
        ThongKeThangHandler.initRoutes();
        ThongKeNamHandler.initRoutes();

        // --- CHỨC NĂNG ĐĂNG KÝ ---
        post("/api/dang-ky", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, String> data = gson.fromJson(req.body(), Map.class);
                String tenTK = data.get("tenTK");
                String matKhau = data.get("matKhau");
                String emailDK = data.get("email");

                // Tạo một mã chung cho cả maTK và maKH
                String maDinhDanh = "KH" + System.currentTimeMillis();

                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    // Tắt chế độ AutoCommit để thực hiện Transaction
                    conn.setAutoCommit(false);

                    try {
                        // 1. Chèn vào bảng TaiKhoan
                        String sqlTK = "INSERT INTO TaiKhoan (maTK, tenTK, matKhau, loaiTK, emailDK) VALUES (?, ?, ?, 'KH', ?)";
                        PreparedStatement pstmtTK = conn.prepareStatement(sqlTK);
                        pstmtTK.setString(1, maDinhDanh);
                        pstmtTK.setString(2, tenTK);
                        pstmtTK.setString(3, matKhau);
                        pstmtTK.setString(4, emailDK);
                        pstmtTK.executeUpdate();

                        // 2. Chèn vào bảng KhachHang (maKH dùng chung mã với maTK)
                        String sqlKH = "INSERT INTO KhachHang (maKH, maTK) VALUES (?, ?)";
                        PreparedStatement pstmtKH = conn.prepareStatement(sqlKH);
                        pstmtKH.setString(1, maDinhDanh); // maKH
                        pstmtKH.setString(2, maDinhDanh); // maTK tham chiếu
                        pstmtKH.executeUpdate();

                        // Nếu cả 2 lệnh trên thành công, xác nhận lưu vào DB
                        conn.commit();
                        return "{\"status\":\"success\"}";

                    } catch (SQLException e) {
                        // Nếu có bất kỳ lỗi nào, hoàn tác lại toàn bộ (không lưu gì cả)
                        conn.rollback();
                        throw e;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // --- CHỨC NĂNG ĐĂNG NHẬP ---
        post("/api/dang-nhap", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String tenTK = data.get("tenTK");
            String matKhau = data.get("matKhau");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Truy vấn lấy loaiTK từ bảng TaiKhoan
                String sql = "SELECT loaiTK FROM TaiKhoan WHERE tenTK = ? AND matKhau = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tenTK);
                pstmt.setString(2, matKhau);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String loaiTK = rs.getString("loaiTK");
                    // Trả về JSON bao gồm cả vai trò
                    return "{\"status\":\"success\", \"loaiTK\":\"" + loaiTK + "\", \"username\":\"" + tenTK + "\"}";
                } else {
                    res.status(401);
                    return "{\"status\":\"error\", \"message\":\"Sai tài khoản hoặc mật khẩu\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
            }
        });

        // --- API Lấy danh sách lịch khám đã đến (khachDenKham = 'Yes') ---
        get("/api/kh/lich-da-kham", (req, res) -> {
            res.type("application/json");
            String tenTK = req.queryParams("tenTK");
            List<Map<String, Object>> list = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // SQL mới: JOIN thêm bảng DichVu để lấy tenDV
                String sql = "SELECT DL.maDatLich, N.hoTen as tenBacSi, DV.tenDV, DL.ngayDat, DL.khachDenKham, DL.danhGia "
                        + "FROM DatLich DL "
                        + "JOIN BacSi BS ON DL.maBS = BS.maBS "
                        + "JOIN Nguoi N ON BS.maTK = N.maTK "
                        + "JOIN DichVu DV ON DL.maDV = DV.maDV " // Thêm dòng này
                        + "JOIN KhachHang KH ON DL.maKH = KH.maKH "
                        + "JOIN TaiKhoan TK ON KH.maTK = TK.maTK "
                        + "WHERE TK.tenTK = ? AND DL.khachDenKham = 'Yes'";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tenTK);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maDatLich", rs.getString("maDatLich"));
                    item.put("tenBacSi", rs.getString("tenBacSi"));
                    item.put("tenDV", rs.getString("tenDV")); // Thêm thông tin dịch vụ
                    item.put("ngayDat", rs.getString("ngayDat"));
                    item.put("khachDenKham", rs.getString("khachDenKham"));
                    item.put("danhGia", rs.getString("danhGia"));
                    list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "[]";
            }
            return gson.toJson(list);
        });

// --- API Lưu đánh giá (Cập nhật cột danhGia) ---
        post("/api/kh/luu-danh-gia", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String maDL = data.get("maDatLich");
            String noiDungMoi = data.get("noiDungMoi"); // Chuỗi đã bao gồm tiền tố từ JS

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                // Trước tiên lấy dữ liệu cũ để tránh ghi đè mất loại đánh giá kia
                String selectSql = "SELECT danhGia FROM DatLich WHERE maDatLich = ?";
                PreparedStatement pSelect = conn.prepareStatement(selectSql);
                pSelect.setString(1, maDL);
                ResultSet rs = pSelect.executeQuery();

                String danhGiaHienTai = "";
                if (rs.next()) {
                    danhGiaHienTai = rs.getString("danhGia");
                    if (danhGiaHienTai == null) {
                        danhGiaHienTai = "";
                    }
                }

                // Logic gộp chuỗi: Nếu đã có đánh giá khác thì cộng dồn, nếu cùng loại thì cập nhật
                // (Để đơn giản, JS sẽ gửi chuỗi định dạng sẵn, Java chỉ việc cập nhật)
                String updateSql = "UPDATE DatLich SET danhGia = ? WHERE maDatLich = ?";
                PreparedStatement pUpdate = conn.prepareStatement(updateSql);
                pUpdate.setString(1, noiDungMoi);
                pUpdate.setString(2, maDL);

                int rowAffected = pUpdate.executeUpdate();
                if (rowAffected > 0) {
                    return "{\"status\":\"success\"}";
                } else {
                    return "{\"status\":\"fail\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        });
    }
}
