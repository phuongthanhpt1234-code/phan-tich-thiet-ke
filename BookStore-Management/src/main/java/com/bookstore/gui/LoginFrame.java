package com.bookstore.gui;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.config.MySQLConnect;
import com.bookstore.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField(18);
    private final JPasswordField txtPass = new JPasswordField(18);
    private final EmployeeBUS bus = new EmployeeBUS();

    public LoginFrame() {
        super("Đăng nhập - Quản lý Nhà sách");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(24, 32, 24, 32));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UIUtil.PRIMARY);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; p.add(title, g);
        g.gridwidth = 1;
        g.gridy = 1; p.add(new JLabel("Tên đăng nhập:"), g);
        g.gridx = 1; p.add(txtUser, g);
        g.gridx = 0; g.gridy = 2; p.add(new JLabel("Mật khẩu:"), g);
        g.gridx = 1; p.add(txtPass, g);
        JButton btn = UIUtil.button("Đăng nhập", UIUtil.PRIMARY);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2; p.add(btn, g);

        btn.addActionListener(e -> login());
        txtPass.addActionListener(e -> login());
        txtUser.addActionListener(e -> txtPass.requestFocus());
        getRootPane().setDefaultButton(btn);

        setContentPane(p);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        UIUtil.run(this, () -> {
            MySQLConnect.test();
            Employee e = bus.login(txtUser.getText(), new String(txtPass.getPassword()));
            dispose();
            new MainFrame(e).setVisible(true);
        });
    }
}
