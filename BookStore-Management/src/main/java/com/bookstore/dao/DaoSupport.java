package com.bookstore.dao;

import com.bookstore.config.MySQLConnect;
import com.bookstore.util.AppException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Tiện ích JDBC dùng chung cho các DAO: chuẩn bị câu lệnh, gán tham số, ánh xạ dòng kết quả. */
public final class DaoSupport {
    private DaoSupport() {}

    @FunctionalInterface
    public interface Mapper<T> { T map(ResultSet rs) throws SQLException; }

    private static void bind(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
    }

    public static <T> List<T> query(String sql, Mapper<T> mapper, Object... args) {
        List<T> list = new ArrayList<>();
        try (Connection c = MySQLConnect.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapper.map(rs)); }
        } catch (SQLException e) { throw AppException.of(e); }
        return list;
    }

    public static <T> T queryOne(String sql, Mapper<T> mapper, Object... args) {
        List<T> list = query(sql, mapper, args);
        return list.isEmpty() ? null : list.get(0);
    }

    /** INSERT/UPDATE/DELETE (kết nối riêng). Trả về số dòng ảnh hưởng. */
    public static int update(String sql, Object... args) {
        try (Connection c = MySQLConnect.getConnection()) { return update(c, sql, args); }
        catch (SQLException e) { throw AppException.of(e); }
    }

    /** Dùng trong giao dịch: kết nối do BUS quản lý. */
    public static int update(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) { bind(ps, args); return ps.executeUpdate(); }
    }

    /** INSERT và trả về khóa tự tăng. */
    public static int insert(String sql, Object... args) {
        try (Connection c = MySQLConnect.getConnection()) { return insert(c, sql, args); }
        catch (SQLException e) { throw AppException.of(e); }
    }

    public static int insert(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, args);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public static Integer intOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    public static Double doubleOrNull(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    public static java.time.LocalDate date(ResultSet rs, String col) throws SQLException {
        java.sql.Date d = rs.getDate(col);
        return d == null ? null : d.toLocalDate();
    }

    /** Chuỗi rỗng -> NULL để không vi phạm UNIQUE (isbn, ...) và dữ liệu sạch hơn. */
    public static String nz(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
