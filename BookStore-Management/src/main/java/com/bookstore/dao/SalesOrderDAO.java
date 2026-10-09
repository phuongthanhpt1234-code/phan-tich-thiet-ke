package com.bookstore.dao;

import com.bookstore.model.SalesOrder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class SalesOrderDAO {
    private static final String SELECT =
            "SELECT o.*, e.name AS emp_name, c.full_name AS cus_name, p.payment_method FROM Sales_Orders o " +
            "JOIN Employee e ON o.employee_id = e.id " +
            "LEFT JOIN Customers c ON o.customer_id = c.customer_id " +
            "LEFT JOIN Payments p ON p.order_id = o.order_id ";

    private static final DaoSupport.Mapper<SalesOrder> MAP = rs -> {
        SalesOrder o = new SalesOrder();
        o.setOrderId(rs.getInt("order_id")); o.setCustomerId(DaoSupport.intOrNull(rs, "customer_id"));
        o.setEmployeeId(rs.getInt("employee_id")); o.setOrderDate(rs.getTimestamp("order_date"));
        o.setTotalAmount(rs.getDouble("total_amount")); o.setOrderStatus(rs.getString("order_status"));
        o.setEmployeeName(rs.getString("emp_name")); o.setCustomerName(rs.getString("cus_name"));
        o.setPaymentMethod(rs.getString("payment_method"));
        return o;
    };

    public int insert(Connection c, SalesOrder o) throws SQLException {
        return DaoSupport.insert(c, "INSERT INTO Sales_Orders(customer_id, employee_id, order_date, total_amount, order_status) VALUES (?,?,?,?,?)",
                o.getCustomerId(), o.getEmployeeId(), o.getOrderDate(), o.getTotalAmount(), o.getOrderStatus());
    }

    public SalesOrder selectById(int id) { return DaoSupport.queryOne(SELECT + "WHERE o.order_id=?", MAP, id); }

    public SalesOrder selectByIdForUpdate(Connection c, int id) throws SQLException {
        try (java.sql.PreparedStatement ps = c.prepareStatement(SELECT + "WHERE o.order_id=? FOR UPDATE")) {
            ps.setInt(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) { return rs.next() ? MAP.map(rs) : null; }
        }
    }

    public List<SalesOrder> selectByRange(Timestamp from, Timestamp to) {
        return DaoSupport.query(SELECT + "WHERE o.order_date >= ? AND o.order_date < ? ORDER BY o.order_date DESC", MAP, from, to);
    }

    public int updateStatus(Connection c, int id, String status) throws SQLException {
        return DaoSupport.update(c, "UPDATE Sales_Orders SET order_status=? WHERE order_id=?", status, id);
    }

    /** {số đơn, doanh thu} của các đơn đã hoàn tất. */
    public double[] summary(Timestamp from, Timestamp to) {
        return DaoSupport.queryOne("SELECT COUNT(*), COALESCE(SUM(total_amount),0) FROM Sales_Orders WHERE order_status='completed' AND order_date >= ? AND order_date < ?",
                rs -> new double[]{rs.getInt(1), rs.getDouble(2)}, from, to);
    }

    /** Mỗi dòng: {ngày (String), số đơn (Integer), doanh thu (Double)}. */
    public List<Object[]> revenueByDay(Timestamp from, Timestamp to) {
        return DaoSupport.query("SELECT DATE(order_date) d, COUNT(*) n, SUM(total_amount) s FROM Sales_Orders " +
                "WHERE order_status='completed' AND order_date >= ? AND order_date < ? GROUP BY DATE(order_date) ORDER BY d",
                rs -> new Object[]{rs.getString("d"), rs.getInt("n"), rs.getDouble("s")}, from, to);
    }
}
