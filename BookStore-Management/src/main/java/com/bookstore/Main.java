package com.bookstore;

import com.bookstore.gui.LoginFrame;

import javax.swing.*;
import java.awt.Font;

public class Main {
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(info.getClassName()); break; }
            }
            UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
