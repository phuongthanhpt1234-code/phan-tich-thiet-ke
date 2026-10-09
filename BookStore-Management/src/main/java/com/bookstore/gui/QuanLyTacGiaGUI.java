package com.bookstore.gui;

import com.bookstore.bus.AuthorBUS;
import com.bookstore.model.Author;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyTacGiaGUI extends CrudPanel {
    private final AuthorBUS bus = new AuthorBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField();
    private final JTextArea txtBio = UIUtil.textArea(5);

    public QuanLyTacGiaGUI() {
        super(new String[]{"Mã", "Tên tác giả", "Tiểu sử"});
        txtId.setEditable(false);
        clearForm();
        addField("Mã tác giả:", txtId);
        addField("Tên tác giả:", txtName);
        addField("Tiểu sử:", new JScrollPane(txtBio));
        init();
    }

    private Author fromForm(Integer id) {
        Author a = new Author();
        if (id != null) a.setAuthorId(id);
        a.setAuthorName(txtName.getText());
        a.setBio(txtBio.getText());
        return a;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Author a : bus.search(k)) rows.add(new Object[]{a.getAuthorId(), a.getAuthorName(), a.getBio() == null ? "" : a.getBio()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Author a = bus.getById(Integer.parseInt(id));
        if (a == null) return;
        txtId.setText(String.valueOf(a.getAuthorId())); txtName.setText(a.getAuthorName());
        txtBio.setText(a.getBio() == null ? "" : a.getBio());
    }

    @Override protected void clearForm() { txtId.setText("(tự động)"); txtName.setText(""); txtBio.setText(""); }
    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa tác giả có mã " + id + "? Các sách của tác giả này sẽ chuyển thành chưa có tác giả."; }
}
