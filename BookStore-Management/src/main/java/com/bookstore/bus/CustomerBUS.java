package com.bookstore.bus;

import com.bookstore.dao.CustomerDAO;
import com.bookstore.model.Customer;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerBUS {
    private final CustomerDAO dao = new CustomerDAO();

    public List<Customer> getAll() { return dao.selectAll(); }
    public Customer getById(int id) { return dao.selectById(id); }

    public List<Customer> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(c -> k.isEmpty() || c.getFullName().toLowerCase().contains(k)
                || (c.getPhone() != null && c.getPhone().contains(k))
                || (c.getEmail() != null && c.getEmail().toLowerCase().contains(k))
                || String.valueOf(c.getCustomerId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Customer c) {
        Validate.required(c.getFullName(), "Họ tên khách hàng");
        Validate.maxLen(c.getFullName(), 100, "Họ tên");
        Validate.phone(c.getPhone(), "Số điện thoại");
        Validate.email(c.getEmail());
        Validate.maxLen(c.getAddress(), 150, "Địa chỉ");
        c.setFullName(c.getFullName().trim());
    }

    public void add(Customer c) { validate(c); dao.insert(c); }
    public void update(Customer c) { validate(c); dao.update(c); }
    /** Xóa khách: các đơn hàng cũ vẫn giữ lại (customer_id chuyển thành NULL = khách vãng lai). */
    public void delete(int id) { dao.delete(id); }
}
