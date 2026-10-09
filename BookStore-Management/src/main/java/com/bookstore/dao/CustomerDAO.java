package com.bookstore.dao;

import com.bookstore.model.Customer;

import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class CustomerDAO {
    private static final DaoSupport.Mapper<Customer> MAP = rs -> {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id")); c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email")); c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address")); c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    };

    public List<Customer> selectAll() { return DaoSupport.query("SELECT * FROM Customers ORDER BY customer_id", MAP); }
    public Customer selectById(int id) { return DaoSupport.queryOne("SELECT * FROM Customers WHERE customer_id=?", MAP, id); }
    public int insert(Customer c) {
        return DaoSupport.insert("INSERT INTO Customers(full_name, email, phone, address) VALUES (?,?,?,?)",
                c.getFullName(), nz(c.getEmail()), nz(c.getPhone()), nz(c.getAddress()));
    }
    public int update(Customer c) {
        return DaoSupport.update("UPDATE Customers SET full_name=?, email=?, phone=?, address=? WHERE customer_id=?",
                c.getFullName(), nz(c.getEmail()), nz(c.getPhone()), nz(c.getAddress()), c.getCustomerId());
    }
    public int delete(int id) { return DaoSupport.update("DELETE FROM Customers WHERE customer_id=?", id); }
}
