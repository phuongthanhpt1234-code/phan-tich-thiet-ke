package com.bookstore.gui;

import com.bookstore.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(Employee user) {
        super("Phần mềm Quản lý Nhà sách");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtil.PRIMARY);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("NHÀ SÁCH - QUẢN LÝ BÁN HÀNG");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        String role = user.getPositionName() == null ? "" : user.getPositionName();
        JLabel who = new JLabel("Xin chào, " + user.getName() + " (" + role + ")   ");
        who.setForeground(Color.WHITE);
        JButton logout = UIUtil.button("Đăng xuất", UIUtil.DANGER);
        logout.addActionListener(e -> {
            if (UIUtil.confirm(this, "Đăng xuất khỏi hệ thống?")) { dispose(); new LoginFrame().setVisible(true); }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(who); right.add(logout);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Bán hàng", new QuanLyBanHangGUI(user));
        tabs.addTab("Quản lý sách", new QuanLySachGUI());
        tabs.addTab("Khách hàng", new QuanLyKhachHangGUI());
        tabs.addTab("Danh mục", new CatalogTabs(user));
        if (user.isAdmin()) {   // quyền quản trị: chức vụ "Quản lý"
            tabs.addTab("Nhân viên", new QuanLyNhanVienGUI(user));
            tabs.addTab("Thống kê", new ThongKeGUI(user));
        }
        // Mỗi lần chuyển tab sẽ nạp lại dữ liệu để các tab luôn đồng bộ (tồn kho, danh mục...)
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Refreshable) ((Refreshable) c).reload();
        });

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
