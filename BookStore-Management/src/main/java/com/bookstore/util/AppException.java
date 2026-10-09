package com.bookstore.util;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Ngoại lệ chung của ứng dụng; message luôn là tiếng Việt, hiển thị trực tiếp cho người dùng. */
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }

    public static AppException of(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String m = String.valueOf(e.getMessage());
            if (m.contains("Duplicate")) return new AppException("Dữ liệu bị trùng (mã hoặc giá trị duy nhất đã tồn tại).", e);
            return new AppException("Dữ liệu đang được sử dụng ở nơi khác (đã có hóa đơn/liên kết), không thể thực hiện.", e);
        }
        return new AppException("Lỗi cơ sở dữ liệu: " + e.getMessage(), e);
    }
}
