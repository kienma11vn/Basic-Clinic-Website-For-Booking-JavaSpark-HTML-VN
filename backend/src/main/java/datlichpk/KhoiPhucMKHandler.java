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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.*;
import javax.mail.internet.*;
import io.github.cdimascio.dotenv.Dotenv;

public class KhoiPhucMKHandler {

    // Nạp file .env (nếu không thấy file .env sẽ không bị sập ứng dụng nếu dùng ignoreIfMissing)
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	
    // Thông tin kết nối CSDL
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASS = dotenv.get("DB_PASS");
    private static final Gson gson = new Gson();

    // 1. Tạo Thread Pool để gửi email bất đồng bộ (tối đa 5 luồng cùng lúc)
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);

    // Lưu trữ mã OTP tạm thời: Map<Email, String[Mã OTP, Thời gian hết hạn]>
    private static Map<String, String[]> otpCache = new HashMap<>();

    public static void initRoutes() {

        // BƯỚC 1: Kiểm tra thông tin và gửi mã OTP (Bất đồng bộ)
        post("/api/gui-otp", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, String> data = gson.fromJson(req.body(), Map.class);
                String tenTK = data.get("tenTK");
                String email = data.get("email");

                // Kiểm tra tài khoản và email có khớp trong DB không
                try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                    String sql = "SELECT * FROM TaiKhoan WHERE tenTK = ? AND emailDK = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, tenTK);
                    ps.setString(2, email);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        // Tạo mã OTP ngẫu nhiên 6 số
                        String otp = String.valueOf(new Random().nextInt(899999) + 100000);

                        // Lưu OTP vào cache (hết hạn sau 5 phút)
                        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);
                        otpCache.put(email, new String[]{otp, String.valueOf(expiryTime)});

                        // GỌI GỬI EMAIL TRONG LUỒNG RIÊNG (Không đợi kết quả)
                        sendEmailAsync(email, otp);

                        return "{\"status\":\"success\", \"message\":\"Mã xác thực đã được gửi! Vui lòng kiểm tra email của bạn.\"}";
                    } else {
                        res.status(400);
                        return "{\"status\":\"error\", \"message\":\"Tên đăng nhập hoặc Email không đúng!\"}";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}";
            }
        });

        // BƯỚC 2: Xác nhận mã OTP
        post("/api/xac-nhan-otp", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String email = data.get("email");
            String otpInput = data.get("otp");

            if (otpCache.containsKey(email)) {
                String[] otpData = otpCache.get(email);
                String correctOtp = otpData[0];
                long expiryTime = Long.parseLong(otpData[1]);

                if (System.currentTimeMillis() > expiryTime) {
                    otpCache.remove(email);
                    return "{\"status\":\"error\", \"message\":\"Mã OTP đã hết hạn!\"}";
                }

                if (correctOtp.equals(otpInput)) {
                    return "{\"status\":\"success\"}";
                }
            }
            return "{\"status\":\"error\", \"message\":\"Mã OTP không chính xác!\"}";
        });

        // BƯỚC 3: Đổi mật khẩu mới
        post("/api/doi-mat-khau-moi", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = gson.fromJson(req.body(), Map.class);
            String tenTK = data.get("tenTK");
            String newPass = data.get("newPass");

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, newPass);
                ps.setString(2, tenTK);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    return "{\"status\":\"success\"}";
                } else {
                    return "{\"status\":\"error\", \"message\":\"Không tìm thấy tài khoản để cập nhật!\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"status\":\"error\", \"message\":\"Lỗi DB: " + e.getMessage() + "\"}";
            }
        });
    }

    // Hàm gửi email bất đồng bộ sử dụng Thread Pool
    private static void sendEmailAsync(String toEmail, String otp) {
        emailExecutor.submit(() -> {
            final String fromEmail = dotenv.get("EMAIL_SENDER");
            final String appPassword = dotenv.get("EMAIL_SENDER_APP_PASS");

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            // Cấu hình Timeout cực kỳ quan trọng
            props.put("mail.smtp.connectiontimeout", "60000"); // 60 giây kết nối
            props.put("mail.smtp.timeout", "60000");           // 60 giây chờ phản hồi
            props.put("mail.smtp.writetimeout", "60000");      // 60 giây gửi dữ liệu

            // Ép Java ưu tiên dùng IPv4 (rất quan trọng nếu mạng của bạn không ổn định với IPv6)
            System.setProperty("java.net.preferIPv4Stack", "true");

            // Khắc phục lỗi tương thích TLS trên Java mới
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, appPassword);
                }
            });

            // Bật debug để xem chi tiết quá trình bắt tay với server trong console
            session.setDebug(true);
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Ma OTP khoi phuc mat khau - An Tam Clinic");
                message.setText("Ma xác thuc cua ban là: " + otp);

                Transport.send(message);
                System.out.println("=> [EMAIL SENT SUCCESS] tới: " + toEmail);
            } catch (Exception e) {
                System.err.println("=> [EMAIL SENT FAILED]: " + e.getMessage());
                // In ra nguyên nhân gốc nếu có
                if (e.getCause() != null) {
                    System.err.println("Cause: " + e.getCause());
                }
            }
        });
    }
}
