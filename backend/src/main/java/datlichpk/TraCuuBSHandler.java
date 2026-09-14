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

public class TraCuuBSHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static Gson gson = new Gson();

    public static void initRoutes() {
        // API 1: Lấy danh sách bác sĩ (có lọc)
        get("/api/bac-si", (req, res) -> {
            List<Map<String, Object>> list = new ArrayList<>();
            String name = req.queryParams("name");
            String ck = req.queryParams("ck");
            String cd = req.queryParams("cd");
            String tn = req.queryParams("tn");
            String gt = req.queryParams("gt");

            String sql = "SELECT B.maBS, N.hoTen, B.tenCK, B.chucDanh, B.thamNien "
                    + "FROM BacSi B JOIN Nguoi N ON B.maTK = N.maTK WHERE 1=1";

            if (name != null && !name.isEmpty()) {
                sql += " AND N.hoTen LIKE N'%" + name + "%'";
            }
            if (ck != null && !ck.isEmpty()) {
                sql += " AND B.tenCK LIKE N'%" + ck + "%'";
            }
            if (cd != null && !cd.isEmpty()) {
                sql += " AND B.chucDanh LIKE N'%" + cd + "%'";
            }
            if (tn != null && !tn.isEmpty()) {
                sql += " AND B.thamNien = " + tn;
            }
            if (gt != null && !gt.isEmpty()) {
                sql += " AND N.gioiTinh = N'" + gt + "'";
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("maBS", rs.getString("maBS"));
                    item.put("hoTen", rs.getString("hoTen"));
                    item.put("tenCK", rs.getString("tenCK"));
                    item.put("chucDanh", rs.getString("chucDanh"));
                    item.put("thamNien", rs.getInt("thamNien"));
                    list.add(item);
                }
            }
            return gson.toJson(list);
        });

        // API 2: Lấy chi tiết 1 bác sĩ khi Click
        get("/api/bac-si/:id", (req, res) -> {
            String id = req.params(":id");
            Map<String, Object> item = new HashMap<>();
            String sql = "SELECT B.maBS, N.hoTen, B.tenCK, B.chucDanh, B.thamNien, N.ngaySinh, N.SDT, N.gioiTinh "
                    + "FROM BacSi B JOIN Nguoi N ON B.maTK = N.maTK WHERE B.maBS = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    item.put("maBS", rs.getString("maBS"));
                    item.put("hoTen", rs.getString("hoTen"));
                    item.put("tenCK", rs.getString("tenCK"));
                    item.put("chucDanh", rs.getString("chucDanh"));
                    item.put("thamNien", rs.getInt("thamNien"));
                    item.put("ngaySinh", rs.getString("ngaySinh"));
                    item.put("SDT", rs.getString("SDT"));
                    item.put("gioiTinh", rs.getString("gioiTinh"));
                }
            }
            return gson.toJson(item);
        });
    }
}
