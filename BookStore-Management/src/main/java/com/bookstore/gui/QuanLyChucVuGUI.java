package com.bookstore.gui;

import com.bookstore.bus.PositionBUS;
import com.bookstore.model.Position;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyChucVuGUI extends CrudPanel {
    private final PositionBUS bus = new PositionBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField();
    private final JTextArea txtDesc = UIUtil.textArea(5);

    public QuanLyChucVuGUI() {
        super(new String[]{"Mã", "Tên chức vụ", "Mô tả"});
        txtId.setEditable(false);
        clearForm();
        addField("Mã chức vụ:", txtId);
        addField("Tên chức vụ:", txtName);
        addField("Mô tả:", new JScrollPane(txtDesc));
        init();
    }

    private Position fromForm(Integer id) {
        Position p = new Position();
        if (id != null) p.setId(id);
        p.setPositionName(txtName.getText()); p.setDescription(txtDesc.getText());
        return p;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Position p : bus.search(k)) rows.add(new Object[]{p.getId(), p.getPositionName(), p.getDescription() == null ? "" : p.getDescription()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Position p = bus.getById(Integer.parseInt(id));
        if (p == null) return;
        txtId.setText(String.valueOf(p.getId())); txtName.setText(p.getPositionName());
        txtDesc.setText(p.getDescription() == null ? "" : p.getDescription());
    }

    @Override protected void clearForm() { txtId.setText("(tự động)"); txtName.setText(""); txtDesc.setText(""); }
    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa chức vụ có mã " + id + "? Nhân viên giữ chức vụ này sẽ chuyển thành chưa có chức vụ."; }
}
