package com.bookstore.gui;

import com.bookstore.bus.CustomerBUS;
import com.bookstore.model.Customer;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class QuanLyKhachHangGUI extends CrudPanel {
    private final CustomerBUS bus = new CustomerBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField(), txtPhone = new JTextField(),
            txtEmail = new JTextField(), txtAddress = new JTextField(), txtCreated = new JTextField();

    public QuanLyKhachHangGUI() {
        super(new String[]{"Mã KH", "Họ tên", "Số điện thoại", "Email", "Địa chỉ", "Ngày đăng ký"});
        txtId.setEditable(false);
        txtCreated.setEditable(false);
        clearForm();
        addField("Mã KH:", txtId);
        addField("Họ tên:", txtName);
        addField("Điện thoại:", txtPhone);
        addField("Email:", txtEmail);
        addField("Địa chỉ:", txtAddress);
        addField("Ngày đăng ký:", txtCreated);
        init();
    }

    private static String s(String v) { return v == null ? "" : v; }

    private Customer fromForm(Integer id) {
        Customer c = new Customer();
        if (id != null) c.setCustomerId(id);
        c.setFullName(txtName.getText()); c.setPhone(txtPhone.getText().trim());
        c.setEmail(txtEmail.getText()); c.setAddress(txtAddress.getText());
        return c;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Customer c : bus.search(k)) rows.add(new Object[]{c.getCustomerId(), c.getFullName(), s(c.getPhone()), s(c.getEmail()),
                s(c.getAddress()), c.getCreatedAt() == null ? "" : f.format(c.getCreatedAt())});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Customer c = bus.getById(Integer.parseInt(id));
        if (c == null) return;
        txtId.setText(String.valueOf(c.getCustomerId())); txtName.setText(c.getFullName());
        txtPhone.setText(s(c.getPhone())); txtEmail.setText(s(c.getEmail())); txtAddress.setText(s(c.getAddress()));
        txtCreated.setText(c.getCreatedAt() == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(c.getCreatedAt()));
    }

    @Override protected void clearForm() {
        txtId.setText("(tự động)"); txtName.setText(""); txtPhone.setText(""); txtEmail.setText("");
        txtAddress.setText(""); txtCreated.setText("(tự động)");
    }

    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa khách hàng có mã " + id + "? Các đơn hàng cũ vẫn được giữ lại (hiển thị là khách vãng lai)."; }
}
