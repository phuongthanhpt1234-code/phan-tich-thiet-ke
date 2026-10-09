package com.bookstore.gui;

import com.bookstore.model.Employee;

import javax.swing.*;
import java.awt.*;

/** Nhóm các danh mục: Thể loại, Tác giả, Nhà xuất bản (+ Chức vụ, Phòng ban cho quản trị). */
public class CatalogTabs extends JTabbedPane implements Refreshable {
    public CatalogTabs(Employee user) {
        super(JTabbedPane.TOP);
        addTab("Thể loại", new QuanLyTheLoaiGUI());
        addTab("Tác giả", new QuanLyTacGiaGUI());
        addTab("Nhà xuất bản", new QuanLyNhaXuatBanGUI());
        if (user.isAdmin()) {
            addTab("Chức vụ", new QuanLyChucVuGUI());
            addTab("Phòng ban", new QuanLyPhongBanGUI());
        }
        addChangeListener(e -> reload());
    }

    @Override public void reload() {
        Component c = getSelectedComponent();
        if (c instanceof Refreshable) ((Refreshable) c).reload();
    }
}
