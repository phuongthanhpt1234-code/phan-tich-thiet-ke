package com.bookstore.dao;

import com.bookstore.model.OrderItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class OrderItemDAO {
    private static final DaoSupport.Mapper<OrderItem> MAP = rs -> {
        OrderItem d = new OrderItem(rs.getInt("book_id"), rs.getString("title"), rs.getInt("quantity"), rs.getDouble("unit_price"));
        d.setOrderItemId(rs.getInt("order_item_id")); d.setOrderId(rs.getInt("order_id"));
        return d;
    };

    public void insert(Connection c, OrderItem d) throws SQLException {
        d.setOrderItemId(DaoSupport.insert(c, "INSERT INTO Order_Items(order_id, book_id, quantity, unit_price) VALUES (?,?,?,?)",
                d.getOrderId(), d.getBookId(), d.getQuantity(), d.getUnitPrice()));
    }

    public List<OrderItem> selectByOrder(int orderId) {
        return DaoSupport.query("SELECT d.*, b.title FROM Order_Items d JOIN Books b ON d.book_id = b.book_id WHERE d.order_id=? ORDER BY d.order_item_id", MAP, orderId);
    }

    public List<OrderItem> selectByOrder(Connection c, int orderId) throws SQLException {
        try (java.sql.PreparedStatement ps = c.prepareStatement("SELECT d.*, b.title FROM Order_Items d JOIN Books b ON d.book_id = b.book_id WHERE d.order_id=?")) {
            ps.setInt(1, orderId);
            List<OrderItem> list = new java.util.ArrayList<>();
            try (java.sql.ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(MAP.map(rs)); }
            return list;
        }
    }

    /** Mỗi dòng: {mã sách, tên sách, số lượng bán (Integer), doanh thu (Double)} - chỉ tính đơn đã hoàn tất. */
    public List<Object[]> topBooks(Timestamp from, Timestamp to, int limit) {
        return DaoSupport.query("SELECT b.book_id, b.title, SUM(d.quantity) q, SUM(d.quantity * d.unit_price) s " +
                "FROM Order_Items d JOIN Sales_Orders o ON d.order_id = o.order_id JOIN Books b ON d.book_id = b.book_id " +
                "WHERE o.order_status='completed' AND o.order_date >= ? AND o.order_date < ? " +
                "GROUP BY b.book_id, b.title ORDER BY q DESC LIMIT ?",
                rs -> new Object[]{rs.getInt("book_id"), rs.getString("title"), rs.getInt("q"), rs.getDouble("s")}, from, to, limit);
    }
}
