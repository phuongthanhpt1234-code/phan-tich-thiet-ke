package com.bookstore.gui;

import com.bookstore.bus.CategoryBUS;
import com.bookstore.model.Category;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyTheLoaiGUI extends CrudPanel {
    private final CategoryBUS bus = new CategoryBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField();

    public QuanLyTheLoaiGUI() {
        super(new String[]{"Mã", "Tên thể loại"});
        txtId.setEditable(false);
        clearForm();
        addField("Mã thể loại:", txtId);
        addField("Tên thể loại:", txtName);
        init();
    }

    private Category fromForm(Integer id) {
        Category c = new Category();
        if (id != null) c.setCategoryId(id);
        c.setCategoryName(txtName.getText());
        return c;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Category c : bus.search(k)) rows.add(new Object[]{c.getCategoryId(), c.getCategoryName()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Category c = bus.getById(Integer.parseInt(id));
        if (c != null) { txtId.setText(String.valueOf(c.getCategoryId())); txtName.setText(c.getCategoryName()); }
    }

    @Override protected void clearForm() { txtId.setText("(tự động)"); txtName.setText(""); }
    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa thể loại có mã " + id + "? Các sách thuộc thể loại này sẽ chuyển thành chưa phân loại."; }
}
