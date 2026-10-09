package com.bookstore.gui;

import com.bookstore.bus.PublisherBUS;
import com.bookstore.model.Publisher;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyNhaXuatBanGUI extends CrudPanel {
    private final PublisherBUS bus = new PublisherBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField(), txtPhone = new JTextField(),
            txtEmail = new JTextField(), txtAddress = new JTextField();

    public QuanLyNhaXuatBanGUI() {
        super(new String[]{"Mã", "Tên nhà xuất bản", "Điện thoại", "Email", "Địa chỉ"});
        txtId.setEditable(false);
        clearForm();
        addField("Mã NXB:", txtId);
        addField("Tên NXB:", txtName);
        addField("Điện thoại:", txtPhone);
        addField("Email:", txtEmail);
        addField("Địa chỉ:", txtAddress);
        init();
    }

    private Publisher fromForm(Integer id) {
        Publisher p = new Publisher();
        if (id != null) p.setPublisherId(id);
        p.setPublisherName(txtName.getText()); p.setPhone(txtPhone.getText());
        p.setEmail(txtEmail.getText()); p.setAddress(txtAddress.getText());
        return p;
    }

    private static String s(String v) { return v == null ? "" : v; }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Publisher p : bus.search(k)) rows.add(new Object[]{p.getPublisherId(), p.getPublisherName(), s(p.getPhone()), s(p.getEmail()), s(p.getAddress())});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Publisher p = bus.getById(Integer.parseInt(id));
        if (p == null) return;
        txtId.setText(String.valueOf(p.getPublisherId())); txtName.setText(p.getPublisherName());
        txtPhone.setText(s(p.getPhone())); txtEmail.setText(s(p.getEmail())); txtAddress.setText(s(p.getAddress()));
    }

    @Override protected void clearForm() {
        txtId.setText("(tự động)"); txtName.setText(""); txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText("");
    }

    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa nhà xuất bản có mã " + id + "? Các sách của NXB này sẽ chuyển thành chưa có NXB."; }
}
