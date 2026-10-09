package com.bookstore.config;

import com.bookstore.util.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Đọc cấu hình từ db.properties và cấp kết nối MySQL bằng JDBC. */
public class MySQLConnect {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = MySQLConnect.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) throw new IllegalStateException("Không tìm thấy file db.properties");
            PROPS.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được db.properties", e);
        }
    }

    private MySQLConnect() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password", ""));
    }

    /** Kiểm tra kết nối, ném AppException nếu lỗi. */
    public static void test() {
        try (Connection c = getConnection()) {
            c.isValid(2);
        } catch (SQLException e) {
            throw new AppException("Không kết nối được MySQL: " + e.getMessage()
                    + "\nHãy kiểm tra db.properties và đã chạy database/bookstore.sql chưa.");
        }
    }
}
