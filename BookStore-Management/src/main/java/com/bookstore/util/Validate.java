package com.bookstore.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Các hàm kiểm tra dữ liệu đầu vào dùng chung (ném AppException tiếng Việt khi sai). */
public final class Validate {
    private Validate() {}

    public static boolean blank(String s) { return s == null || s.isBlank(); }

    public static void required(String value, String field) {
        if (blank(value)) throw new AppException(field + " không được để trống.");
    }

    /** Số điện thoại 10 số bắt đầu bằng 0 (cột CHAR(10)); rỗng là hợp lệ. */
    public static void phone(String s, String field) {
        if (!blank(s) && !s.trim().matches("0\\d{9}")) throw new AppException(field + " phải gồm đúng 10 chữ số và bắt đầu bằng 0.");
    }

    public static void email(String s) {
        if (!blank(s) && !s.trim().matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) throw new AppException("Email không hợp lệ.");
    }

    public static void maxLen(String s, int max, String field) {
        if (s != null && s.trim().length() > max) throw new AppException(field + " tối đa " + max + " ký tự.");
    }

    public static LocalDate date(String s, String field) {
        if (blank(s)) return null;
        try { return LocalDate.parse(s.trim()); }
        catch (DateTimeParseException e) { throw new AppException(field + " không hợp lệ, định dạng đúng: yyyy-MM-dd (vd 2026-10-04)."); }
    }
}
