package com.bookstore.dao;

import com.bookstore.model.Payment;

import java.sql.Connection;
import java.sql.SQLException;

public class PaymentDAO {
    public int insert(Connection c, Payment p) throws SQLException {
        return DaoSupport.insert(c, "INSERT INTO Payments(order_id, payment_date, amount, payment_method, payment_status) VALUES (?,?,?,?,?)",
                p.getOrderId(), p.getPaymentDate(), p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus());
    }

    public int updateStatusByOrder(Connection c, int orderId, String status) throws SQLException {
        return DaoSupport.update(c, "UPDATE Payments SET payment_status=? WHERE order_id=?", status, orderId);
    }
}
